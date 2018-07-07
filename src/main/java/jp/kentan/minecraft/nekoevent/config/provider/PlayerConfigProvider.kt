package jp.kentan.minecraft.nekoevent.config.provider

import jp.kentan.minecraft.nekoevent.util.Log
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

class PlayerConfigProvider(dataFolder: File) : BaseConfigProvider(dataFolder, "players${File.separator}") {

    @Throws(Exception::class)
    fun getConfig(uuid: UUID): Pair<File, YamlConfiguration> {
        val file = File(configFile, "$uuid.yml")
        return file to YamlConfiguration.loadConfiguration(file)
    }

    fun get(uuid: UUID, path: String): Any? {
        try {
            val config = getConfig(uuid).second
            return config.get(path)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }

        return null
    }

    fun save(uuid: UUID, dataMap: Map<String, Any>): Boolean {
        try {
            val file = File(configFile, "$uuid.yml")
            if (!file.exists()) {
                file.createNewFile()
            }

            val config = YamlConfiguration.loadConfiguration(file)
            dataMap.forEach(config::set)

            config.save(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
            return false
        }

        return true
    }
}