package jp.kentan.minecraft.nekoevent.manager

import jp.kentan.minecraft.nekoevent.NekoEvent
import jp.kentan.minecraft.nekoevent.component.ParkourFlag
import jp.kentan.minecraft.nekoevent.component.ParkourFlag.*
import jp.kentan.minecraft.nekoevent.component.TicketType
import jp.kentan.minecraft.nekoevent.component.model.Parkour
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.config.provider.ParkourConfigProvider
import jp.kentan.minecraft.nekoevent.config.provider.SignConfigProvider
import jp.kentan.minecraft.nekoevent.listener.SignListener
import jp.kentan.minecraft.nekoevent.manager.ParkourManager.SignAction.*
import jp.kentan.minecraft.nekoevent.util.*
import org.bukkit.ChatColor
import org.bukkit.block.Sign
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerInteractEvent

class ParkourManager(
        private val parkourConfig: ParkourConfigProvider,
        private val signConfig: SignConfigProvider,
        private val spawnManager: SpawnManager,
        private val ticketManager: TicketManager
) : ConfigUpdateListener<Parkour>, SignListener {

    companion object {
        private val EVENT_TICKET_ONCE_A_DAY = "${NekoEvent.PREFIX}&7各アスレの&6&lイベントチケット&7報酬は,&c1日1回&7です.".formatColorCode()

        private val SIGN_INDEX = "&8&l[&a&lアスレ&8&l]".formatColorCode()
        val SIGN_KEY = Pair("[parkour]", SIGN_INDEX)

        private const val ID_METADATA_KEY = "parkourId"
        private const val ACTION_METADATA_KEY = "parkourAction"
    }

    private enum class SignAction { JOIN, CLEAR, BACK }

    private val parkourMap = mutableMapOf<String, Parkour>()

    init {
        parkourConfig.listener = this
    }

    fun getParkourIdList() = parkourMap.values.map { it.id }.sorted()

    fun join(strPlayer: String, parkourId: String) {
        val player = strPlayer.toPlayerOrError() ?: return
        val parkour = parkourMap.getOrError(parkourId) ?: return

        join(player, parkour)
    }

    private fun join(player: Player, parkour: Parkour) {
        parkour.joinLocation?.let {
            player.teleport(it)
        }

        parkour.formatJoinMessage?.let {
            player.sendMessage(NekoEvent.PREFIX + it)
        }

        parkour.formatJoinBroadcastMessage?.let { message ->
            player.broadcastMessageWithoutMe(
                    NekoEvent.PREFIX + message
                            .replace("{player}", player.name)
                            .replace("{username}", player.displayName))
        }

        parkour.joinLocation?.let {
            spawnManager.setSpawn(player, it)
        }

        Log.info(player.name + "がパルクール(${parkour.id})に参加しました.")
    }

    fun clear(strPlayer: String, parkourId: String) {
        val player = strPlayer.toPlayerOrError() ?: return
        val parkour = parkourMap.getOrError(parkourId) ?: return

        clear(player, parkour)
    }

    private fun clear(player: Player, parkour: Parkour) {
        parkour.clearLocation?.let {
            player.teleport(it)
            spawnManager.setSpawn(player, it, false)
        }

        parkour.formatClearMessage?.let {
            player.sendMessage(NekoEvent.PREFIX + it)
        }

        parkour.formatClearBroadcastMessage?.let { message ->
            player.broadcastMessageWithoutMe(
                    NekoEvent.PREFIX + message
                            .replace("{player}", player.name)
                            .replace("{username}", player.displayName))
        }

        if (parkour.hasRewardTicket) {
            if (parkourConfig.hasClearedToday(player, parkour)) {
                player.sendMessage(EVENT_TICKET_ONCE_A_DAY)
            } else {
                ticketManager.give(player, TicketType.EVENT, parkour.rewardTicketAmount, false)
            }
        }

        parkourConfig.saveClearDate(player, parkour)

        Log.info(player.name + "がパルクール(${parkour.id})をクリアしました.")
    }

    fun back(strPlayer: String, parkourId: String) {
        val player = strPlayer.toPlayerOrError() ?: return
        val parkour = parkourMap.getOrError(parkourId) ?: return

        back(player, parkour)
    }

    private fun back(player: Player, parkour: Parkour) {
        parkour.backLocation?.let {
            player.teleport(it)
            spawnManager.setSpawn(player, it, false)
        }

        parkour.formatBackMessage?.let {
            player.sendMessage(NekoEvent.PREFIX + it)
        }

        Log.info(player.name + "がパルクール(${parkour.id})から退出しました.")
    }

    fun create(sender: CommandSender, parkourId: String) {
        if (parkourMap.containsKey(parkourId)) {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${parkourId}は既に使用されています.")
            return
        }

        val parkour = Parkour(parkourId)

        if (parkourConfig.update(parkour)) {
            parkourMap[parkourId] = parkour
            sender.sendMessage(NekoEvent.PREFIX + parkour.id + ChatColor.GREEN + "を登録しました.")
        } else {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "保存に失敗しました.")
        }
    }

    fun delete(sender: CommandSender, parkourId: String) {
        if (!parkourMap.containsKey(parkourId)) {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${parkourId}は存在しません.")
            return
        }

        if (parkourConfig.delete(parkourId)) {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.GREEN + "${parkourId}を消去しました.")
        } else {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "消去に失敗しました.")
        }
    }

    fun flag(player: Player, parkourId: String, flagId: String, flagArgs: List<String>) {
        var parkour = parkourMap.getOrError(parkourId) ?: return

        val flagType = ParkourFlag.find(flagId) ?: let {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${flagId}は正しいフラグIDではありません.")
            return
        }

        try {
            parkour = when (flagType) {
                REWARD_TICKET            -> parkour.copy(rewardTicketAmount = flagArgs.flagInt(0))
                JOIN_LOCATION            -> parkour.copy(joinLocation = flagArgs.flagLocation(player, null))
                JOIN_MESSAGE             -> parkour.copy(joinMessage = flagArgs.flagString(null))
                JOIN_BROADCAST_MESSAGE   -> parkour.copy(joinBroadcastMessage = flagArgs.flagString(null))
                CLEAR_LOCATION           -> parkour.copy(clearLocation = flagArgs.flagLocation(player, null))
                CLEAR_MESSAGE            -> parkour.copy(clearMessage = flagArgs.flagString(null))
                CLEAR_BROADCAST_MESSAGE  -> parkour.copy(clearBroadcastMessage = flagArgs.flagString(null))
                BACK_LOCATION            -> parkour.copy(backLocation = flagArgs.flagLocation(player, null))
                BACK_MESSAGE             -> parkour.copy(backMessage = flagArgs.flagString(null))
            }
        } catch (e: InvalidFlagException) {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + e.message)
            return
        }

        if (parkourConfig.update(parkour)) {
            parkourMap[parkourId] = parkour

            player.sendMessage(NekoEvent.PREFIX + ChatColor.GREEN + "$parkourId を更新しました.")
        } else {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "更新に失敗しました.")
        }
    }

    fun sendList(sender: CommandSender) {
        val sb = StringBuilder("&7--------- &4パルクール一覧 &7---------&r".formatColorCode())
        sb.append('\n')

        parkourMap.toSortedMap().forEach { id, password ->
            sb.append(' ')
            sb.append(id)
            sb.append(": ")
            sb.append(password.formatName)
            sb.append('\n')
            sb.append(ChatColor.RESET)
        }

        sender.sendMessage(sb.toString())
    }

    fun sendInfo(sender: CommandSender, parkourId: String) {
        val parkour = parkourMap.getOrError(parkourId) ?: return

        val messages = arrayOf(
                "&7--------- &4パルクール情報 &7---------".formatColorCode(),
                " ID: ${parkour.id}",
                " 名前: ${parkour.formatName}",
                " 報酬チケット: ${parkour.rewardTicketAmount}",
                " 参加TP位置: ${parkour.joinLocation?.formatString()}",
                " 参加ﾒｯｾｰｼﾞ: ${parkour.formatJoinMessage}",
                " 参加放送ﾒｯｾｰｼﾞ: ${parkour.formatJoinBroadcastMessage}",
                " クリアTP位置: ${parkour.clearLocation?.formatString()}",
                " クリアﾒｯｾｰｼﾞ: ${parkour.formatClearMessage}",
                " クリア放送ﾒｯｾｰｼﾞ: ${parkour.formatClearBroadcastMessage}",
                " バックﾒｯｾｰｼﾞ: ${parkour.formatBackMessage}"
        )

        sender.sendMessage(messages)
    }

    fun reload() {
        parkourConfig.load()
    }

    override fun onConfigUpdate(dataMap: Map<String, Parkour>) {
        parkourMap.clear()
        parkourMap.putAll(dataMap)

        Log.info("${parkourMap.size}個のパルクールを読み込みました.")
    }

    /**
     * 看板フォーマット
     * 0: [parkour]
     * 1: parkourId
     * 2: [SignAction]
     * 3:
     */
    override fun onSignChanged(event: SignChangeEvent) {
        val player = event.player
        val parkourId = event.getLine(1)
        val strAction = event.getLine(2).toUpperCase()

        val parkour = parkourMap[parkourId] ?: let {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "パルクール($parkourId)は存在しません.")
            return
        }

        val action: SignAction
        try {
            action = SignAction.valueOf(strAction)
        } catch (e: Exception) {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "正しいSignActionを指定してください.")
            return
        }

        val signMetadataMap = LinkedHashMap<String, Any>().apply {
            put(ID_METADATA_KEY, parkourId)
            put(ACTION_METADATA_KEY, action.name)
        }

        val sign = event.block.state as Sign

        if (!signConfig.save(sign.location, signMetadataMap)) {
            Log.error("看板ﾒﾀﾃﾞｰﾀの保存に失敗しました.")
            return
        }

        event.setLine(0, SIGN_INDEX)
        event.setLine(1, parkour.formatName)
        event.setLine(2, "")

        when (action) {
            JOIN  -> event.setLine(3, "&c&n参加".formatColorCode())
            CLEAR -> event.setLine(3, "&b&nクリア".formatColorCode())
            BACK  -> event.setLine(3, "&1&n戻る".formatColorCode())
        }
    }

    override fun onPlayerInteract(event: PlayerInteractEvent, sign: Sign) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }

        val parkourId = signConfig.getMetadata(sign.location, ID_METADATA_KEY) as String? ?: return
        val parkour = parkourMap.getOrError(parkourId) ?: return

        val strAction = signConfig.getMetadata(sign.location, ACTION_METADATA_KEY) as String? ?: return

        when (SignAction.valueOf(strAction)) {
            JOIN  -> join(event.player, parkour)
            CLEAR -> clear(event.player, parkour)
            BACK  -> back(event.player, parkour)
        }
    }

    private fun MutableMap<String, Parkour>.getOrError(id: String): Parkour? {
        return parkourMap[id] ?: let {
            Log.error("パルクール($id)は存在しません.")
            return@let null
        }
    }
}