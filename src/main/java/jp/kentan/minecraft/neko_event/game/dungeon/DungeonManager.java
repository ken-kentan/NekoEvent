package jp.kentan.minecraft.neko_event.game.dungeon;

import jp.kentan.minecraft.neko_event.NekoEvent;
import jp.kentan.minecraft.neko_event.config.listener.ConfigListener;
import jp.kentan.minecraft.neko_event.config.provider.DungeonConfigProvider;
import jp.kentan.minecraft.neko_event.config.provider.SignConfigProvider;
import jp.kentan.minecraft.neko_event.game.dungeon.model.Dungeon;
import jp.kentan.minecraft.neko_event.gacha.GachaManager;
import jp.kentan.minecraft.neko_event.listener.SignEventListener;
import jp.kentan.minecraft.neko_event.listener.SignListener;
import jp.kentan.minecraft.neko_event.spawn.SpawnManager;
import jp.kentan.minecraft.neko_event.ticket.TicketProvider;
import jp.kentan.minecraft.neko_event.util.Log;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jp.kentan.minecraft.neko_event.gacha.GachaManager.GACHA_ID_NOT_FOUND;

public class DungeonManager implements SignListener, ConfigListener<Dungeon> {

    private static Plugin sPlugin;
    private final static BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    private static DungeonManager sInstance = new DungeonManager();

    private static Map<String, Dungeon> sDungeonMap = new HashMap<>();

    public static void setup(JavaPlugin plugin){
        sPlugin = plugin;

        DungeonConfigProvider.bindListener(sInstance);
        DungeonConfigProvider.load();

        SignEventListener.bindDungeonSignListener(sInstance);

        plugin.getCommand("dungeon").setExecutor(new DungeonCommandExecutor());
    }

