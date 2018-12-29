package jp.kentan.minecraft.nekoevent

import jp.kentan.minecraft.nekoevent.command.*
import jp.kentan.minecraft.nekoevent.config.ConfigManager
import jp.kentan.minecraft.nekoevent.listener.BukkitEventListener
import jp.kentan.minecraft.nekoevent.manager.*
import jp.kentan.minecraft.nekoevent.util.Log
import org.bukkit.ChatColor
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.java.JavaPlugin

class NekoEvent : JavaPlugin() {

    companion object {
        val PREFIX: String = ChatColor.translateAlternateColorCodes('&', "&7[&6Neko&eEvent&7]&r ")
        const val WORLD_NAME = "EventWorld"
    }

    override fun onEnable() {
        Log.logger = logger

        val configManager = ConfigManager(dataFolder)

        val ticketManager = TicketManager(configManager.ticketConfigProvider)
        val spawnManager = SpawnManager(configManager.signConfigProvider)
        val reviveManager = ReviveManager(configManager.signConfigProvider)

        val keyManager = KeyManager(configManager.keyConfigProvider)
        val gachaManager = GachaManager(
                ticketManager,
                keyManager,
                configManager.gachaConfigProvider,
                configManager.signConfigProvider)
        val passwordManager = PasswordManager(configManager.passwordConfigProvider)
        val parkourManager = ParkourManager(
                configManager.parkourConfigProvider,
                configManager.signConfigProvider,
                spawnManager,
                ticketManager
        )
        val dungeonManager = DungeonManager(
                this,
                configManager.dungeonConfigProvider,
                configManager.signConfigProvider,
                spawnManager,
                gachaManager,
                ticketManager
        )

        getCommand("event").set(EventCommand(this, configManager, ticketManager, spawnManager))
        getCommand("gacha").set(GachaCommand(gachaManager))
        getCommand("key").set(KeyCommand(keyManager))
        getCommand("password").set(PasswordCommand(passwordManager))
        getCommand("parkour").set(ParkourCommand(parkourManager))
        getCommand("dungeon").set(DungeonCommand(dungeonManager))


        Log.info("有効化しました.")

        configManager.load()

        val bukkitEventListener = BukkitEventListener(this, spawnManager)
        bukkitEventListener.registerSignListener(GachaManager.SIGN_KEY, gachaManager)
        bukkitEventListener.registerSignListener(SpawnManager.SIGN_KEY, spawnManager)
        bukkitEventListener.registerSignListener(ReviveManager.SIGN_KEY, reviveManager)
        bukkitEventListener.registerSignListener(ParkourManager.SIGN_KEY, parkourManager)
        bukkitEventListener.registerSignListener(DungeonManager.SIGN_KEY, dungeonManager)

        server.pluginManager.registerEvents(bukkitEventListener, this)
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