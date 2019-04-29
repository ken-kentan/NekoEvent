package jp.kentan.minecraft.nekoevent.component.model

import jp.kentan.minecraft.nekoevent.component.TicketType
import jp.kentan.minecraft.nekoevent.util.formatColorCode
import jp.kentan.minecraft.nekoevent.util.isFull
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

abstract class Ticket(
        type: TicketType,
        lore: List<String>,
        private val checkString: String
) {
    protected val itemStack: ItemStack = ItemStack(Material.PAPER)

    protected val dropMessage = "インベントリに空きがないため ${type.displayName}&r を&cドロップ&rしました.".formatColorCode()
    private val shortageMessage = "${type.displayName}&eが{amount}枚&c不足&eしています.".formatColorCode()

    init {
        val meta = itemStack.itemMeta?.apply {
            setDisplayName(type.displayName)
            setLore(lore)
            addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, false)
            addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
        itemStack.itemMeta = meta
    }

    open fun give(player: Player, amount: Int) {
        val itemStack = itemStack.clone()

        itemStack.amount = amount

        if (player.inventory.isFull()) {
            player.sendMessage(dropMessage)
            player.world.dropItemNaturally(player.location, itemStack)
        } else {
            player.inventory.addItem(itemStack)
        }
    }

    open fun isSimilar(playerName: String, similarItem: ItemStack?): Boolean {
        if (similarItem == null || !similarItem.hasItemMeta()) {
            return false
        }

        val meta = similarItem.itemMeta ?: return false

        return itemStack.type == similarItem.type && meta.hasLore() && meta.lore?.firstOrNull()?.contains(checkString) == true
    }

    fun getShortageMessage(amount: Int): String {
        return shortageMessage.replace("{amount}", amount.toString())
    }
}