package jp.kentan.minecraft.nekoevent.command

import jp.kentan.minecraft.nekoevent.component.PasswordFlag
import jp.kentan.minecraft.nekoevent.component.model.CommandArgument
import jp.kentan.minecraft.nekoevent.manager.PasswordManager
import jp.kentan.minecraft.nekoevent.util.doIfArguments
import jp.kentan.minecraft.nekoevent.util.getPlayerNames
import jp.kentan.minecraft.nekoevent.util.sendInGameCommand
import jp.kentan.minecraft.nekoevent.util.sendUnknownCommand
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PasswordCommand(
        private val manager: PasswordManager
) : BaseCommand() {

    companion object {
        private val ARGUMENT_LIST = listOf(
                CommandArgument("input", CommandArgument.PLAYER, "[passId]", "[text]"),
                CommandArgument("set", "[passId]", "[text]"),
                CommandArgument("reset", CommandArgument.PLAYER, "[passId]"),
                CommandArgument("create", "[passId]"),
                CommandArgument("delete", "[passId]"),
                CommandArgument("flag", "[passId]", "[flagId]", "<flagArgs..>"),
                CommandArgument("flaglist"),
                CommandArgument("list"),
                CommandArgument("info", "[passId]"),
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
            "input" -> sender.doIfArguments(args, 3) {
                manager.input(args[1], args[2], args[3])
            }
            "set" -> sender.doIfArguments(args, 2) {
                manager.set(args[1], args[2])
            }
            "reset" -> sender.doIfArguments(args, 2) {
                manager.reset(args[1], args[2])
            }
            "create" -> sender.doIfArguments(args, 1) {
                manager.create(it, args[1])
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
            "flaglist" -> PasswordFlag.sendList(sender)
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
        if (!sender.hasPermission("neko.event.password") || args.isEmpty()) {
            return mutableListOf()
        }

        if (args.size == 1) {
            return ARGUMENT_LIST.filter { it.matchFirst(args[0]) }.mapNotNull { it.get(args) }.toMutableList()
        }

        val commandArg = ARGUMENT_LIST.find { it.matchFirst(args[0]) } ?: return mutableListOf()
        val prefix = args.last()

        return when (commandArg.get(args)) {
            CommandArgument.PLAYER -> getPlayerNames(prefix).toMutableList()
            "[passId]"             -> manager.getKeyIdList().filter { it.startsWith(prefix, true) }.toMutableList()
            "[flagId]"             -> PasswordFlag.idList.filter { it.startsWith(prefix, true) }.toMutableList()
            else -> mutableListOf()
        }
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("---------- NekoEvent Passwordコマンドヘルプ ----------")
        sender.sendMessage("| " + ChatColor.BLUE + "/password input [player] [passId] [text]")
        sender.sendMessage("| " + ChatColor.BLUE + "/password set [passId] [text]")
        sender.sendMessage("| " + ChatColor.BLUE + "/password reset [player] [passId] (入力ﾊﾞｯﾌｧをクリア)")
        sender.sendMessage("| " + ChatColor.BLUE + "/password create [passId]")
        sender.sendMessage("| " + ChatColor.BLUE + "/password delete [passId]")
        sender.sendMessage("| " + ChatColor.BLUE + "/password flag [passId] [flagId] <flagArgs..>")
        sender.sendMessage("| " + ChatColor.BLUE + "/password flaglist")
        sender.sendMessage("| " + ChatColor.BLUE + "/password list")
        sender.sendMessage("| " + ChatColor.BLUE + "/password info [passId]")
        sender.sendMessage("| " + ChatColor.BLUE + "/password reload")
        sender.sendMessage("| " + ChatColor.BLUE + "/password help")
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.")
        sender.sendMessage("| " + ChatColor.GRAY + "'-o'は複数指定が可能です.")
        sender.sendMessage("---------------------------------------")
    }
}