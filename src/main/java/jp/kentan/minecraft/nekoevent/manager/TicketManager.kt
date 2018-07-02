package jp.kentan.minecraft.nekoevent.manager

import jp.kentan.minecraft.nekoevent.NekoEvent
import jp.kentan.minecraft.nekoevent.manager.factory.TicketFactory
import jp.kentan.minecraft.nekoevent.component.TicketType
import jp.kentan.minecraft.nekoevent.component.TicketType.*
import jp.kentan.minecraft.nekoevent.util.Log
import org.bukkit.entity.Player
import kotlin.math.max

class TicketManager {

    private val eventTicket = TicketFactory.create(EVENT)
    private val voteTicket  = TicketFactory.create(VOTE)

    fun give(player: Player, type: TicketType, amount: Int) {
        if (amount < 1) {
            Log.warn("1以上の枚数を指定して下さい.")
            return
        }

        when (type) {
            EVENT -> eventTicket.give(player, amount)
            VOTE -> voteTicket.give(player, amount)
        }

        Log.info("${player.name}に${type.displayName}を${amount}枚与えました.")
    }

    fun take(player: Player, type: TicketType, amount: Int): Boolean {
        if (amount < 1) {
            Log.warn("1以上の枚数を指定して下さい.")
            return false
        }

        val playerName = player.name
        val inventory = player.inventory

        val ticket = when (type) {
            EVENT -> eventTicket
            VOTE -> voteTicket
        }

        val playerTicketAmount = player.inventory.contents.filter { ticket.isSimilar(playerName, it) }.sumBy { it.amount }

        // 枚数不足確認
        if (playerTicketAmount < amount) {
            player.sendMessage(NekoEvent.PREFIX + ticket.getShortageMessage(amount - playerTicketAmount))
            return false
        }

        var ticketAmount = amount

        for (i in 0 until inventory.size) {
            val item = inventory.getItem(i)
            if (item == null || !ticket.isSimilar(playerName, item)) {
                continue
            }

            val newAmount = item.amount - ticketAmount
            item.amount = newAmount
            inventory.setItem(i, if (newAmount > 0) item else null)

            ticketAmount = max(-newAmount, 0)

            if (ticketAmount <= 0) {
                break
            }
        }

        return true
    }
}