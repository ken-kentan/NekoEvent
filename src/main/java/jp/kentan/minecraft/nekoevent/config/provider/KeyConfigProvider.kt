package jp.kentan.minecraft.nekoevent.config.provider

import jp.kentan.minecraft.nekoevent.component.model.Key
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.util.Log
import jp.kentan.minecraft.nekoevent.util.formatColorCode
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
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

            val keyIdSet = config.getConfigurationSection("Key").getKeys(false)
            val keyMap = keyIdSet.associate { id ->
                val path = "Key.$id"

                val key  = Key(
                        id,
                        ItemStack.deserialize(config.getConfigurationSection("$path.ItemStack").getValues(true)),
                        config.getBoolean("$path.enabledTake"),
                        config.getInt("$path.expireMinutes"),
                        if (config.isString("$path.Block.material"))
                            Material.matchMaterial(config.getString("$path.Block.material"))
                        else
                            null,
                        if (config.isConfigurationSection("$path.Block.Location"))
                            Location(
                                    Bukkit.getWorld(config.getString("$path.Block.Location.world")),
                                    config.getDouble("$path.Block.Location.x"),
                                    config.getDouble("$path.Block.Location.y"),
                                    config.getDouble("$path.Block.Location.z")
                            )
                        else
                            null,
                        config.getString("$path.matchMessage").formatColorCode(),
                        config.getString("$path.notMatchMessage").formatColorCode(),
                        config.getString("$path.expiredMessage").formatColorCode(),
                        config.getString("$path.shortAmountMessage").formatColorCode()
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
        val dataMap = LinkedHashMap<String, Any>().apply {
            put("$path.enabledTake", key.enabledTake)
            put("$path.expireMinutes", key.expireMinutes)

            key.block?.let { (material, location) ->
                put("$path.Block.material", material.toString())
                put("$path.Block.Location.world", location.world.name)
                put("$path.Block.Location.x", location.blockX)
                put("$path.Block.Location.y", location.blockY)
                put("$path.Block.Location.z", location.blockZ)
            }

            key.matchMessage?.let { put("$path.matchMessage", it) }
            key.notMatchMessage?.let { put("$path.notMatchMessage", it) }
            key.expiredMessage?.let { put("$path.expiredMessage", it) }
            key.shortAmountMessage?.let { put("$path.shortAmountMessage", it) }

            put("$path.ItemStack.", key.getItemStack())
        }

        return super.save(dataMap)
    }

    fun delete(keyId: String): Boolean {
        return super.save(mapOf("Key.$keyId" to null))
    }
}