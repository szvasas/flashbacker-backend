package dev.vasas.flashbacker.persistence.dynamodb

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import dev.vasas.flashbacker.domain.PageRequest
import dev.vasas.flashbacker.domain.Story
import dev.vasas.flashbacker.domain.StoryKey
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.dateHappenedFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.idFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.textFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.timestampCreatedFieldName
import dev.vasas.flashbacker.testtooling.DynamoDbIntegrationTest
import dev.vasas.flashbacker.testtooling.USER_ID_OF_BOB
import dev.vasas.flashbacker.testtooling.allStories
import dev.vasas.flashbacker.testtooling.greatStoryOfBob
import dev.vasas.flashbacker.testtooling.niceStoryOfAlice
import dev.vasas.flashbacker.testtooling.niceStoryOfAliceWithBlankLocation
import dev.vasas.flashbacker.testtooling.niceStoryOfAliceWithoutLocation
import dev.vasas.flashbacker.testtooling.storiesOfBob
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

@DynamoDbIntegrationTest
internal class DynamoDbStoryRepositoryTest(
        @Autowired private val dynamoDbMapper: DynamoDBMapper,
        @Autowired private val dynamoDbStoryRepository: DynamoDbStoryRepository
) {

    @AfterEach
    fun clearTestData() {
        val failedBatches = dynamoDbMapper.batchDelete(allStories.map { it.toStoryEntity() })
        failedBatches.firstOrNull()?.let { throw it.exception }
    }

    @Nested
    inner class `given there are no items in the DB` {

        @Test
        fun `findByKey method returns null`() {
            // when
            val searchResult = dynamoDbStoryRepository.findByKey(greatStoryOfBob.key)

            // then
            assertThat(searchResult).isNull()
        }

        @Test
        fun `deleteByKey method does not throw`() {
            dynamoDbStoryRepository.deleteByKey(greatStoryOfBob.key)
        }

        @Test
        fun `findStoriesForUser method returns an empty page with no next page`() {
            // when
            val storiesPage = dynamoDbStoryRepository.findStoriesForUser(USER_ID_OF_BOB, unlimitedSizeRequest())

            assertSoftly {
                it.assertThat(storiesPage.content).isEmpty()
                it.assertThat(storiesPage.hasNext).isFalse()
            }
        }
    }

    @Nested
    inner class `given there are valid items in the DB` {

        @BeforeEach
        fun loadTestData() {
            val failedBatches = dynamoDbMapper.batchSave(allStories.map { it.toStoryEntity() })
            failedBatches.firstOrNull()?.let { throw it.exception }
        }

        @Test
        fun `repository finds saved Story by its key`() {
            // when
            val foundStory = dynamoDbStoryRepository.findByKey(greatStoryOfBob.key)

            // then
            assertThat(foundStory).isEqualTo(greatStoryOfBob)
        }

        @Test
        fun `repository deletes existing Story by its key`() {
            // when
            dynamoDbStoryRepository.deleteByKey(greatStoryOfBob.key)

            // then
            assertThat(dynamoDbStoryRepository.findByKey(greatStoryOfBob.key)).isNull()
        }

        @Test
        fun `repository finds all the stories of the specified user but does not load stories of other users`() {
            // when
            val storiesOfAUser = dynamoDbStoryRepository.findStoriesForUser(USER_ID_OF_BOB, unlimitedSizeRequest())

            // then
            assertThat(storiesOfAUser.content).containsExactlyInAnyOrderElementsOf(storiesOfBob)
        }

        @Test
        fun `stories for a user are sorted by dateHappened and id descending`() {
            // when
            val storiesOfAUser = dynamoDbStoryRepository.findStoriesForUser(USER_ID_OF_BOB, unlimitedSizeRequest())

            // then
            val comparator = compareBy(Story::dateHappened, Story::id).reversed()
            assertThat(storiesOfAUser.content).isEqualTo(storiesOfBob.sortedWith(comparator))
        }

        @Test
        fun `given there are enough stories in the DB story count in a page is equal to the requested limit`() {
            // given
            val requestLimit = storiesOfBob.size - 1

            // when
            val foundPage = dynamoDbStoryRepository.findStoriesForUser(USER_ID_OF_BOB, PageRequest(requestLimit))

            // then
            assertThat(foundPage.content.size).isEqualTo(requestLimit)
        }

        @Test
        fun `the last element of a story page is equal to the first element of the next page`() {
            // given
            val requestLimit = storiesOfBob.size - 1
            val firstPage = dynamoDbStoryRepository.findStoriesForUser(USER_ID_OF_BOB, PageRequest(requestLimit))
            val lastElementOfTheFirstPage = firstPage.content.last()

            // when
            val secondPage = dynamoDbStoryRepository.findStoriesForUser(USER_ID_OF_BOB, PageRequest(requestLimit, lastElementOfTheFirstPage.key))

            // then
            assertThat(lastElementOfTheFirstPage).isEqualTo(secondPage.content.first())
        }

        @Test
        fun `the union of all the story pages contains exactly all of the stories for a user`() {
            // given
            val unionOfAllPages = mutableSetOf<Story>()
            val requestLimit = 2
            var lastProcessedKey: StoryKey? = null

            // when
            do {
                val page = dynamoDbStoryRepository.findStoriesForUser(USER_ID_OF_BOB, PageRequest(requestLimit, lastProcessedKey))
                unionOfAllPages.addAll(page.content)
                lastProcessedKey = page.content.last().key
            } while (page.hasNext)

            // then
            assertThat(unionOfAllPages).isEqualTo(storiesOfBob.toSet())
        }

    }

    @Nested
    inner class `given there is an invalid item in the DB` {

        @ParameterizedTest
        @ValueSource(strings = [idFieldName, dateHappenedFieldName, timestampCreatedFieldName, textFieldName])
        fun `repository throws when a mandatory field is not present in a DB item`(missingFieldName: String) {
            // given
            val invalidStoryEntity = clearFieldValue(greatStoryOfBob.toStoryEntity(), missingFieldName)
            dynamoDbMapper.save(invalidStoryEntity)

            // expect
            val thrown = assertThrows<InvalidDynamoDbItemException> {
                dynamoDbStoryRepository.findStoriesForUser(greatStoryOfBob.userId, unlimitedSizeRequest())
            }

            assertThat(thrown.message).isEqualTo("Cannot load item from DynamoDb! Missing mandatory field: $missingFieldName")
        }
    }

    @Test
    fun `save method saves Story with location properly`() {
        // given
        dynamoDbStoryRepository.save(niceStoryOfAlice)

        // when
        val savedStory = dynamoDbStoryRepository.findByKey(niceStoryOfAlice.key)

        // then
        assertThat(savedStory).isEqualTo(niceStoryOfAlice)
    }

    @Test
    fun `save method saves Story without location properly`() {
        // given
        dynamoDbStoryRepository.save(niceStoryOfAliceWithoutLocation)

        // when
        val savedStory = dynamoDbStoryRepository.findByKey(niceStoryOfAliceWithoutLocation.key)

        // then
        assertThat(savedStory).isEqualTo(niceStoryOfAliceWithoutLocation)
    }

    @Test
    fun `save method saves Story with blank location properly`() {
        // given
        dynamoDbStoryRepository.save(niceStoryOfAliceWithBlankLocation)

        // when
        val savedStory = dynamoDbStoryRepository.findByKey(niceStoryOfAliceWithBlankLocation.key)

        // then
        assertThat(savedStory).isEqualTo(niceStoryOfAliceWithBlankLocation.copy(location = null))
    }

    private fun clearFieldValue(storyEntity: StoryEntity, fieldName: String): StoryEntity {
        val result = storyEntity.copy()
        StoryEntity::class
                .memberProperties
                .filterIsInstance<KMutableProperty<*>>()
                .find { it.name == fieldName }!!
                .setter
                .call(result, null)
        return result
    }

    private fun <T> unlimitedSizeRequest() = PageRequest<T>(Integer.MAX_VALUE)
}
