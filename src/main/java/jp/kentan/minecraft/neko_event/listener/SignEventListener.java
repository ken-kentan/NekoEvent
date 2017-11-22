package jp.kentan.minecraft.neko_event.listener;

import jp.kentan.minecraft.neko_event.gacha.GachaManager;
import jp.kentan.minecraft.neko_event.game.dungeon.DungeonManager;
import jp.kentan.minecraft.neko_event.game.parkour.ParkourManager;
import jp.kentan.minecraft.neko_event.spawn.SpawnManager;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignEventListener implements Listener {

    private static SignListener sGachaSignListener;
    private static SignListener sDungeonSignListener;
    private static SignListener sParkourSignListener;
    private static SignListener sSpawnSignListener;

    public static void bindGachaSignListener(SignListener listener){
        sGachaSignListener = listener;
    }

    public static void bindDungeonSignListener(SignListener listener){
        sDungeonSignListener = listener;
    }

    public static void bindParkourSignListener(SignListener listener){
        sParkourSignListener = listener;
    }

    public static void bindSpawnSignListener(SignListener listener){
        sSpawnSignListener = listener;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChanged(SignChangeEvent event) {
        if(!event.getPlayer().isOp() || !event.getLine(0).equals("[event]")){
            return;
        }

        switch (event.getLine(1)){
            case "gacha":
                sGachaSignListener.onSignChanged(event);
                break;
            case "dungeon":
                sDungeonSignListener.onSignChanged(event);
                break;
            case "parkour":
                sParkourSignListener.onSignChanged(event);
                break;
            case "setspawn":
                sSpawnSignListener.onSignChanged(event);
                break;
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final BlockState blockState = event.getClickedBlock().getState();

        if(blockState instanceof Sign){
            final Sign sign = (Sign)blockState;
            final String index = sign.getLine(0);

            if(index.equals(SpawnManager.SETSPAWN_SIGN_INDEX)){
                sSpawnSignListener.onPlayerInteract(event, sign);
            }else if(index.equals(GachaManager.GACHA_SIGN_INDEX)){
                sGachaSignListener.onPlayerInteract(event, sign);
            }else if(index.equals(ParkourManager.PARKOUR_SIGN_INDEX)){
                sParkourSignListener.onPlayerInteract(event, sign);
            }else if(index.equals(DungeonManager.DUNGEON_SIGN_INDEX)){
                sDungeonSignListener.onPlayerInteract(event, sign);
            }
        }
    }
}
