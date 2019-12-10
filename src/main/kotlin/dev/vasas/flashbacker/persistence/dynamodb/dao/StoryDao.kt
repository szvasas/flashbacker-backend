package dev.vasas.flashbacker.persistence.dynamodb.dao

import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository

@EnableScan
interface StoryDao : CrudRepository<StoryEntity, String> {

    fun findByUserId(userId: String): List<StoryEntity>
}
