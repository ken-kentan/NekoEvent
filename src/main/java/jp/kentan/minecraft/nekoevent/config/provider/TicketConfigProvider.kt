package jp.kentan.minecraft.nekoevent.config.provider

import jp.kentan.minecraft.nekoevent.component.TicketType
import jp.kentan.minecraft.nekoevent.config.ConfigManager.Companion.DATE_FORMAT
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.util.Log
import org.apache.commons.lang.time.DateUtils
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*

class TicketConfigProvider(
        dataFolder: File,
        private val playerConfig: PlayerConfigProvider
) : BaseConfigProvider(dataFolder, "ticket.yml") {

    var listener: ConfigUpdateListener<Any>? = null

    fun load() {
        try {
            val config = YamlConfiguration.loadConfiguration(configFile)

            val dataMap = TicketType.values().associate { type ->
                "Ticket.${type.path}.dayLimitAmount" to config.getInt("Ticket.${type.path}.dayLimitAmount")
            }

            listener?.onConfigUpdate(dataMap)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }
    }

    fun getTodayTicketAmount(player: Player, type: TicketType): Int {
        try {
            val config = playerConfig.getConfig(player.uniqueId).second

            val path = "Ticket.${type.path}"

            val strDate = config.getString("$path.date") ?: return 0
            val amount = config.getInt("$path.amount")

            val prev = DATE_FORMAT.parse(strDate)
            val today = DateUtils.truncate(Date(), Calendar.DAY_OF_MONTH)

            if (prev >= today) {
                return amount
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }

        return 0
    }

    fun addTodayTicketAmount(player: Player, type: TicketType, addAmount: Int) {
        try {
            val (file, config) = playerConfig.getConfig(player.uniqueId)

            val path = "Ticket.${type.path}"

            val strDate = config.getString("$path.date", "2000/01/01")
            val oldAmount = config.getInt("$path.amount")

            val prev = DATE_FORMAT.parse(strDate)
            val today = DateUtils.truncate(Date(), Calendar.DAY_OF_MONTH)

            val amount = if (prev >= today) {
                oldAmount + addAmount
            } else {
                addAmount
            }

            config.set("$path.date", DATE_FORMAT.format(Date()))
            config.set("$path.amount", amount)

            config.save(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }
    }
}