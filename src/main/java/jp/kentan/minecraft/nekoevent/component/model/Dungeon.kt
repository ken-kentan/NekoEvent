package jp.kentan.minecraft.nekoevent.component.model

import jp.kentan.minecraft.nekoevent.util.formatColorCode
import javax.xml.stream.Location

data class Dungeon(
        val id: String,
        val name: String = "",
        val eventTicketAmount: Int = 0,
        val gachaId: String? = null,
        val joinLocation: Location? = null,
        val joinMessage: String? = "{name}&rに&c参加&rしました！",
        val joinBroadcastMessage: String? = null,
        val clearLocation: Location? = null,
        val clearMessage: String? = "&dクリアおめでとう！&rまた&c挑戦&rしてね！",
        val clearBroadcastMessage: String? = null,
        val enabledClearSound: Boolean = true,
        val timerMinutes: Int = 0,
        val timeoutBroadcastMessage: String? = null
) {

    val formatJoinMessage = joinMessage.format()
    val formatJoinBroadcastMessage = joinBroadcastMessage.format()
    val formatClearMessage = clearMessage.format()
    val formatClearBroadcastMessage = clearBroadcastMessage.format()
    val formatTimeoutBroadcastMessage = timeoutBroadcastMessage.format()

    private fun String?.format() = this?.replace("{name}", name)?.formatColorCode()
}