package jp.kentan.minecraft.nekoevent.command

import jp.kentan.minecraft.nekoevent.component.KeyFlag
import jp.kentan.minecraft.nekoevent.component.model.CommandArgument
import jp.kentan.minecraft.nekoevent.component.model.CommandArgument.Companion.PLAYER
import jp.kentan.minecraft.nekoevent.manager.KeyManager
import jp.kentan.minecraft.nekoevent.util.doIfArguments
import jp.kentan.minecraft.nekoevent.util.getPlayerNames
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
        private val ARGUMENT_LIST = listOf(
                CommandArgument("use", PLAYER, "[keyId]"),
                CommandArgument("give", PLAYER, "[keyId]", "<amount>"),
                CommandArgument("drop", "[keyId]", "[world]", "[x y z]", "<amount>"),
                CommandArgument("create", "[keyId]"),
                CommandArgument("delete", "[keyId]"),
                CommandArgument("flag", "[keyId]", "[flagId]", "<flagArgs..>"),
                CommandArgument("flaglist"),
                CommandArgument("list"),
                CommandArgument("info", "[keyId]"),
                CommandArgument("reload"),
                CommandArgument("help")
        )
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty() || args[0] == "help") {
            sendHelp(sender)
            return true
        }

        when (args[0]) {
            "use" -> sender.doIfArguments(args, 2) {
                manager.use(it, args[1], args[2])
            }
            "give" -> sender.doIfArguments(args, 2) {
                manager.give(it, args[1], args[2], if (args.size >= 4) args[3] else "1")
            }
            "drop" -> sender.doIfArguments(args, 5) {
                manager.drop(args[1], args.slice(2..5), if (args.size >= 7) args[6] else "1")
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

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): MutableList<String> {
        if (!sender.hasPermission("neko.event.key") || args.isEmpty()) {
            return mutableListOf()
        }

        if (args.size == 1) {
            return ARGUMENT_LIST.filter { it.matchFirst(args[0]) }.mapNotNull { it.get(args) }.toMutableList()
        }

        val commandArg = ARGUMENT_LIST.find { it.matchFirst(args[0]) } ?: return mutableListOf()
        val prefix = args.last()

        return when (commandArg.get(args)) {
            PLAYER     -> getPlayerNames(prefix).toMutableList()
            "[keyId]"  -> manager.getKeyIdList().filter { it.startsWith(prefix, true) }.toMutableList()
            "[flagId]" -> KeyFlag.idList.filter { it.startsWith(prefix, true) }.toMutableList()
            else       -> mutableListOf()
        }
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
        sender.sendMessage("---------------------------------------")
    }
}