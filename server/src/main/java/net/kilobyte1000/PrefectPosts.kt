package net.kilobyte1000

enum class PrefectPosts(val cssName: String, val placeHolderName: String) {
    HOUSE_PREFECT_BOY("house_boy", "<!--House Prefect Boy Section-->"),
    HOUSE_PREFECT_GIRL("house_girl", "<!--House Prefect Girl Section-->"),
    SPORTS_PREFECT_BOY("sports_boy", "<!--Sports Prefect Boy Section-->"),
    SPORTS_PREFECT_GIRL("sports_girl","<!--Sports Prefect Girl Section-->");

    companion object {
        const val postCount = 4
    }
}