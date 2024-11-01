package dev.decourcy.linkwardensharehelper.data.model

data class LinkwardenRequest(
    val url: String,
    val title: String,
    val description: String = "",
    val collectionId: Int? = null,
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = false
)