package jp.kentan.minecraft.nekoevent.listener

import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent

class BukkitEventListener : Listener {

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
            val index = blockState.getLine(0)

//            if (index == SpawnManager.SETSPAWN_SIGN_INDEX) {
//                sSpawnSignListener.onPlayerInteract(event, blockState)
//            } else if (index == GachaManager.GACHA_SIGN_INDEX) {
//                sGachaSignListener.onPlayerInteract(event, blockState)
//            } else if (index == ParkourManager.PARKOUR_SIGN_INDEX) {
//                sParkourSignListener.onPlayerInteract(event, blockState)
//            } else if (index == DungeonManager.DUNGEON_SIGN_INDEX) {
//                sDungeonSignListener.onPlayerInteract(event, blockState)
//            }
        }
    }
}