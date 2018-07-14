package jp.kentan.minecraft.nekoevent.command

import jp.kentan.minecraft.nekoevent.component.DungeonFlag
import jp.kentan.minecraft.nekoevent.component.model.CommandArgument
import jp.kentan.minecraft.nekoevent.component.model.CommandArgument.Companion.PLAYER
import jp.kentan.minecraft.nekoevent.manager.DungeonManager
import jp.kentan.minecraft.nekoevent.util.doIfArguments
import jp.kentan.minecraft.nekoevent.util.getPlayerNames
import jp.kentan.minecraft.nekoevent.util.sendInGameCommand
import jp.kentan.minecraft.nekoevent.util.sendUnknownCommand
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DungeonCommand(
        private val manager: DungeonManager
) : BaseCommand() {

    companion object {
        private val ARGUMENT_LIST = listOf(
                CommandArgument("join", PLAYER, "[dungeonId]"),
                CommandArgument("clear", PLAYER, "[dungeonId]"),
                CommandArgument("lock", "[dungeonId]", "[seconds]"),
                CommandArgument("unlock", "[dungeonId]"),
                CommandArgument("create", "[dungeonId]"),
                CommandArgument("delete", "[dungeonId]"),
                CommandArgument("flag", "[dungeonId]", "[flagId]", "<flagArgs..>"),
                CommandArgument("flaglist"),
                CommandArgument("list"),
                CommandArgument("info", "[dungeonId]"),
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
            "lock" -> sender.doIfArguments(args, 2) {
                manager.lock(args[1], args[2])
            }
            "unlock" -> sender.doIfArguments(args, 1) {
                manager.unlock(args[1])
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
            "flaglist" -> DungeonFlag.sendList(sender)
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
        if (!sender.hasPermission("neko.event.dungeon") || args.isEmpty()) {
            return mutableListOf()
        }

        if (args.size == 1) {
            return ARGUMENT_LIST.filter { it.matchFirst(args[0]) }.mapNotNull { it.get(args) }.toMutableList()
        }

        val commandArg = ARGUMENT_LIST.find { it.matchFirst(args[0]) } ?: return mutableListOf()
        val prefix = args.last()

        return when (commandArg.get(args)) {
            PLAYER        -> getPlayerNames(prefix).toMutableList()
            "[dungeonId]" -> manager.getDungeonIdList().filter { it.startsWith(prefix, true) }.toMutableList()
            "[flagId]"    -> DungeonFlag.idList.filter { it.startsWith(prefix, true) }.toMutableList()
            else -> mutableListOf()
        }
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("---------- NekoEvent Dungeonコマンドヘルプ ----------")
        sender.sendMessage("| " + ChatColor.RED + "/dungeon join [player] [dungeonId]")
        sender.sendMessage("| " + ChatColor.RED + "/dungeon clear [player] [dungeonId]")
        sender.sendMessage("| " + ChatColor.RED + "/dungeon lock [dungeonId] [seconds]")
        sender.sendMessage("| " + ChatColor.RED + "/dungeon unlock [dungeonId]")
        sender.sendMessage("| " + ChatColor.RED + "/dungeon create [dungeonId]")
        sender.sendMessage("| " + ChatColor.RED + "/dungeon delete [dungeonId]")
        sender.sendMessage("| " + ChatColor.RED + "/dungeon flag [dungeonId] [flagId] <flagArgs..>")
        sender.sendMessage("| " + ChatColor.RED + "/dungeon flaglist")
        sender.sendMessage("| " + ChatColor.RED + "/dungeon list")
        sender.sendMessage("| " + ChatColor.RED + "/dungeon info [dungeonId]")
        sender.sendMessage("| " + ChatColor.RED + "/dungeon reload")
        sender.sendMessage("| " + ChatColor.RED + "/dungeon help")
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.")
        sender.sendMessage("---------------------------------------")
    }
}