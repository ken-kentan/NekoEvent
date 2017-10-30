package jp.kentan.minecraft.neko_event.util;

import jp.kentan.minecraft.neko_event.util.math.MersenneTwisterFast;
import org.bukkit.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;


public class GameUtil {
    private static Plugin sPlugin;
    private final static BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    private final static MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    public static void setPlugin(Plugin plugin){
        sPlugin = plugin;
    }

    public static void sendCustomMessage(String playerName, String sender, String message){
        Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        player.sendMessage(
                " " + ((sender.equals("null")) ? "" : (ChatColor.translateAlternateColorCodes('&', sender) + ChatColor.GREEN + ": ")) +
                ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void placeBlockByRandom(BlockCommandSender sender, String[] args, int index){
        final int pivot = RANDOM.nextInt((args.length - index) / 3) * 3 + index;

        final Location location = NekoUtil.toLocation(
                sender.getBlock().getLocation(),
                new String[]{args[pivot], args[pivot+1], args[pivot+2]}
        );

        location.getBlock().setType(Material.REDSTONE_BLOCK);

        SCHEDULER.scheduleSyncDelayedTask(sPlugin, () -> location.getBlock().setType(Material.AIR), 10L);
    }

    public static void placeBlockByDelay(BlockCommandSender sender, int delaySeconds, String[] strLocation){
        final Location location = NekoUtil.toLocation(
                sender.getBlock().getLocation(), strLocation
        );

        SCHEDULER.scheduleSyncDelayedTask(sPlugin, () -> location.getBlock().setType(Material.REDSTONE_BLOCK), 20L * delaySeconds);
    }

    public static void jumpPlayer(String playerName, double height, double length){
        final Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        final Location location = player.getLocation();
        final Vector direction = location.getDirection().setY(0D).normalize();

        direction.multiply(length);
        direction.setY(height);

        location.getWorld().playEffect(location, Effect.SMOKE, 4);

        player.playSound(location, Sound.ENTITY_GHAST_SHOOT, 1f, 1f);
        player.setVelocity(direction);
    }
}
