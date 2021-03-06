package dev.vasas.flashbacker.persistence.dynamodb

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class DynamoDbConnection(
        @Value("\${flashbacker.dynamodb.endpoint}")
        private val endpoint: String,

        @Value("\${flashbacker.dynamodb.accessKey}")
        private val accessKey: String,

        @Value("\${flashbacker.dynamodb.secretKey}")
        private val secretKey: String,

        @Value("\${flashbacker.dynamodb.region}")
        private val region: String
) {

    @Bean
    fun amazonDynamoDB(): AmazonDynamoDB {
        val dynamoDBClientBuilder = AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(accessKey, secretKey)))

        if (region.isEmpty()) {
            dynamoDBClientBuilder.withEndpointConfiguration(EndpointConfiguration(endpoint, ""))
        } else {
            dynamoDBClientBuilder.withRegion(Regions.fromName(region))
        }

        return dynamoDBClientBuilder.build()
    }
}

@Configuration
class DynamoDbTools(
        private val amazonDynamoDB: AmazonDynamoDB
) {

    @Bean
    fun dynamoDB(): DynamoDB {
        return DynamoDB(amazonDynamoDB)
    }

    @Bean
    fun dynamoDBMapper(): DynamoDBMapper {
        return DynamoDBMapper(amazonDynamoDB)
    }
}
