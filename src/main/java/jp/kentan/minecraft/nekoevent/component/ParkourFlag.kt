package jp.kentan.minecraft.nekoevent.component

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

enum class ParkourFlag(
        val id: String,
        val help: String
) {
    REWARD_TICKET("reward-ticket", "[amount] イベントチケット報酬"),
    JOIN_LOCATION("join-location", "[here/x y z <yaw pitch>] 参加TP位置"),
    JOIN_MESSAGE("join-msg", "[msg] 参加時に表示"),
    JOIN_BROADCAST_MESSAGE("join-broadcast-msg", "[msg] 参加時に全員に表示"),
    CLEAR_LOCATION("clear-location", "[here/x y z <yaw pitch>] クリアTP位置"),
    CLEAR_MESSAGE("clear-msg", "[msg] クリア時に表示"),
    CLEAR_BROADCAST_MESSAGE("clear-broadcast-msg", "[msg] クリア時に全員に表示"),
    BACK_LOCATION("back-location", "[here/x y z <yaw pitch>] バックTP位置"),
    BACK_MESSAGE("back-msg", "[msg] バック時に表示"),;

    companion object {
        val idList = values().map { it.id }

        fun find(flagId: String) = values().find { it.id == flagId }

        fun sendList(sender: CommandSender) {
            sender.sendMessage("---------- NekoEvent Parkourフラグヘルプ ----------")

            values().forEach { flag ->
                sender.sendMessage("| " + ChatColor.AQUA + "${flag.id} ${flag.help}")
            }

            sender.sendMessage("| " + ChatColor.GRAY + "必須: [], 任意: <>")
            sender.sendMessage("---------------------------------------")
        }
    }
}