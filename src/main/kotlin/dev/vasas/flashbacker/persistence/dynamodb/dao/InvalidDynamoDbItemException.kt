package dev.vasas.flashbacker.persistence.dynamodb.dao

class InvalidDynamoDbItemException(message: String) : RuntimeException(message) {

    companion object {
        fun missingMandatoryField(fieldName: String): InvalidDynamoDbItemException {
            return InvalidDynamoDbItemException("Cannot load item from DynamoDb! Missing mandatory field: $fieldName")
        }
    }
}
