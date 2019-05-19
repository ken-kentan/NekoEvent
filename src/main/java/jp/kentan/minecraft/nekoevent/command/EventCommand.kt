package jp.kentan.minecraft.nekoevent.command

import jp.kentan.minecraft.nekoevent.component.TicketType
import jp.kentan.minecraft.nekoevent.component.model.CommandArgument
import jp.kentan.minecraft.nekoevent.component.model.CommandArgument.Companion.PLAYER
import jp.kentan.minecraft.nekoevent.config.ConfigManager
import jp.kentan.minecraft.nekoevent.manager.SpawnManager
import jp.kentan.minecraft.nekoevent.manager.TicketManager
import jp.kentan.minecraft.nekoevent.util.*
import org.bukkit.*
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Player
import org.bukkit.entity.SmallFireball
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

class EventCommand(
        private val plugin: Plugin,
        private val config: ConfigManager,
        private val ticketManager: TicketManager,
        private val spawnManager: SpawnManager
) : BaseCommand() {

    companion object {
        private val ARGUMENT_LIST = listOf(
                CommandArgument("ticket", PLAYER, "[ticketType]", "[amount]"),
                CommandArgument("tp", PLAYER, "[x y z <yaw pitch>]"),
                CommandArgument("msg", PLAYER, "[sender]", "[message]"),
                CommandArgument("setspawn", PLAYER, "[x y z]"),
                CommandArgument("jump", PLAYER, "[height]", "[length]"),
                CommandArgument("exp", PLAYER, "[value]"),
                CommandArgument("random", "[x y z]", "[x y z]", "<x y z>"),
                CommandArgument("randomtp", PLAYER, "[x y z]", "[x y z]", "<x y z>"),
                CommandArgument("delay", "[seconds]", "[x y z]"),
                CommandArgument("checkdelay", "[x y z]"),
                CommandArgument("notdelay", "[x y z]", "[x y z]"),
                CommandArgument("reset_status", PLAYER),
                CommandArgument("reload"),
                CommandArgument("help")
        )

        private val RANDOM = MersenneTwisterFast()
    }

    private val scheduler = Bukkit.getScheduler()
    private val delayTaskIdMap = ConcurrentHashMap<Location, Int>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty() || args[0] == "help") {
            sendHelp(sender)
            return true
        }

        when (args[0]) {
            "ticket" -> sender.doIfArguments(args, 3) {
                ticketManager.give(args[1], args[2], args[3])
            }
            "tp" -> sender.doIfArguments(args, 4) {
                eventTp(sender, args[1], args.drop(2))
            }
            "msg" -> sender.doIfArguments(args, 3) {
                eventMessage(sender, args[1], args[2], args.drop(3))
            }
            "setspawn" -> sender.doIfArguments(args, 4) {
                if (it is BlockCommandSender) {
                    spawnManager.setSpawn(sender, args[1], listOf(it.block.world.name, args[2], args[3], args[4]))
                } else {
                    it.sendCommandBlockCommand()
                }
            }
            "jump" -> sender.doIfArguments(args, 3) {
                eventJump(sender, args[1], args[2], args[3])
            }
            "exp" -> sender.doIfArguments(args, 2) {
                eventExp(args[1], args[2])
            }
            "random" -> sender.doIfArguments(args, 6) {
                if (it is BlockCommandSender) {
                    eventRandom(it, args.drop(1))
                } else {
                    it.sendCommandBlockCommand()
                }
            }
            "randomtp" -> sender.doIfArguments(args, 6) {
                if (it is BlockCommandSender) {
                    eventRandomTp(args[1], it, args.drop(2))
                } else {
                    it.sendCommandBlockCommand()
                }
            }
            "delay" -> sender.doIfArguments(args, 4) {
                if (it is BlockCommandSender) {
                    eventDelay(it, args[1], args.drop(2))
                } else {
                    it.sendCommandBlockCommand()
                }
            }
            "canceldelay" -> sender.doIfArguments(args, 3) {
                if (it is BlockCommandSender) {
                    cancelEventDelay(it, args.drop(1))
                } else {
                    it.sendCommandBlockCommand()
                }
            }
            "notdelay" -> sender.doIfArguments(args, 6) {
                if (it is BlockCommandSender) {
                    checkEventDelay(it, args.slice(1..3), args.slice(4..6))
                } else {
                    it.sendCommandBlockCommand()
                }
            }
            "reset_status" -> sender.doIfArguments(args, 1) {
                resetPlayerStatus(sender, args[1])
            }
            "reload" -> config.reload()
            "debug" -> {
                if (sender.isOp) {
                    val p = sender as Player
                    val loc = Location(p.world, 0.0, 0.0, 0.0)

                    val list = loc.chunk.entities.filter { it is SmallFireball }
                    Log.info("Find: ${list.size}")
                    Log.info("Removing...")
                    list.forEach { it.remove() }
                    Log.info("Done.")
                }
            }
            else -> sender.sendUnknownCommand()
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): MutableList<String> {
        if (!sender.hasPermission("neko.event") || args.isEmpty()) {
            return mutableListOf()
        }

        if (args.size == 1) {
            return ARGUMENT_LIST.filter { it.matchFirst(args[0]) }.mapNotNull { it.get(args) }.toMutableList()
        }

        val commandArg = ARGUMENT_LIST.find { it.matchFirst(args[0]) } ?: return mutableListOf()
        val prefix = args.last()

        return when (commandArg.get(args)) {
            PLAYER         -> getPlayerNames(prefix).toMutableList()
            "[ticketType]" -> TicketType.idList.filter { it.startsWith(prefix, true) }.toMutableList()
            else -> mutableListOf()
        }
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("---------- NekoEvent コマンドヘルプ ----------")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event ticket [player] [ticketType] [amount]")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event tp [player] [x y z <yaw pitch>] (相対指定時の基準はプレイヤー座標)")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event msg [player] [sender] [message] ([sender]にnullを指定で[message]のみ表示)")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event setspawn [player] <x y z> (相対指定時の基準はｺﾏﾝﾄﾞﾌﾞﾛｯｸ座標)")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event jump [player] [height] [length] (各値は小数点係数)")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event exp [player] [value] (経験値ｵｰﾌﾞをドロップ)")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event random [x y z] [x y z] <x y z> (座標は複数指定可能. 0.5s後に消滅)")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event randomtp [x y z] [x y z] <x y z> (座標は複数指定可能)")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event delay [seconds] [x y z]")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event canceldelay [x y z]")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event notdelay [x y z] [x y z] (delay位置, RED_STONE位置)")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event reset_status [player] (プレイヤーのステータスをリセットして体力20)")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event reload")
        sender.sendMessage("| " + ChatColor.YELLOW + "/event help")
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha")
        sender.sendMessage("| " + ChatColor.GOLD + "/key")
        sender.sendMessage("| " + ChatColor.BLUE + "/password")
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour")
        sender.sendMessage("| " + ChatColor.RED + "/dungeon")
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.")
        sender.sendMessage("| " + ChatColor.GRAY + "装飾記号は'&'を使用してください.")
        sender.sendMessage("---------------------------------------")
    }

    private fun eventTp(sender: CommandSender, selector: String, strLocation: List<String>) {
        selector.toPlayersOrError(sender).forEach { player ->
            val location = strLocation.toLocationOrError(player.location) ?: return@forEach
            player.teleport(location)

            Log.info("${player.name}を${location.formatString()}にテレポートしました.")
        }
    }

    private fun eventMessage(sender: CommandSender, selector: String, speaker: String, messages: List<String>) {
        val message = " ${if (speaker != "null") "$speaker§a: "  else ""}§r${messages.joinToString(separator = " ")}"
                .formatColorCode()

        selector.toPlayersOrError(sender).forEach {
            it.sendMessage(message)
        }
    }

    private fun eventJump(sender: CommandSender, selector: String, strHeight: String, strLength: String) {
        val players = selector.toPlayersOrError(sender)
        val height = strHeight.toDoubleOrError() ?: return
        val length = strLength.toDoubleOrError() ?: return

        players.forEach { player ->
            val location = player.location
            val direction = location.direction.setY(0.0).normalize()

            direction.multiply(length)
            direction.y = height

            location.world?.playEffect(location, Effect.SMOKE, 4)

            player.playSound(location, Sound.ENTITY_GHAST_SHOOT, 1f, 1f)
            player.velocity = direction
        }
    }

    private fun eventExp(strPlayer: String, strValue: String) {
        val player = strPlayer.toPlayerOrError() ?: return
        val value = strValue.toIntOrError() ?: return

        val location = player.location
        location.add(0.0, 0.5, 0.0)

        val expOrb = player.world.spawnEntity(location, EntityType.EXPERIENCE_ORB) as ExperienceOrb
        expOrb.experience = value
    }

    private fun eventRandom(sender: BlockCommandSender, strLocationList: List<String>) {
        val pivot = RANDOM.nextInt(strLocationList.size / 3) * 3
        val location = strLocationList.slice(pivot..pivot+2).toLocationOrError(sender.block.location) ?: return

        location.block.type = Material.REDSTONE_BLOCK

        scheduler.scheduleSyncDelayedTask(plugin, { location.block.type = Material.AIR }, 10L)
    }

    private fun eventRandomTp(strPlayer: String, sender: BlockCommandSender, strLocationList: List<String>) {
        val pivot = RANDOM.nextInt(strLocationList.size / 3) * 3
        val location = strLocationList.slice(pivot..pivot+2).toLocationOrError(sender.block.location) ?: return

        val player = strPlayer.toPlayerOrError() ?: return
        location.yaw = player.location.yaw
        location.pitch = player.location.pitch

        player.teleport(location)
    }

    private fun eventDelay(sender: BlockCommandSender, strSeconds: String, strLocationList: List<String>) {
        val seconds = strSeconds.toIntOrError() ?: return
        val location = strLocationList.toLocationOrError(sender.block.location) ?: return

        val oldTaskId = delayTaskIdMap[location]
        if (oldTaskId != null) {
            scheduler.cancelTask(oldTaskId)
        }

        delayTaskIdMap[location] = scheduler.scheduleSyncDelayedTask(plugin, {
            delayTaskIdMap.remove(location)
            location.block.type = Material.REDSTONE_BLOCK
        }, 20L * seconds)
    }

    private fun cancelEventDelay(sender: BlockCommandSender, strLocationList: List<String>) {
        val location = strLocationList.toLocationOrError(sender.block.location) ?: return

        scheduler.cancelTask(delayTaskIdMap.remove(location) ?: return)
    }

    private fun checkEventDelay(sender: BlockCommandSender, strDelayLocationList: List<String>, strLocationList: List<String>) {
        val delayLocation = strDelayLocationList.toLocationOrError(sender.block.location) ?: return
        val location = strLocationList.toLocationOrError(sender.block.location) ?: return

        location.block.type = if (delayTaskIdMap.containsKey(delayLocation)) Material.AIR else Material.REDSTONE_BLOCK
    }

    private fun resetPlayerStatus(sender: CommandSender, selector: String) {
        selector.toPlayersOrError(sender).forEach { it.resetStatus() }
    }
}