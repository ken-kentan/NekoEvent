package jp.kentan.minecraft.nekoevent.component

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

enum class KeyFlag(
        val id: String,
        val help: String
) {
    ITEM("item", "手のアイテムを鍵に設定"),
    TAKE("take", "[on/off] 使用した鍵を消去"),
    EXPIRE("expire", "[minutes] 鍵の有効期限"),
    BLOCK_MATERIAL("block-material", "[material] 設置ブロック種類"),
    BLOCK_LOCATION("block-location", "[here/x y z] 設置ブロック位置"),
    MATCH_MESSAGE("match-msg", "[msg] 一致時に表示"),
    NOT_MATCH_MESSAGE("not-match-msg", "[msg] 不一致時に表示"),
    EXPIRED_MESSAGE("expired-msg", "[msg] 期限切れ時に表示"),
    SHORT_AMOUNT_MESSAGE("short-amount-msg", "[msg] 数が足りないときに表示");

    companion object {
        val idList = values().map { it.id }

        fun find(flagId: String) = values().find { it.id == flagId }

        fun sendList(sender: CommandSender) {
            sender.sendMessage("---------- NekoEvent Keyフラグヘルプ ----------")

            values().forEach { flag ->
                sender.sendMessage("| " + ChatColor.AQUA + "${flag.id} ${flag.help}")
            }

            sender.sendMessage("| " + ChatColor.GRAY + "必須: [], 任意: <>")
            sender.sendMessage("| " + ChatColor.GRAY + "[msg]では {name} でキー名に置換されます.")
            sender.sendMessage("---------------------------------------")
        }
    }
}