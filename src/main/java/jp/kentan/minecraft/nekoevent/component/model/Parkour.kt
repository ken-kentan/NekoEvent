package jp.kentan.minecraft.nekoevent.component.model

import jp.kentan.minecraft.nekoevent.util.formatColorCode
import org.bukkit.Location

data class Parkour(
        val id: String,
        val name: String = "アスレチック",
        val rewardTicketAmount: Int = 0,
        val joinLocation: Location? = null,
        val joinMessage: String? = "{name}アスレ&rに&c挑戦&r！",
        val joinBroadcastMessage: String? = null,
        val clearLocation: Location? = null,
        val clearMessage: String? = "{name}アスレ&rを&bクリア&r！",
        val clearBroadcastMessage: String? = null,
        val backLocation: Location? = null,
        val backMessage: String? = null
) {
    val hasRewardTicket = rewardTicketAmount > 0

    val formatName = name.formatColorCode()
    val formatJoinMessage = joinMessage.format()
    val formatJoinBroadcastMessage = joinBroadcastMessage.format()
    val formatClearMessage = clearMessage.format()
    val formatClearBroadcastMessage = clearBroadcastMessage.format()
    val formatBackMessage = backMessage.format()

    private fun String?.format() = this?.replace("{name}", formatName)?.formatColorCode()
}