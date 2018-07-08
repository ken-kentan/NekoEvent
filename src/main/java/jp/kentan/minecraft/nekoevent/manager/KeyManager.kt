package jp.kentan.minecraft.nekoevent.manager

import jp.kentan.minecraft.nekoevent.NekoEvent
import jp.kentan.minecraft.nekoevent.component.KeyFlag
import jp.kentan.minecraft.nekoevent.component.KeyFlag.*
import jp.kentan.minecraft.nekoevent.component.KeyResult
import jp.kentan.minecraft.nekoevent.component.model.Key
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.config.provider.KeyConfigProvider
import jp.kentan.minecraft.nekoevent.util.*
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class KeyManager(
        private val keyConfig: KeyConfigProvider
) : ConfigUpdateListener<Key> {

    private val keyMap = mutableMapOf<String, Key>()

    init {
        keyConfig.listener = this
    }

    fun getKey(keyId: String): Key? = keyMap[keyId]

    fun getKeyIdList() = keyMap.values.map { it.id }.sorted()

    fun give(strPlayer: String, keyId: String, strAmount: String) {
        val player = strPlayer.toPlayerOrError() ?: return
        val amount = strAmount.toIntOrError() ?: return

        give(player, keyId, amount)
    }

    fun give(player: Player, keyId: String, amount: Int) {
        val key = keyMap.getOrError(keyId) ?: return

        give(player, key, amount)
    }

    private fun give(player: Player, key: Key, amount: Int) {
        player.inventory.addItem(key.getItemStack(amount))
    }

    fun drop(keyId: String, strLocation: Array<String>, strAmount: String) {
        val key = keyMap.getOrError(keyId) ?: return

        val location = strLocation.toLocationOrError() ?: return
        val amount = strAmount.toIntOrError() ?: return

        drop(key, location, amount)
    }

    private fun drop(key: Key, location: Location, amount: Int) {
        location.world.dropItemNaturally(location, key.getItemStack(amount))
    }

    fun use(strPlayer: String, keyId: String) {
        val player = strPlayer.toPlayerOrError() ?: return

        use(player, keyId)
    }

    fun use(player: Player, keyId: String): Boolean {
        val key = keyMap.getOrError(keyId) ?: return false

        return use(player, key)
    }

    private fun use(player: Player, key: Key): Boolean {
        val handItem = player.inventory.itemInMainHand

        when (key.compareTo(handItem)) {
            KeyResult.MATCH -> {
                if (key.enabledTake) {
                    val amount = handItem.amount - key.amount
                    player.inventory.itemInMainHand.amount = amount
                }

                key.block?.let { (material, location) ->
                    location.block.type = material
                }

                key.formatMatchMessage?.let { player.sendMessage(it) }

                return true
            }
            KeyResult.NOT_MATCH -> key.formatNotMatchMessage?.let { player.sendMessage(it) }
            KeyResult.EXPIRED -> key.formatExpiredMessage?.let { player.sendMessage(it) }
            KeyResult.SHORT_AMOUNT -> key.formatShortAmountMessage?.let { player.sendMessage(it) }
        }

        return false
    }

    fun create(player: Player, keyId: String) {
        val itemStack = player.inventory.itemInMainHand
        if (itemStack == null || itemStack.type == Material.AIR) {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "登録するアイテムを持って下さい.")
            return
        }

        if (keyMap.containsKey(keyId)) {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "ID: ${keyId}は既に使用されています.")
            return
        }

        val key = Key(keyId, itemStack)
        if (keyConfig.update(key)) {
            keyMap[keyId] = key
            player.sendMessage(NekoEvent.PREFIX + key.name + ChatColor.GREEN + "を登録しました.")
        } else {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "保存に失敗しました.")
        }
    }

    fun delete(sender: CommandSender, keyId: String) {
        if (!keyMap.containsKey(keyId)) {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${keyId}は存在しません.")
            return
        }

        if (keyConfig.delete(keyId)) {
            keyMap.remove(keyId)
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.GREEN + "${keyId}を消去しました.")
        } else {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "消去に失敗しました.")
        }
    }

    fun flag(player: Player, keyId: String, flagId: String, flagArgs: List<String>) {
        var key = keyMap[keyId] ?: let {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${keyId}は存在しません.")
            return
        }

        val flagType = KeyFlag.find(flagId) ?: let {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${flagId}は正しいフラグIDではありません.")
            return
        }

        try {
            key = when (flagType) {
                ITEM                 -> key.copy(itemStack = flagArgs.flagItemStack(player))
                TAKE                 -> key.copy(enabledTake = flagArgs.flagBoolean(false))
                EXPIRE               -> key.copy(expireMinutes = flagArgs.flagInt(0))
                BLOCK_MATERIAL       -> key.copy(blockMaterial = flagArgs.flagMaterial(null))
                BLOCK_LOCATION       -> key.copy(blockLocation = flagArgs.flagLocation(player, null))
                MATCH_MESSAGE        -> key.copy(matchMessage = flagArgs.flagString(null))
                NOT_MATCH_MESSAGE    -> key.copy(notMatchMessage = flagArgs.flagString(null))
                EXPIRED_MESSAGE      -> key.copy(expiredMessage = flagArgs.flagString(null))
                SHORT_AMOUNT_MESSAGE -> key.copy(shortAmountMessage = flagArgs.flagString(null))
            }
        } catch (e: InvalidFlagException) {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + e.message)
            return
        }

        if (keyConfig.update(key)) {
            keyMap[keyId] = key
            player.sendMessage(NekoEvent.PREFIX + ChatColor.GREEN + "$keyId を更新しました.")
        } else {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "更新に失敗しました.")
        }
    }

    fun sendList(sender: CommandSender) {
        val sb = StringBuilder("&7--------- &6鍵一覧 &7---------&r".formatColorCode())
        sb.append('\n')

        keyMap.toSortedMap().forEach { id, key ->
            sb.append(' ')
            sb.append(id)
            sb.append(": ")
            sb.append(key.name)
            sb.append('\n')
            sb.append(ChatColor.RESET)
        }

        sender.sendMessage(sb.toString())
    }

    fun sendInfo(sender: CommandSender, keyId: String) {
        val key = keyMap.getOrError(keyId) ?: return

        val messages = arrayOf(
                "&7--------- &6鍵情報 &7---------".formatColorCode(),
                " ID: ${key.id}",
                " 名前: ${key.name}",
                " 回収: ${key.enabledTake}",
                " 期限: ${if (key.enabledExpire) "${key.expireMinutes}分" else "なし" }",
                " ブロック: ${if (key.block != null) "${key.block.first}, ${key.block.second.formatString()}" else "なし"}",
                " 一致: ${key.formatMatchMessage}",
                " 不一致: ${key.formatNotMatchMessage}",
                " 期限切れ: ${key.formatExpiredMessage}",
                " 枚数不足: ${key.formatShortAmountMessage}"
        )

        sender.sendMessage(messages)
    }

    fun reload() {
        keyConfig.load()
    }

    override fun onConfigUpdate(dataMap: Map<String, Key>) {
        keyMap.clear()
        keyMap.putAll(dataMap)

        Log.info("${keyMap.size}個の鍵を読み込みました.")
    }

    private fun MutableMap<String, Key>.getOrError(id: String): Key? {
        return keyMap[id] ?: let {
            Log.error("鍵($id)は存在しません.")
            return@let null
        }
    }
}