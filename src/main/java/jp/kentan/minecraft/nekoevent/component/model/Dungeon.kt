package jp.kentan.minecraft.nekoevent.component.model

import jp.kentan.minecraft.nekoevent.util.Log
import jp.kentan.minecraft.nekoevent.util.formatColorCode
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class Dungeon(
        val id: String,
        val name: String = "ダンジョン",
        val rewardTicketAmount: Int = 0,
        val rewardGachaId: String? = null,
        val joinLocation: Location? = null,
        val joinMessage: String? = "{name}&rに&c参加&rしました！",
        val joinBroadcastMessage: String? = null,
        val clearLocation: Location? = null,
        val clearMessage: String? = "&dクリアおめでとう！&rまた&c挑戦&rしてね！",
        val clearBroadcastMessage: String? = null,
        val enabledClearSound: Boolean = true,
        val lockMessage: String? = "{name}&rは&cロック中&rです！ &b{time}&rお待ちください...",
        val lockBroadcastMessage: String? = null,
        val unlockBroadcastMessage: String? = null,
        private var lockTimerSeconds: AtomicInteger = AtomicInteger(0)
) {

    companion object {
        private val asyncLockTimerTaskMap = ConcurrentHashMap<String, BukkitTask>()
    }

    val hasRewardTicket = rewardTicketAmount > 0

    val formatName = name.formatColorCode()
    val formatJoinMessage = joinMessage.format()
    val formatJoinBroadcastMessage = joinBroadcastMessage.format()
    val formatClearMessage = clearMessage.format()
    val formatClearBroadcastMessage = clearBroadcastMessage.format()
    private val _formatLockTimerMessage = lockMessage.format()
    val formatLockMessage: String?
        get() {
            val sec = lockTimerSeconds.get()
            val timer = if (sec >= 60) { "${sec / 60}分" } else { "${lockTimerSeconds}秒" }
            return _formatLockTimerMessage?.replace("{time}", timer)
        }
    val formatLockBroadcastMessage = lockBroadcastMessage.format()
    val formatUnlockBroadcastMessage = unlockBroadcastMessage.format()

    val isLock: Boolean
        get() = lockTimerSeconds.get() > 0


    fun startLockTimer(scheduler: BukkitScheduler, plugin: Plugin, seconds: Int) {
        lockTimerSeconds.set(seconds)

        scheduler.scheduleAsyncLockTimerTask(plugin)

        formatLockBroadcastMessage?.let {
            Bukkit.broadcastMessage(it)
        }

        Log.info("ダンジョン($id)にﾛｯｸﾀｲﾏｰ(${seconds}s)を設定しました.")
    }

    fun stopLockTimer() {
        asyncLockTimerTaskMap.remove(id)?.let { it ->
            it.cancel()
            formatUnlockBroadcastMessage?.run(Bukkit::broadcastMessage)
        }

        lockTimerSeconds.set(0)
    }

    fun restoreLockTimerIfNeed(scheduler: BukkitScheduler, plugin: Plugin) {
        if (lockTimerSeconds.get() < 1) {
            return
        }

        scheduler.scheduleAsyncLockTimerTask(plugin)
    }

    private fun BukkitScheduler.scheduleAsyncLockTimerTask(plugin: Plugin) {
        val asyncTask = runTaskTimerAsynchronously(plugin, Runnable {
            if (lockTimerSeconds.decrementAndGet() < 1){
                asyncLockTimerTaskMap.remove(id)?.cancel()

                formatUnlockBroadcastMessage?.let {
                    Bukkit.broadcastMessage(it)
                }

                Log.info("ダンジョン($id)のﾛｯｸﾀｲﾏｰが終了しました.")
            }
        }, 20L, 20L)

        asyncLockTimerTaskMap.remove(id)?.cancel()
        asyncLockTimerTaskMap[id] = asyncTask
    }

    private fun String?.format() = this?.replace("{name}", formatName)?.formatColorCode()
}