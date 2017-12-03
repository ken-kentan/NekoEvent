package jp.kentan.minecraft.neko_event.util;

import jp.kentan.minecraft.neko_event.util.math.MersenneTwisterFast;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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


    public static void resetPlayerStatus(String playerName){
        final Player player = NekoUtil.toPlayer(playerName);

        if(player == null){
            return;
        }

        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        player.setFireTicks(0);

        final double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        if(maxHealth >= 1.0D) {
            player.setHealth(maxHealth);
        }
    }

    public static void giveExp(final Player player, final int value){
        if(player != null) {
            final Location location = player.getLocation();
            location.add(0D, 0.5D, 0D);

            final ExperienceOrb expOrb = (ExperienceOrb) location.getWorld().spawnEntity(location, EntityType.EXPERIENCE_ORB);
            expOrb.setExperience(value);
        }
    }

    public static void removeVanishingItem(Player player){
        Inventory inventory = player.getInventory();

        for(int i = 0, size = inventory.getSize(); i < size; ++i){
            ItemStack item = inventory.getItem(i);

            if(item == null){
                continue;
            }

            if(item.getEnchantmentLevel(Enchantment.VANISHING_CURSE) > 0){
                inventory.setItem(i, null);
            }
        }
    }
}
