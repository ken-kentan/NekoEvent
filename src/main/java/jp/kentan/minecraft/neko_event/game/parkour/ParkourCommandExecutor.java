package jp.kentan.minecraft.neko_event.game.parkour;

import jp.kentan.minecraft.neko_event.config.provider.ParkourConfigProvider;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ParkourCommandExecutor implements CommandExecutor {
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
                    ParkourManager.join(args[1], args[2]);
                }
                break;
            case "back":
                if(NekoUtil.checkParams(params, 2)) {
                    ParkourManager.back(args[1], args[2]);
                }
                break;
            case "clear":
                if(NekoUtil.checkParams(params, 2)) {
                    ParkourManager.clear(args[1], args[2]);
                }
                break;
            case "create":
                if(NekoUtil.checkParams(params, 2) && NekoUtil.isPlayer(sender)) {
                    ParkourManager.create((Player) sender, args[1], args[2], NekoUtil.getOptionMap(args));
                }
                break;
            case "delete":
                if(NekoUtil.checkParams(params, 1)){
                    ParkourManager.delete(sender, args[1]);
                }
                break;
            case "set":
                if(NekoUtil.checkParams(params, 4) && NekoUtil.isPlayer(sender)){
                    ParkourManager.setOptions((Player) sender, args[1], NekoUtil.getOptionMap(args));
                }
                break;
            case "option":
                ParkourManager.sendOptionHelp(sender);
                break;
            case "list":
                ParkourManager.sendList(sender);
                break;
            case "reload":
                ParkourConfigProvider.load();
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private static void sendHelp(CommandSender sender){
        sender.sendMessage("---------- NekoEvent Parkourコマンドヘルプ ----------");
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour join [player] [parkourId]");
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour back [player] [parkourId]");
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour clear [player] [parkourId]");
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour create [parkourId] [parkourName] <-o OPTION val>");
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour delete [parkourId]");
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour set [parkourId] [-o OPTION val]");
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour option ('-o'で指定できるオプションリストを表示します)");
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour list");
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour reload");
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour help");
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.");
        sender.sendMessage("| " + ChatColor.GRAY + "'-o'は複数指定が可能です.");
        sender.sendMessage("---------------------------------------");
    }
}
