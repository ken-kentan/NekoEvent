package jp.kentan.minecraft.neko_event.game.dungeon;

import jp.kentan.minecraft.neko_event.config.provider.DungeonConfigProvider;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class DungeonCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final int params = args.length - 1;

        if(params < 0 || args[0].equals("help")){
            sendHelp(sender);
            return true;
        }

        switch (args[0]){
            case "join":
                if(NekoUtil.checkParams(params, 2)) {
                    DungeonManager.join(args[1], args[2]);
                }
                break;
            case "clear":
                if(NekoUtil.checkParams(params, 2)) {
                    DungeonManager.clear(args[1], args[2]);
                }
                break;
            case "lock":
                if(NekoUtil.checkParams(params, 2)){
                    DungeonManager.lock(args[1], NekoUtil.toInteger(args[2]));
                }
                break;
            case "unlock":
                if(NekoUtil.checkParams(params, 1)){
                    DungeonManager.unlock(args[1]);
                }
                break;
            case "create":
                if(NekoUtil.checkParams(params, 2) && NekoUtil.isPlayer(sender)) {
                    DungeonManager.create((Player) sender, args[1], args[2], NekoUtil.getOptionMap(args));
                }
                break;
            case "delete":
                if(NekoUtil.checkParams(params, 1)){
                    DungeonManager.delete(sender, args[1]);
                }
                break;
            case "set":
                if(NekoUtil.checkParams(params, 4) && NekoUtil.isPlayer(sender)){
                    DungeonManager.setOptions((Player) sender, args[1], NekoUtil.getOptionMap(args));
                }
                break;
            case "option":
                DungeonManager.sendOptionHelp(sender);
                break;
            case "list":
                DungeonManager.sendList(sender);
                break;
            case "reload":
                DungeonConfigProvider.load();
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private static void sendHelp(CommandSender sender){
        sender.sendMessage("---------- NekoEvent Dungeonコマンドヘルプ ----------");
        sender.sendMessage("| " + ChatColor.RED + "/dungeon join [player] [dungeonId]");
        sender.sendMessage("| " + ChatColor.RED + "/dungeon clear [player] [dungeonId]");
        sender.sendMessage("| " + ChatColor.RED + "/dungeon lock [dungeonId] [timerSecond]");
        sender.sendMessage("| " + ChatColor.RED + "/dungeon unlock [dungeonId]");
        sender.sendMessage("| " + ChatColor.RED + "/dungeon create [dungeonId] [dungeonName] <-o OPTION val>");
        sender.sendMessage("| " + ChatColor.RED + "/dungeon delete [dungeonId]");
        sender.sendMessage("| " + ChatColor.RED + "/dungeon set [dungeonId] [-o OPTION val]");
        sender.sendMessage("| " + ChatColor.RED + "/dungeon option ('-o'で指定できるオプションリストを表示します)");
        sender.sendMessage("| " + ChatColor.RED + "/dungeon list");
        sender.sendMessage("| " + ChatColor.RED + "/dungeon reload");
        sender.sendMessage("| " + ChatColor.RED + "/dungeon help");
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.");
        sender.sendMessage("| " + ChatColor.GRAY + "'-o'は複数指定が可能です.");
        sender.sendMessage("---------------------------------------");
    }
}
