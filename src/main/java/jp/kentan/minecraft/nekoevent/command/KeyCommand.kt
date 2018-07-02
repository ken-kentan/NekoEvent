package jp.kentan.minecraft.nekoevent.command

import jp.kentan.minecraft.nekoevent.component.KeyFlag
import jp.kentan.minecraft.nekoevent.manager.KeyManager
import jp.kentan.minecraft.nekoevent.util.doIfArguments
import jp.kentan.minecraft.nekoevent.util.sendInGameCommand
import jp.kentan.minecraft.nekoevent.util.sendUnknownCommand
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class KeyCommand(
        private val keyManager: KeyManager
) : BaseCommand() {

    companion object {
        private val ARGUMENT_LIST = listOf("use", "give", "drop", "create", "delete", "flag", "flaglist", "list", "info", "help")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.size <= 1 || label == "help") {
            sendHelp(sender)
            return true
        }

        when (label) {
            "use" -> sender.doIfArguments(args, 2) {
                keyManager.use(args[1], args[2])
            }
            "give" -> sender.doIfArguments(args, 2) {
                keyManager.give(args[1], args[2], if (args.size >= 4) args[3] else "1")
            }
            "drop" -> sender.doIfArguments(args, 5) {
                keyManager.drop(args[1], args.sliceArray(2..5), if (args.size > 5) args[6] else "1")
            }
            "create" -> sender.doIfArguments(args, 1) {
                if (it is Player) {
                    keyManager.create(it, args[1])
                } else {
                    it.sendInGameCommand()
                }
            }
            "delete" -> sender.doIfArguments(args, 1) {
                keyManager.delete(it, args[1])
            }
            "flag" -> sender.doIfArguments(args, 2) {
                if (it is Player) {
                    keyManager.flag(it, args[1], args[2], args.drop(3))
                } else {
                    it.sendInGameCommand()
                }
            }
            "flaglist" -> KeyFlag.sendList(sender)
            "list" -> keyManager.sendList(sender)
            "info" -> sender.doIfArguments(args, 1) {
                keyManager.sendInfo(it, args[1])
            }
            "reload" -> keyManager.reload()
            else -> sender.sendUnknownCommand()
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        if (!sender.hasPermission("neko.event.key") || args.isEmpty()) {
            return mutableListOf()
        }

        if (args.size == 1) {
            return ARGUMENT_LIST.filter { it.startsWith(args[0], true) }.toMutableList()
        }

        when (args[1]) {
            "use", "give" -> {
                if (args.size == 3) return keyManager.getKeyIdList().toMutableList()
            }
            "drop", "delete", "info" -> {
                if (args.size == 2) return keyManager.getKeyIdList().toMutableList()
            }
            "flag" -> {
                if (args.size == 2) return keyManager.getKeyIdList().toMutableList()
                else if (args.size == 3) return KeyFlag.idList.toMutableList()
            }
            else -> {}
        }

        return mutableListOf()
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("---------- NekoEvent Keyコマンドヘルプ ----------")
        sender.sendMessage("| " + ChatColor.GOLD + "/key use [player] [keyId]")
        sender.sendMessage("| " + ChatColor.GOLD + "/key give [player] [keyId] <amount>")
        sender.sendMessage("| " + ChatColor.GOLD + "/key drop [keyId] [world] [x y z] <amount>")
        sender.sendMessage("| " + ChatColor.GOLD + "/key create [keyId]")
        sender.sendMessage("| " + ChatColor.GOLD + "/key delete [keyId]")
        sender.sendMessage("| " + ChatColor.GOLD + "/key flag [keyId] [flagId] <flagArgs..>")
        sender.sendMessage("| " + ChatColor.GOLD + "/key flaglist")
        sender.sendMessage("| " + ChatColor.GOLD + "/key list")
        sender.sendMessage("| " + ChatColor.GOLD + "/key info [keyId]")
        sender.sendMessage("| " + ChatColor.GOLD + "/key reload")
        sender.sendMessage("| " + ChatColor.GOLD + "/key help")
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.")
        sender.sendMessage("| " + ChatColor.GRAY + "'-o'は複数指定が可能です.")
        sender.sendMessage("---------------------------------------")
    }
}