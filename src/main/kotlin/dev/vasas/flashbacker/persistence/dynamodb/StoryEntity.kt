package dev.vasas.flashbacker.persistence.dynamodb

data class StoryEntity(
        val id: String,
        val userId: String,
        val dateHappenedAndId: String,
        val location: String? = null,
        val dateHappened: Long,
        val timestampCreated: Long,
        val text: String
) {

    companion object {
        const val storyTableName = "story"

        const val idFieldName = "id"
        const val userIdFieldName = "userId"
        const val dateHappenedAndIdFieldName = "dateHappenedAndId"
        const val locationFieldName = "location"
        const val dateHappenedFieldName = "dateHappened"
        const val timestampCreatedFieldName = "timestampCreated"
        const val textFieldName = "text"
    }
}
