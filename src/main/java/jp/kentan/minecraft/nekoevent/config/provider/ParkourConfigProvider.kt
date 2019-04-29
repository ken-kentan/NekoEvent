package jp.kentan.minecraft.nekoevent.config.provider

import jp.kentan.minecraft.nekoevent.component.model.Parkour
import jp.kentan.minecraft.nekoevent.config.ConfigManager.Companion.DATE_FORMAT
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.util.Log
import org.apache.commons.lang.time.DateUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.*

class ParkourConfigProvider(
        dataFolder: File,
        private val playerConfig: PlayerConfigProvider
) : BaseConfigProvider(dataFolder, "parkour.yml") {

    var listener: ConfigUpdateListener<Parkour>? = null

    fun load() {
        try {
            val config = YamlConfiguration.loadConfiguration(configFile)

            if (!config.isConfigurationSection("Parkour")) {
                Log.warn("Parkourセクションがありません.")
                return
            }

            val parkourIdSet = config.getConfigurationSection("Parkour")?.getKeys(false).orEmpty()
            val parkourMap = parkourIdSet.associate { id ->
                val path = "Parkour.$id"

                val parkour = Parkour(
                        id,
                        config.getString("$path.name") ?: "パルクール",
                        config.getInt("$path.Reward.ticketAmount"),
                        if (config.isConfigurationSection("$path.Join.Location"))
                            Location(
                                    Bukkit.getWorld(config.getString("$path.Join.Location.world").orEmpty()),
                                    config.getDouble("$path.Join.Location.x"),
                                    config.getDouble("$path.Join.Location.y"),
                                    config.getDouble("$path.Join.Location.z"),
                                    config.getDouble("$path.Join.Location.yaw").toFloat(),
                                    config.getDouble("$path.Join.Location.pitch").toFloat()
                            )
                        else
                            null,
                        config.getString("$path.Join.message"),
                        config.getString("$path.Join.broadcastMessage"),
                        if (config.isConfigurationSection("$path.Clear.Location"))
                            Location(
                                    Bukkit.getWorld(config.getString("$path.Clear.Location.world").orEmpty()),
                                    config.getDouble("$path.Clear.Location.x"),
                                    config.getDouble("$path.Clear.Location.y"),
                                    config.getDouble("$path.Clear.Location.z"),
                                    config.getDouble("$path.Clear.Location.yaw").toFloat(),
                                    config.getDouble("$path.Clear.Location.pitch").toFloat()
                            )
                        else
                            null,
                        config.getString("$path.Clear.message"),
                        config.getString("$path.Clear.broadcastMessage"),
                        if (config.isConfigurationSection("$path.Back.Location"))
                            Location(
                                    Bukkit.getWorld(config.getString("$path.Back.Location.world").orEmpty()),
                                    config.getDouble("$path.Back.Location.x"),
                                    config.getDouble("$path.Back.Location.y"),
                                    config.getDouble("$path.Back.Location.z"),
                                    config.getDouble("$path.Back.Location.pitch").toFloat(),
                                    config.getDouble("$path.Back.Location.yaw").toFloat()
                            )
                        else
                            null,
                        config.getString("$path.Back.message")
                )

                return@associate id to parkour
            }

            listener?.onConfigUpdate(parkourMap)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }
    }

    fun update(parkour: Parkour): Boolean {
        val path = "Parkour.${parkour.id}"
        val dataMap = LinkedHashMap<String, Any?>().apply {
            put("$path.name", parkour.name)

            put("$path.Reward.ticketAmount", parkour.rewardTicketAmount)

            if (parkour.joinLocation != null) {
                val loc = parkour.joinLocation
                put("$path.Join.Location.world", loc.world?.name)
                put("$path.Join.Location.x", loc.x)
                put("$path.Join.Location.y", loc.y)
                put("$path.Join.Location.z", loc.z)
                put("$path.Join.Location.yaw", loc.yaw)
                put("$path.Join.Location.pitch", loc.pitch)
            } else {
                put("$path.Join.Location", null)
            }
            put("$path.Join.message", parkour.joinMessage)
            put("$path.Join.broadcastMessage", parkour.joinBroadcastMessage)

            if (parkour.clearLocation != null) {
                val loc = parkour.clearLocation
                put("$path.Clear.Location.world", loc.world?.name)
                put("$path.Clear.Location.x", loc.x)
                put("$path.Clear.Location.y", loc.y)
                put("$path.Clear.Location.z", loc.z)
                put("$path.Clear.Location.yaw", loc.yaw)
                put("$path.Clear.Location.pitch", loc.pitch)
            } else {
                put("$path.Clear.Location", null)
            }
            put("$path.Clear.message", parkour.clearMessage)
            put("$path.Clear.broadcastMessage", parkour.clearBroadcastMessage)

            if (parkour.backLocation != null) {
                val loc = parkour.backLocation
                put("$path.Back.Location.world", loc.world?.name)
                put("$path.Back.Location.x", loc.x)
                put("$path.Back.Location.y", loc.y)
                put("$path.Back.Location.z", loc.z)
                put("$path.Back.Location.yaw", loc.yaw)
                put("$path.Back.Location.pitch", loc.pitch)
            } else {
                put("$path.Back.Location", null)
            }
            put("$path.Back.message", parkour.backMessage)
        }

        return super.save(dataMap)
    }

    fun delete(keyId: String): Boolean {
        return super.save(mapOf("Parkour.$keyId" to null))
    }

    fun hasClearedToday(player: Player, parkour: Parkour): Boolean {
        try {
            val oldDate = playerConfig.get(player.uniqueId, "Parkour.${parkour.id}.clearDate") as String? ?: return false

            val prev = DATE_FORMAT.parse(oldDate)
            val today = DateUtils.truncate(Date(), Calendar.DAY_OF_MONTH)

            return prev.compareTo(today) == 0
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }

        return false
    }

    fun saveClearDate(player: Player, parkour: Parkour) {
        playerConfig.save(player.uniqueId, mapOf("Parkour.${parkour.id}.clearDate" to DATE_FORMAT.format(Date())))
    }
}