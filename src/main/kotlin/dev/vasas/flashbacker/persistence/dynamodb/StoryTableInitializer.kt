package dev.vasas.flashbacker.persistence.dynamodb

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component

@Component
class StoryTableInitializer(
        private val dynamoDbMapper: DynamoDBMapper,
        private val dynamoDb: DynamoDB
) : InitializingBean {

    private val logger = LoggerFactory.getLogger(StoryTableInitializer::class.java)

    companion object {
        private const val DEFAULT_THROUGHPUT = 5L
    }

    fun createTableIfNotExist() {
        val createTableRequest = dynamoDbMapper.generateCreateTableRequest(StoryEntity::class.java)
                .withProvisionedThroughput(ProvisionedThroughput(DEFAULT_THROUGHPUT, DEFAULT_THROUGHPUT))

        try {
            dynamoDb.createTable(createTableRequest).waitForActive()
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
