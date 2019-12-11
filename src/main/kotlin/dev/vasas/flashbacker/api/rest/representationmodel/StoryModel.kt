package dev.vasas.flashbacker.api.rest.representationmodel

import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.time.LocalDate

@Relation(itemRelation = StoryModel.itemRelationName, collectionRelation = StoryModel.collectionRelationName)
data class StoryModel(
        val id: String,
        val location: String,
        val dateHappened: LocalDate,
        val text: String
) : RepresentationModel<StoryModel>() {

    companion object {
        const val itemRelationName: String = "story"
        const val collectionRelationName: String = "stories"
    }
}
