package jp.kentan.minecraft.neko_event.gacha;

import jp.kentan.minecraft.neko_event.config.provider.GachaConfigProvider;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GachaCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final int params = args.length - 1;

        if(params < 0 || args[0].equals("help")){
            sendHelp(sender);
            return true;
        }

        switch (args[0]){
            case "play":
                if(NekoUtil.checkParams(params, 2)){
                    GachaManager.play(args[2], args[1], (params > 3) ? args[3] : null);
                }
                break;
            case "demo":
                if(NekoUtil.checkParams(params, 2) && NekoUtil.isPlayer(sender)){
                    GachaManager.demo((Player)sender, args[1], args[2]);
                }
                break;
            case "list":
                GachaManager.sendList(sender);
                break;
            case "info":
                if(NekoUtil.checkParams(params, 1)){
                    GachaManager.sendInfo(sender, args[1]);
                }
                break;
            case "reload":
                GachaConfigProvider.load();
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private static void sendHelp(CommandSender sender){
        sender.sendMessage("---------- NekoEvent Gachaコマンドヘルプ ----------");
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha play [player] [gachaId] <ticketCost>");
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha demo [gachaId] [times]");
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha list");
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha info [gachaId]");
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha reload");
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha help");
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.");
        sender.sendMessage("---------------------------------------");
    }
}
