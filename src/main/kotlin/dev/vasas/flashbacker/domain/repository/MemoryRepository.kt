package dev.vasas.flashbacker.domain.repository

import dev.vasas.flashbacker.domain.Memory

interface MemoryRepository {

    fun save(memory: Memory): Memory

    fun deleteById(id: String)

    fun findById(id: String): Memory?

    fun findMemoriesForUser(userName: String): List<Memory>
}
