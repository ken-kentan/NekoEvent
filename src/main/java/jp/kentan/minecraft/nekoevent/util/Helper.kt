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

fun CommandSender.doIfArguments(args: Array<String>, requireSize: Int, block: (sender: CommandSender) -> Unit) {
    if (args.size-1 < requireSize) {
        sendArgumentShortage()
    } else {
        block(this)
    }
}

fun String.formatColorCode(): String = ChatColor.translateAlternateColorCodes('&', this)

fun String.toPlayerOrError(): Player? = Bukkit.getServer().getPlayer(this) ?: let {
    Log.error("プレイヤー($it)が見つかりませんでした.")
    return@let null
}

fun getPlayerNames(filter: String) = Bukkit.getOnlinePlayers()
        .map { it.displayName }
        .filter { it.startsWith(filter) }

fun Player.broadcastMessageWithoutMe(message: String) {
    Bukkit.getServer().onlinePlayers
            .filter { it != this }
            .forEach { it.sendMessage(message) }
}

fun PlayerInventory.isFull() = firstEmpty() == -1

fun Location.formatString() = "(${world.name}, XYZ:${x.toInt()}/${y.toInt()}/${z.toInt()})"

fun Array<String>.toLocationOrError(): Location? {
    if (size < 4) { return null }

    val world = Bukkit.getWorld(get(0)) ?: let {
        Log.error("ワールド(${get(0)})は存在しません.")
        return null
    }

    val location = Location(
            world,
            get(1).toDoubleOrError() ?: return null,
            get(2).toDoubleOrError() ?: return null,
            get(3).toDoubleOrError() ?: return null
    )

    if (size >= 6) {
        location.yaw = get(4).toFloatOrError() ?: return null
        location.pitch = get(5).toFloatOrError() ?: return null
    }

    return location
}

fun String.toIntOrError(): Int? {
    return toIntOrNull() ?: let {
        Log.error("$this)をInt(数値)に変換できません.")
        return null
    }
}
private fun String.toFloatOrError(): Float? {
    return toFloatOrNull() ?: let {
        Log.error("$this)をFloat(数値)に変換できません.")
        return null
    }
}
fun String.toDoubleOrError(): Double? {
    return toDoubleOrNull() ?: let {
        Log.error("$this)をDouble(数値)に変換できません.")
        return null
    }
}