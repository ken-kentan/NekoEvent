package jp.kentan.minecraft.nekoevent.command

import jp.kentan.minecraft.nekoevent.manager.GachaManager
import jp.kentan.minecraft.nekoevent.util.doIfParameter
import jp.kentan.minecraft.nekoevent.util.sendUnknownCommand
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GachaCommand(
        private val manager: GachaManager
) : BaseCommand() {

    private companion object {
        val ARGUMENT_LIST = mutableListOf("play", "demo", "list", "info", "reload", "help")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val param = args.size-1

        if (param < 0 || label == "help") {
            sendHelp(sender)
            return true
        }

        when (args[0]) {
            "play" -> sender.doIfParameter(param, 2) {
                    manager.play(args[1], args[2])
            }
            "demo" -> sender.doIfParameter(param, 2) {
                manager.demo(it, args[1], args[2])
            }
            "list" -> manager.sendList(sender)
            "info" -> sender.doIfParameter(param, 1) {
                manager.sendInfo(it, args[1])
            }
            "reload" -> manager.reload()
            else -> sender.sendUnknownCommand()
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        if (!sender.hasPermission("neko.event.gacha")) {
            return mutableListOf()
        }

        if (args.size == 1) {
            return ARGUMENT_LIST.filter { it.startsWith(args[0], true) }.toMutableList()
        }

        return mutableListOf()
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