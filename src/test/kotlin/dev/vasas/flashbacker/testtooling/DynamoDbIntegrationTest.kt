package dev.vasas.flashbacker.testtooling

import org.junit.jupiter.api.extension.ExtendWith
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@ActiveProfiles("dev")
@SpringBootTest
@EnableDynamoDBRepositories("dev.vasas.flashbacker.persistence.dynamodb.dao")
@ExtendWith(DynamoDbIntegrationTestExtension::class)
annotation class DynamoDbIntegrationTest
