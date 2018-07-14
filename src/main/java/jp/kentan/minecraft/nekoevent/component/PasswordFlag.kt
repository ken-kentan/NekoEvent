package jp.kentan.minecraft.nekoevent.component

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

enum class PasswordFlag(
        val id: String,
        val help: String
) {
    DEFAULT("default", "[text] 初期パスワードを設定"),
    BLOCK_MATERIAL("block-material", "[material] 設置ブロック種類"),
    BLOCK_LOCATION("block-location", "[here/x y z] 設置ブロック位置"),
    MATCH_MESSAGE("match-msg", "[msg] 一致時に表示"),
    NOT_MATCH_MESSAGE("not-match-msg", "[msg] 不一致時に表示"),
    INPUT_MESSAGE("input-msg", "[msg] 入力時に表示, {buff}は入力値に置換"),
    RESET_MESSAGE("reset-msg", "[msg] 入力リセット時に表示");

    companion object {
        val idList = values().map { it.id }

        fun find(flagId: String) = values().find { it.id == flagId }

        fun sendList(sender: CommandSender) {
            sender.sendMessage("---------- NekoEvent Passwordフラグヘルプ ----------")

            values().forEach { flag ->
                sender.sendMessage("| " + ChatColor.AQUA + "${flag.id} ${flag.help}")
            }

            sender.sendMessage("| " + ChatColor.GRAY + "必須: [], 任意: <>")
            sender.sendMessage("---------------------------------------")
        }
    }
}