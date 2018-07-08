package jp.kentan.minecraft.nekoevent.config.provider

import jp.kentan.minecraft.nekoevent.util.Log
import jp.kentan.minecraft.nekoevent.util.formatString
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.LinkedHashMap

class SignConfigProvider(dataFolder: File) : BaseConfigProvider(dataFolder, "sign.yml") {

    private val signMetadataMap = mutableMapOf<Location, Metadata>()

    init {
        load()
    }

    fun load() {
        try {
            val config = YamlConfiguration.loadConfiguration(configFile)

            if (!config.isConfigurationSection("Sign")) {
                Log.warn("Signセクションが見つかりません.")
                return
            }

            val indexSet = config.getConfigurationSection("Sign").getKeys(false)

            val metadataMap = indexSet.mapNotNull { strIndex ->
                val path = "Sign.$strIndex"

                val location = Location(
                        Bukkit.getWorld(config.getString("$path.Location.world")),
                        config.getDouble("$path.Location.x"),
                        config.getDouble("$path.Location.y"),
                        config.getDouble("$path.Location.z")
                )

                //ワールドが見つからない場合は消去せずスキップ
                if (location.world == null) {
                    Log.warn("看板($strIndex)が見つかりませんでした.")
                    return@mapNotNull null
                }

                // 看板がない場合は消去
                if (location.block.state !is Sign) {
                    config.set(path, null)
                    Log.warn("看板${location.formatString()}が見つかりませんでした.")
                    return@mapNotNull null
                }

                val index = strIndex.toIntOrNull() ?: let {
                    Log.warn("看板($strIndex)のindexを整数に変換できません.")
                    return@mapNotNull null
                }


                val metadataSet = config.getConfigurationSection("$path.Metadata").getKeys(false)
                val metadataPath = "$path.Metadata."
                val metadataMap = metadataSet.associate { it to config.get(metadataPath + it) }.toMutableMap()

                return@mapNotNull location to Metadata(index, metadataMap)
            }.toMap()

            config.save(configFile)

            signMetadataMap.clear()
            signMetadataMap.putAll(metadataMap)

            Log.info("${signMetadataMap.size}件の看板ﾒﾀﾃﾞｰﾀを読み込みました.")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
        }
    }

    fun save(location: Location, dataMap: Map<String, Any>): Boolean {
        try {
            val config = YamlConfiguration.loadConfiguration(configFile)

            // クリーニング
            signMetadataMap[location]?.apply {
                super.save(mapOf("Sign.${this.index}" to null))
            }

            // 利用可能なindexを探す
            val index: Int = kotlin.run {
                var count = 0
                var prev = 0
                config.getConfigurationSection("Sign").getKeys(false)
                        .mapNotNull { it.toInt() }
                        .sorted()
                        .forEach {
                            count = it

                            if (count - prev > 1) { return@run prev + 1 }

                            prev = count
                        }

                return@run count + 1
            }

            val path = "Sign.$index"

            val metadataMap = LinkedHashMap<String, Any>().apply {
                put("$path.Location.world", location.world.name)
                put("$path.Location.x", location.x)
                put("$path.Location.y", location.y)
                put("$path.Location.z", location.z)

                val metadataPath = "$path.Metadata."

                dataMap.forEach { key, data -> put(metadataPath + key, data) }
            }

            if (super.save(metadataMap)) {
                signMetadataMap[location] = SignConfigProvider.Metadata(index, dataMap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.error(e)
            return false
        }

        return true
    }

    fun getMetadata(location: Location, key: String): Any? {
        return signMetadataMap[location]?.get(key) ?: let {
            Log.error("看板" + location.formatString() + "にﾒﾀﾃﾞｰﾀ($key)が不足しています.")
            return null
        }
    }

    private class Metadata(
            val index: Int,
            val metadataMap: Map<String, Any>)
    {
        fun get(key: String) = metadataMap[key]
    }
}