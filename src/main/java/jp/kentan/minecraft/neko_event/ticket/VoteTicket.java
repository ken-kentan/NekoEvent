package jp.kentan.minecraft.neko_event.ticket;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class VoteTicket extends Ticket {

    private final static Pattern LORE_PATTERN = Pattern.compile(".*#(\\w*)");
    private final static String LORE_CONTENT = ChatColor.translateAlternateColorCodes('&', "&6&o投票&3&oでもらえる不思議なチケット&r #");

    private final static List<String> LORE = Arrays.asList(
            ChatColor.translateAlternateColorCodes('&', "&6&o投票&3&oでもらえる不思議なチケット&r #{player}"),
            ChatColor.translateAlternateColorCodes('&', "&8本人しか使用できないよ(｡･ω･｡)")
    );

    VoteTicket() {
        super("&a&l投票限定チケット&6(猫)&r", LORE, LORE_CONTENT);
    }

    @Override
    void give(Player player, int amount){
        ItemStack ticket = super.getItemStack();
        ticket.setAmount(amount);

        ItemMeta meta = ticket.getItemMeta();

        List<String> lore = new ArrayList<>();
        lore.addAll(meta.getLore());
        lore.set(0, LORE_CONTENT + player.getName());

        meta.setLore(lore);

        ticket.setItemMeta(meta);

        player.getInventory().addItem(ticket);
    }

    @Override
    boolean isSimilar(String playerName, ItemStack itemStack){
        if(!super.isSimilar(null, itemStack)){
            return false;
        }

        Matcher matcher = LORE_PATTERN.matcher(itemStack.getItemMeta().getLore().get(0));
        if(!matcher.find()){
            return false;
        }

        String ownerName = matcher.group(1);

        return playerName.equals(ownerName);
    }
}
