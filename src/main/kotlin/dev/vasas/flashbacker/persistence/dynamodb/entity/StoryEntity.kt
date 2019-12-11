package dev.vasas.flashbacker.persistence.dynamodb.entity

data class StoryEntity(
        val id: String,
        val userId: String,
        val location: String,
        val date: Long,
        val text: String
) {

    companion object {
        const val storyTableName = "story"

        const val idFieldName = "id"
        const val userIdFieldName = "userId"
        const val locationFieldName = "location"
        const val dateFieldName = "date"
        const val textFieldName = "text"
    }
}
