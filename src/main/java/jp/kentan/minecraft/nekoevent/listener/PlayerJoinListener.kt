package jp.kentan.minecraft.nekoevent.listener

import org.bukkit.entity.Player

interface PlayerJoinListener {
    fun onPlayerJoin(player: Player)
}