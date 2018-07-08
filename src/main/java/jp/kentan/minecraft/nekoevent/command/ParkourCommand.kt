package jp.kentan.minecraft.nekoevent.command

import jp.kentan.minecraft.nekoevent.component.ParkourFlag
import jp.kentan.minecraft.nekoevent.component.model.CommandArgument
import jp.kentan.minecraft.nekoevent.component.model.CommandArgument.Companion.PLAYER
import jp.kentan.minecraft.nekoevent.manager.ParkourManager
import jp.kentan.minecraft.nekoevent.util.doIfArguments
import jp.kentan.minecraft.nekoevent.util.getPlayerNames
import jp.kentan.minecraft.nekoevent.util.sendInGameCommand
import jp.kentan.minecraft.nekoevent.util.sendUnknownCommand
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ParkourCommand(
        private val manager: ParkourManager
) : BaseCommand() {

    companion object {
        private val ARGUMENT_LIST = listOf(
                CommandArgument("join", PLAYER, "[parkourId]"),
                CommandArgument("clear", PLAYER, "[parkourId]"),
                CommandArgument("back", PLAYER, "[parkourId]"),
                CommandArgument("create", "[parkourId]"),
                CommandArgument("delete", "[parkourId]"),
                CommandArgument("flag", "[parkourId]", "[flagId]", "<flagArgs..>"),
                CommandArgument("flaglist"),
                CommandArgument("list"),
                CommandArgument("info", "[parkourId]"),
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
            "join" -> sender.doIfArguments(args, 2) {
                manager.join(args[1], args[2])
            }
            "clear" -> sender.doIfArguments(args, 2) {
                manager.clear(args[1], args[2])
            }
            "back" -> sender.doIfArguments(args, 2) {
                manager.back(args[1], args[2])
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
            "flaglist" -> ParkourFlag.sendList(sender)
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
        if (!sender.hasPermission("neko.event.parkour") || args.isEmpty()) {
            return mutableListOf()
        }

        if (args.size == 1) {
            return ARGUMENT_LIST.filter { it.matchFirst(args[0]) }.mapNotNull { it.get(args) }.toMutableList()
        }

        val commandArg = ARGUMENT_LIST.find { it.matchFirst(args[0]) } ?: return mutableListOf()
        val prefix = args.last()

        return when (commandArg.get(args)) {
            PLAYER        -> getPlayerNames(prefix).toMutableList()
            "[parkourId]" -> manager.getParkourIdList().filter { it.startsWith(prefix, true) }.toMutableList()
            "[flagId]"    -> ParkourFlag.idList.filter { it.startsWith(prefix, true) }.toMutableList()
            else          -> mutableListOf()
        }
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("---------- NekoEvent Parkourコマンドヘルプ ----------")
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour join [player] [parkourId]")
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour clear [player] [parkourId]")
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour back [player] [parkourId]")
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour create [parkourId]")
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour delete [parkourId]")
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour flag [parkourId] [flagId] <flagArgs..>")
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour flaglist")
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour list")
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour info [parkourId]")
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour reload")
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour help")
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.")
        sender.sendMessage("---------------------------------------")
    }
}