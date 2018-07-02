package jp.kentan.minecraft.nekoevent.config

import jp.kentan.minecraft.nekoevent.config.provider.GachaConfigProvider
import jp.kentan.minecraft.nekoevent.config.provider.KeyConfigProvider
import jp.kentan.minecraft.nekoevent.config.provider.SignConfigProvider
import java.io.File

class ConfigManager(dataFolder: File) {
    val signConfigProvider = SignConfigProvider(dataFolder)
    val gachaConfigProvider = GachaConfigProvider(dataFolder)
    val keyConfigProvider = KeyConfigProvider(dataFolder)

    fun load() {
        gachaConfigProvider.load()
        keyConfigProvider.load()
    }
}