package dev.vasas.flashbacker.persistence.dynamodb.dao

import dev.vasas.flashbacker.persistence.dynamodb.entity.MemoryEntity
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository

@EnableScan
interface MemoryDao : CrudRepository<MemoryEntity, String> {

    fun findByUserName(userName: String): List<MemoryEntity>
}
