package jp.kentan.minecraft.nekoevent.listener

import jp.kentan.minecraft.nekoevent.NekoEvent
import jp.kentan.minecraft.nekoevent.manager.SpawnManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.attribute.Attribute
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
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class BukkitEventListener(
        private val plugin: Plugin,
        private val spawn: SpawnManager
) : Listener {

    private val scheduler = Bukkit.getScheduler()

    private val signListenerMap = mutableMapOf<String, SignListener>()

    fun registerSignListener(key: String, listener: SignListener) {
        signListenerMap["[$key]"] = listener
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onSignChanged(event: SignChangeEvent) {
        if (!event.player.isOp) {
            return
        }

        signListenerMap[event.getLine(0)]?.onSignChanged(event)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val blockState = event.clickedBlock.state

        if (blockState is Sign) {
            signListenerMap[blockState.getLine(0)]?.onPlayerInteract(event, blockState)
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
            val maxHealth = event.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
            maxHealth.baseValue = maxHealth.defaultValue
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.from.world.isEventWorld() && !event.to.world.isEventWorld()) {
            spawn.removeBedSpawnIfNeed(event.player)
        }
    }

    private fun Player.isInEventWorld() = (player.gameMode == GameMode.ADVENTURE) && world.isEventWorld()

    private fun World.isEventWorld() = name == NekoEvent.WORLD_NAME

    private fun Player.removeVanishingItems() {
        val inventory = player.inventory

        for (i in 0 until inventory.size) {
            val item = inventory.getItem(i) ?: continue

            if (item.getEnchantmentLevel(Enchantment.VANISHING_CURSE) > 0) {
                inventory.setItem(i, null)
            }
        }
    }
}