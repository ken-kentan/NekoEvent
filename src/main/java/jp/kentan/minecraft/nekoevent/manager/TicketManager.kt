package jp.kentan.minecraft.nekoevent.manager

import jp.kentan.minecraft.nekoevent.NekoEvent
import jp.kentan.minecraft.nekoevent.component.TicketType
import jp.kentan.minecraft.nekoevent.component.TicketType.EVENT
import jp.kentan.minecraft.nekoevent.component.TicketType.VOTE
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.config.provider.TicketConfigProvider
import jp.kentan.minecraft.nekoevent.manager.factory.TicketFactory
import jp.kentan.minecraft.nekoevent.util.Log
import jp.kentan.minecraft.nekoevent.util.toIntOrError
import jp.kentan.minecraft.nekoevent.util.toPlayerOrError
import org.bukkit.entity.Player
import kotlin.math.max

class TicketManager(
        private val config: TicketConfigProvider
) : ConfigUpdateListener<Any> {

    private val eventTicket = TicketFactory.create(EVENT)
    private val voteTicket  = TicketFactory.create(VOTE)

    private val dayLimitAmountMap = mutableMapOf(EVENT to 5, VOTE to 2)

    init {
        config.listener = this
    }

    fun give(strPlayer: String, strType: String, strAmount: String) {
        val player = strPlayer.toPlayerOrError() ?: return
        val type: TicketType
        try {
            type = TicketType.valueOf(strType.toUpperCase())
        } catch (e: Exception) {
            Log.error(e)
            return
        }
        val amount = strAmount.toIntOrError() ?: return

        give(player, type, amount)
    }

    fun give(player: Player, type: TicketType, amount: Int, ignoreDayLimit: Boolean = true) {
        if (amount < 1) {
            Log.warn("1以上の枚数を指定して下さい.")
            return
        }

        var giveAmount = amount

        if (!ignoreDayLimit) {
            val limitAmount = dayLimitAmountMap[type]

            if (limitAmount != null) {
                val todayAmount = config.getTodayTicketAmount(player, type)

                if (todayAmount >= limitAmount) {
                    player.sendMessage(NekoEvent.PREFIX + type.getReachDayLimitMessage(limitAmount))
                    return
                }

                if ((todayAmount + amount) > limitAmount) {
                    giveAmount = limitAmount - todayAmount
                }
            } else {
                Log.warn("チケット(${type.name})の1日上限が無視されました.")
            }
        }

        when (type) {
            EVENT -> eventTicket.give(player, giveAmount)
            VOTE -> voteTicket.give(player, giveAmount)
        }

        config.addTodayTicketAmount(player, type, giveAmount)

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

    override fun onConfigUpdate(dataMap: Map<String, Any>) {
        TicketType.values().forEach { type ->
            val key = "Ticket.${type.path}.dayLimitAmount"
            val amount = dataMap[key] as Int?

            if (amount == null || amount < 1) {
                Log.warn("${key}を正しく読み込めませんでした.")
                return@forEach
            }

            dayLimitAmountMap[type] = amount

            Log.info("${key}を${amount}で設定しました.")
        }
    }
}