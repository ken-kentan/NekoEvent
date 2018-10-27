package jp.kentan.minecraft.nekoevent.util

import jp.kentan.minecraft.nekoevent.NekoEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.PlayerInventory

fun CommandSender.sendUnknownCommand() {
    sendMessage("${NekoEvent.PREFIX}${ChatColor.YELLOW}そのコマンドは存在しません.")
}

fun CommandSender.sendInGameCommand() {
    sendMessage("${NekoEvent.PREFIX}${ChatColor.YELLOW}ゲーム内専用コマンドです.")
}

fun CommandSender.sendCommandBlockCommand() {
    sendMessage("${NekoEvent.PREFIX}${ChatColor.YELLOW}コマンドブロック専用コマンドです.")
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
        .map { it.name }
        .filter { it.startsWith(filter) }

fun Player.broadcastMessageWithoutMe(message: String) {
    Bukkit.getServer().onlinePlayers
            .filter { it != this }
            .forEach { it.sendMessage(message) }
}

fun Player.resetStatus() {
    activePotionEffects.forEach { e -> player.removePotionEffect(e.type) }
    fireTicks = 0

    val maxHealth = getAttribute(Attribute.GENERIC_MAX_HEALTH).value

    if (maxHealth >= 1.0) {
        health = maxHealth
    }
}

fun Player.resetHealthStatus() {
    val maxHealth = getAttribute(Attribute.GENERIC_MAX_HEALTH)
    maxHealth.baseValue = maxHealth.defaultValue
}

fun PlayerInventory.isFull() = firstEmpty() == -1

fun Location.formatString() = "(${world.name}, XYZ:$blockX/$blockY/$blockZ)"

fun List<String>.toLocationOrError(): Location? {
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

/**
 * 相対座標対応版
 * [x y z] or [x y z yaw pitch]
 */
fun List<String>.toLocationOrError(base: Location): Location? {
    if (size !in 3..5) {
        Log.error("座標パラメータ数が不正です.")
        return null
    }

    val vector5 = doubleArrayOf(
            base.x,
            base.y,
            base.z,
            base.yaw.toDouble(),
            base.pitch.toDouble()
    )

    forEachIndexed { index, str ->
        if (str.startsWith("~")) {
            val strVal = str.substring(1)
            if (strVal.isNotEmpty()) {
                vector5[index] += strVal.toDoubleOrError() ?: return null
            }
        } else {
            vector5[index] = str.toDoubleOrError() ?: return null
        }
    }

    return Location(base.world, vector5[0], vector5[1], vector5[2], vector5[3].toFloat(), vector5[4].toFloat())
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