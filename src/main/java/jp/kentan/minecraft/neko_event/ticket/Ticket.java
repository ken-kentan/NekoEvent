package jp.kentan.minecraft.neko_event.ticket;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

abstract class Ticket {
    private final ItemStack ITEM_STACK;
    private final String DISPLAY_NAME;

    private final String CHECK_STRING;

    private final String MSG_LOST_WHY_INV_FULL;
    private final String MSG_SHORTAGE;

    Ticket(String displayName, List<String> lore, String checkStr){
        ITEM_STACK = new ItemStack(Material.PAPER);

        DISPLAY_NAME = ChatColor.translateAlternateColorCodes('&', displayName);

        CHECK_STRING = checkStr;

        ItemMeta meta = ITEM_STACK.getItemMeta();
        meta.setDisplayName(DISPLAY_NAME);
        meta.setLore(lore);
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        ITEM_STACK.setItemMeta(meta);

        MSG_LOST_WHY_INV_FULL = ChatColor.translateAlternateColorCodes('&', "インベントリに空きがないため" + DISPLAY_NAME + "&r{amount}枚を&cロスト&rしました.");
        MSG_SHORTAGE = ChatColor.translateAlternateColorCodes('&', DISPLAY_NAME + "&eが{amount}枚&c不足&eしています.");
    }

    ItemStack getItemStack(){
        return ITEM_STACK.clone();
    }

    void give(Player player, int amount){
        ItemStack itemStack = ITEM_STACK.clone();

        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    String getDisplayName(){
        return DISPLAY_NAME;
    }

    boolean isSimilar(String playerName, ItemStack itemStack){
        if(itemStack == null || !itemStack.hasItemMeta()){
            return false;
        }

        final ItemMeta meta = itemStack.getItemMeta();

        return ITEM_STACK.getType() == itemStack.getType() && meta.hasLore() && meta.getLore().get(0).contains(CHECK_STRING);
    }

    String getLostMessage(int amount){
        return MSG_LOST_WHY_INV_FULL.replace("{amount}", Integer.toString(amount));
    }

    String getShortageMessage(int amount){
        return MSG_SHORTAGE.replace("{amount}", Integer.toString(amount));
    }
}
