package jp.kentan.minecraft.nekoevent.config.provider

import jp.kentan.minecraft.nekoevent.component.model.Dungeon
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

class DungeonConfigProvider(
        dataFolder: File,
        private val playerConfig: PlayerConfigProvider
) : BaseConfigProvider(dataFolder, "dungeon.yml") {

    var listener: ConfigUpdateListener<Dungeon>? = null

    fun load() {
        try {
            val config = YamlConfiguration.loadConfiguration(configFile)

            if (!config.isConfigurationSection("Dungeon")) {
                Log.warn("Dungeonセクションがありません.")
                return
            }

            val dungeonIdSet = config.getConfigurationSection("Dungeon").getKeys(false)
            val dungeonMap = dungeonIdSet.associate { id ->
                val path = "Dungeon.$id"

                val dungeon = Dungeon(
                        id,
                        config.getString("$path.name", "ダンジョン"),
                        config.getInt("$path.Reward.ticketAmount"),
                        config.getString("$path.Reward.gachaId"),
                        if (config.isConfigurationSection("$path.Join.Location"))
                            Location(
                                    Bukkit.getWorld(config.getString("$path.Join.Location.world")),
                                    config.getDouble("$path.Join.Location.x"),
                                    config.getDouble("$path.Join.Location.y"),
                                    config.getDouble("$path.Join.Location.z"),
                                    config.getDouble("$path.Join.Location.pitch").toFloat(),
                                    config.getDouble("$path.Join.Location.yaw").toFloat()
                            )
                        else
                            null,
                        config.getString("$path.Join.message"),
                        config.getString("$path.Join.broadcastMessage"),
                        if (config.isConfigurationSection("$path.Clear.Location"))
                            Location(
                                    Bukkit.getWorld(config.getString("$path.Clear.Location.world")),
                                    config.getDouble("$path.Clear.Location.x"),
                                    config.getDouble("$path.Clear.Location.y"),
                                    config.getDouble("$path.Clear.Location.z"),
                                    config.getDouble("$path.Clear.Location.pitch").toFloat(),
                                    config.getDouble("$path.Clear.Location.yaw").toFloat()
                            )
                        else
                            null,
                        config.getString("$path.Clear.message"),
                        config.getString("$path.Clear.broadcastMessage"),
                        config.getBoolean("$path.Clear.enabledSound"),
                        config.getString("$path.Lock.message"),
                        config.getString("$path.Lock.broadcastMessage"),
                        config.getString("$path.Unlock.broadcastMessage")
                )

                return@associate id to dungeon
            }

            listener?.onConfigUpdate(dungeonMap)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }
    }

    fun update(dungeon: Dungeon): Boolean {
        val path = "Dungeon.${dungeon.id}"
        val dataMap = LinkedHashMap<String, Any?>().apply {
            put("$path.name", dungeon.name)

            put("$path.Reward.ticketAmount", dungeon.rewardTicketAmount)
            put("$path.Reward.gachaId", dungeon.rewardGachaId)

            if (dungeon.joinLocation != null) {
                val loc = dungeon.joinLocation
                put("$path.Join.Location.world", loc.world.name)
                put("$path.Join.Location.x", loc.x)
                put("$path.Join.Location.y", loc.y)
                put("$path.Join.Location.z", loc.z)
                put("$path.Join.Location.yaw", loc.yaw)
                put("$path.Join.Location.pitch", loc.pitch)
            } else {
                put("$path.Join.Location", null)
            }
            put("$path.Join.message", dungeon.joinMessage)
            put("$path.Join.broadcastMessage", dungeon.joinBroadcastMessage)

            if (dungeon.clearLocation != null) {
                val loc = dungeon.clearLocation
                put("$path.Clear.Location.world", loc.world.name)
                put("$path.Clear.Location.x", loc.x)
                put("$path.Clear.Location.y", loc.y)
                put("$path.Clear.Location.z", loc.z)
                put("$path.Clear.Location.yaw", loc.yaw)
                put("$path.Clear.Location.pitch", loc.pitch)
            } else {
                put("$path.Clear.Location", null)
            }
            put("$path.Clear.message", dungeon.clearMessage)
            put("$path.Clear.broadcastMessage", dungeon.clearBroadcastMessage)
            put("$path.Clear.enabledSound", dungeon.enabledClearSound)

            put("$path.Lock.message", dungeon.lockMessage)
            put("$path.Lock.broadcastMessage", dungeon.lockBroadcastMessage)

            put("$path.Unlock.broadcastMessage", dungeon.unlockBroadcastMessage)
        }

        return super.save(dataMap)
    }

    fun delete(keyId: String): Boolean {
        return super.save(mapOf("Dungeon.$keyId" to null))
    }

    fun hasClearedToday(player: Player, dungeon: Dungeon): Boolean {
        try {
            val oldDate = playerConfig.get(player.uniqueId, "Dungeon.${dungeon.id}.clearDate") as String? ?: return false

            val prev = DATE_FORMAT.parse(oldDate)
            val today = DateUtils.truncate(Date(), Calendar.DAY_OF_MONTH)

            return prev.compareTo(today) == 0
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }

        return false
    }

    fun saveClearDate(player: Player, dungeon: Dungeon) {
        playerConfig.save(player.uniqueId, mapOf("Dungeon.${dungeon.id}.clearDate" to DATE_FORMAT.format(Date())))
    }
}