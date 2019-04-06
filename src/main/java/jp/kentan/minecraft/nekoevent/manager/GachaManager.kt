package jp.kentan.minecraft.nekoevent.manager

import jp.kentan.minecraft.nekoevent.NekoEvent
import jp.kentan.minecraft.nekoevent.component.GachaCost
import jp.kentan.minecraft.nekoevent.component.GachaCost.*
import jp.kentan.minecraft.nekoevent.component.TicketType
import jp.kentan.minecraft.nekoevent.component.model.Gacha
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.config.provider.GachaConfigProvider
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

class GachaManager(
        private val ticketManager: TicketManager,
        private val keyManager: KeyManager,
        private val gachaConfig: GachaConfigProvider,
        private val signConfig: SignConfigProvider
) : ConfigUpdateListener<Gacha>, SignListener {

    companion object {
        private val SIGN_INDEX = "&8&l[&d&lガチャ&8&l]".formatColorCode()
        val SIGN_KEY = Pair("[gacha]", SIGN_INDEX)

        private const val ID_METADATA_KEY = "gachaId"
        private const val COST_METADATA_KEY = "gachaCost"
        private const val COST_DETAIL_METADATA_KEY = "gachaCostDetail"
    }

    private val gachaMap = mutableMapOf<String, Gacha>()

    init {
        gachaConfig.listener = this
    }

    fun getGachaIdList() = gachaMap.values.map { it.id }.sorted()

    fun play(strPlayer: String, gachaId: String) {
        val player = strPlayer.toPlayerOrError() ?: return

        play(player, gachaId)
    }

    fun play(player: Player, gachaId: String) {
        val gacha = gachaMap.getOrError(gachaId) ?: return

        play(player, gacha)
    }

    private fun playWithTicket(player: Player, gachaId: String, ticket: TicketType, amount: Int) {
        if (ticketManager.take(player, ticket, amount)) {
            play(player, gachaId)
        }
    }

    private fun playWithKey(player: Player, gachaId: String, keyId: String) {
        if (keyManager.use(player, keyId)) {
            play(player, gachaId)
        }
    }

    private fun play(player: Player, gacha: Gacha) {
        val component = gacha.getByRandom()

        val soundEffect: Sound

        if (component.isWin()) {
            component.commandList.forEach { Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), it.replace("{player}", player.name)) }

            gacha.winMessage?.let {
                player.sendMessage(NekoEvent.PREFIX + it.replace("{name}", component.name))
            }

            gacha.broadcastMessage?.let {
                if (component.isDisabledBroadcast) {
                    return@let
                }

                val message = it
                        .replace("{player}", player.name)
                        .replace("{username}", player.displayName)
                        .replace("{name}", component.name)

                player.broadcastMessageWithoutMe(NekoEvent.PREFIX + message)
            }

            soundEffect = Sound.ENTITY_PLAYER_LEVELUP

            Log.info(player.name + "にｶﾞﾁｬ(" + gacha.id + ")で" + component.name + ChatColor.RESET + "を与えました.")
        } else {
            gacha.loseMessage?.let {
                player.sendMessage(NekoEvent.PREFIX + it)
            }

            soundEffect = Sound.ENTITY_PIG_DEATH
        }

        if (gacha.enabledEffect) {
            player.playSound(player.location, soundEffect, 1.0f, 0.0f)
        }
    }

    fun demo(sender: CommandSender, gachaId: String, strTimes: String) {
        val gacha = gachaMap.getOrError(gachaId) ?: return

        val time = strTimes.toIntOrNull() ?: let {
            Log.error("$strTimes は0以上の整数にしてください.")
            return
        }

        if (time > 1000000) {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "100万回以上は指定出来ません.")
            return
        }

        val resultMap = mutableMapOf<String, Int>()

        for (i in 0 until time) {
            val key = gacha.getByRandom().name
            val count = resultMap[key]

            if (count != null) {
                resultMap.replace(key, count + 1)
            } else {
                resultMap[key] = 1
            }
        }

        sender.sendMessage(NekoEvent.PREFIX + "ｶﾞﾁｬ(" + gachaId + ")を" + time + "回引いた結果")

        resultMap.forEach { name, count -> sender.sendMessage(name + ChatColor.RESET + ": " + count + "回") }
    }

    fun sendList(sender: CommandSender) {
        val sb = StringBuilder("&7--------- &dガチャ一覧 &7---------&r".formatColorCode())
        sb.append('\n')

        gachaMap.toSortedMap().forEach { id, gacha ->
            sb.append(' ')
            sb.append(id)
            sb.append(": ")
            sb.append(gacha.formatName)
            sb.append('\n')
            sb.append(ChatColor.RESET)
        }

        sender.sendMessage(sb.toString())
    }

    fun sendInfo(sender: CommandSender, gachaId: String) {
        val gacha = gachaMap.getOrError(gachaId) ?: return

        val messages = arrayOf(
                "&7--------- &dガチャ情報 &7---------".formatColorCode(),
                " ID: $gachaId",
                " 名前: ${gacha.formatName}",
                " コンポ数: ${gacha.componentList.size}",
                " あたり: ${gacha.winMessage}",
                " はずれ: ${gacha.loseMessage}",
                " ﾌﾞﾛｰﾄﾞｷｬｽﾄ: ${gacha.broadcastMessage}",
                " エフェクト: ${gacha.enabledEffect}")

        sender.sendMessage(messages)
    }

    fun reload() {
        gachaConfig.load()
    }

    override fun onConfigUpdate(dataMap: Map<String, Gacha>) {
        gachaMap.clear()
        gachaMap.putAll(dataMap)

        Log.info("${dataMap.size}個のガチャを読み込みました.")
    }

    /**
     * 看板フォーマット
     * 0: [gacha]
     * 1: gachaId
     * 2: [jp.kentan.minecraft.nekoevent.model.GachaCost].id
     * 3: cost detail (ex. ticket num)
     */
    override fun onSignChanged(event: SignChangeEvent) {
        val gachaId = event.getLine(1)
        val strCost = event.getLine(2)
        val strCostDetail = event.getLine(3)

        // コスト判定
        val cost = GachaCost.find(strCost) ?: let {
            Log.error("ガチャコスト($strCost)は不正な値です.")
            return
        }

        val signMetadataMap = LinkedHashMap<String, Any>().apply {
            put(ID_METADATA_KEY, gachaId)
            put(COST_METADATA_KEY, cost.name)
        }

        val costDetailText: String

        when (cost) {
            EVENT_TICKET, VOTE_TICKET -> {
                val amount = strCostDetail.toIntOrError() ?: return

                costDetailText = "&9&n1プレイ&r &a&n${amount}枚".formatColorCode()

                signMetadataMap[COST_DETAIL_METADATA_KEY] = amount
            }
            KEY -> {
                val key = keyManager.getKey(strCostDetail) ?: let {
                    Log.error("鍵($strCost)は存在しません.")
                    return
                }

                costDetailText = key.name

                signMetadataMap[COST_DETAIL_METADATA_KEY] = key.id
            }
            FREE -> {
                costDetailText = "&c無料".formatColorCode()
            }
        }


        val gacha = gachaMap.getOrError(gachaId) ?: return

        val sign = event.block.state as Sign
        if (!signConfig.save(sign.location, signMetadataMap)) {
            Log.error("看板ﾒﾀﾃﾞｰﾀの保存に失敗しました.")
            return
        }


        event.setLine(0, SIGN_INDEX)
        event.setLine(1, gacha.formatName)
        event.setLine(2, cost.signText)
        event.setLine(3, costDetailText)
    }

    override fun onPlayerInteract(event: PlayerInteractEvent, sign: Sign) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }

        val gachaId = signConfig.getMetadata(sign.location, ID_METADATA_KEY) as String? ?: return

        val strCost = signConfig.getMetadata(sign.location, COST_METADATA_KEY) as String? ?: return
        val cost = GachaCost.valueOf(strCost)
        val costDetail = signConfig.getMetadata(sign.location, COST_DETAIL_METADATA_KEY)?.toString() ?: return

        when (cost) {
            FREE -> play(event.player, gachaId)
            EVENT_TICKET -> playWithTicket(event.player, gachaId, TicketType.EVENT, costDetail.toIntOrNull() ?: 0)
            VOTE_TICKET  -> playWithTicket(event.player, gachaId, TicketType.VOTE, costDetail.toIntOrNull() ?: 0)
            KEY -> playWithKey(event.player, gachaId, costDetail)
        }
    }

    private fun MutableMap<String, Gacha>.getOrError(id: String): Gacha? {
        return gachaMap[id] ?: let {
            Log.error("ガチャ($id)は存在しません.")
            return@let null
        }
    }
}