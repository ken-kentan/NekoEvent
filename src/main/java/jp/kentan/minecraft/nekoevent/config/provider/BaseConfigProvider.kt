package jp.kentan.minecraft.nekoevent.config.provider

import jp.kentan.minecraft.nekoevent.util.Log
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

abstract class BaseConfigProvider(dataFolder: File, fileName: String? = null) {

    protected val configFile: File? = if (fileName != null) {
        File("$dataFolder${File.separator}$fileName")
    } else {
        null
    }

    init {
        createIfNeed()
    }

    private fun createIfNeed() {
        configFile?.let { file ->
            if (file.exists()) { return }

            try {
                if (file.createNewFile()) {
                    Log.info(file.name + "を作成しました.")
                } else {
                    Log.warn(file.name + "の作成に失敗しました.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun save(dataMap: Map<String, Any?>): Boolean {
        try {
            val config = YamlConfiguration.loadConfiguration(configFile)

            dataMap.forEach(config::set)

            config.save(configFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
            return false
        }

        return true
    }
}