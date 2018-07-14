package jp.kentan.minecraft.nekoevent.component

enum class GachaCost(
        private val id: String,
        val signText: String
) {
    FREE("free", ""),
    EVENT_TICKET("event", ""),
    VOTE_TICKET("vote", ""),
    KEY("key", "");

    companion object {
        fun find(id: String?) = values().find { it.id == id?.toLowerCase() }
    }
}