package jp.kentan.minecraft.neko_event.module.key;

import jp.kentan.minecraft.neko_event.config.provider.KeyConfigProvider;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class KeyCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final int params = args.length - 1;

        if(params < 0 || args[0].equals("help")){
            sendHelp(sender);
            return true;
        }

        switch (args[0]){
            case "use":
                if(NekoUtil.checkParams(params, 2)) {
                    KeyManager.use(args[1], args[2]);
                }
                break;
            case "give":
                if(NekoUtil.checkParams(params, 2)) {
                    KeyManager.give(args[1], args[2], (params >= 3) ? args[3] : null);
                }
                break;
            case "drop":
                if(NekoUtil.checkParams(params, 5)) {
                    KeyManager.drop(args[1], args[2], args[3], args[4], args[5], (params >= 6) ? args[6] : null);
                }
                break;
            case "create":
                if(NekoUtil.checkParams(params, 1) && NekoUtil.isPlayer(sender)) {
                    KeyManager.create((Player) sender, args[1], NekoUtil.getOptionMap(args));
                }
                break;
            case "delete":
                if(NekoUtil.checkParams(params, 1)){
                    KeyManager.delete(sender, args[1]);
                }
                break;
            case "overwrite":
                if(NekoUtil.checkParams(params, 1) && NekoUtil.isPlayer(sender)){
                    KeyManager.overwriteKeyItem((Player) sender, args[1]);
                }
                break;
            case "set":
                if(NekoUtil.checkParams(params, 4) && NekoUtil.isPlayer(sender)){
                    KeyManager.setOptions((Player) sender, args[1], NekoUtil.getOptionMap(args));
                }
                break;
            case "option":
                KeyManager.sendOptionHelp(sender);
                break;
            case "info":
                if(NekoUtil.checkParams(params, 1)){
                    KeyManager.sendInfo(sender, args[1]);
                }
                break;
            case "list":
                KeyManager.sendList(sender);
                break;
            case "reload":
                KeyConfigProvider.load();
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private static void sendHelp(CommandSender sender){
        sender.sendMessage("---------- NekoEvent Keyコマンドヘルプ ----------");
        sender.sendMessage("| " + ChatColor.GOLD + "/key use [player] [keyId]");
        sender.sendMessage("| " + ChatColor.GOLD + "/key give [player] [keyId] <amount>");
        sender.sendMessage("| " + ChatColor.GOLD + "/key drop [keyId] [world] [x y z] <amount>");
        sender.sendMessage("| " + ChatColor.GOLD + "/key create [keyId] <-o OPTION val>");
        sender.sendMessage("| " + ChatColor.GOLD + "/key delete [keyId]");
        sender.sendMessage("| " + ChatColor.GOLD + "/key overwrite [keyId] (キーアイテムを上書き)");
        sender.sendMessage("| " + ChatColor.GOLD + "/key set [keyId] [-o OPTION val]");
        sender.sendMessage("| " + ChatColor.GOLD + "/key option ('-o'で指定できるオプションリストを表示します)");
        sender.sendMessage("| " + ChatColor.GOLD + "/key list");
        sender.sendMessage("| " + ChatColor.GOLD + "/key reload");
        sender.sendMessage("| " + ChatColor.GOLD + "/key help");
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.");
        sender.sendMessage("| " + ChatColor.GRAY + "'-o'は複数指定が可能です.");
        sender.sendMessage("---------------------------------------");
    }
}
