package jp.kentan.minecraft.nekoevent.manager

import jp.kentan.minecraft.nekoevent.component.model.Bonus
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.config.provider.BonusConfigProvider
import jp.kentan.minecraft.nekoevent.listener.PlayerJoinListener
import jp.kentan.minecraft.nekoevent.util.Log
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class BonusManager(
        private val plugin: Plugin,
        private val bonusConfigProvider: BonusConfigProvider
) : ConfigUpdateListener<Bonus>, PlayerJoinListener {

    private companion object {
        const val DELAY_TICK = 100L // 5sec
    }

    private val bonusList: MutableList<Bonus> = mutableListOf()

    init {
        bonusConfigProvider.listener = this
    }

    override fun onPlayerJoin(player: Player) {
        if (bonusList.isEmpty()) {
            return
        }

        val now = Date()

        Bukkit.getScheduler().runTaskLater(plugin, {
            if (!player.isOnline) {
                return@runTaskLater
            }

            bonusList
                    .filter {
                        it.inRange(now) && bonusConfigProvider.isNotReceived(player, it.id)
                    }
                    .forEach {
                        bonusConfigProvider.setReceived(player, it.id)

                        Bukkit.getServer().dispatchCommand(
                                Bukkit.getConsoleSender(),
                                it.command.replace("{player}", player.name)
                        )

                        it.message?.run(player::sendMessage)

                        Log.info("${player.name}にボーナス(${it.id})を実行しました.")
                    }
        }, DELAY_TICK)
    }

    override fun onConfigUpdate(dataMap: Map<String, Bonus>) {
        bonusList.apply {
            clear()
            addAll(dataMap.values)
        }

        Log.info("${dataMap.size}個のボーナスを読み込みました.")
    }
}