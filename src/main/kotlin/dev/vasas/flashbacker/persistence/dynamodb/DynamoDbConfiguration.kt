package dev.vasas.flashbacker.persistence.dynamodb

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@EnableDynamoDBRepositories("dev.vasas.flashbacker.persistence.dynamodb.dao")
@Profile("!dev")
class DynamoDbConfiguration(
        @Value("\${flashbacker.dynamodb.endpoint}")
        private val endpoint: String,

        @Value("\${flashbacker.dynamodb.accesskey}")
        private val accessKey: String,

        @Value("\${flashbacker.dynamodb.secretkey}")
        private val secretKey: String,

        @Value("\${flashbacker.dynamodb.region}")
        private val region: String
) {
    @Bean
    fun amazonAWSCredentials(): AWSCredentials {
        return BasicAWSCredentials(accessKey, secretKey)
    }

    @Bean
    fun amazonAWSCredentialsProvider(): AWSCredentialsProvider {
        return AWSStaticCredentialsProvider(amazonAWSCredentials())
    }

    @Bean
    fun amazonDynamoDB(): AmazonDynamoDB {
        val dynamoDBClientBuilder = AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(amazonAWSCredentialsProvider())

        if (region.isEmpty()) {
            dynamoDBClientBuilder.withEndpointConfiguration(EndpointConfiguration(endpoint, ""))
        } else {
            dynamoDBClientBuilder.withRegion(Regions.fromName(region))
        }

        return dynamoDBClientBuilder.build()
    }
}
