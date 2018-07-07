package jp.kentan.minecraft.nekoevent.manager

import jp.kentan.minecraft.nekoevent.NekoEvent
import jp.kentan.minecraft.nekoevent.component.DungeonFlag
import jp.kentan.minecraft.nekoevent.component.DungeonFlag.*
import jp.kentan.minecraft.nekoevent.component.TicketType
import jp.kentan.minecraft.nekoevent.component.model.Dungeon
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.config.provider.DungeonConfigProvider
import jp.kentan.minecraft.nekoevent.config.provider.SignConfigProvider
import jp.kentan.minecraft.nekoevent.listener.SignListener
import jp.kentan.minecraft.nekoevent.util.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.block.Sign
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin

class DungeonManager(
        private val plugin: Plugin,
        private val dungeonConfig: DungeonConfigProvider,
        private val signConfig: SignConfigProvider,
        private val spawnManager: SpawnManager,
        private val gachaManager: GachaManager,
        private val ticketManager: TicketManager
) : ConfigUpdateListener<Dungeon>, SignListener {

    companion object {
        private val EVENT_TICKET_ONCE_A_DAY = "${NekoEvent.PREFIX}&7各ダンジョンの&6&lイベントチケット&7報酬は,&c1日1回&7です.".formatColorCode()

        private val SIGN_INDEX = "&8&l[&4&lダンジョン&8&l]".formatColorCode()
        val SIGN_KEY = Pair("[dungeon]", SIGN_INDEX)

        private const val ID_METADATA_KEY = "dungeonId"
        private const val ACTION_METADATA_KEY = "dungeonAction"
    }

    private val scheduler =  Bukkit.getScheduler()

    private val dungeonMap = mutableMapOf<String, Dungeon>()

    init {
        dungeonConfig.listener = this
    }

    fun getDungeonIdList() = dungeonMap.values.map { it.id }.sorted()

    fun join(strPlayer: String, dungeonId: String) {
        val player = strPlayer.toPlayerOrError() ?: return
        val dungeon = dungeonMap[dungeonId] ?: let {
            Log.error("ダンジョン($dungeonId)は存在しません.")
            return
        }

        join(player, dungeon)
    }

    private fun join(player: Player, dungeon: Dungeon) {
        if (dungeon.isLock) {
            dungeon.formatLockMessage?.let {
                player.sendMessage(NekoEvent.PREFIX + it)
            }
            return
        }

        dungeon.joinLocation?.let {
            player.teleport(it)
        }

        dungeon.formatJoinMessage?.let {
            player.sendMessage(NekoEvent.PREFIX + it)
        }

        dungeon.formatJoinBroadcastMessage?.let { message ->
            player.broadcastMessageWithoutMe(
                    NekoEvent.PREFIX + message
                            .replace("{player}", player.name)
                            .replace("{username}", player.displayName))
        }

        dungeon.joinLocation?.let {
            spawnManager.setSpawn(player, it)
        }

        Log.info(player.name + "がダンジョン(${dungeon.id})に参加しました.")
    }

    fun clear(strPlayer: String, dungeonId: String) {
        val player = strPlayer.toPlayerOrError() ?: return
        val dungeon = dungeonMap[dungeonId] ?: let {
            Log.error("ダンジョン($dungeonId)は存在しません.")
            return
        }

        clear(player, dungeon)
    }

    private fun clear(player: Player, dungeon: Dungeon) {
        dungeon.clearLocation?.let {
            player.teleport(it)
            spawnManager.setSpawn(player, it, false)
        }

        dungeon.formatClearMessage?.let {
            player.sendMessage(NekoEvent.PREFIX + it)
        }

        dungeon.formatClearBroadcastMessage?.let { message ->
            player.broadcastMessageWithoutMe(
                    NekoEvent.PREFIX + message
                            .replace("{player}", player.name)
                            .replace("{username}", player.displayName))
        }

        if (dungeon.enabledClearSound) {
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.2f, 0.0f)
        }

        if (dungeon.hasRewardTicket) {
            if (dungeonConfig.hasClearedToday(player, dungeon)) {
                player.sendMessage(EVENT_TICKET_ONCE_A_DAY)
            } else {
                ticketManager.give(player, TicketType.EVENT, dungeon.rewardTicketAmount, false)
            }
        }

        dungeon.rewardGachaId?.let {
            scheduler.scheduleSyncDelayedTask(plugin, { gachaManager.play(player, it) }, 200L)
        }

        dungeonConfig.saveClearDate(player, dungeon)

        Log.info(player.name + "がダンジョン(${dungeon.id})をクリアしました.")
    }

    fun lock(dungeonId: String, strSeconds: String) {
        val dungeon = dungeonMap[dungeonId] ?: let {
            Log.error("ダンジョン($dungeonId)は存在しません.")
            return
        }
        val seconds = strSeconds.toIntOrError() ?: return

        lock(dungeon, seconds)
    }

    private fun lock(dungeon: Dungeon, seconds: Int) {
        if (seconds < 1) {
            Log.error("ロックタイマーは1秒以上で指定してください.")
            return
        }

        dungeon.startLockTimer(scheduler, plugin, seconds)
    }

    fun unlock(dungeonId: String) {
        val dungeon = dungeonMap[dungeonId] ?: let {
            Log.error("ダンジョン($dungeonId)は存在しません.")
            return
        }

        dungeon.stopLockTimer()
    }

    fun create(sender: CommandSender, dungeonId: String) {
        if (dungeonMap.containsKey(dungeonId)) {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${dungeonId}は既に使用されています.")
            return
        }

        val dungeon = Dungeon(dungeonId)

        if (dungeonConfig.update(dungeon)) {
            dungeonMap[dungeonId] = dungeon
            sender.sendMessage(NekoEvent.PREFIX + dungeon.id + ChatColor.GREEN + "を登録しました.")
        } else {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "保存に失敗しました.")
        }
    }

    fun delete(sender: CommandSender, dungeonId: String) {
        if (!dungeonMap.containsKey(dungeonId)) {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${dungeonId}は存在しません.")
            return
        }

        if (dungeonConfig.delete(dungeonId)) {
            dungeonMap.remove(dungeonId)?.stopLockTimer()
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.GREEN + "${dungeonId}を消去しました.")
        } else {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "消去に失敗しました.")
        }
    }

    fun flag(player: Player, dungeonId: String, flagId: String, flagArgs: List<String>) {
        var dungeon = dungeonMap[dungeonId] ?: let {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${dungeonId}は存在しません.")
            return
        }

        val flagType = DungeonFlag.find(flagId) ?: let {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${flagId}は正しいフラグIDではありません.")
            return
        }

        try {
            dungeon = when (flagType) {
                REWARD_TICKET            -> dungeon.copy(rewardTicketAmount = flagArgs.flagInt(0))
                REWARD_GACHA             -> dungeon.copy(rewardGachaId = flagArgs.flagString(null))
                JOIN_LOCATION            -> dungeon.copy(joinLocation = flagArgs.flagLocation(player, null))
                JOIN_MESSAGE             -> dungeon.copy(joinMessage = flagArgs.flagString(null))
                JOIN_BROADCAST_MESSAGE   -> dungeon.copy(joinBroadcastMessage = flagArgs.flagString(null))
                CLEAR_LOCATION           -> dungeon.copy(clearLocation = flagArgs.flagLocation(player, null))
                CLEAR_MESSAGE            -> dungeon.copy(clearMessage = flagArgs.flagString(null))
                CLEAR_BROADCAST_MESSAGE  -> dungeon.copy(clearBroadcastMessage = flagArgs.flagString(null))
                CLEAR_SOUND              -> dungeon.copy(enabledClearSound = flagArgs.flagBoolean(true))
                LOCK_MESSAGE             -> dungeon.copy(lockMessage = flagArgs.flagString(null))
                LOCK_BROADCAST_MESSAGE   -> dungeon.copy(lockBroadcastMessage = flagArgs.flagString(null))
                UNLOCK_BROADCAST_MESSAGE -> dungeon.copy(unlockBroadcastMessage = flagArgs.flagString(null))
            }
        } catch (e: InvalidFlagException) {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + e.message)
            return
        }

        if (dungeonConfig.update(dungeon)) {
            dungeonMap[dungeonId] = dungeon

            // ロックタイマー復元
            dungeon.restoreLockTimerIfNeed(scheduler, plugin)

            player.sendMessage(NekoEvent.PREFIX + ChatColor.GREEN + "$dungeonId を更新しました.")
        } else {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "更新に失敗しました.")
        }
    }

    fun sendList(sender: CommandSender) {
        val sb = StringBuilder("&7--------- &4ダンジョン一覧 &7---------&r".formatColorCode())
        sb.append('\n')

        dungeonMap.toSortedMap().forEach { id, password ->
            sb.append(' ')
            sb.append(id)
            sb.append(": ")
            sb.append(password.formatName)
            sb.append('\n')
            sb.append(ChatColor.RESET)
        }

        sender.sendMessage(sb.toString())
    }

    fun sendInfo(sender: CommandSender, dungeonId: String) {
        val dungeon = dungeonMap[dungeonId] ?: let {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "ダンジョン($dungeonId)は存在しません.")
            return
        }

        val messages = arrayOf(
                "&7--------- &4ダンジョン情報 &7---------".formatColorCode(),
                " ID: ${dungeon.id}",
                " 名前: ${dungeon.formatName}",
                " 報酬チケット: ${dungeon.rewardTicketAmount}",
                " 報酬ガチャ: ${dungeon.rewardGachaId}",
                " 参加TP位置: ${dungeon.joinLocation?.formatString()}",
                " 参加ﾒｯｾｰｼﾞ: ${dungeon.formatJoinMessage}",
                " 参加放送ﾒｯｾｰｼﾞ: ${dungeon.formatJoinBroadcastMessage}",
                " クリアTP位置: ${dungeon.clearLocation?.formatString()}",
                " クリアﾒｯｾｰｼﾞ: ${dungeon.formatClearMessage}",
                " クリア放送ﾒｯｾｰｼﾞ: ${dungeon.formatClearBroadcastMessage}",
                " クリアサウンド: ${dungeon.enabledClearSound}",
                " ロックﾒｯｾｰｼﾞ: ${dungeon.formatLockMessage}",
                " ロック放送ﾒｯｾｰｼﾞ: ${dungeon.formatLockBroadcastMessage}",
                " アンロック放送ﾒｯｾｰｼﾞ: ${dungeon.formatUnlockBroadcastMessage}"
        )

        sender.sendMessage(messages)
    }

    fun reload() {
        dungeonConfig.load()
    }

    override fun onConfigUpdate(dataMap: Map<String, Dungeon>) {
        dungeonMap.clear()
        dungeonMap.putAll(dataMap)

        Log.info("${dungeonMap.size}個のダンジョンを読み込みました.")
    }

    /**
     * 看板フォーマット
     * 0: [dungeon]
     * 1: dungeonId
     * 2: join / clear
     * 3:
     */
    override fun onSignChanged(event: SignChangeEvent) {
        val player = event.player
        val dungeonId = event.getLine(1)
        val action = event.getLine(2)

        val dungeon = dungeonMap[dungeonId] ?: let {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "ダンジョン($dungeonId)は存在しません.")
            return
        }

        if (action != "join" && action != "clear") {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "アクションは[join/clear]で指定してください.")
            return
        }

        val signMetadataMap = LinkedHashMap<String, Any>().apply {
            put(ID_METADATA_KEY, dungeonId)
            put(ACTION_METADATA_KEY, action)
        }

        val sign = event.block.state as Sign

        if (!signConfig.save(sign.location, signMetadataMap)) {
            Log.error("看板ﾒﾀﾃﾞｰﾀの保存に失敗しました.")
            return
        }

        event.setLine(0, SIGN_INDEX)
        event.setLine(1, dungeon.formatName)
        event.setLine(2, "")
        event.setLine(3, (if (action == "join") "&c&n参加" else "&b&nクリア").formatColorCode())
    }

    override fun onPlayerInteract(event: PlayerInteractEvent, sign: Sign) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }

        val dungeonId = signConfig.getMetadata(sign.location, ID_METADATA_KEY) as String? ?: return
        val dungeon = dungeonMap[dungeonId] ?: let {
            Log.error("ダンジョン($dungeonId)は存在しません.")
            return
        }

        val action = signConfig.getMetadata(sign.location, ACTION_METADATA_KEY) as String? ?: return

        when (action) {
            "join"  -> join(event.player, dungeon)
            "clear" -> clear(event.player, dungeon)
            else -> {}
        }
    }
}