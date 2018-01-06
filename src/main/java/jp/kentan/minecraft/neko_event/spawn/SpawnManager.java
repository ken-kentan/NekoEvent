package jp.kentan.minecraft.neko_event.spawn;

import jp.kentan.minecraft.neko_event.NekoEvent;
import jp.kentan.minecraft.neko_event.config.provider.SignConfigProvider;
import jp.kentan.minecraft.neko_event.listener.SignEventListener;
import jp.kentan.minecraft.neko_event.listener.SignListener;
import jp.kentan.minecraft.neko_event.util.Log;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class SpawnManager implements SignListener {

    private static SpawnManager sInstance = new SpawnManager();

    public static void setup(){
        SignEventListener.bindSpawnSignListener(sInstance);
    }

    public static void setSpawn(BlockCommandSender sender, String playerName, String[] strLocation){
        Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        final Location location;
        if(strLocation == null){
            location = player.getLocation();
        }else{
            location = NekoUtil.toLocation(sender.getBlock().getLocation(), strLocation);
            location.setDirection(player.getLocation().getDirection());
        }

        setSpawn(player, location);
    }

    public static void setSpawn(Player player, Location location){
        setSpawn(player, location, true);
    }

    public static void setSpawn(Player player, Location location, boolean isSendMessage){
        int maxOffset = 1;

        location.setY(Math.ceil(location.getY()));

        while (!canSetBedSpawn(location.getBlock())){
            if(--maxOffset < 0){
                if(isSendMessage){
                    player.sendMessage(NekoEvent.PREFIX + ChatColor.RED + "セーブに失敗しました. 位置を変更して下さい!");
                }else{
                    Log.error(player.getName() + "のスポーンセット" + NekoUtil.toString(location) + "に失敗しました.");
                }

                return;
            }

            location.add(0D, 1D, 0D);
        }

        player.setBedSpawnLocation(location, true);

        if(isSendMessage) {
            player.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.1f);
            player.sendMessage(NekoEvent.PREFIX + ChatColor.AQUA + "セーブしました！");
        }
    }

    public static void removeSpawn(Player player){
        Location spawn = player.getBedSpawnLocation();

        if(spawn == null || !spawn.getWorld().getName().equals("EventWorld")){
            return;
        }

        player.setBedSpawnLocation(null, true);
        player.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "セーブポイントが自動的に消去されました.");
    }

    private static boolean hasSpawnMetadata(Sign sign){
        if(!SignConfigProvider.hasMetadata(sign.getLocation(), SPAWN_LOCATION_PLAYER_METADATA_KEY)){
            if (!SignConfigProvider.hasMetadata(sign.getLocation(), SPAWN_LOCATION_X_METADATA_KEY)) {
                Log.error("看板" + NekoUtil.toString(sign.getLocation()) + "にﾒﾀﾃﾞｰﾀ(setspawnX)が不足しています.");
                return false;
            }
            if (!SignConfigProvider.hasMetadata(sign.getLocation(), SPAWN_LOCATION_Y_METADATA_KEY)) {
                Log.error("看板" + NekoUtil.toString(sign.getLocation()) + "にﾒﾀﾃﾞｰﾀ(setspawnY)が不足しています.");
                return false;
            }
            if (!SignConfigProvider.hasMetadata(sign.getLocation(), SPAWN_LOCATION_Z_METADATA_KEY)) {
                Log.error("看板" + NekoUtil.toString(sign.getLocation()) + "にﾒﾀﾃﾞｰﾀ(setspawnZ)が不足しています.");
                return false;
            }

            Log.error("看板" + NekoUtil.toString(sign.getLocation()) + "にﾒﾀﾃﾞｰﾀ(setspawnPlayerLocation)が不足しています.");
        }

        return true;
    }

    private static boolean canSetBedSpawn(Block block){
        return !block.isLiquid() && block.getRelative(BlockFace.UP).isEmpty();
    }

    @Override
    public void onSignChanged(SignChangeEvent event) {
        final Player player = event.getPlayer();
        final String[] strLocation = event.getLine(2).split(" ");

        final Sign sign = (Sign)event.getBlock().getState();

        boolean setspawnByPlayerLocation = strLocation.length < 3;

        Location location = null;

        if(!setspawnByPlayerLocation){
            location = NekoUtil.toLocation(
                    player.getLocation(),
                    new String[]{
                            strLocation[0],
                            strLocation[1],
                            strLocation[2],
                    }
            );

            if(location == null){
                Log.error("指定された座標を解決できませんでした.");
                return;
            }
        }


        //看板のメタデータにパルクール情報を保存
        Map<String, Object> metadataMap = new HashMap<>();
        if(setspawnByPlayerLocation){
            metadataMap.put(SPAWN_LOCATION_PLAYER_METADATA_KEY, true);
        }else{
            metadataMap.put(SPAWN_LOCATION_PLAYER_METADATA_KEY, false);
            metadataMap.put(SPAWN_LOCATION_X_METADATA_KEY, location.getX());
            metadataMap.put(SPAWN_LOCATION_Y_METADATA_KEY, location.getY());
            metadataMap.put(SPAWN_LOCATION_Z_METADATA_KEY, location.getZ());
        }

        if(!SignConfigProvider.saveMetadata(sign.getLocation(), metadataMap)){
            Log.error("看板ﾒﾀﾃﾞｰﾀの保存に失敗しました.");
            return;
        }

        event.setLine(0, SETSPAWN_SIGN_INDEX);
        event.setLine(1, "");
        event.setLine(2, event.getLine(3));
        event.setLine(3, "");
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event, Sign sign) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || !hasSpawnMetadata(sign)){
            return;
        }

        final Player player = event.getPlayer();
        final boolean setspawnByPlayerLocation = (boolean) SignConfigProvider.getMetadata(sign.getLocation(), SPAWN_LOCATION_PLAYER_METADATA_KEY);

        if(setspawnByPlayerLocation) {
            setSpawn(event.getPlayer(), event.getPlayer().getLocation());
        }else{
            final Location location = player.getLocation();
            location.setX((Double) SignConfigProvider.getMetadata(sign.getLocation(), SPAWN_LOCATION_X_METADATA_KEY));
            location.setY((Double) SignConfigProvider.getMetadata(sign.getLocation(), SPAWN_LOCATION_Y_METADATA_KEY));
            location.setZ((Double) SignConfigProvider.getMetadata(sign.getLocation(), SPAWN_LOCATION_Z_METADATA_KEY));

            setSpawn(event.getPlayer(), location);
        }
    }

    public final static String SETSPAWN_SIGN_INDEX = ChatColor.translateAlternateColorCodes('&', "&8&l[&3&lセーブ&8&l]");

    private final static String SPAWN_LOCATION_PLAYER_METADATA_KEY = "setspawnByPlayerLocation";
    private final static String SPAWN_LOCATION_X_METADATA_KEY = "setspawnX";
    private final static String SPAWN_LOCATION_Y_METADATA_KEY = "setspawnY";
    private final static String SPAWN_LOCATION_Z_METADATA_KEY = "setspawnZ";
}
