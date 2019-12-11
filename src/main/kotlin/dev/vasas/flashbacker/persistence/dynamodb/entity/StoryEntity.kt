package dev.vasas.flashbacker.persistence.dynamodb.entity

data class StoryEntity(
        val id: String,
        val userId: String,
        val dateHappenedAndId: String,
        val location: String,
        val dateHappened: Long,
        val timestampAdded: Long,
        val text: String
) {

    companion object {
        const val storyTableName = "story"

        const val idFieldName = "id"
        const val userIdFieldName = "userId"
        const val dateHappenedAndIdFieldName = "dateHappenedAndId"
        const val locationFieldName = "location"
        const val dateHappenedFieldName = "dateHappened"
        const val timestampAddedFieldName = "timestampAdded"
        const val textFieldName = "text"
    }
}
