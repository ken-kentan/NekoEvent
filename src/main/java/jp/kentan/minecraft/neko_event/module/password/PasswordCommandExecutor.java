package jp.kentan.minecraft.neko_event.module.password;

import jp.kentan.minecraft.neko_event.config.provider.PasswordConfigProvider;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class PasswordCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final int params = args.length - 1;

        if(params < 0 || args[0].equals("help")){
            sendHelp(sender);
            return true;
        }

        switch (args[0]){
            case "input":
                if(NekoUtil.checkParams(params, 3)) {
                    PasswordManager.input(args[1], args[2], args[3]);
                }
                break;
            case "clear":
                if(NekoUtil.checkParams(params, 2)) {
                    PasswordManager.clearBuffer(args[1], args[2]);
                }
                break;
            case "update":
                if(NekoUtil.checkParams(params, 2)) {
                    PasswordManager.updatePassword(args[1], args[2]);
                }
                break;
            case "create":
                if(NekoUtil.checkParams(params, 1) && NekoUtil.isPlayer(sender)) {
                    PasswordManager.create((Player) sender, args[1], NekoUtil.getOptionMap(args));
                }
                break;
            case "delete":
                if(NekoUtil.checkParams(params, 1)){
                    PasswordManager.delete(sender, args[1]);
                }
                break;
            case "set":
                if(NekoUtil.checkParams(params, 4) && NekoUtil.isPlayer(sender)){
                    PasswordManager.setOptions((Player) sender, args[1], NekoUtil.getOptionMap(args));
                }
                break;
            case "option":
                PasswordManager.sendOptionHelp(sender);
                break;
            case "info":
                if(NekoUtil.checkParams(params, 1)){
                    PasswordManager.sendInfo(sender, args[1]);
                }
                break;
            case "list":
                PasswordManager.sendList(sender);
                break;
            case "reload":
                PasswordConfigProvider.load();
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private static void sendHelp(CommandSender sender){
        sender.sendMessage("---------- NekoEvent Passwordコマンドヘルプ ----------");
        sender.sendMessage("| " + ChatColor.BLUE + "/password input [player] [passId] [text]");
        sender.sendMessage("| " + ChatColor.BLUE + "/password clear [player] [passId] (入力ﾊﾞｯﾌｧをクリア)");
        sender.sendMessage("| " + ChatColor.BLUE + "/password update [passId] [pass]");
        sender.sendMessage("| " + ChatColor.BLUE + "/password create [passId] <-o OPTION val>");
        sender.sendMessage("| " + ChatColor.BLUE + "/password delete [passId]");
        sender.sendMessage("| " + ChatColor.BLUE + "/password set [passId] [-o OPTION val]");
        sender.sendMessage("| " + ChatColor.BLUE + "/password option ('-o'で指定できるオプションリストを表示します)");
        sender.sendMessage("| " + ChatColor.BLUE + "/password info [passId]");
        sender.sendMessage("| " + ChatColor.BLUE + "/password list");
        sender.sendMessage("| " + ChatColor.BLUE + "/password reload");
        sender.sendMessage("| " + ChatColor.BLUE + "/password help");
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.");
        sender.sendMessage("| " + ChatColor.GRAY + "'-o'は複数指定が可能です.");
        sender.sendMessage("---------------------------------------");
    }
}
