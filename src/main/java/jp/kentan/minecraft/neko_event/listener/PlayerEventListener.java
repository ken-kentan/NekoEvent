package jp.kentan.minecraft.neko_event.listener;

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

    private static boolean isInEventWorld(Player player) {
        return player.getGameMode() == GameMode.ADVENTURE && isInEventWorld(player.getWorld());

    }
    private static boolean isInEventWorld(World word){
        final String worldName = word.getName();
        return worldName.equals("Ivents_World") || worldName.equals("EventWorld");
    }
}
