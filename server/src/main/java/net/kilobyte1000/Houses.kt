package net.kilobyte1000

enum class Houses(val displayText: String) {
    KABIR("Kabir"),
    RAMAN("Raman"),
    TAGORE("Tagore"),
    TILAK("Tilak"),
    VASHISHTH("Vashishth"),
    VIVEKANAND("Vivekanand");

    companion object {
        const val SIZE = 6
    }
}