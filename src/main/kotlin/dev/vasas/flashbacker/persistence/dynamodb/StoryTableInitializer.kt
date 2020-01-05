package dev.vasas.flashbacker.persistence.dynamodb

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component

@Component
class StoryTableInitializer(
        private val dynamoDb: AmazonDynamoDB
) : InitializingBean {

    private val logger = LoggerFactory.getLogger(StoryTableInitializer::class.java)

    companion object {
        private const val DEFAULT_THROUGHPUT = 5L
    }

    fun createTableIfNotExist() {
        val createTableRequest = CreateTableRequest()
                .withTableName(StoryEntity.storyTableName)
                .withKeySchema(
                        KeySchemaElement(StoryEntity.userIdFieldName, KeyType.HASH),
                        KeySchemaElement(StoryEntity.dateHappenedAndIdFieldName, KeyType.RANGE)
                ).withAttributeDefinitions(
                        AttributeDefinition(StoryEntity.userIdFieldName, ScalarAttributeType.S),
                        AttributeDefinition(StoryEntity.dateHappenedAndIdFieldName, ScalarAttributeType.S)
                ).withProvisionedThroughput(ProvisionedThroughput(DEFAULT_THROUGHPUT, DEFAULT_THROUGHPUT))

        try {
            dynamoDb.createTable(createTableRequest)
            logger.info("Table ${StoryEntity.storyTableName} is created.")
        } catch (e: ResourceInUseException) {
            logger.info("Table ${StoryEntity.storyTableName} already exists.")
            logger.debug(e.message, e)
        }
    }

    override fun afterPropertiesSet() {
        createTableIfNotExist()
    }
}
