package jp.kentan.minecraft.neko_event.game.parkour;

import jp.kentan.minecraft.neko_event.NekoEvent;
import jp.kentan.minecraft.neko_event.config.listener.ConfigListener;
import jp.kentan.minecraft.neko_event.config.provider.ParkourConfigProvider;
import jp.kentan.minecraft.neko_event.config.provider.SignConfigProvider;
import jp.kentan.minecraft.neko_event.listener.SignEventListener;
import jp.kentan.minecraft.neko_event.listener.SignListener;
import jp.kentan.minecraft.neko_event.game.parkour.model.Parkour;
import jp.kentan.minecraft.neko_event.spawn.SpawnManager;
import jp.kentan.minecraft.neko_event.ticket.EventTicketProvider;
import jp.kentan.minecraft.neko_event.util.Log;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkourManager implements SignListener, ConfigListener<Parkour> {

    private static ParkourManager sInstance = new ParkourManager();

    private static Map<String, Parkour> sParkourMap = new HashMap<>();

    public static void setup(JavaPlugin plugin){
        ParkourConfigProvider.bindListener(sInstance);
        ParkourConfigProvider.load();

        SignEventListener.bindParkourSignListener(sInstance);

        plugin.getCommand("parkour").setExecutor(new ParkourCommandExecutor());
    }

    static void create(Player player, String id, String name, Map<String, List<String>> optionMap){
        final Option option = Option.analyze(player, optionMap);

        if(option == null){
            return;
        }

        final Parkour parkour = ParkourConfigProvider.create(id, name, option.mJoinLocation, option.mClearLocation, option.mBackLocation,
                option.mJoinPlayerMsg, option.mJoinBroadcastMsg, option.mClearPlayerMsg, option.mClearBroadcastMsg, option.mClearTitleText,
                option.mBackPlayerMsg);

        if(parkour == null){
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "作成に失敗しました.");
        }else{
            sParkourMap.put(id, parkour);
            Log.info("{player}がパルクール({id})を作成しました.".replace("{player}", player.getName()).replace("{id}",id));
        }
    }

    static void delete(CommandSender sender, String id){
        if(!sParkourMap.containsKey(id)){
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "パルクール({id})は存在しません.".replace("{id}", id));
            return;
        }

        if(ParkourConfigProvider.delete(id)){
            sParkourMap.remove(id);
            Log.warn("{sender}がパルクール({id})を消去しました.".replace("{sender}", sender.getName()).replace("{id}",id));
        }else{
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "消去に失敗しました.");
        }
    }

    private static void join(Player player, String id){
        final Parkour parkour = sParkourMap.get(id);

        if(parkour == null){
            Log.error(PARKOUR_NOT_FOUND_TEXT.replace("{id}", id));
            return;
        }

        Location location = null;

        if(parkour.hasJoinLocation()){
            location = parkour.getJoinLocation();
            player.teleport(location);
        }

        if(parkour.hasJoinPlayerMessage()){
            player.sendMessage(NekoEvent.PREFIX + parkour.getJoinPlayerMessage().replace("{player}", player.getName()));
        }

        if(parkour.hasJoinBroadcastMessage()){
            NekoUtil.broadcastMessage(NekoEvent.PREFIX + parkour.getJoinBroadcastMessage().replace("{player}", player.getName()), player);
        }

        if(location != null) {
            SpawnManager.setSpawn(player, location);
        }

        Log.info(player.getName() + "がパルクール({id})に参加しました.".replace("{id}", id));
    }

    static void join(String playerName, String id){
        Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        join(player, id);
    }

    private static void clear(Player player, String id){
        final Parkour parkour = sParkourMap.get(id);

        if(parkour == null){
            Log.error(PARKOUR_NOT_FOUND_TEXT.replace("{id}", id));
            return;
        }

        if(parkour.hasClearLocation()){
            Location location = parkour.getClearLocation();

            player.teleport(location);
            SpawnManager.setSpawn(player, location, false);
        }

        if(parkour.hasClearTitleText()){
            player.sendTitle(parkour.getClearTitleText(), "", 10, 70, 20);
        }

        if(parkour.hasClearPlayerMessage()){
            player.sendMessage(NekoEvent.PREFIX + parkour.getClearPlayerMessage().replace("{player}", player.getName()));
        }

        if(parkour.hasClearBroadcastMessage()){
            NekoUtil.broadcastMessage(NekoEvent.PREFIX + parkour.getClearBroadcastMessage().replace("{player}", player.getName()), player);
        }

        if(ParkourConfigProvider.hasClearedToday(player.getUniqueId(), id)){
            player.sendMessage(EVENT_TICKET_ONCE_A_DAY_MSG);
        }else {
            if(EventTicketProvider.give(player, 1)){
                player.sendMessage(EVENT_TICKET_GET_MSG);
            }
        }

        ParkourConfigProvider.saveClearDate(player.getUniqueId(), id);

        Log.info(player.getName() + "がパルクール({id})をクリアしました.".replace("{id}", id));
    }

    static void clear(String playerName, String id){
        Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        clear(player, id);
    }

    private static void back(Player player, String id){
        final Parkour parkour = sParkourMap.get(id);

        if(parkour == null){
            Log.error(PARKOUR_NOT_FOUND_TEXT.replace("{id}", id));
            return;
        }

        if(parkour.hasBackLocation()){
            Location location = parkour.getBackLocation();

            player.teleport(location);
            SpawnManager.setSpawn(player, location, false);
        }

        if(parkour.hasBackPlayerMessage()){
            player.sendMessage(NekoEvent.PREFIX + parkour.getBackPlayerMessage().replace("{player}", player.getName()));
        }


        Log.info(player.getName() + "がパルクール({id})を退出しました.".replace("{id}", id));
    }

    static void back(String playerName, String id){
        Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        back(player, id);
    }

    static void setOptions(Player player, String id, Map<String, List<String>> optionMap){
        final Parkour parkour = sParkourMap.get(id);

        if(parkour == null){
            Log.error(PARKOUR_NOT_FOUND_TEXT.replace("{id}", id));
            return;
        }

        final Option option = Option.analyze(player, optionMap);

        if(option == null){
            return;
        }

        if(parkour.update(option.mJoinLocation, option.mClearLocation, option.mBackLocation, option.mJoinPlayerMsg, option.mJoinBroadcastMsg,
                option.mClearPlayerMsg, option.mClearBroadcastMsg, option.mClearTitleText, option.mBackPlayerMsg)){
            Log.info(player.getName() + "がパルクール({id})を更新しました.".replace("{id}", id));
        }else{
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "パルクール({id})の更新に失敗しました.".replace("{id}", id));
        }
    }

    @Override
    public void onUpdate(Map<String, Parkour> parkourMap) {
        sParkourMap.clear();
        sParkourMap.putAll(parkourMap);
    }

    @Override
    public void onSignChanged(SignChangeEvent event) {
        final Player player = event.getPlayer();
        final String parkourId = event.getLine(2);
        final String parkourAction = event.getLine(3);

        final Parkour parkour = sParkourMap.get(parkourId);

        if(parkour == null){
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + PARKOUR_NOT_FOUND_TEXT.replace("{id}", parkourId));
            return;
        }

        if(!parkourAction.equals("join") && !parkourAction.equals("clear") && !parkourAction.equals("back")){
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + parkourAction + "は適切なアクションではありません.");
            return;
        }

        final Sign sign = (Sign)event.getBlock().getState();

        //看板のメタデータにパルクール情報を保存
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put(PARKOUR_ID_METADATA_KEY, parkourId);
        metadataMap.put(PARKOUR_ACTION_METADATA_KEY, parkourAction);

        if(!SignConfigProvider.saveMetadata(sign.getLocation(), metadataMap)){
            Log.error("看板ﾒﾀﾃﾞｰﾀの保存に失敗しました.");
            return;
        }

        event.setLine(0, PARKOUR_SIGN_INDEX);
        event.setLine(1, parkour.getName());
        event.setLine(2, "");

        switch (parkourAction){
            case "clear":
                event.setLine(3, PARKOUR_SIGN_CLEAR);
                break;
            case "back":
                event.setLine(3, PARKOUR_SIGN_BACK);
                break;
            default:
                event.setLine(3, PARKOUR_SIGN_JOIN);
                break;
        }
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event, Sign sign) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || !hasParkourMetadata(sign)){
            return;
        }

        final String parkourId = (String) SignConfigProvider.getMetadata(sign.getLocation(), PARKOUR_ID_METADATA_KEY);
        final String parkourAction = (String) SignConfigProvider.getMetadata(sign.getLocation(), PARKOUR_ACTION_METADATA_KEY);

        if(!sParkourMap.containsKey(parkourId)){
            Log.error(PARKOUR_NOT_FOUND_TEXT.replace("{id}", parkourId));
            return;
        }

        switch (parkourAction){
            case "clear":
                clear(event.getPlayer(), parkourId);
                break;
            case "back":
                back(event.getPlayer(), parkourId);
                break;
            default:
                join(event.getPlayer(), parkourId);
                break;
        }
    }

    static void sendList(CommandSender sender){
        StringBuilder builder = new StringBuilder(PARKOUR_ID_LIST_TEXT + "\n");

        sParkourMap.entrySet().stream()
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

    static void sendOptionHelp(CommandSender sender){
        sender.sendMessage("---------- NekoEvent Parkourオプションヘルプ ----------");
        sender.sendMessage("| " + ChatColor.AQUA + "JOIN_LOCATION [HERE/x y z]    (参加時のスポーンを設定)");
        sender.sendMessage("| " + ChatColor.AQUA + "CLEAR_LOCATION [HERE/x y z]   (クリア時のスポーンを設定)");
        sender.sendMessage("| " + ChatColor.AQUA + "BACK_LOCATION [HERE/x y z]    (戻る時のスポーンを設定)");
        sender.sendMessage("| " + ChatColor.AQUA + "JOIN_PLAYER_MSG [message]     (参加時にﾌﾟﾚｲﾔｰに表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "JOIN_BROADCAST_MSG [message]  (参加時にﾌﾟﾚｲﾔｰ以外の全員に表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "CLEAR_PLAYER_MSG [message]    (クリア時にﾌﾟﾚｲﾔｰ自身に表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "CLEAR_BROADCAST_MSG [message] (クリア時にﾌﾟﾚｲﾔｰ以外の全員に表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "CLEAR_TITLE_TEXT [title]      (クリア時にﾌﾟﾚｲﾔｰにタイトルを表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "BACK_PLAYER_MSG [message]     (戻る時にﾌﾟﾚｲﾔｰに表示)");
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.");
        sender.sendMessage("| " + ChatColor.GRAY + "[message]では{name}でﾊﾟﾙｸｰﾙ名,{player}でﾌﾟﾚｲﾔｰ名に置換されます.");
        sender.sendMessage("---------------------------------------");
    }


    private static boolean hasParkourMetadata(Sign sign){
        if(!SignConfigProvider.hasMetadata(sign.getLocation(), PARKOUR_ID_METADATA_KEY)){
            Log.error("看板" + NekoUtil.toString(sign.getLocation()) + "にﾒﾀﾃﾞｰﾀ(parkourId)が不足しています.");
            return false;
        }
        if(!SignConfigProvider.hasMetadata(sign.getLocation(), PARKOUR_ACTION_METADATA_KEY)){
            Log.error("看板" + NekoUtil.toString(sign.getLocation()) + "にﾒﾀﾃﾞｰﾀ(parkourAction)が不足しています.");
            return false;
        }

        return true;
    }


    private final static String PARKOUR_NOT_FOUND_TEXT = "パルクール({id})が存在しません.";
    private final static String EVENT_TICKET_GET_MSG =
            NekoEvent.PREFIX + ChatColor.translateAlternateColorCodes('&',"&6&lイベントチケット&c報酬&rを&bゲット&r！");
    private final static String EVENT_TICKET_ONCE_A_DAY_MSG =
            NekoEvent.PREFIX + ChatColor.translateAlternateColorCodes('&',"&7各アスレの&6&lイベントチケット&7報酬は,&c1日1枚&7です.");

    public final static String PARKOUR_SIGN_INDEX = ChatColor.translateAlternateColorCodes('&', "&8&l[&a&lアスレ&8&l]");
    private final static String PARKOUR_SIGN_JOIN = ChatColor.translateAlternateColorCodes('&', "&c&n参加");
    private final static String PARKOUR_SIGN_CLEAR = ChatColor.translateAlternateColorCodes('&', "&b&nクリア");
    private final static String PARKOUR_SIGN_BACK = ChatColor.translateAlternateColorCodes('&', "&1&n戻る");

    private final static String PARKOUR_ID_LIST_TEXT = ChatColor.translateAlternateColorCodes('&', "&7--------- &aパルクール一覧 &7---------&r");

    private final static String PARKOUR_ID_METADATA_KEY = "parkourId";
    private final static String PARKOUR_ACTION_METADATA_KEY = "parkourAction";


    static class Option {
        final static String OPTION_JOIN_PLAYER_MSG_KEY = "JOIN_PLAYER_MSG";
        final static String OPTION_JOIN_BROADCAST_MSG_KEY = "JOIN_BROADCAST_MSG";
        final static String OPTION_CLEAR_PLAYER_MSG_KEY = "CLEAR_PLAYER_MSG";
        final static String OPTION_CLEAR_TITLE_TEXT_KEY = "CLEAR_TITLE_TEXT";
        final static String OPTION_CLEAR_BROADCAST_MSG_KEY = "CLEAR_BROADCAST_MSG";
        final static String OPTION_BACK_PLAYER_MSG_KEY = "BACK_PLAYER_MSG";
        final static String OPTION_JOIN_LOCATION_KEY = "JOIN_LOCATION";
        final static String OPTION_CLEAR_LOCATION_KEY = "CLEAR_LOCATION";
        final static String OPTION_BACK_LOCATION_KEY = "BACK_LOCATION";

        String mJoinPlayerMsg, mJoinBroadcastMsg, mClearPlayerMsg, mClearBroadcastMsg, mClearTitleText, mBackPlayerMsg;
        Location mJoinLocation = null, mClearLocation = null, mBackLocation;

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

        private Option(Player player, Map<String, List<String>> optionMap) throws Exception {
            mJoinPlayerMsg = NekoUtil.appendStrings(optionMap.remove(OPTION_JOIN_PLAYER_MSG_KEY));
            mJoinBroadcastMsg = NekoUtil.appendStrings(optionMap.remove(OPTION_JOIN_BROADCAST_MSG_KEY));
            mClearPlayerMsg = NekoUtil.appendStrings(optionMap.remove(OPTION_CLEAR_PLAYER_MSG_KEY));
            mClearBroadcastMsg = NekoUtil.appendStrings(optionMap.remove(OPTION_CLEAR_BROADCAST_MSG_KEY));
            mClearTitleText = NekoUtil.appendStrings(optionMap.remove(OPTION_CLEAR_TITLE_TEXT_KEY));
            mBackPlayerMsg = NekoUtil.appendStrings(optionMap.remove(OPTION_BACK_PLAYER_MSG_KEY));

            List<String> location = new ArrayList<>();

            if(optionMap.containsKey(OPTION_JOIN_LOCATION_KEY)) {
                location.addAll(optionMap.remove(OPTION_JOIN_LOCATION_KEY));

                if (location.size() > 0) {
                    if (location.get(0).equals("HERE")) {
                        mJoinLocation = player.getLocation();
                    } else if (NekoUtil.checkParams(location.size(), 3, OPTION_JOIN_LOCATION_KEY)) {
                        mJoinLocation = NekoUtil.toLocation(
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

            if(optionMap.containsKey(OPTION_CLEAR_LOCATION_KEY)) {
                location.clear();
                location.addAll(optionMap.remove(OPTION_CLEAR_LOCATION_KEY));
                if (location.size() > 0) {
                    if (location.get(0).equals("HERE")) {
                        mClearLocation = player.getLocation();
                    } else if (NekoUtil.checkParams(location.size(), 3, OPTION_CLEAR_LOCATION_KEY)) {
                        mClearLocation = NekoUtil.toLocation(
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

            if(optionMap.containsKey(OPTION_BACK_LOCATION_KEY)) {
                location.clear();
                location.addAll(optionMap.remove(OPTION_BACK_LOCATION_KEY));
                if (location.size() > 0) {
                    if (location.get(0).equals("HERE")) {
                        mBackLocation = player.getLocation();
                    } else if (NekoUtil.checkParams(location.size(), 3, OPTION_BACK_LOCATION_KEY)) {
                        mBackLocation = NekoUtil.toLocation(
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

            if (optionMap.size() > 0) {
                optionMap.forEach((key, val) -> Log.error("オプション({key})は存在しません.".replace("{key}", key)));
                throw new Exception("Invalid option.");
            }
        }
    }
}
