package jp.kentan.minecraft.nekoevent.listener

import jp.kentan.minecraft.nekoevent.NekoEvent
import jp.kentan.minecraft.nekoevent.manager.SpawnManager
import jp.kentan.minecraft.nekoevent.util.resetHealthStatus
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Sign
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class BukkitEventListener(
        private val plugin: Plugin,
        private val spawn: SpawnManager
) : Listener {

    private val scheduler = Bukkit.getScheduler()

    private val signChangedListenerMap = mutableMapOf<String, SignListener>()
    private val signInteractListenerMap = mutableMapOf<String, SignListener>()

    private var playerJoinListener: PlayerJoinListener? = null

    fun registerSignListener(key: Pair<String, String>, listener: SignListener) {
        val (changed, interact) = key

        signChangedListenerMap[changed] = listener
        signInteractListenerMap[interact] = listener
    }

    fun registerPlayerJoinListener(listener: PlayerJoinListener) {
        playerJoinListener = listener
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onSignChanged(event: SignChangeEvent) {
        if (!event.player.hasPermission("neko.event")) {
            return
        }

        signChangedListenerMap[event.getLine(0)]?.onSignChanged(event)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val blockState = event.clickedBlock?.state

        if (blockState is Sign) {
            signInteractListenerMap[blockState.getLine(0)]?.onPlayerInteract(event, blockState)
        }

        if (event.player.isInEventWorld() && (blockState?.type == Material.ANVIL || blockState?.type == Material.ENCHANTING_TABLE || event.player.isCustomRiptiding())) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val player = e.entity

        if (player.isInEventWorld()) {
            scheduler.scheduleSyncDelayedTask(plugin, {
                if (!player.isDead) {
                    return@scheduleSyncDelayedTask
                }

                player.removeVanishingItems()

                player.spigot().respawn()

                player.activePotionEffects.forEach { effect -> player.removePotionEffect(effect.type) }
            }, 10L)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerChangeWorld(event: PlayerChangedWorldEvent) {
        if (event.from.isEventWorld()) {
            event.player.resetHealthStatus()
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.from.world.isEventWorld() && !event.to?.world.isEventWorld()) {
            spawn.removeBedSpawnIfNeed(event.player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        playerJoinListener?.onPlayerJoin(event.player)
    }

    private fun Player.isInEventWorld() = (gameMode == GameMode.ADVENTURE) && world.isEventWorld()

    private fun World?.isEventWorld() = this?.name == NekoEvent.WORLD_NAME

    private fun Player.removeVanishingItems() {
        val inventory = this.inventory

        for (i in 0 until inventory.size) {
            val item = inventory.getItem(i) ?: continue

            if (item.containsEnchantment(Enchantment.VANISHING_CURSE)) {
                inventory.setItem(i, null)
            }
        }
    }

    private fun Player.isCustomRiptiding() = with(inventory.itemInMainHand) {
        type == Material.TRIDENT && enchantments.containsKey(Enchantment.RIPTIDE)
    }
}