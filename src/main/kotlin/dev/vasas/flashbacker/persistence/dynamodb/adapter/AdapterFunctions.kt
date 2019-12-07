package dev.vasas.flashbacker.persistence.dynamodb.adapter

import dev.vasas.flashbacker.domain.Memory
import dev.vasas.flashbacker.persistence.dynamodb.entity.MemoryEntity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun Memory.toMemoryEntity(): MemoryEntity {
    return MemoryEntity(
            id = this.id,
            userId = this.userId,
            location = this.location,
            date = this.date.toInstant(ZoneOffset.UTC).toEpochMilli(),
            text = this.text
    )
}

fun MemoryEntity.toMemory(): Memory {
    return Memory(
            id = this.id ?: "",
            userId = this.userId ?: "",
            location = this.location ?: "",
            date = this.date?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC)
            } ?: LocalDateTime.MIN,
            text = this.text ?: ""
    )
}