    static void create(Player player, String id, String name, Map<String, List<String>> optionMap){
        final Option option = Option.analyze(player, optionMap);

        if(option == null){
            return;
        }

        final Dungeon dungeon = DungeonConfigProvider.create(id, name, option.mJoinLocation, option.mClearLocation,
                option.mJoinPlayerMsg, option.mJoinBroadcastMsg, option.mClearPlayerMsg, option.mClearBroadcastMsg, option.mClearTitleText,
                option.mEnableClearSound, option.mRewordTicketAmount, option.mRewordGachaId);

        if(dungeon == null){
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "作成に失敗しました.");
        }else{
            sDungeonMap.put(id, dungeon);
            Log.info("{player}がダンジョン({id})を作成しました.".replace("{player}", player.getName()).replace("{id}",id));
        }
    }

    static void delete(CommandSender sender, String id){
        if(!sDungeonMap.containsKey(id)){
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "ダンジョン({id})は存在しません.".replace("{id}", id));
            return;
        }

        if(DungeonConfigProvider.delete(id)){
            sDungeonMap.remove(id);
            Log.warn("{sender}がダンジョン({id})を消去しました.".replace("{sender}", sender.getName()).replace("{id}",id));
        }else{
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "消去に失敗しました.");
        }
    }

    private static void join(Player player, String id){
        final Dungeon dungeon = sDungeonMap.get(id);

        if(dungeon == null){
            Log.error(DUNGEON_NOT_FOUND_TEXT.replace("{id}", id));
            return;
        }

        if(dungeon.isLock()){
            String strSec = Integer.toString(dungeon.getLockTimer());
            player.sendMessage(NekoEvent.PREFIX + DUNGEON_LOCK_TEXT.replace("{name}",dungeon.getName()).replace("{sec}", strSec));
            return;
        }

        Location location = null;

        if(dungeon.hasJoinLocation()){
            location = dungeon.getJoinLocation();
            player.teleport(location);
        }

        if(dungeon.hasJoinPlayerMessage()){
            player.sendMessage(NekoEvent.PREFIX + dungeon.getJoinPlayerMessage().replace("{player}", player.getName()));
        }

        if(dungeon.hasJoinBroadcastMessage()){
            NekoUtil.broadcastMessage(NekoEvent.PREFIX + dungeon.getJoinBroadcastMessage().replace("{player}", player.getName()), player);
        }

        if(location != null) {
            SpawnManager.setSpawn(player, location);
        }

        Log.info(player.getName() + "がダンジョン({id})に参加しました.".replace("{id}", id));
    }

    static void join(String playerName, String id){
        Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        join(player, id);
    }

    private static void clear(Player player, String id){
        final Dungeon dungeon = sDungeonMap.get(id);

        if(dungeon == null){
            Log.error(DUNGEON_NOT_FOUND_TEXT.replace("{id}", id));
            return;
        }

        if(dungeon.hasClearLocation()){
            Location location = dungeon.getClearLocation();

            player.teleport(location);
            SpawnManager.setSpawn(player, location, false);
        }

        if(dungeon.hasClearTitleText()){
            player.sendTitle(dungeon.getClearTitleText(), "", 10, 70, 20);
        }

        if(dungeon.hasClearPlayerMessage()){
            player.sendMessage(NekoEvent.PREFIX + dungeon.getClearPlayerMessage().replace("{player}", player.getName()));
        }

        if(dungeon.hasClearBroadcastMessage()){
            NekoUtil.broadcastMessage(NekoEvent.PREFIX + dungeon.getClearBroadcastMessage().replace("{player}", player.getName()), player);
        }

        if(dungeon.hasRewordTicket()){ //1日1回
            if(DungeonConfigProvider.hasClearedToday(player.getUniqueId(), id)){
                player.sendMessage(EVENT_TICKET_ONCE_A_DAY_MSG);
            }else {
                TicketProvider.giveEventTicket(player, dungeon.getRewardTicketAmount());
            }
        }

        if(dungeon.enableClearSound()){
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.2f, 0.0f);
        }

        if(dungeon.hasRewordGacha()) {
            SCHEDULER.scheduleSyncDelayedTask(sPlugin, () -> GachaManager.play(player, dungeon.getRewordGachaId()), 200L);
        }

        DungeonConfigProvider.saveClearDate(player.getUniqueId(), id);

        Log.info(player.getName() + "がダンジョン({id})をクリアしました.".replace("{id}", id));
    }

    static void clear(String playerName, String id){
        Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        clear(player, id);
    }

    static void setOptions(Player player, String id, Map<String, List<String>> optionMap){
        final Dungeon dungeon = sDungeonMap.get(id);

        if(dungeon == null){
            Log.error(DUNGEON_NOT_FOUND_TEXT.replace("{id}", id));
            return;
        }

        final Option option = Option.analyze(player, optionMap);

        if(option == null){
            return;
        }

        if(dungeon.update(option.mJoinLocation, option.mClearLocation, option.mJoinPlayerMsg, option.mJoinBroadcastMsg,
                option.mClearPlayerMsg, option.mClearBroadcastMsg, option.mClearTitleText, option.mEnableClearSound, option.mRewordTicketAmount, option.mRewordGachaId)){
            Log.info(player.getName() + "がダンジョン({id})を更新しました.".replace("{id}", id));
        }else{
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "ダンジョン({id})の更新に失敗しました.".replace("{id}", id));
        }
    }

    static void lock(String id, int timerSec){
        if(!sDungeonMap.containsKey(id)){
            Log.error("ダンジョン({id})が存在しないためﾛｯｸできませんでした.".replace("{id}", id));
            return;
        }

        if(timerSec <= 0){
            Log.error("タイマーは1秒以上を指定してください.");
            return;
        }

        final Dungeon dungeon = sDungeonMap.get(id);
        dungeon.startLockTimer(sPlugin, timerSec);
    }

    static void unlock(String id){
        if(!sDungeonMap.containsKey(id)){
            Log.error("ダンジョン({id})が存在しないためﾛｯｸを解除できませんでした.".replace("{id}", id));
            return;
        }

        final Dungeon dungeon = sDungeonMap.get(id);
        dungeon.resetLockTimer();
    }

    static void sendList(CommandSender sender){
        StringBuilder builder = new StringBuilder(DUNGEON_ID_LIST_TEXT + "\n");

        sDungeonMap.entrySet().stream()
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
        sender.sendMessage("---------- NekoEvent Dungeonオプションヘルプ ----------");
        sender.sendMessage("| " + ChatColor.AQUA + "JOIN_LOCATION [HERE/x y z]    (参加時のスポーンを設定)");
        sender.sendMessage("| " + ChatColor.AQUA + "CLEAR_LOCATION [HERE/x y z]   (クリア時のスポーンを設定)");
        sender.sendMessage("| " + ChatColor.AQUA + "JOIN_PLAYER_MSG [message]     (参加時にﾌﾟﾚｲﾔｰに表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "JOIN_BROADCAST_MSG [message]  (参加時にﾌﾟﾚｲﾔｰ以外の全員に表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "CLEAR_PLAYER_MSG [message]    (クリア時にﾌﾟﾚｲﾔｰ自身に表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "CLEAR_BROADCAST_MSG [message] (クリア時にﾌﾟﾚｲﾔｰ以外の全員に表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "CLEAR_TITLE_TEXT [title]      (クリア時にﾌﾟﾚｲﾔｰにタイトルを表示)");
        sender.sendMessage("| " + ChatColor.AQUA + "CLEAR_SOUND [true/false]      (クリア時にﾌﾟﾚｲﾔｰにSEを再生)");
        sender.sendMessage("| " + ChatColor.AQUA + "REWARD_TICKET [amount]        (クリア時にｲﾍﾞﾝﾄﾁｹｯﾄを与える)");
        sender.sendMessage("| " + ChatColor.AQUA + "REWARD_GACHA_ID [gachaId]     (クリア時にガチャを実行)");
        sender.sendMessage("| " + ChatColor.GRAY + "[]は必須,<>は任意,()は説明です.");
        sender.sendMessage("| " + ChatColor.GRAY + "[message]では{name}でﾀﾞﾝｼﾞｮﾝ名,{player}でﾌﾟﾚｲﾔｰ名に置換されます.");
        sender.sendMessage("---------------------------------------");
    }

    @Override
    public void onUpdate(Map<String, Dungeon> dungeonMap) {
        sDungeonMap.clear();
        sDungeonMap.putAll(dungeonMap);
    }

    @Override
    public void onSignChanged(SignChangeEvent event) {
        final Player player = event.getPlayer();
        final String dungeonId = event.getLine(2);
        final String dungeonAction = event.getLine(3);

        final Dungeon dungeon = sDungeonMap.get(dungeonId);

        if(dungeon == null){
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + DUNGEON_NOT_FOUND_TEXT.replace("{id}", dungeonId));
            return;
        }

        if(!dungeonAction.equals("join") && !dungeonAction.equals("clear")){
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + dungeonAction + "は適切なアクションではありません.");
            return;
        }

        final Sign sign = (Sign)event.getBlock().getState();

        //看板のメタデータにダンジョン情報を保存
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put(DUNGEON_ID_METADATA_KEY, dungeonId);
        metadataMap.put(DUNGEON_ACTION_METADATA_KEY, dungeonAction);

        if(!SignConfigProvider.saveMetadata(sign.getLocation(), metadataMap)){
            Log.error("看板ﾒﾀﾃﾞｰﾀの保存に失敗しました.");
            return;
        }

        event.setLine(0, DUNGEON_SIGN_INDEX);
        event.setLine(1, dungeon.getName());
        event.setLine(2, "");

        switch (dungeonAction){
            case "clear":
                event.setLine(3, DUNGEON_SIGN_CLEAR);
                break;
            default:
                event.setLine(3, DUNGEON_SIGN_JOIN);
                break;
        }
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event, Sign sign) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || !hasDungeonMetadata(sign)){
            return;
        }

        final String dungeonId = (String) SignConfigProvider.getMetadata(sign.getLocation(), DUNGEON_ID_METADATA_KEY);
        final String dungeonAction = (String) SignConfigProvider.getMetadata(sign.getLocation(), DUNGEON_ACTION_METADATA_KEY);

        if(!sDungeonMap.containsKey(dungeonId)){
            Log.error(DUNGEON_NOT_FOUND_TEXT.replace("{id}", dungeonId));
            return;
        }

        switch (dungeonAction){
            case "clear":
                clear(event.getPlayer(), dungeonId);
                break;
            default:
                join(event.getPlayer(), dungeonId);
                break;
        }
    }

    private static boolean hasDungeonMetadata(Sign sign){
        if(!SignConfigProvider.hasMetadata(sign.getLocation(), DUNGEON_ID_METADATA_KEY)){
            Log.error("看板" + NekoUtil.toString(sign.getLocation()) + "にﾒﾀﾃﾞｰﾀ(dungeonId)が不足しています.");
            return false;
        }
        if(!SignConfigProvider.hasMetadata(sign.getLocation(), DUNGEON_ACTION_METADATA_KEY)){
            Log.error("看板" + NekoUtil.toString(sign.getLocation()) + "にﾒﾀﾃﾞｰﾀ(dungeonAction)が不足しています.");
            return false;
        }

        return true;
    }

    private final static String DUNGEON_NOT_FOUND_TEXT = "ダンジョン({id})が存在しません.";
    private final static String DUNGEON_LOCK_TEXT =
            ChatColor.translateAlternateColorCodes('&',"{name}&rは&cロック中&rです！ &b{sec}秒&rお待ちください...");
    private final static String EVENT_TICKET_ONCE_A_DAY_MSG =
            NekoEvent.PREFIX + ChatColor.translateAlternateColorCodes('&',"&7各ダンジョンの&6&lイベントチケット&7報酬は,&c1日1回&7です.");

    public final static String DUNGEON_SIGN_INDEX = ChatColor.translateAlternateColorCodes('&', "&8&l[&4&lダンジョン&8&l]");
    private final static String DUNGEON_SIGN_JOIN = ChatColor.translateAlternateColorCodes('&', "&c&n参加");
    private final static String DUNGEON_SIGN_CLEAR = ChatColor.translateAlternateColorCodes('&', "&b&nクリア");

    private final static String DUNGEON_ID_LIST_TEXT = ChatColor.translateAlternateColorCodes('&', "&7--------- &4ダンジョン一覧 &7---------&r");

    private final static String DUNGEON_ID_METADATA_KEY = "dungeonId";
    private final static String DUNGEON_ACTION_METADATA_KEY = "dungeonAction";


    static class Option{
        final static String OPTION_JOIN_PLAYER_MSG_KEY     = "JOIN_PLAYER_MSG";
        final static String OPTION_JOIN_BROADCAST_MSG_KEY  = "JOIN_BROADCAST_MSG";
        final static String OPTION_CLEAR_PLAYER_MSG_KEY    = "CLEAR_PLAYER_MSG";
        final static String OPTION_CLEAR_TITLE_TEXT_KEY    = "CLEAR_TITLE_TEXT";
        final static String OPTION_CLEAR_BROADCAST_MSG_KEY = "CLEAR_BROADCAST_MSG";
        final static String OPTION_REWORD_TICKET_KEY       = "REWARD_TICKET";
        final static String OPTION_REWARD_GACHA_ID_KEY     = "REWARD_GACHA_ID";
        final static String OPTION_JOIN_LOCATION_KEY       = "JOIN_LOCATION";
        final static String OPTION_CLEAR_LOCATION_KEY      = "CLEAR_LOCATION";
        final static String OPTION_CLEAR_SOUND_KEY         = "CLEAR_SOUND";

        String mJoinPlayerMsg, mJoinBroadcastMsg, mClearPlayerMsg, mClearBroadcastMsg, mClearTitleText, mRewordGachaId;
        Location mJoinLocation = null, mClearLocation = null;
        int mRewordTicketAmount;
        Boolean mEnableClearSound;

        static Option analyze(Player player, Map<String, List<String>> optionMap){
            Option option = null;

            try {
                option = new Option(player, optionMap);
            } catch (Exception e){
                e.printStackTrace();
                Log.error(e.getMessage());
            }

            return option;
        }

        private Option(Player player, Map<String, List<String>> optionMap) throws Exception{
            mJoinPlayerMsg     = NekoUtil.appendStrings(optionMap.remove(OPTION_JOIN_PLAYER_MSG_KEY));
            mJoinBroadcastMsg  = NekoUtil.appendStrings(optionMap.remove(OPTION_JOIN_BROADCAST_MSG_KEY));

            mClearPlayerMsg    = NekoUtil.appendStrings(optionMap.remove(OPTION_CLEAR_PLAYER_MSG_KEY));
            mClearBroadcastMsg = NekoUtil.appendStrings(optionMap.remove(OPTION_CLEAR_BROADCAST_MSG_KEY));
            mClearTitleText    = NekoUtil.appendStrings(optionMap.remove(OPTION_CLEAR_TITLE_TEXT_KEY));
            mEnableClearSound  = optionMap.containsKey(OPTION_CLEAR_SOUND_KEY) ? optionMap.remove(OPTION_CLEAR_SOUND_KEY).get(0).equals("true") : null;

            mRewordGachaId = optionMap.containsKey(OPTION_REWARD_GACHA_ID_KEY) ? optionMap.remove(OPTION_REWARD_GACHA_ID_KEY).get(0) : null;

            mRewordTicketAmount = optionMap.containsKey(OPTION_REWORD_TICKET_KEY) ? Integer.parseInt(optionMap.remove(OPTION_REWORD_TICKET_KEY).get(0)) : 0;

            List<String> strJoinLocation = optionMap.remove(OPTION_JOIN_LOCATION_KEY);
            if(strJoinLocation != null){
                if(strJoinLocation.get(0).equals("HERE")){
                    mJoinLocation = player.getLocation();
                }else if(NekoUtil.checkParams(strJoinLocation.size(), 3, OPTION_JOIN_LOCATION_KEY)){
                    mJoinLocation = NekoUtil.toLocation(
                            player.getLocation(),
                            new String[]{
                                    strJoinLocation.get(0),
                                    strJoinLocation.get(1),
                                    strJoinLocation.get(2)
                            }
                    );
                }
            }

            List<String> strClearLocation = optionMap.remove(OPTION_CLEAR_LOCATION_KEY);
            if(strClearLocation != null){
                if(strClearLocation.get(0).equals("HERE")){
                    mClearLocation = player.getLocation();
                }else if(NekoUtil.checkParams(strClearLocation.size(), 3, OPTION_CLEAR_LOCATION_KEY)){
                    mClearLocation = NekoUtil.toLocation(
                            player.getLocation(),
                            new String[]{
                                    strClearLocation.get(0),
                                    strClearLocation.get(1),
                                    strClearLocation.get(2)
                            }
                    );
                }
            }

            if(optionMap.size() > 0){
                optionMap.forEach((key, val) -> Log.error("オプション({key})は存在しません.".replace("{key}", key)));
                throw new Exception("Invalid option.");
            }

            if(mRewordTicketAmount < 0){
                Log.error("ｲﾍﾞﾝﾄﾁｹｯﾄ報酬は0枚以上を指定して下さい.");
                throw new Exception("Invalid ticket amount.");
            }

            if(mRewordGachaId != null && !GachaManager.hasGacha(mRewordGachaId)){
                Log.error(GACHA_ID_NOT_FOUND.replace("{id}",mRewordGachaId));
                throw new Exception("Invalid gacha id.");
            }
        }
    }
}
