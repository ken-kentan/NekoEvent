package jp.kentan.minecraft.neko_event.listener;

import jp.kentan.minecraft.neko_event.spawn.SpawnManager;
import jp.kentan.minecraft.neko_event.util.GameUtil;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class PlayerEventListener implements Listener{

    private final Plugin PLUGIN;
    private final BukkitScheduler SCHEDULER;

    public PlayerEventListener(Plugin plugin){
        PLUGIN = plugin;
        SCHEDULER = plugin.getServer().getScheduler();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e)
    {
        final Player player = e.getEntity();

        if(isInEventWorld(player)){
            SCHEDULER.scheduleSyncDelayedTask(PLUGIN, () -> {
                if(!player.isDead()) return;

              GameUtil.removeVanishingItem(player);

                player.spigot().respawn();

                player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
                player.setFireTicks(0);
            }, 10L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event){
        if(isInEventWorld(event.getFrom())){
            final AttributeInstance maxHealth = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
            maxHealth.setBaseValue(maxHealth.getDefaultValue());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event){
        if(isInEventWorld(event.getFrom().getWorld()) && !isInEventWorld(event.getTo().getWorld())){
            SpawnManager.removeSpawn(event.getPlayer());
        }

        // for 2018障害物レース用
        if(event.getFrom().getWorld() == event.getTo().getWorld()){
            return;
        }

        if(isInRaceWorld(event.getFrom().getWorld()) || isInRaceWorld(event.getTo().getWorld())){
            final Player player = event.getPlayer();

            if (player.getGameMode() == GameMode.CREATIVE){
                return;
            }

            if(!NekoUtil.isEmpty(player.getInventory())){
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "アイテムを持った状態では移動できません.");
            }
        }
    }

    private static boolean isInEventWorld(Player player) {
        return player.getGameMode() == GameMode.ADVENTURE && isInEventWorld(player.getWorld());

    }
    private static boolean isInEventWorld(World world){
        return world.getName().equals("EventWorld");
    }

    private static boolean isInRaceWorld(World world){
        return world.getName().equals("test_world");
    }
}
