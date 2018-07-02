package jp.kentan.minecraft.nekoevent.util

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


@Throws(InvalidFlagException::class)
fun List<String>.flagBoolean(def: Boolean): Boolean {
    if (isEmpty()) { return def }

    return when (firstOrNull()) {
        "on" -> true
        "off" -> false
        else -> throw InvalidFlagException("(on|off)を指定してください.")
    }
}

@Throws(InvalidFlagException::class)
fun List<String>.flagInt(def: Int): Int {
    if (isEmpty()) { return def }

    return firstOrNull()?.toIntOrNull() ?: let {
        throw InvalidFlagException("整数を指定してください.")
    }
}

@Throws(InvalidFlagException::class)
fun List<String>.flagString(def: String?): String? {
    if (isEmpty()) { return def }

    return joinToString(separator = " ").also {
        if (it.isBlank()) { throw InvalidFlagException("文字列を指定してください.") }
    }
}

@Throws(InvalidFlagException::class)
fun List<String>.flagItemStack(player: Player): ItemStack {
    val itemStack = player.inventory.itemInMainHand
    if (itemStack == null || itemStack.type == Material.AIR) {
        throw InvalidFlagException("登録するアイテムを持って下さい.")
    }

    return itemStack
}

@Throws(InvalidFlagException::class)
fun List<String>.flagMaterial(def: Material?): Material? {
    if (isEmpty()) { return def }

    return Material.matchMaterial(firstOrNull() ?: "") ?: let {
        throw InvalidFlagException("正しいマテリアル名を指定してください.(例: ${Material.REDSTONE_BLOCK.name})")
    }
}

@Throws(InvalidFlagException::class)
fun List<String>.flagLocation(player: Player, def: Location?): Location? {
    if (isEmpty()) { return def }

    if (first() == "here") {
        return player.location
    } else if (size < 3) {
        throw InvalidFlagException("正しい位置情報を指定してください.(here または X Y Z)")
    }

    return player.location.apply {
        x = get(0).toDoubleOrNull() ?: throw InvalidFlagException("数値に変換できません.")
        y = get(1).toDoubleOrNull() ?: throw InvalidFlagException("数値に変換できません.")
        z = get(2).toDoubleOrNull() ?: throw InvalidFlagException("数値に変換できません.")
    }
}


class InvalidFlagException(override val message: String) : Exception()