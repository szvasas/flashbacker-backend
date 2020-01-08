package dev.vasas.flashbacker.persistence.dynamodb

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable

@DynamoDBTable(tableName = StoryEntity.storyTableName)
data class StoryEntity(
        @get:DynamoDBAttribute(attributeName = idFieldName)
        var id: String? = null,
        @get:DynamoDBHashKey(attributeName = userIdFieldName)
        var userId: String? = null,
        @get:DynamoDBRangeKey(attributeName = dateHappenedAndIdFieldName)
        var dateHappenedAndId: String? = null,
        @get:DynamoDBAttribute(attributeName = locationFieldName)
        var location: String? = null,
        @get:DynamoDBAttribute(attributeName = dateHappenedFieldName)
        var dateHappened: Long? = null,
        @get:DynamoDBAttribute(attributeName = timestampCreatedFieldName)
        var timestampCreated: Long? = null,
        @get:DynamoDBAttribute(attributeName = textFieldName)
        var text: String? = null
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
