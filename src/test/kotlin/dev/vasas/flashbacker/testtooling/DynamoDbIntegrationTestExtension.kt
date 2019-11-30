package dev.vasas.flashbacker.testtooling

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.testcontainers.dynamodb.DynaliteContainer

/**
 * This is a JUnit5 extension class which starts a test DynamoDB simulator in a container
 * and exposes a client to this simulator as a Spring Bean.
 *
 * The container will be reused by all the classes using this extension.
 * It is stopped by the Testcontainers Ryuk container (for more information see:
 * https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/)
 *
 * It is meant to be used by the [DynamoDbIntegrationTest] annotation only.
 *
 */
internal class DynamoDbIntegrationTestExtension : BeforeAllCallback {

    /**
     * Note that we have to create the DynaliteContainer in the companion object
     * to be able to expose it's client as a Spring Bean.
     */
    companion object {
        val dynamoDb: DynaliteContainer = DynaliteContainer()

        init {
            dynamoDb.start()
            createTables()
        }

        private fun createTables() {
            val createTableRequest = CreateTableRequest()
                    .withTableName("memory")
                    .withKeySchema(KeySchemaElement("id", KeyType.HASH))
                    .withAttributeDefinitions(AttributeDefinition("id", ScalarAttributeType.S))
                    .withProvisionedThroughput(ProvisionedThroughput(1L, 1L))

            dynamoDb.client.createTable(createTableRequest)
        }
    }

    @Configuration
    @Profile("dev")
    internal class TestDynamoDbConfig {
        @Bean
        fun amazonDynamoDB(): AmazonDynamoDB {
            return dynamoDb.client
        }
    }

    override fun beforeAll(context: ExtensionContext?) {
        check(dynamoDb.isRunning) {
            "DynamoDB test instance is not running!"
        }
    }
}
