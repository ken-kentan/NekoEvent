package jp.kentan.minecraft.neko_event.ticket;

import jp.kentan.minecraft.neko_event.NekoEvent;
import jp.kentan.minecraft.neko_event.util.Log;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;

public class EventTicketProvider {

    private final static String MSG_TICKET_LOST_WHY_INV_FULL =
            ChatColor.translateAlternateColorCodes('&', "インベントリに空きがないため&6&lイベントチケット&r{amount}枚を&cロスト&rしました.");
    private final static String MSG_TICKET_SHORTAGE =
            ChatColor.translateAlternateColorCodes('&', "&6&lイベントチケット&eが{amount}枚&c不足&eしています.");

    private final static ItemStack TICKET_ITEM_STACK = new ItemStack(Material.PAPER);


    public static void setup(){
        final ItemMeta meta = TICKET_ITEM_STACK.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lイベントチケット&a(猫)&r"));
        meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', "&3&oイベントワールドで使用する特別なチケット&r")));
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, false);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        TICKET_ITEM_STACK.setItemMeta(meta);

        Log.info("EventTicketProvider created.");
    }

    public static boolean give(Player player, int amount){
        if(amount < 1){
            Log.warn("1以上の整数値を指定して下さい.");
            return false;
        }

        final PlayerInventory inventory = player.getInventory();

        if(inventory.firstEmpty() == -1){ //インベントリに空きがなければキャンセル
            player.sendMessage(NekoEvent.PREFIX + MSG_TICKET_LOST_WHY_INV_FULL.replace("{amount}", Integer.toString(amount)));
            return false;
        }

        TICKET_ITEM_STACK.setAmount(amount);
        player.getInventory().addItem(TICKET_ITEM_STACK);

        Log.info(player.getName() + "にイベントチケットを" + amount + "枚与えました.");

        return true;
    }

    public static boolean give(String playerName, String amount) {
        Player player = NekoUtil.toPlayer(playerName);

        return player != null && give(player, NekoUtil.toInteger(amount));

    }

    public static boolean remove(Player player, int amount){
        if(amount < 1){
            Log.warn("1以上の整数値を指定して下さい.");
            return false;
        }

        final PlayerInventory inventory = player.getInventory();

        final int playerEventTicketAmount = Arrays.stream(inventory.getContents())
                .filter(EventTicketProvider::isEventTicket)
                .mapToInt(ItemStack::getAmount).sum();

        if(playerEventTicketAmount < amount){
            player.sendMessage(NekoEvent.PREFIX + MSG_TICKET_SHORTAGE.replace("{amount}", Integer.toString(amount - playerEventTicketAmount)));
            return false;
        }

        for(int i = 0; i < inventory.getSize(); ++i){
            final ItemStack item = inventory.getItem(i);
            if(!isEventTicket(item)){
                continue;
            }

            final int newAmount = item.getAmount() - amount;

            item.setAmount(newAmount);
            inventory.setItem(i, (newAmount > 0) ? item : null);

            amount = Math.max(-newAmount, 0);

            if(amount <= 0){
                break;
            }
        }

        return true;
    }

    private static boolean isEventTicket(ItemStack itemStack){
        if(itemStack == null){
            return false;
        }

        final ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta.hasLore() && itemMeta.getLore().get(0).contains("イベントワールドで使用する特別なチケット");
    }
}
