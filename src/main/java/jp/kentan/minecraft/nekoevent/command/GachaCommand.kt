package jp.kentan.minecraft.nekoevent.command

import jp.kentan.minecraft.nekoevent.component.model.CommandArgument
import jp.kentan.minecraft.nekoevent.manager.GachaManager
import jp.kentan.minecraft.nekoevent.util.doIfArguments
import jp.kentan.minecraft.nekoevent.util.getPlayerNames
import jp.kentan.minecraft.nekoevent.util.sendUnknownCommand
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class GachaCommand(
        private val manager: GachaManager
) : BaseCommand() {

    companion object {
        private val ARGUMENT_LIST = listOf(
                CommandArgument("play", CommandArgument.PLAYER, "[gachaId]"),
                CommandArgument("demo", "[gachaId]", "[times]"),
                CommandArgument("list"),
                CommandArgument("info", "[gachaId]"),
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
            "play" -> sender.doIfArguments(args, 2) {
                    manager.play(args[1], args[2])
            }
            "demo" -> sender.doIfArguments(args, 2) {
                manager.demo(it, args[1], args[2])
            }
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
        if (!sender.hasPermission("neko.event.gacha") || args.isEmpty()) {
            return mutableListOf()
        }

        if (args.size == 1) {
            return ARGUMENT_LIST.filter { it.matchFirst(args[0]) }.mapNotNull { it.get(args) }.toMutableList()
        }

        val commandArg = ARGUMENT_LIST.find { it.matchFirst(args[0]) } ?: return mutableListOf()
        val prefix = args.last()

        return when (commandArg.get(args)) {
            CommandArgument.PLAYER -> {
                getPlayerNames(prefix).toMutableList()
            }
            "[gachaId]" -> {
                manager.getGachaIdList().filter { it.startsWith(prefix, true) }.toMutableList()
            }
            else -> mutableListOf()
        }
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("---------- NekoEvent Gachaコマンドヘルプ ----------")
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha play [player] [gachaId]")
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha demo [gachaId] [times]")
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha list")
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha info [gachaId]")
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha reload")
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha help")
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.")
        sender.sendMessage("---------------------------------------")
    }
}