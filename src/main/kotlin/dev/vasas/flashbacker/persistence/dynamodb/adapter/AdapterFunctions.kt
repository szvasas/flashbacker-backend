package dev.vasas.flashbacker.persistence.dynamodb.adapter

import dev.vasas.flashbacker.domain.Story
import dev.vasas.flashbacker.persistence.dynamodb.entity.StoryEntity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun Story.toStoryEntity(): StoryEntity {
    return StoryEntity(
            id = this.id,
            userId = this.userId,
            location = this.location,
            date = this.date.toInstant(ZoneOffset.UTC).toEpochMilli(),
            text = this.text
    )
}

fun StoryEntity.toStory(): Story {
    return Story(
            id = this.id ?: "",
            userId = this.userId ?: "",
            location = this.location ?: "",
            date = this.date?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC)
            } ?: LocalDateTime.MIN,
            text = this.text ?: ""
    )
}
