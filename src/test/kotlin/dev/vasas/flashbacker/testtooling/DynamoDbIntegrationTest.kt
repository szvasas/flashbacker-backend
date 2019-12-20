package dev.vasas.flashbacker.testtooling

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@SpringBootTest
@ExtendWith(DynamoDbIntegrationTestExtension::class)
annotation class DynamoDbIntegrationTest
