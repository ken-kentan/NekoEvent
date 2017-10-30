package jp.kentan.minecraft.neko_event.teleport;

import jp.kentan.minecraft.neko_event.util.Log;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportProvider {

    private static void teleport(Player player, Location location){
        player.teleport(location);

        Log.info(player.getName() + "を" + NekoUtil.toString(location) + "にテレポートしました.");
    }

    public static void teleport(String playerName, String[] strLocation){
        Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        teleport(player, NekoUtil.toLocation(player.getLocation(), strLocation));
    }
}
