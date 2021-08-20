package net.kilobyte1000

class PrefectList {
    private val prefectList = List(PrefectPosts.postCount) { mutableListOf<String>()}

    fun getList(post: PrefectPosts) = prefectList[post.ordinal]

    val allContainElements
        get() = prefectList.all { it.isNotEmpty() }
}