package jp.kentan.minecraft.nekoevent.config.provider

import jp.kentan.minecraft.nekoevent.component.model.Key
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.util.Log
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class KeyConfigProvider(dataFolder: File) : BaseConfigProvider(dataFolder, "key.yml") {

    var listener: ConfigUpdateListener<Key>? = null

    fun load() {
        try {
            val config = YamlConfiguration.loadConfiguration(configFile)

            if (!config.isConfigurationSection("Key")) {
                Log.warn("Keyセクションがありません.")
                return
            }

            val keyIdSet = config.getConfigurationSection("Key")?.getKeys(false).orEmpty()
            val keyMap = keyIdSet.associate { id ->
                val path = "Key.$id"

                val key = Key(
                        id,
                        config.getItemStack("$path.ItemStack") ?: throw IllegalArgumentException(),
                        config.getBoolean("$path.enabledTake"),
                        config.getInt("$path.expireMinutes"),
                        if (config.isString("$path.Block.material"))
                            Material.matchMaterial(config.getString("$path.Block.material").orEmpty())
                        else
                            null,
                        if (config.isConfigurationSection("$path.Block.Location"))
                            Location(
                                    Bukkit.getWorld(config.getString("$path.Block.Location.world").orEmpty()),
                                    config.getDouble("$path.Block.Location.x"),
                                    config.getDouble("$path.Block.Location.y"),
                                    config.getDouble("$path.Block.Location.z")
                            )
                        else
                            null,
                        config.getString("$path.matchMessage", null),
                        config.getString("$path.notMatchMessage", null),
                        config.getString("$path.expiredMessage", null),
                        config.getString("$path.shortAmountMessage", null)
                )

                return@associate id to key
            }

            listener?.onConfigUpdate(keyMap)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }
    }

    fun update(key: Key): Boolean {
        val path = "Key.${key.id}"
        val dataMap = LinkedHashMap<String, Any?>().apply {
            put("$path.enabledTake", key.enabledTake)
            put("$path.expireMinutes", key.expireMinutes)

            put("$path.Block.material", key.blockMaterial?.name)

            if (key.blockLocation != null) {
                val loc = key.blockLocation
                put("$path.Block.Location.world", loc.world?.name)
                put("$path.Block.Location.x", loc.blockX)
                put("$path.Block.Location.y", loc.blockY)
                put("$path.Block.Location.z", loc.blockZ)
            } else {
                put("$path.Block.Location", null)
            }

            put("$path.matchMessage", key.matchMessage)
            put("$path.notMatchMessage", key.notMatchMessage)
            put("$path.expiredMessage", key.expiredMessage)
            put("$path.shortAmountMessage", key.shortAmountMessage)

            put("$path.ItemStack", key.getItemStack())
        }

        return super.save(dataMap)
    }

    fun delete(keyId: String): Boolean {
        return super.save(mapOf("Key.$keyId" to null))
    }
}