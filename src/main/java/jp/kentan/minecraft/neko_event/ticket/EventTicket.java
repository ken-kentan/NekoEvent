package jp.kentan.minecraft.neko_event.ticket;

import org.bukkit.ChatColor;

import java.util.Collections;

class EventTicket extends Ticket {
    EventTicket() {
        super(
                "&6&lイベントチケット&a(猫)&r",
                Collections.singletonList(ChatColor.translateAlternateColorCodes('&', "&3&oイベントワールドで使用する特別なチケット&r")),
                "イベントワールドで使用する特別なチケット"
        );
    }
}
