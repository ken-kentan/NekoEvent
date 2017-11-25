package jp.kentan.minecraft.neko_event;

import jp.kentan.minecraft.neko_event.config.ConfigManager;
import jp.kentan.minecraft.neko_event.game.dungeon.DungeonManager;
import jp.kentan.minecraft.neko_event.gacha.GachaManager;
import jp.kentan.minecraft.neko_event.listener.PlayerEventListener;
import jp.kentan.minecraft.neko_event.listener.SignEventListener;
import jp.kentan.minecraft.neko_event.game.parkour.ParkourManager;
import jp.kentan.minecraft.neko_event.module.key.KeyManager;
import jp.kentan.minecraft.neko_event.module.password.PasswordManager;
import jp.kentan.minecraft.neko_event.spawn.SpawnManager;
import jp.kentan.minecraft.neko_event.teleport.TeleportProvider;
import jp.kentan.minecraft.neko_event.ticket.EventTicketProvider;
import jp.kentan.minecraft.neko_event.util.GameUtil;
import jp.kentan.minecraft.neko_event.util.Log;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class NekoEvent extends JavaPlugin {

    public final static String PREFIX = ChatColor.translateAlternateColorCodes('&', "&7[&6Neko&eEvent&7]&r ");

    private ConfigManager mConfigManager;

    @Override
    public void onEnable() {
        Log.set(getServer(), getLogger());
        GameUtil.setPlugin(this);

        mConfigManager = new ConfigManager(getDataFolder());


        EventTicketProvider.setup();
        SpawnManager.setup();
        DungeonManager.setup(this);
        ParkourManager.setup(this);
        GachaManager.setup(this);
        KeyManager.setup(this);
        PasswordManager.setup(this);

        getServer().getPluginManager().registerEvents(new SignEventListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);

        Log.info("有効化しました.");
    }

    @Override
    public void onDisable() {
        Log.info("無効化しました.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        final int params = args.length - 1;

        if(params < 0 || args[0].equals("help")){
            sendHelp(sender);
            return true;
        }

        switch (args[0]){
            case "ticket":
                if(!NekoUtil.checkParams(params, 2)){
                    return true;
                }

                if(EventTicketProvider.give(args[1], args[2])) {
                    Log.warn(sender.getName() + "が{sender}にイベントチケットを{amount}枚与えました.".replace("{sender}", args[1]).replace("{amount}", args[2]));
                }
                break;
            case "tp":
                if(NekoUtil.checkParams(params, 4)){
                    TeleportProvider.teleport(args[1], new String[]{args[2], args[3], args[4]});
                }
                break;
            case "msg":
                if(NekoUtil.checkParams(params, 3)){
                    GameUtil.sendCustomMessage(args[1], args[2], NekoUtil.appendStrings(args, 3));
                }
                break;
            case "setspawn":
                if(NekoUtil.checkParams(params, 1) && NekoUtil.checkCommandBlock(sender)){
                    SpawnManager.setSpawn((BlockCommandSender)sender, args[1], (params >= 4) ? new String[]{args[2], args[3], args[4]} : null);
                }
                break;
            case "jump":
                if(NekoUtil.checkParams(params, 3)){
                    GameUtil.jumpPlayer(args[1], NekoUtil.toDouble(args[2]), NekoUtil.toDouble(args[3]));
                }
                break;
            case "random":
                if(NekoUtil.checkParams(params, 6) && NekoUtil.checkCommandBlock(sender)){
                    GameUtil.placeBlockByRandom((BlockCommandSender)sender, args, 1);
                }
                break;
            case "delay":
                if(NekoUtil.checkParams(params, 4) && NekoUtil.checkCommandBlock(sender)){
                    GameUtil.placeBlockByDelay((BlockCommandSender)sender, NekoUtil.toInteger(args[1]), new String[]{args[2], args[3], args[4]});
                }
                break;
            case "reset_status":
                if(NekoUtil.checkParams(params, 1)){
                    GameUtil.resetPlayerStatus(args[1]);
                }
                break;
            case "test":
                ItemStack itemStack = ((Player)sender).getInventory().getItemInMainHand();
                sender.sendMessage(itemStack.getItemMeta() + "");
                break;
            case "bed":
                ((Player)sender).setBedSpawnLocation(((Player) sender).getLocation(), true);
                break;
            case "reload":
                mConfigManager.reload();
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private static void sendHelp(CommandSender sender){
        sender.sendMessage("---------- NekoEvent コマンドヘルプ ----------");
        sender.sendMessage("| " + ChatColor.YELLOW + "/event ticket [player] [amount]");
        sender.sendMessage("| " + ChatColor.YELLOW + "/event tp [player] [x y z] (相対指定時の基準はプレイヤー座標)");
        sender.sendMessage("| " + ChatColor.YELLOW + "/event msg [player] [sender] [message] ([sender]にnullを指定で[message]のみ表示)");
        sender.sendMessage("| " + ChatColor.YELLOW + "/event setspawn [player] <x y z> (相対指定時の基準はｺﾏﾝﾄﾞﾌﾞﾛｯｸ座標)");
        sender.sendMessage("| " + ChatColor.YELLOW + "/event jump [player] [height] [length] (各値は小数点係数)");
        sender.sendMessage("| " + ChatColor.YELLOW + "/event random [x y z] [x y z] <x y z> (座標は複数指定可能. 0.5s後に消滅)");
        sender.sendMessage("| " + ChatColor.YELLOW + "/event reset_status [player] (プレイヤーのステータスをリセットして体力20)");
        sender.sendMessage("| " + ChatColor.YELLOW + "/event delay [seconds] [x y z]");
        sender.sendMessage("| " + ChatColor.YELLOW + "/event reload");
        sender.sendMessage("| " + ChatColor.YELLOW + "/event help");
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/gacha");
        sender.sendMessage("| " + ChatColor.GOLD + "/key");
        sender.sendMessage("| " + ChatColor.BLUE + "/password");
        sender.sendMessage("| " + ChatColor.RED + "/dungeon");
        sender.sendMessage("| " + ChatColor.GREEN + "/parkour");
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.");
        sender.sendMessage("| " + ChatColor.GRAY + "装飾記号は'&'を使用してください.");
        sender.sendMessage("---------------------------------------");
    }
}
