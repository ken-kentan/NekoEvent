package jp.kentan.minecraft.neko_event.listener;

import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public interface SignListener {
    void onSignChanged(SignChangeEvent event);
    void onPlayerInteract(PlayerInteractEvent event, Sign sign);
}
