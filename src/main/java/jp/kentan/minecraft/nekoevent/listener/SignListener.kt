package jp.kentan.minecraft.nekoevent.listener

import org.bukkit.block.Sign
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent

interface SignListener {
    fun onSignChanged(event: SignChangeEvent)
    fun onPlayerInteract(event: PlayerInteractEvent, sign: Sign)
}