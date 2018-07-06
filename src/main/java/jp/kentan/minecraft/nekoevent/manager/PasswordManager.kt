package jp.kentan.minecraft.nekoevent.manager

import jp.kentan.minecraft.nekoevent.NekoEvent
import jp.kentan.minecraft.nekoevent.component.PasswordFlag
import jp.kentan.minecraft.nekoevent.component.PasswordFlag.*
import jp.kentan.minecraft.nekoevent.component.PasswordResult
import jp.kentan.minecraft.nekoevent.component.model.Password
import jp.kentan.minecraft.nekoevent.config.ConfigUpdateListener
import jp.kentan.minecraft.nekoevent.config.provider.PasswordConfigProvider
import jp.kentan.minecraft.nekoevent.util.*
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PasswordManager(
        private val config: PasswordConfigProvider
) : ConfigUpdateListener<Password> {

    companion object {
        private const val ID_NOT_FOUND = "パスワード({id})は存在しません."
    }

    private val passwordMap = mutableMapOf<String, Password>()

    fun getKeyIdList() = passwordMap.values.map { it.id }.sorted()

    fun input(strPlayer: String, passwordId: String, text: String) {
        val player = strPlayer.toPlayerOrError() ?: return
        val password = passwordMap[passwordId] ?: let {
            Log.error(ID_NOT_FOUND.replace("{id}", passwordId))
            return
        }

        input(player, password, text)
    }

    private fun input(player: Player, password: Password, text: String) {
        val result = password.input(text)

        password.formatInputMessage?.let {
            player.sendMessage(it)
        }

        when (result) {
            PasswordResult.MATCH -> {
                password.block?.let { (material, location) ->
                    location.block.type = material
                }

                password.formatMatchMessage?.let {
                    player.sendMessage(it)
                }
            }
            PasswordResult.NOT_MATCH -> {
                password.formatNotMatchMessage?.let {
                    player.sendMessage(it)
                }
            }
            PasswordResult.CONTINUE -> {}
        }
    }

    fun set(passwordId: String, text: String) {
        val password = passwordMap[passwordId] ?: let {
            Log.error(ID_NOT_FOUND.replace("{id}", passwordId))
            return
        }

        password.passwordText = text
        Log.info("パスワード($passwordId)を'$text'に設定しました.")
    }

    fun reset(strPlayer: String, passwordId: String) {
        val player = strPlayer.toPlayerOrError() ?: return
        val password = passwordMap[passwordId] ?: let {
            Log.error(ID_NOT_FOUND.replace("{id}", passwordId))
            return
        }

        reset(player, password)
    }

    private fun reset(player: Player, password: Password) {
        password.reset()

        password.formatResetMessage?.let {
            player.sendMessage(it)
        }
    }

    fun create(sender: CommandSender, passwordId: String) {
        if (passwordMap.containsKey(passwordId)) {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${passwordId}は既に使用されています.")
            return
        }

        val password = Password(passwordId)

        if (config.update(password)) {
            passwordMap[passwordId] = password
            sender.sendMessage(NekoEvent.PREFIX + password.id + ChatColor.GREEN + "を登録しました.")
        } else {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "保存に失敗しました.")
        }
    }

    fun delete(sender: CommandSender, passwordId: String) {
        if (!passwordMap.containsKey(passwordId)) {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${passwordId}は存在しません.")
            return
        }

        if (config.delete(passwordId)) {
            passwordMap.remove(passwordId)
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.GREEN + "${passwordId}を消去しました.")
        } else {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "消去に失敗しました.")
        }
    }

    fun flag(player: Player, passwordId: String, flagId: String, flagArgs: List<String>) {
        var password = passwordMap[passwordId] ?: let {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${passwordId}は存在しません.")
            return
        }

        val flagType = PasswordFlag.find(flagId) ?: let {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "${flagId}は正しいフラグIDではありません.")
            return
        }

        try {
            password = when (flagType) {
                DEFAULT           -> password.copy(default = flagArgs.flagString(null) ?: "")
                BLOCK_MATERIAL    -> password.copy(blockMaterial = flagArgs.flagMaterial(null))
                BLOCK_LOCATION    -> password.copy(blockLocation = flagArgs.flagLocation(player, null))
                MATCH_MESSAGE     -> password.copy(matchMessage = flagArgs.flagString(null))
                NOT_MATCH_MESSAGE -> password.copy(notMatchMessage = flagArgs.flagString(null))
                INPUT_MESSAGE     -> password.copy(inputMessage = flagArgs.flagString(null))
                RESET_MESSAGE     -> password.copy(resetMessage = flagArgs.flagString(null))
            }
        } catch (e: InvalidFlagException) {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + e.message)
            return
        }

        if (config.update(password)) {
            passwordMap[passwordId] = password
            player.sendMessage(NekoEvent.PREFIX + ChatColor.GREEN + "$passwordId を更新しました.")
        } else {
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "更新に失敗しました.")
        }
    }

    fun sendList(sender: CommandSender) {
        val sb = StringBuilder("&7--------- &6パスワード一覧 &7---------&r".formatColorCode())
        sb.append('\n')

        passwordMap.toSortedMap().forEach { id, password ->
            sb.append(' ')
            sb.append(id)
            sb.append(": ")
            sb.append(password.passwordText)
            sb.append('\n')
            sb.append(ChatColor.RESET)
        }

        sender.sendMessage(sb.toString())
    }

    fun sendInfo(sender: CommandSender, keyId: String) {
        val password = passwordMap[keyId] ?: let {
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + ID_NOT_FOUND.replace("{id}", keyId))
            return
        }

        val messages = arrayOf(
                "&7--------- &6鍵情報 &7---------".formatColorCode(),
                " ID: ${password.id}",
                " 初期値: ${password.default}",
                " パスワード: ${password.passwordText}",
                " バッファ: ${password.bufferText}",
                " ブロック: ${if (password.block != null) "${password.block.first}, ${password.block.second}" else "なし"}",
                " 一致: ${password.formatMatchMessage}",
                " 不一致: ${password.formatNotMatchMessage}",
                " 入力: ${password.formatInputMessage}",
                " リセット: ${password.formatResetMessage}"
        )

        sender.sendMessage(messages)
    }

    fun reload() {
        config.load()
    }

    override fun onConfigUpdate(dataMap: Map<String, Password>) {
        passwordMap.clear()
        passwordMap.putAll(dataMap)

        Log.info("${passwordMap.size}個の鍵を読み込みました.")
    }
}