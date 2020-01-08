package dev.vasas.flashbacker.persistence.dynamodb

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import com.amazonaws.services.dynamodbv2.model.Condition
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.dateHappenedAndIdFieldName
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DynamoDbStoryDao(
        private val dynamoDbMapper: DynamoDBMapper
) {

    fun save(storyEntity: StoryEntity) {
        dynamoDbMapper.save(storyEntity)
    }

    fun deleteByUserIdAndDateHappenedAndId(userId: String, dateHappenedAndId: String) {
        dynamoDbMapper.delete(
                StoryEntity(
                        userId = userId,
                        dateHappenedAndId = dateHappenedAndId
                )
        )
    }

    fun findByUserId(userId: String): List<StoryEntity> {
        val queryExpression = DynamoDBQueryExpression<StoryEntity>().withHashKeyValues(
                StoryEntity(userId = userId)
        )
        return dynamoDbMapper.query(StoryEntity::class.java, queryExpression)
    }

    fun findByUserIdAndDateHappenedAndId(userId: String, dateHappenedAndId: String): StoryEntity? {
        return dynamoDbMapper.load(
                StoryEntity(
                        userId = userId,
                        dateHappenedAndId = dateHappenedAndId
                )
        )
    }

    fun findByUserIdAndDateHappened(userId: String, dateHappened: LocalDate): List<StoryEntity> {
        val beginsWithInputDate = Condition()
                .withComparisonOperator(ComparisonOperator.BEGINS_WITH)
                .withAttributeValueList(AttributeValue(dateHappened.toString()))

        val queryExpression = DynamoDBQueryExpression<StoryEntity>()
                .withHashKeyValues(StoryEntity(userId = userId))
                .withRangeKeyCondition(dateHappenedAndIdFieldName, beginsWithInputDate)

        return dynamoDbMapper.query(StoryEntity::class.java, queryExpression)
    }
}
