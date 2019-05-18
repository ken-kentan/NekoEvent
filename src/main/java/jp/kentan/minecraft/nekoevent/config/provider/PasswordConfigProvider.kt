package jp.kentan.minecraft.nekoevent.config.provider

import jp.kentan.minecraft.nekoevent.component.model.Password
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.util.Log
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class PasswordConfigProvider(dataFolder: File) : BaseConfigProvider(dataFolder, "password.yml") {

    var listener: ConfigUpdateListener<Password>? = null

    fun load() {
        try {
            val config = YamlConfiguration.loadConfiguration(configFile)

            if (!config.isConfigurationSection("Password")) {
                Log.warn("Passwordセクションがありません.")
                return
            }

            val passwordIdSet = config.getConfigurationSection("Password")?.getKeys(false).orEmpty()
            val passwordMap = passwordIdSet.associate { id ->
                val path = "Password.$id"

                val password = Password(
                        id,
                        config.getString("$path.default").orEmpty(),
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
                        config.getString("$path.matchMessage"),
                        config.getString("$path.notMatchMessage"),
                        config.getString("$path.inputMessage"),
                        config.getString("$path.resetMessage")
                )

                return@associate id to password
            }

            listener?.onConfigUpdate(passwordMap)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }
    }

    fun update(password: Password): Boolean {
        val path = "Password.${password.id}"
        val dataMap = LinkedHashMap<String, Any?>().apply {
            put("$path.default", password.default)
            put("$path.Block.material", password.blockMaterial?.name)

            if (password.blockLocation != null) {
                val loc = password.blockLocation
                put("$path.Block.Location.world", loc.world?.name)
                put("$path.Block.Location.x", loc.blockX)
                put("$path.Block.Location.y", loc.blockY)
                put("$path.Block.Location.z", loc.blockZ)
            } else {
                put("$path.Block.Location", null)
            }

            put("$path.matchMessage", password.matchMessage)
            put("$path.notMatchMessage", password.notMatchMessage)
            put("$path.inputMessage", password.inputMessage)
            put("$path.resetMessage", password.resetMessage)
        }

        return super.save(dataMap)
    }

    fun delete(keyId: String): Boolean {
        return super.save(mapOf("Password.$keyId" to null))
    }
}