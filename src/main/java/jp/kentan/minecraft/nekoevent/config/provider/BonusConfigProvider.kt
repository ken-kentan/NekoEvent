package jp.kentan.minecraft.nekoevent.config.provider

import jp.kentan.minecraft.nekoevent.component.model.Bonus
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.util.Log
import jp.kentan.minecraft.nekoevent.util.formatColorCode
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.text.SimpleDateFormat

class BonusConfigProvider(
        dataFolder: File,
        private val playerConfig: PlayerConfigProvider
) : BaseConfigProvider(dataFolder, "bonus.yml") {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

    var listener: ConfigUpdateListener<Bonus>? = null

    fun load() {
        try {
            val config = YamlConfiguration.loadConfiguration(configFile)

            if (!config.isConfigurationSection("Bonus")) {
                Log.warn("Bonusセクションがありません.")
                return
            }

            val bonusIdSet = config.getConfigurationSection("Bonus").getKeys(false)
            val bonusMap = bonusIdSet.associate { id ->
                val path = "Bonus.$id"

                val startDate = dateFormat.parse(config.getString("$path.start"))
                val endDate = dateFormat.parse(config.getString("$path.end"))

                val bonus = Bonus(
                        id,
                        startDate,
                        endDate,
                        config.getString("$path.command", ""),
                        config.getString("$path.message")?.formatColorCode()
                )

                return@associate id to bonus
            }

            listener?.onConfigUpdate(bonusMap)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }
    }

    fun setReceived(player: Player, bonusId: String) {
        try {
            val (file, config) = playerConfig.getConfig(player.uniqueId)

            config.set("Bonus.$bonusId", true)

            config.save(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }
    }

    fun isNotReceived(player: Player, bonusId: String): Boolean {
        try {
            val config = playerConfig.getConfig(player.uniqueId).second
            return !config.getBoolean("Bonus.$bonusId")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }

        return false
    }
}