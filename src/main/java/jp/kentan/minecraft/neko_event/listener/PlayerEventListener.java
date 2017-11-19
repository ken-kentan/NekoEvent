package jp.kentan.minecraft.neko_event.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class PlayerEventListener implements Listener{

    private final Plugin PLUGIN;
    private final BukkitScheduler SCHEDULER;

    public PlayerEventListener(Plugin plugin){
        PLUGIN = plugin;
        SCHEDULER = plugin.getServer().getScheduler();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e)
    {
        final Player player = e.getEntity();

        if(inEventWorld(player)){
            SCHEDULER.scheduleSyncDelayedTask(PLUGIN, () -> {
                if(!player.isDead()) return;

                player.spigot().respawn();

                player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
                player.setFireTicks(0);
            }, 10L);
        }
    }

    private static boolean inEventWorld(Player player){
        if(player.getGameMode() != GameMode.ADVENTURE) return false;

        final String worldName = player.getLocation().getWorld().getName();

        return worldName.equals("Ivents_World") || worldName.equals("EventWorld");
    }
}
