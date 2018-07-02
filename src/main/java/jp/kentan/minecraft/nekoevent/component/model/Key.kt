package jp.kentan.minecraft.nekoevent.component.model

import jp.kentan.minecraft.nekoevent.component.KeyResult
import jp.kentan.minecraft.nekoevent.util.formatColorCode
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class Key(
        val id: String,
        private val itemStack: ItemStack,
        val enabledTake: Boolean = true,
        val expireMinutes: Int = 0,
        val blockMaterial: Material? = null,
        val blockLocation: Location? = null,
        val matchMessage: String?       = "&b一致",
        val notMatchMessage: String?    = "&c不一致",
        val expiredMessage: String?     = "&e期限切れです.",
        val shortAmountMessage: String? = "&6数が足りません."
) {

    companion object {
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
    }

    val name: String = itemStack.itemMeta?.displayName ?: itemStack.type.name
    val amount = itemStack.amount

    val block: Pair<Material, Location>? = if (blockMaterial != null && blockLocation != null) Pair(blockMaterial, blockLocation) else null

    val formatMatchMessage = matchMessage.format()
    val formatNotMatchMessage = notMatchMessage.format()
    val formatExpiredMessage = expiredMessage.format()
    val formatShortAmountMessage = shortAmountMessage.format()

    val enabledExpire = expireMinutes > 0

    fun getItemStack() = itemStack.clone()

    fun getItemStack(amount: Int): ItemStack {
        val itemStack = itemStack.clone()

        itemStack.apply {
            if (enabledExpire) {
                itemMeta = itemMeta.also { meta ->
                    val expired = DATE_FORMAT.format(ZonedDateTime.now().plusMinutes(expireMinutes.toLong()))

                    val lore = meta.lore ?: mutableListOf()
                    lore.add("expired @ $expired")
                    meta.lore = lore
                }
            }

            setAmount(amount)
        }

        return itemStack
    }

    fun compareTo(target: ItemStack?): KeyResult {
        if (target == null) {
            return KeyResult.NOT_MATCH
        }

        if (enabledExpire) {
            val compare = target.clone()

            val strPeriod = compare.itemMeta.run {
                if (this == null) {
                    return KeyResult.NOT_MATCH
                }

                val strPeriod = lore.lastOrNull() ?: ""
                lore = lore.dropLast(1) //比較用に最終行消去

                compare.itemMeta = this

                return@run strPeriod
            }

            // フォーマット [expired @ yyyy-MM-dd HH:mm]
            if (!isSimilar(compare) || strPeriod.length < 26) {
                return KeyResult.NOT_MATCH
            }

            if (isExpired(strPeriod)) {
                return KeyResult.EXPIRED
            }
        } else if (!isSimilar(target)) {
            return KeyResult.NOT_MATCH
        }

        return if (target.amount >= itemStack.amount) KeyResult.MATCH else KeyResult.SHORT_AMOUNT
    }

    private fun isSimilar(target: ItemStack?): Boolean {
        if (target == null) { return false }

        return if (target == itemStack) {
            true
        } else{
            itemStack.type == target.type && itemStack.hasItemMeta() == target.hasItemMeta() && (!itemStack.hasItemMeta() || Bukkit.getItemFactory().equals(itemStack.itemMeta, target.itemMeta))
        }
    }

    private fun isExpired(strPeriod: String): Boolean {
        try {
            val period = ZonedDateTime.parse(strPeriod, DATE_FORMAT)
            return period < ZonedDateTime.now()
        } catch (e: Exception) {}

        return false
    }

    private fun String?.format() = this?.replace("{name}", name)?.formatColorCode()
}