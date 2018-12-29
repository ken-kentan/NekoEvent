package jp.kentan.minecraft.nekoevent.util

import org.bukkit.entity.Player
import kotlin.math.roundToInt

fun Player.getExperience(): Int {
    var fixedExp = (getExpAtLevel(this) * exp).roundToInt()
    var currentLevel = level

    while (currentLevel > 0) {
        currentLevel--
        fixedExp += getExpAtLevel(currentLevel)
    }
    if (fixedExp < 0) {
        fixedExp = Int.MAX_VALUE
    }

    return fixedExp
}

fun Player.takeExperience(takeExp: Int) {
    if (takeExp < 1) {
        Log.error("1以上の整数を指定してください.")
        return
    }

    val amount = getExperience() - takeExp

    exp = 0.0f
    level = 0

    if (amount > 0) {
        giveExp(amount)
    }
}

private fun getExpAtLevel(player: Player) = getExpAtLevel(player.level)

private fun getExpAtLevel(level: Int) = when {
    level <= 15 -> 2 * level + 7
    level in 16..30 -> 5 * level - 38
    else -> 9 * level - 158
}