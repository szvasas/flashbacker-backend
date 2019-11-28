package dev.vasas.flashbacker.persistence.dynamodb.repository

import dev.vasas.flashbacker.domain.Memory
import dev.vasas.flashbacker.domain.repository.MemoryRepository
import dev.vasas.flashbacker.persistence.dynamodb.adapter.toMemory
import dev.vasas.flashbacker.persistence.dynamodb.adapter.toMemoryEntity
import dev.vasas.flashbacker.persistence.dynamodb.dao.MemoryDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class DynamoDbMemoryRepository(@Autowired private val memoryDao: MemoryDao) : MemoryRepository {

    override fun save(memory: Memory): Memory {
        return memoryDao.save(memory.toMemoryEntity()).toMemory()
    }

    override fun deleteById(id: String) {
        memoryDao.deleteById(id)
    }

    override fun findById(id: String): Memory? {
        return memoryDao.findById(id)
                .map { it.toMemory() }
                .orElse(null)
    }

    override fun findMemoriesForUser(userName: String): List<Memory> {
        return memoryDao.findByUserName(userName).map { it.toMemory() }
    }
}
