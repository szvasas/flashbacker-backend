package dev.vasas.flashbacker.persistence.dynamodb

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import com.amazonaws.services.dynamodbv2.model.Condition
import com.amazonaws.services.dynamodbv2.model.QueryRequest
import dev.vasas.flashbacker.domain.Page
import dev.vasas.flashbacker.domain.PageRequest
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.dateHappenedAndIdFieldName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.storyTableName
import dev.vasas.flashbacker.persistence.dynamodb.StoryEntity.Companion.userIdFieldName
import org.springframework.stereotype.Component

@Component
class DynamoDbStoryDao(
        private val dynamoDb: AmazonDynamoDB,
        private val dynamoDbMapper: DynamoDBMapper
) {

    fun save(storyEntity: StoryEntity) {
        dynamoDbMapper.save(storyEntity)
    }

    fun deleteByKey(key: StoryEntityKey) {
        dynamoDbMapper.delete(
                StoryEntity(
                        userId = key.userId,
                        dateHappenedAndId = key.dateHappenedAndId
                )
        )
    }

    fun findByUserId(userId: String): List<StoryEntity> {
        val queryExpression = DynamoDBQueryExpression<StoryEntity>()
                .withHashKeyValues(StoryEntity(userId = userId))
                .withScanIndexForward(false)

        return dynamoDbMapper.query(StoryEntity::class.java, queryExpression)
    }

    fun findByUserIdPaged(userId: String, pageRequest: PageRequest<StoryEntityKey>): Page<StoryEntity> {
        val userEqualToInputCondition = Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(AttributeValue(userId))

        val queryRequest = QueryRequest(storyTableName)
                .withKeyConditions(mapOf(userIdFieldName to userEqualToInputCondition))
                .withScanIndexForward(false)
                .withLimit(pageRequest.size)

        pageRequest.lastProcessedKey?.let {
            queryRequest.withExclusiveStartKey(mapOf(
                    userIdFieldName to AttributeValue(it.userId),
                    dateHappenedAndIdFieldName to AttributeValue(it.dateHappenedAndId)
            ))
        }

        val queryResult = dynamoDb.query(queryRequest)

        val content = dynamoDbMapper.marshallIntoObjects(StoryEntity::class.java, queryResult.items)
        val hasNext = !queryResult.lastEvaluatedKey.isNullOrEmpty()

        return Page(content, hasNext)
    }

    fun findByKey(key: StoryEntityKey): StoryEntity? {
        return dynamoDbMapper.load(
                StoryEntity(
                        userId = key.userId,
                        dateHappenedAndId = key.dateHappenedAndId
                )
        )
    }

}
