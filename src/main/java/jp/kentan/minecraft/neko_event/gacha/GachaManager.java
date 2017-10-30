package jp.kentan.minecraft.neko_event.gacha;

import jp.kentan.minecraft.neko_event.NekoEvent;
import jp.kentan.minecraft.neko_event.config.listener.ConfigListener;
import jp.kentan.minecraft.neko_event.config.provider.GachaConfigProvider;
import jp.kentan.minecraft.neko_event.config.provider.SignConfigProvider;
import jp.kentan.minecraft.neko_event.gacha.model.Gacha;
import jp.kentan.minecraft.neko_event.listener.SignEventListener;
import jp.kentan.minecraft.neko_event.listener.SignListener;
import jp.kentan.minecraft.neko_event.ticket.EventTicketProvider;
import jp.kentan.minecraft.neko_event.util.Log;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class GachaManager implements SignListener, ConfigListener<Gacha> {

    private final static GachaManager sInstance = new GachaManager();

    private final static Server SERVER = Bukkit.getServer();
    private final static ConsoleCommandSender COMMAND_SENDER = SERVER.getConsoleSender();

    private static Map<String, Gacha> sGachaMap = new HashMap<>();

    public static void setup(JavaPlugin plugin){
        GachaConfigProvider.bindListener(sInstance);
        GachaConfigProvider.load();

        SignEventListener.bindGachaSignListener(sInstance);

        plugin.getCommand("gacha").setExecutor(new GachaCommandExecutor());
    }

    public static void play(Player player, String gachaId){
        if(!sGachaMap.containsKey(gachaId)){
            Log.error(GACHA_ID_NOT_FOUND.replace("{id}", gachaId));
            return;
        }

        final Gacha gacha = sGachaMap.get(gachaId);
        Gacha.Component component = gacha.getByRandom();

        final Location location = player.getLocation();
        final Sound soundEffect;

        if(component.hasCommands()){ //あたり
            final String playerName = player.getName();

            component.getCommands().forEach(cmd -> SERVER.dispatchCommand(COMMAND_SENDER, cmd.replace("{player}", playerName)));

            if(gacha.hasGetMessage()) {
                player.sendMessage(NekoEvent.PREFIX + gacha.getMessage().replace("{name}", component.getName()));
            }

            if(gacha.hasBroadcastMessage()){
                final String broadcastMsg = gacha.getBroadcastMessage().replace("{player}", player.getName()).replace("{name}", component.getName());
                NekoUtil.broadcastMessage(NekoEvent.PREFIX + broadcastMsg, player);
            }

            Log.info(player.getName() + "にｶﾞﾁｬ(" + gachaId + ")で" + component.getName() + ChatColor.RESET + "を与えました.");

            soundEffect = Sound.ENTITY_PLAYER_LEVELUP;
        }else{ //はずれ
            if(gacha.hasMissMessage()) {
                player.sendMessage(NekoEvent.PREFIX + gacha.getMissMessage());
            }

            soundEffect = Sound.ENTITY_PIG_DEATH;
        }

        if(gacha.enableEffect()){
            player.playSound(location, soundEffect, 1.0f, 0.0f);
        }
    }

    static void play(String playerName, String gachaId, String ticketCost){
        Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        if(ticketCost == null || EventTicketProvider.remove(player, NekoUtil.toInteger(ticketCost))) {
            play(player, gachaId);
        }
    }

    private static void play(Player player, String gachaId, int ticketCost){

        if(ticketCost > 0 && !EventTicketProvider.remove(player, ticketCost)) {
            return;
        }

        play(player, gachaId);
    }

    static void sendList(CommandSender sender){
        StringBuilder builder = new StringBuilder(GACHA_ID_LIST_TEXT + "\n");

        sGachaMap.entrySet().stream()
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

    static void sendInfo(CommandSender sender, String gachaId){
        final Gacha gacha = sGachaMap.get(gachaId);

        if(gacha == null){
            sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + GACHA_ID_NOT_FOUND.replace("{id}",gachaId));
            return;
        }

        sender.sendMessage(GACHA_INFO_TEXT);
        sender.sendMessage(" ID: " + gachaId);
        sender.sendMessage(" 名前: " + gacha.getName());
        sender.sendMessage(" 要素数: " + gacha.getSize());
        sender.sendMessage(" ｹﾞｯﾄﾒｯｾｰｼﾞ: " + gacha.hasGetMessage());
        sender.sendMessage(" ﾐｽﾒｯｾｰｼﾞ: " + gacha.hasMissMessage());
        sender.sendMessage(" ﾌﾞﾛｰﾄﾞｷｬｽﾄ: " + gacha.hasBroadcastMessage());
        sender.sendMessage(" ｻｳﾝﾄﾞｴﾌｪｸﾄ: " + gacha.enableEffect());
    }

    private static boolean hasGachaMetadata(Sign sign){
        if(!SignConfigProvider.hasMetadata(sign.getLocation(), GACHA_ID_METADATA_KEY)){
            Log.error("看板" + NekoUtil.toString(sign.getLocation()) + "にﾒﾀﾃﾞｰﾀ(gachaId)が不足しています.");
            return false;
        }
        if(!SignConfigProvider.hasMetadata(sign.getLocation(), GACHA_TICKET_COST_METADATA_KEY)){
            Log.error("看板" + NekoUtil.toString(sign.getLocation()) + "にﾒﾀﾃﾞｰﾀ(gachaTicketCost)が不足しています.");
            return false;
        }

        return true;
    }

    public static boolean hasGacha(String id){
        return sGachaMap.containsKey(id);
    }

    @Override
    public void onSignChanged(SignChangeEvent event) {
        final Player player = event.getPlayer();
        final String gachaId = event.getLine(2);
        final String strTicketCost = event.getLine(3);

        final int ticketCost;
        if(strTicketCost == null || strTicketCost.length() <= 0){
            ticketCost = 0;
        }else{
            ticketCost = NekoUtil.toInteger(strTicketCost);
        }

        final Gacha gacha = sGachaMap.get(gachaId);

        if(gacha == null){
            player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + GACHA_ID_NOT_FOUND.replace("{id}", gachaId));
            return;
        }

        final Sign sign = (Sign)event.getBlock().getState();

        //看板のメタデータにガチャ情報を保存
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put(GACHA_ID_METADATA_KEY, gachaId);
        metadataMap.put(GACHA_TICKET_COST_METADATA_KEY, ticketCost);

        if(!SignConfigProvider.saveMetadata(sign.getLocation(), metadataMap)){
            Log.error("看板ﾒﾀﾃﾞｰﾀの保存に失敗しました.");
            return;
        }

        event.setLine(0, GACHA_SIGN_INDEX);
        event.setLine(1, gacha.getName());
        event.setLine(2, "");

        if(ticketCost > 0) {
            event.setLine(3, GACHA_SIGN_TICKET_COST.replace("{amount}", strTicketCost));
        }else{
            event.setLine(3, GACHA_SIGN_TICKET_COST_FREE);
        }
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event, Sign sign) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || !hasGachaMetadata(sign)){
            return;
        }

        final String gachaId = (String) SignConfigProvider.getMetadata(sign.getLocation(), GACHA_ID_METADATA_KEY);
        final int ticketCost = (int) SignConfigProvider.getMetadata(sign.getLocation(), GACHA_TICKET_COST_METADATA_KEY);

        if(!sGachaMap.containsKey(gachaId)){
            Log.error(GACHA_ID_NOT_FOUND.replace("{id}",gachaId));
            return;
        }

        play(event.getPlayer(), gachaId, ticketCost);
    }

    @Override
    public void onUpdate(Map<String, Gacha> gachaMap) {
        sGachaMap.clear();
        sGachaMap.putAll(gachaMap);
    }

    private final static String GACHA_ID_METADATA_KEY = "gachaId";
    private final static String GACHA_TICKET_COST_METADATA_KEY = "gachaTicketCost";

    public final static String GACHA_SIGN_INDEX = ChatColor.translateAlternateColorCodes('&', "&8&l[&d&lガチャ&8&l]");
    private final static String GACHA_SIGN_TICKET_COST = ChatColor.translateAlternateColorCodes('&', "&9&n1プレイ&r &a&n{amount}枚");
    private final static String GACHA_SIGN_TICKET_COST_FREE = ChatColor.translateAlternateColorCodes('&', "&c無料");

    private final static String GACHA_ID_LIST_TEXT = ChatColor.translateAlternateColorCodes('&', "&7--------- &dガチャ一覧 &7---------&r");
    private final static String GACHA_INFO_TEXT = ChatColor.translateAlternateColorCodes('&', "&7--------- &dガチャ情報 &7---------");

    public final static String GACHA_ID_NOT_FOUND = "ガチャID({id})は存在しません.";
}
