package dev.vasas.flashbacker.api.rest.representationmodel

import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.time.LocalDateTime

@Relation(itemRelation = "memory", collectionRelation = "memories")
data class MemoryModel(
        val id: String,
        val location: String,
        val date: LocalDateTime,
        val text: String
) : RepresentationModel<MemoryModel>()
