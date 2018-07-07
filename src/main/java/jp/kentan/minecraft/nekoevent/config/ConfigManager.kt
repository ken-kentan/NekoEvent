package jp.kentan.minecraft.nekoevent.config

import jp.kentan.minecraft.nekoevent.config.provider.*
import java.io.File
import java.text.SimpleDateFormat

class ConfigManager(dataFolder: File) {

    companion object {
        val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd")
    }

    private val playerConfigProvider = PlayerConfigProvider(dataFolder)

    val signConfigProvider = SignConfigProvider(dataFolder)
    val gachaConfigProvider = GachaConfigProvider(dataFolder)
    val keyConfigProvider = KeyConfigProvider(dataFolder)
    val passwordConfigProvider = PasswordConfigProvider(dataFolder)
    val dungeonConfigProvider = DungeonConfigProvider(dataFolder, playerConfigProvider)
    val ticketConfigProvider = TicketConfigProvider(dataFolder, playerConfigProvider)

    fun load() {
        gachaConfigProvider.load()
        keyConfigProvider.load()
        passwordConfigProvider.load()
        dungeonConfigProvider.load()
        ticketConfigProvider.load()
    }
}