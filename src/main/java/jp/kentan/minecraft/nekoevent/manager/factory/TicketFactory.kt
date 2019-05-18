package jp.kentan.minecraft.nekoevent.manager.factory

import jp.kentan.minecraft.nekoevent.component.TicketType
import jp.kentan.minecraft.nekoevent.component.TicketType.EVENT
import jp.kentan.minecraft.nekoevent.component.TicketType.VOTE
import jp.kentan.minecraft.nekoevent.component.model.Ticket
import jp.kentan.minecraft.nekoevent.util.formatColorCode
import jp.kentan.minecraft.nekoevent.util.isFull
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.regex.Pattern

class TicketFactory {

    companion object {
        fun create(type: TicketType): Ticket {
            return when (type) {
                EVENT -> EventTicket()
                VOTE  -> VoteTicket()
            }
        }
    }

    private class EventTicket : Ticket(EVENT,
            listOf("&3&oイベントワールドで使用する特別なチケット&r".formatColorCode()),
            "イベントワールドで使用する特別なチケット") {

        companion object {
            private val VOTE_TICKET = VoteTicket()
        }

        override fun isSimilar(playerName: String, similarItem: ItemStack?): Boolean {

            if (VOTE_TICKET.isSimilar(playerName, similarItem)) {
                return true
            }

            return super.isSimilar(playerName, similarItem)
        }

    }

    private class VoteTicket : Ticket(VOTE,
            listOf(LORE_CONTENT, "&8本人しか使用できないよ(｡･ω･｡)".formatColorCode()), LORE_CONTENT) {

        companion object {
            private val LORE_PATTERN = Pattern.compile(".*#(\\w*)")
            private val LORE_CONTENT = "&6&o投票&3&oでもらえる不思議なチケット&r #".formatColorCode()
        }

        override fun give(player: Player, amount: Int) {
            val itemStack = itemStack.clone().apply {
                setAmount(amount)
            }

            val meta = itemStack.itemMeta?.apply {
                val lore = ArrayList<String>()
                lore.addAll(getLore().orEmpty())
                lore[0] = LORE_CONTENT + player.name
                setLore(lore)
            }
            itemStack.itemMeta = meta

            if (player.inventory.isFull()) {
                player.sendMessage(dropMessage)
                player.world.dropItemNaturally(player.location, itemStack)
            } else {
                player.inventory.addItem(itemStack)
            }
        }

        override fun isSimilar(playerName: String, similarItem: ItemStack?): Boolean {
            if (!super.isSimilar("", similarItem)) {
                return false
            }

            val matcher = LORE_PATTERN.matcher(similarItem?.itemMeta?.lore?.get(0))
            if (!matcher.find()) {
                return false
            }

            val ownerName = matcher.group(1)
            return playerName == ownerName
        }
    }
}