package dev.vasas.flashbacker.domain.repository

import dev.vasas.flashbacker.domain.Memory

interface MemoryRepository {

    fun save(memory: Memory): Memory

    fun deleteById(id: String)

    fun findMemoriesForUser(userName: String): List<Memory>

}
