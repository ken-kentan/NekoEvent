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
        private val manager: KeyManager
) : BaseCommand() {

    companion object {
        private val ARGUMENT_LIST = listOf("use", "give", "drop", "create", "delete", "flag", "flaglist", "list", "info", "help")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty() || args[0] == "help") {
            sendHelp(sender)
            return true
        }

        when (args[0]) {
            "use" -> sender.doIfArguments(args, 2) {
                manager.use(args[1], args[2])
            }
            "give" -> sender.doIfArguments(args, 2) {
                manager.give(args[1], args[2], if (args.size >= 4) args[3] else "1")
            }
            "drop" -> sender.doIfArguments(args, 5) {
                manager.drop(args[1], args.sliceArray(2..5), if (args.size > 5) args[6] else "1")
            }
            "create" -> sender.doIfArguments(args, 1) {
                if (it is Player) {
                    manager.create(it, args[1])
                } else {
                    it.sendInGameCommand()
                }
            }
            "delete" -> sender.doIfArguments(args, 1) {
                manager.delete(it, args[1])
            }
            "flag" -> sender.doIfArguments(args, 2) {
                if (it is Player) {
                    manager.flag(it, args[1], args[2], args.drop(3))
                } else {
                    it.sendInGameCommand()
                }
            }
            "flaglist" -> KeyFlag.sendList(sender)
            "list" -> manager.sendList(sender)
            "info" -> sender.doIfArguments(args, 1) {
                manager.sendInfo(it, args[1])
            }
            "reload" -> manager.reload()
            else -> sender.sendUnknownCommand()
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        if (!sender.hasPermission("neko.event.key") || args.isEmpty()) {
            return mutableListOf()
        }

        var prefix = ""
        val completeList = mutableListOf<String>()

        if (args.size == 1) {
            prefix = args[0]
            completeList.addAll(ARGUMENT_LIST)
        } else {
            when (args[0]) {
                "use", "give" -> {
                    if (args.size == 3) {
                        prefix = args[2]
                        completeList.addAll(manager.getKeyIdList())
                    }
                }
                "drop", "delete", "info" -> {
                    if (args.size == 2){
                        prefix = args[1]
                        completeList.addAll(manager.getKeyIdList())
                    }
                }
                "flag" -> {
                    if (args.size == 2) {
                        prefix = args[1]
                        completeList.addAll(manager.getKeyIdList())
                    } else if (args.size == 3) {
                        prefix = args[2]
                        completeList.addAll(KeyFlag.idList)
                    }
                }
                else -> {}
            }
        }

        return completeList.filter { it.startsWith(prefix, true) }.toMutableList()
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