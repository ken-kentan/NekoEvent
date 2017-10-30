package jp.kentan.minecraft.neko_event.module.key;

import jp.kentan.minecraft.neko_event.NekoEvent;
import jp.kentan.minecraft.neko_event.config.listener.ConfigListener;
import jp.kentan.minecraft.neko_event.config.provider.KeyConfigProvider;
import jp.kentan.minecraft.neko_event.module.key.model.Key;
import jp.kentan.minecraft.neko_event.util.Log;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyManager implements ConfigListener<Key>{
    private static KeyManager sInstance = new KeyManager();

    private static Map<String, Key> sKeyMap = new HashMap<>();

    public static void setup(JavaPlugin plugin){
        KeyConfigProvider.bindListener(sInstance);
        KeyConfigProvider.load();

        plugin.getCommand("key").setExecutor(new KeyCommandExecutor());
    }

    static void create(Player player, String id, Map<String, List<String>> optionMap){
        final ItemStack itemStack = player.getInventory().getItemInMainHand();

        if(itemStack == null || itemStack.getType() == Material.AIR){
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "登録するアイテムを持って下さい.");
            return;
        }

        final Option option = Option.analyze(player, optionMap);

        if(option == null){
            return;
        }

        if(sKeyMap.containsKey(id)){
            player.sendMessage(KEY_ALREADY_REGISTERED_TEXT.replace("{id}", id));
            return;
        }

        final Key key = KeyConfigProvider.create(id, new ItemStack(itemStack), (option.IS_TAKE != null) ? option.IS_TAKE : false,
                option.PERIOD, option.BLOCK_MATERIAL, option.BLOCK_LOCATION,
                option.MATCH_MSG, option.NOT_MATCH_MSG, option.EXPIRED_MSG, option.SHORT_AMOUNT_MSG);

        if(key == null){
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "作成に失敗しました.");
        }else{
            Log.info("{player}がキー({id})を{item}で作成しました.".replace("{player}", player.getName()).replace("{id}",id).replace("{name}", key.getName()));
            sKeyMap.put(id, key);
        }
    }

    static void delete(CommandSender sender, String id){
        if(!sKeyMap.containsKey(id)){
            sender.sendMessage(KEY_NOT_FOUND_TEXT.replace("{id}", id));
            return;
        }

        if(KeyConfigProvider.delete(id)){
            sKeyMap.remove(id);
            Log.warn("{sender}がキー({id})を消去しました.".replace("{sender}", sender.getName()).replace("{id}",id));
        }else{
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "消去に失敗しました.");
        }
    }

    static void setOptions(Player player, String id, Map<String, List<String>> optionMap){
        final Key key = sKeyMap.get(id);

        if(key == null){
            Log.error(KEY_NOT_FOUND_TEXT.replace("{id}", id));
            return;
        }

        final Option option = Option.analyze(player, optionMap);

        if(option == null){
            return;
        }

        if(key.update(null, option.IS_TAKE, option.PERIOD, option.BLOCK_MATERIAL, option.BLOCK_LOCATION,
                option.MATCH_MSG, option.NOT_MATCH_MSG, option.EXPIRED_MSG, option.SHORT_AMOUNT_MSG)){
            Log.info(player.getName() + "がキー({id})を更新しました.".replace("{id}", id));
        }else{
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "キー({id})の更新に失敗しました.".replace("{id}", id));
        }
    }

    static void overwriteKeyItem(Player player, String id){
        final Key key = sKeyMap.get(id);

        if(key == null){
            Log.error(KEY_NOT_FOUND_TEXT.replace("{id}", id));
            return;
        }

        final ItemStack itemStack = player.getInventory().getItemInMainHand();

        if(itemStack == null || itemStack.getType() == Material.AIR){
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "登録するアイテムを持って下さい.");
            return;
        }

        if(key.update(new ItemStack(itemStack), null, 0, null, null,
                null, null, null, null)){
            Log.info(player.getName() + "がキー({id})のアイテムを{name}で上書きしました.".replace("{id}", id).replace("{name}", key.getName()));
        }else{
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "キー({id})の更新に失敗しました.".replace("{id}", id));
        }
    }

    private static void give(Player player, String id, int amount){
        if(!sKeyMap.containsKey(id)){
            Log.error("キー({id})は存在しません.".replace("{id}",id));
            return;
        }

        player.getInventory().addItem(sKeyMap.get(id).getItemStack(amount));
    }

    static void give(String playerName, String id, String strAmount){
        Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        int amount = 1;

        if(strAmount != null) {
            amount = NekoUtil.toInteger(strAmount);

            if (amount < 1) {
                player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "個数は1以上に設定して下さい.");
                return;
            }
        }

        give(player, id, amount);
    }

    private static void drop(Location location, String id, int amount){
        if(!sKeyMap.containsKey(id)){
            Log.error("キー({id})は存在しません.".replace("{id}",id));
            return;
        }

        location.getWorld().dropItem(location, sKeyMap.get(id).getItemStack(amount));
    }

    static void drop(String id, String worldName, String strX, String strY, String strZ, String strAmount){
        Location location = NekoUtil.toLocation(worldName, strX, strY, strZ);

        if(location == null){
            return;
        }

        int amount = 1;

        if(strAmount != null) {
            amount = NekoUtil.toInteger(strAmount);

            if (amount < 1) {
                Log.error(NekoEvent.PREFIX + ChatColor.YELLOW + "個数は1以上に設定して下さい.");
                return;
            }
        }

        drop(location, id, amount);
    }


    private static void use(Player player, String id){
        Key key = sKeyMap.get(id);

        if(key == null){
            Log.error("キー({id})は存在しません.".replace("{id}",id));
            return;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();

        switch (key.match(handItem)){
            case MATCH:
                if(key.isTake()){
                    int amount = handItem.getAmount() - key.getAmount();
                    player.getInventory().getItemInMainHand().setAmount(amount);
                }

                if(key.hasBlockOption()){
                    key.getBlockLocation().getBlock().setType(key.getBlockMaterial());
                }

                if(key.hasMatchMessage()){
                    player.sendMessage(key.getMatchMessage());
                }
                break;
            case NOT_MATCH:
                if(key.hasNotMatchMessage()){
                    player.sendMessage(key.getNotMatchMessage());
                }
                break;
            case EXPIRED:
                if(key.hasExpiredMessage()){
                    player.sendMessage(key.getExpiredMessage());
                }
                break;
            case SHORT_AMOUNT:
                if(key.hasShortAmountMessage()){
                    player.sendMessage(key.getShortAmountMessage());
                }
                break;
            default:
        }
    }

    static void use(String playerName, String id){
        Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        use(player, id);
    }

    static void sendList(CommandSender sender){
        StringBuilder builder = new StringBuilder(ID_LIST_TEXT + "\n");

        sKeyMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                            builder.append(" ");
                            builder.append(e.getKey());
                            builder.append(": ");
                            builder.append(e.getValue().getName());
                            builder.append('\n');
                            builder.append(ChatColor.RESET);
                        }
                );

        sender.sendMessage(builder.toString());
    }

    static void sendInfo(CommandSender sender, String id){
        final Key key = sKeyMap.get(id);

        if(key == null){
            sender.sendMessage(KEY_NOT_FOUND_TEXT.replace("{id}",id));
            return;
        }

        sender.sendMessage(INFO_TEXT);
        sender.sendMessage(" ID: " + id);
        sender.sendMessage(" 名前: " + key.getName());
        if(key.hasBlockOption()){
            sender.sendMessage(" 設置ﾌﾞﾛｯｸ: " + key.getBlockMaterial());
            sender.sendMessage(" 設置位置: " + NekoUtil.toString(key.getBlockLocation()));
        }
    }

    static void sendOptionHelp(CommandSender sender){
        sender.sendMessage("---------- NekoEvent Keyオプションヘルプ ----------");
        sender.sendMessage("| " + ChatColor.AQUA + "TAKE [true/false]               (解除時にキーを消す)");
        sender.sendMessage("| " + ChatColor.AQUA + "PERIOD [minutes]                (キーの有効期限)");
        sender.sendMessage("| " + ChatColor.AQUA + "BLOCK_MATERIAL [material name]  (解除時に設置するブロック)");
        sender.sendMessage("| " + ChatColor.AQUA + "BLOCK_LOCATION [HERE/x y z]     (解除時に設置する場所)");
        sender.sendMessage("| " + ChatColor.AQUA + "MATCH_MSG [message]           (一致時に表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "NOT_MATCH_MSG [message]       (不一致時に表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "EXPIRED_MSG [message]           (期限切れ時に表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "SHORT_AMOUNT_MSG [message]      (数が足りない時に表示)");
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.");
        sender.sendMessage("| " + ChatColor.GRAY + "[message]では{name}でキー名に置換されます.");
        sender.sendMessage("---------------------------------------");
    }

    @Override
    public void onUpdate(Map<String, Key> dataMap) {
        sKeyMap.clear();
        sKeyMap.putAll(dataMap);
    }

    private final static String KEY_NOT_FOUND_TEXT = NekoEvent.PREFIX + ChatColor.YELLOW + "キー({id})は存在しません.";
    private final static String KEY_ALREADY_REGISTERED_TEXT = NekoEvent.PREFIX + ChatColor.YELLOW + "このID({id})はすでに登録されています.";

    private final static String ID_LIST_TEXT = ChatColor.translateAlternateColorCodes('&', "&7--------- &6キー一覧 &7---------&r");
    private final static String INFO_TEXT = ChatColor.translateAlternateColorCodes('&', "&7--------- &6キー情報 &7---------");


    static class Option {
        final static String TAKE_KEY = "TAKE";
        final static String PERIOD_KEY = "PERIOD";
        final static String BLOCK_MATERIAL_KEY = "BLOCK_MATERIAL";
        final static String BLOCK_LOCATION_KEY = "BLOCK_LOCATION";
        final static String MATCH_MSG_KEY = "MATCH_MSG";
        final static String NOT_MATCH_MSG_KEY = "NOT_MATCH_MSG";
        final static String EXPIRED_MSG_KEY = "EXPIRED_MSG";
        final static String SHORT_AMOUNT_MSG_KEY = "SHORT_AMOUNT_MSG";

        private Boolean IS_TAKE;
        private int PERIOD = 0;
        private Material BLOCK_MATERIAL;
        private Location BLOCK_LOCATION;
        private String MATCH_MSG, NOT_MATCH_MSG, EXPIRED_MSG, SHORT_AMOUNT_MSG;


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
            MATCH_MSG      = NekoUtil.appendStrings(map.remove(MATCH_MSG_KEY));
            NOT_MATCH_MSG  = NekoUtil.appendStrings(map.remove(NOT_MATCH_MSG_KEY));
            EXPIRED_MSG      = NekoUtil.appendStrings(map.remove(EXPIRED_MSG_KEY));
            SHORT_AMOUNT_MSG = NekoUtil.appendStrings(map.remove(SHORT_AMOUNT_MSG_KEY));


            if(map.containsKey(TAKE_KEY)){
                IS_TAKE = map.remove(TAKE_KEY).get(0).equals("true");
            }

            if(map.containsKey(PERIOD_KEY)){
                PERIOD = NekoUtil.toInteger(map.remove(PERIOD_KEY).get(0));
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
