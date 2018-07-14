package jp.kentan.minecraft.nekoevent.config.provider

import jp.kentan.minecraft.nekoevent.component.model.Gacha
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.util.Log
import jp.kentan.minecraft.nekoevent.util.formatColorCode
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class GachaConfigProvider(dataFolder: File) : BaseConfigProvider(dataFolder, "gacha.yml") {

    var listener: ConfigUpdateListener<Gacha>? = null

    fun load() {
        try {
            val config = YamlConfiguration.loadConfiguration(configFile)

            if (!config.isConfigurationSection("Gacha")) {
                Log.warn("Gachaセクションがありません.")
                return
            }

            val gachaIdSet = config.getConfigurationSection("Gacha").getKeys(false)

            val gachaMap: Map<String, Gacha> = gachaIdSet.mapNotNull { id ->
                val path = "Gacha.$id"

                // Gacha Component
                val componentsPath = "Gacha.$id.components"
                val componentIdSet = config.getConfigurationSection(componentsPath).getKeys(false)
                val componentList = componentIdSet.map { index ->
                    val indexPath = "$componentsPath.$index"

                    return@map Gacha.Component(
                            config.getString("$indexPath.name").formatColorCode(),
                            config.getDouble("$indexPath.probability"),
                            config.getStringList("$indexPath.commands")
                    )
                }.toMutableList()

                if (!Gacha.Component.normalize(componentList)) {
                    Log.error("ガチャ($id)の正規化に失敗しました.")
                    return@mapNotNull null
                }

                // Gacha
                return@mapNotNull id to
                    Gacha(
                        id,
                        config.getString("$path.name", "ガチャ"),
                        componentList,
                        config.getString("$path.winMessage")?.formatColorCode(),
                        config.getString("$path.loseMessage")?.formatColorCode(),
                        config.getString("$path.broadcastMessage")?.formatColorCode(),
                        config.getBoolean("$path.enabledEffect", true)
                    )
            }.toMap()

            listener?.onConfigUpdate(gachaMap)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }
    }
}