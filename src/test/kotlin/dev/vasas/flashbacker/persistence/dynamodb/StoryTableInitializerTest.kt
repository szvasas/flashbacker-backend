package dev.vasas.flashbacker.persistence.dynamodb

import dev.vasas.flashbacker.testtooling.DynamoDbIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@DynamoDbIntegrationTest
internal class StoryTableInitializerTest(
        @Autowired private val storyTableInitializer: StoryTableInitializer
) {

    /**
     * In the integration test environment the story table is automatically created
     * so if we get to this point it has to exist.
     */
    @Test
    fun `given the story table exists createTableIfNotExist() does not throw`() {
        storyTableInitializer.createTableIfNotExist()
    }
}
