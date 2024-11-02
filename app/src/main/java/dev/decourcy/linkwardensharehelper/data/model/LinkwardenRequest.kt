package dev.decourcy.linkwardensharehelper.data.model

data class LinkwardenRequest(
    val url: String? = null,
    val name: String? = null,
    val description: String? = null,
    val type: String? = null,
    val tags: List<Tag> = emptyList(),
    val collectionId: Collection? = null
) {
    data class Tag(val name: String)
    data class Collection(val id: Int)
}
