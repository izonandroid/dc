// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json                 = Json { allowStructuredMapKeys = true }
// val serviceOrderBuyAgain = json.parse(ServiceOrderBuyAgain.serializer(), jsonString)

package com.izontechnology.dcapp.data


data class WrappedResponse <T> (
    val status: String? = null,
    val data: T? = null,
    val message: String? = null,
    val devicebrightness: String? = null
)

