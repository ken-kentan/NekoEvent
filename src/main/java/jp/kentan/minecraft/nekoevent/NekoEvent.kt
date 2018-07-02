package jp.kentan.minecraft.nekoevent

import jp.kentan.minecraft.nekoevent.command.BaseCommand
import jp.kentan.minecraft.nekoevent.command.GachaCommand
import jp.kentan.minecraft.nekoevent.command.KeyCommand
import jp.kentan.minecraft.nekoevent.config.ConfigManager
import jp.kentan.minecraft.nekoevent.listener.BukkitEventListener
import jp.kentan.minecraft.nekoevent.manager.GachaManager
import jp.kentan.minecraft.nekoevent.manager.KeyManager
import jp.kentan.minecraft.nekoevent.manager.TicketManager
import jp.kentan.minecraft.nekoevent.util.Log
import org.bukkit.ChatColor
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.java.JavaPlugin

class NekoEvent : JavaPlugin() {

    companion object {
        val PREFIX: String = ChatColor.translateAlternateColorCodes('&', "&7[&6Neko&eEvent&7]&r ")
    }

    override fun onEnable() {
        Log.logger = logger

        val configManager = ConfigManager(dataFolder)

        val ticketManager = TicketManager()
        val keyManager = KeyManager(configManager.keyConfigProvider)
        val gachaManager = GachaManager(
                ticketManager,
                keyManager,
                configManager.gachaConfigProvider,
                configManager.signConfigProvider)

        getCommand("gacha").set(GachaCommand(gachaManager))
        getCommand("key").set(KeyCommand(keyManager))


        Log.info("有効化しました.")

        configManager.load()

        server.pluginManager.registerEvents(BukkitEventListener(), this)
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)

        Log.info("無効化しました.")
    }

    private fun PluginCommand.set(command: BaseCommand) {
        executor = command
        tabCompleter = command
    }
}