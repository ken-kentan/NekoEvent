package jp.kentan.minecraft.nekoevent

import jp.kentan.minecraft.nekoevent.command.BaseCommand
import jp.kentan.minecraft.nekoevent.command.GachaCommand
import jp.kentan.minecraft.nekoevent.config.ConfigManager
import jp.kentan.minecraft.nekoevent.listener.BukkitEventListener
import jp.kentan.minecraft.nekoevent.listener.GachaSignListener
import jp.kentan.minecraft.nekoevent.manager.GachaManager
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

        val gachaManager = GachaManager(configManager.gachaConfigProvider)

        getCommand("gacha").set(GachaCommand(gachaManager))


        // Event
        val bukkitEventListener = BukkitEventListener()

        val gachaSignListener = GachaSignListener(gachaManager)

        bukkitEventListener.registerSignListener("gacha", gachaSignListener)

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