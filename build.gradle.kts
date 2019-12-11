import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.google.cloud.tools.jib") version "1.8.0"
    id("io.gitlab.arturbosch.detekt") version "1.1.0"
    id("org.springframework.boot") version "2.2.1.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("jvm") version "1.3.50"
    kotlin("plugin.spring") version "1.3.50"
    kotlin("plugin.jpa") version "1.3.50"
}

group = "dev.vasas"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val developmentOnly by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(developmentOnly)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito")
    }

    // Security
    implementation("org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:2.2.1.RELEASE")
    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    testImplementation("org.springframework.security:spring-security-test")

    // DynamoDB
    implementation("com.amazonaws:aws-java-sdk-dynamodb:1.11.690")
    testImplementation("org.testcontainers:dynalite:1.12.3")

    // Other development and test tooling
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.assertj:assertj-core:3.14.0")
    testImplementation("io.mockk:mockk:1.9.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging.events("passed", "skipped")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "6.0.1"
}

jib {
    container {
        ports = listOf("8080")
    }
}

detekt {
    input = files("src/")
    config = files("src/test/resources/detekt-config.yml")
}
