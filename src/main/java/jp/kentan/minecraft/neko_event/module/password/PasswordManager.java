package jp.kentan.minecraft.neko_event.module.password;

import jp.kentan.minecraft.neko_event.NekoEvent;
import jp.kentan.minecraft.neko_event.config.listener.ConfigListener;
import jp.kentan.minecraft.neko_event.config.provider.PasswordConfigProvider;
import jp.kentan.minecraft.neko_event.module.password.model.Password;
import jp.kentan.minecraft.neko_event.module.password.model.PasswordResult;
import jp.kentan.minecraft.neko_event.util.Log;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordManager implements ConfigListener<Password>{
    private static PasswordManager sInstance = new PasswordManager();

    private static Map<String, Password> sPasswordMap = new HashMap<>();

    public static void setup(JavaPlugin plugin){
        PasswordConfigProvider.bindListener(sInstance);
        PasswordConfigProvider.load();

        plugin.getCommand("password").setExecutor(new PasswordCommandExecutor());
    }

    static void create(Player player, String id, Map<String, List<String>> optionMap){
        final Option option = Option.analyze(player, optionMap);

        if(option == null){
            return;
        }

        if(sPasswordMap.containsKey(id)){
            player.sendMessage(PASSWORD_ALREADY_REGISTERED_TEXT.replace("{id}", id));
            return;
        }

        final Password pass = PasswordConfigProvider.create(id, option.DEFAULT_PASS, option.BLOCK_MATERIAL, option.BLOCK_LOCATION,
                option.MATCH_MSG, option.NOT_MATCH_MSG, option.INPUT_MSG, option.CLEAR_MSG);

        if(pass == null){
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "作成に失敗しました.");
        }else{
            Log.info("{player}がパスワード({id})を作成しました.".replace("{player}", player.getName()).replace("{id}",id));
            sPasswordMap.put(id, pass);
        }
    }

    static void delete(CommandSender sender, String id){
        if(!sPasswordMap.containsKey(id)){
            sender.sendMessage(PASSWORD_NOT_FOUND_TEXT.replace("{id}", id));
            return;
        }

        if(PasswordConfigProvider.delete(id)){
            sPasswordMap.remove(id);
            Log.warn("{sender}がパスワード({id})を消去しました.".replace("{sender}", sender.getName()).replace("{id}",id));
        }else{
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "消去に失敗しました.");
        }
    }

    static void setOptions(Player player, String id, Map<String, List<String>> optionMap){
        final Password pass = sPasswordMap.get(id);

        if(pass == null){
            Log.error(PASSWORD_NOT_FOUND_TEXT.replace("{id}", id));
            return;
        }

        final Option option = Option.analyze(player, optionMap);

        if(option == null){
            return;
        }

        if(pass.update(option.DEFAULT_PASS, option.BLOCK_MATERIAL, option.BLOCK_LOCATION, option.MATCH_MSG, option.NOT_MATCH_MSG, option.INPUT_MSG, option.CLEAR_MSG)){
            Log.info(player.getName() + "がパスワード({id})を更新しました.".replace("{id}", id));
        }else{
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "パスワード({id})の更新に失敗しました.".replace("{id}", id));
        }
    }

    private static void input(Player player, String id, String text){
        final Password pass = sPasswordMap.get(id);

        if(pass == null){
            Log.error("パスワード({id})が存在しません.".replace("{id}", id));
            return;
        }

        final PasswordResult result = pass.input(text);

        if(pass.hasInputMessage()){
            player.sendMessage(pass.getInputMessage());
        }

        switch (result){
            case MATCH:
                if(pass.hasBlockOption()){
                    pass.getBlockLocation().getBlock().setType(pass.getBlockMaterial());
                }

                if(pass.hasMatchMessage()){
                    player.sendMessage(pass.getMatchMessage());
                }
                break;
            case NOT_MATCH:
                if(pass.hasNotMatchMessage()){
                    player.sendMessage(pass.getNotMatchMessage());
                }
                break;
            default:
                break;
        }
    }

    static void input(String playerName, String id, String text){
        Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        input(player, id, text);
    }

    private static void clearBuffer(Player player, String id){
        final Password pass = sPasswordMap.get(id);

        if(pass == null){
            Log.error("パスワード({id})が存在しません.".replace("{id}", id));
            return;
        }

        pass.clearBuffer();

        if(pass.hasClearMessage()){
            player.sendMessage(pass.getClearMessage());
        }
    }

    static void clearBuffer(String playerName, String id){
        Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        clearBuffer(player, id);
    }

    static void updatePassword(String id, String password){
        final Password pass = sPasswordMap.get(id);

        if(pass == null){
            Log.error("パスワード({id})が存在しません.".replace("{id}", id));
            return;
        }

        pass.updatePassword(password);
        Log.info("パスワード(" + id +")を'" + password + "'に設定しました.");
    }

    static void sendList(CommandSender sender){
        StringBuilder builder = new StringBuilder(ID_LIST_TEXT + "\n");

        sPasswordMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                            builder.append(" ");
                            builder.append(e.getKey());
                            builder.append(": ");
                            builder.append(e.getValue().getPassword());
                            builder.append('\n');
                            builder.append(ChatColor.RESET);
                        }
                );

        sender.sendMessage(builder.toString());
    }

    static void sendInfo(CommandSender sender, String id){
        final Password pass = sPasswordMap.get(id);

        if(pass == null){
            sender.sendMessage(PASSWORD_NOT_FOUND_TEXT.replace("{id}", id));
            return;
        }

        sender.sendMessage(INFO_TEXT);
        sender.sendMessage(" ID: " + id);
        sender.sendMessage(" ﾊﾟｽﾜｰﾄﾞ: " + pass.getPassword());
        sender.sendMessage(" 入力ﾊﾞｯﾌｧ: " + pass.getInputBuffer());
        if(pass.hasBlockOption()){
            sender.sendMessage(" 設置ﾌﾞﾛｯｸ: " + pass.getBlockMaterial());
            sender.sendMessage(" 設置位置: " + NekoUtil.toString(pass.getBlockLocation()));
        }
    }

    static void sendOptionHelp(CommandSender sender){
        sender.sendMessage("---------- NekoEvent Passwordオプションヘルプ ----------");
        sender.sendMessage("| " + ChatColor.AQUA + "DEFAULT [pass]                 (デフォルトのパスワード)");
        sender.sendMessage("| " + ChatColor.AQUA + "BLOCK_MATERIAL [material name] (解除時に設置するブロック)");
        sender.sendMessage("| " + ChatColor.AQUA + "BLOCK_LOCATION [HERE/x y z]    (解除時に設置する場所)");
        sender.sendMessage("| " + ChatColor.AQUA + "MATCH_MSG [message]            (一致時に表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "NOT_MATCH_MSG [message]        (不一致時に表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "INPUT_MSG [message]            (入力時に表示, {buff}は入力ﾊﾞｯﾌｧで置換)");
        sender.sendMessage("| " + ChatColor.AQUA + "CLEAR_MSG [message]            (入力リセット時に表示)");
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.");
        sender.sendMessage("| " + ChatColor.GRAY + "[message]では{name}でキー名に置換されます.");
        sender.sendMessage("---------------------------------------");
    }

    @Override
    public void onUpdate(Map<String, Password> dataMap) {
        sPasswordMap.clear();
        sPasswordMap.putAll(dataMap);
    }

    private final static String PASSWORD_NOT_FOUND_TEXT = NekoEvent.PREFIX + ChatColor.YELLOW + "パスワード({id})は存在しません.";
    private final static String PASSWORD_ALREADY_REGISTERED_TEXT = NekoEvent.PREFIX + ChatColor.YELLOW + "このID({id})はすでに登録されています.";

    private final static String ID_LIST_TEXT = ChatColor.translateAlternateColorCodes('&', "&7--------- &9パスワード一覧 &7---------&r");
    private final static String INFO_TEXT = ChatColor.translateAlternateColorCodes('&', "&7--------- &9パスワード情報 &7---------");


    static class Option {
        final static String DEFAULT_KEY = "DEFAULT";
        final static String BLOCK_MATERIAL_KEY = "BLOCK_MATERIAL";
        final static String BLOCK_LOCATION_KEY = "BLOCK_LOCATION";
        final static String MATCH_MSG_KEY = "MATCH_MSG";
        final static String NOT_MATCH_MSG_KEY = "NOT_MATCH_MSG";
        final static String INPUT_MSG_KEY = "INPUT_MSG";
        final static String CLEAR_MSG_KEY = "CLEAR_MSG";

        private String DEFAULT_PASS;
        private Material BLOCK_MATERIAL;
        private Location BLOCK_LOCATION;
        private String MATCH_MSG, NOT_MATCH_MSG, INPUT_MSG, CLEAR_MSG;


        static Option analyze(Player player, Map<String, List<String>> optionMap) {
            Option option = null;

            try {
                option = new Option(player, optionMap);
            } catch (Exception e) {
                e.printStackTrace();
                Log.error(e.getMessage());
            }

            return option;
        }

        private Option(Player player, Map<String, List<String>> map) throws Exception {
            MATCH_MSG     = NekoUtil.appendStrings(map.remove(MATCH_MSG_KEY));
            NOT_MATCH_MSG = NekoUtil.appendStrings(map.remove(NOT_MATCH_MSG_KEY));
            INPUT_MSG     = NekoUtil.appendStrings(map.remove(INPUT_MSG_KEY));
            CLEAR_MSG     = NekoUtil.appendStrings(map.remove(CLEAR_MSG_KEY));


            if(map.containsKey(DEFAULT_KEY)){
                DEFAULT_PASS = map.remove(DEFAULT_KEY).get(0);
            }


            List<String> location = new ArrayList<>();
            if(map.containsKey(BLOCK_LOCATION_KEY)) {
                location.addAll(map.remove(BLOCK_LOCATION_KEY));

                if (location.size() > 0) {
                    if (location.get(0).equals("HERE")) {
                        BLOCK_LOCATION = player.getLocation();
                    } else if (NekoUtil.checkParams(location.size(), 3, BLOCK_LOCATION_KEY)) {
                        BLOCK_LOCATION = NekoUtil.toLocation(
                                player.getLocation(),
                                new String[]{
                                        location.get(0),
                                        location.get(1),
                                        location.get(2)
                                }
                        );
                    }
                }
            }

            if(map.containsKey(BLOCK_MATERIAL_KEY)){
                String materialName = map.remove(BLOCK_MATERIAL_KEY).get(0);
                BLOCK_MATERIAL = Material.matchMaterial(materialName);

                if(BLOCK_MATERIAL == null){
                    Log.error("マテリアル(" + materialName + ")は存在しません.");
                    throw new Exception("Invalid material name.");
                }

                if(!BLOCK_MATERIAL.isBlock()){
                    Log.error("マテリアル(" + materialName + ")はブロックではありません.");
                    throw new Exception("Invalid material name.");
                }
            }

            if (map.size() > 0) {
                map.forEach((key, val) -> Log.error("オプション({key})は存在しません.".replace("{key}", key)));
                throw new Exception("Invalid option.");
            }
        }
    }
}
