package jp.kentan.minecraft.nekoevent.util

import jp.kentan.minecraft.nekoevent.NekoEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.PlayerInventory

fun CommandSender.sendUnknownCommand() {
    sendMessage("${NekoEvent.PREFIX}${ChatColor.YELLOW}そのコマンドは存在しません.")
}

fun CommandSender.sendInGameCommand() {
    sendMessage("${NekoEvent.PREFIX}${ChatColor.YELLOW}ゲーム内専用コマンドです.")
}

fun CommandSender.sendArgumentShortage() {
    sendMessage("${NekoEvent.PREFIX}${ChatColor.YELLOW}パラメータが不足しています.")
}

fun CommandSender.doIfParameter(size: Int, requireSize: Int, block: (sender: CommandSender) -> Unit) {
    if (size < requireSize) {
        sendArgumentShortage()
    } else {
        block(this)
    }
}

fun String.formatColorCode(): String = ChatColor.translateAlternateColorCodes('&', this)

fun String.toPlayer(): Player? = Bukkit.getServer().getPlayer(this) ?: let {
    Log.error("プレイヤー($it)が見つかりませんでした.")
    return@let null
}

fun Player.broadcastMessageWithoutMe(message: String) {
    Bukkit.getServer().onlinePlayers
            .filter { it != this }
            .forEach { it.sendMessage(message) }
}

fun PlayerInventory.isFull() = firstEmpty() == -1

fun Location.formatString() = "(${world.name}, XYZ:${x.toInt()}/${y.toInt()}/${z.toInt()})"