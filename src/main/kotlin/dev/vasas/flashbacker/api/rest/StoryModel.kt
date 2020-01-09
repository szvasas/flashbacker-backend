package dev.vasas.flashbacker.api.rest

import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.time.LocalDate
import java.time.ZonedDateTime

@Relation(itemRelation = StoryModel.itemRelationName, collectionRelation = StoryModel.collectionRelationName)
data class StoryModel(
        val id: String? = null,
        val location: String? = null,
        val dateHappened: LocalDate,
        val timestampCreated: ZonedDateTime? = null,
        val text: String
) : RepresentationModel<StoryModel>() {

    companion object {
        const val itemRelationName: String = "story"
        const val collectionRelationName: String = "stories"
        const val lastProcessedDateParamName: String = "lastProcessedDate"
        const val lastProcessedIdParamName: String = "lastProcessedId"
        const val limitParamName: String = "limit"
    }
}
