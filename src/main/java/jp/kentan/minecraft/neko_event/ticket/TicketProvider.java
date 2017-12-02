package jp.kentan.minecraft.neko_event.ticket;

import jp.kentan.minecraft.neko_event.NekoEvent;
import jp.kentan.minecraft.neko_event.util.Log;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.function.Predicate;

public class TicketProvider {

    private final static Ticket EVENT_TICKET = new EventTicket();
    private final static Ticket VOTE_TICKET = new VoteTicket();

    private static boolean give(Player player, Ticket ticket, int amount){
        if(amount < 1){
            Log.warn("1以上の整数値を指定して下さい.");
            return false;
        }

        if(player.getInventory().firstEmpty() < 0){ //インベントリに空きがなければキャンセル
            player.sendMessage(NekoEvent.PREFIX + ticket.getLostMessage(amount));
            return false;
        }

        ticket.give(player, amount);

        Log.info(player.getName() + "に" + ticket.getDisplayName() + "を" + amount + "枚与えました.");

        return true;
    }

    public static boolean give(String playerName, String amount, boolean isVoteTicket) {
        Player player = NekoUtil.toPlayer(playerName);

        return give(player, NekoUtil.toInteger(amount), isVoteTicket);
    }

    public static boolean give(Player player, int amount, boolean isVoteTicket) {
        return player != null && give(player, isVoteTicket ? VOTE_TICKET : EVENT_TICKET , amount);
    }

    public static boolean remove(Player player, int amount, boolean isRequireVoteTicket){
        if(amount < 1){
            Log.warn("1以上の整数値を指定して下さい.");
            return false;
        }

        final String playerName = player.getName();
        final PlayerInventory inventory = player.getInventory();

        final int playerTicketAmount = Arrays.stream(inventory.getContents())
                .filter(getTicketPredicate(playerName, isRequireVoteTicket))
                .mapToInt(ItemStack::getAmount).sum();

        if(playerTicketAmount < amount){
            player.sendMessage(NekoEvent.PREFIX + (isRequireVoteTicket ? VOTE_TICKET : EVENT_TICKET).getShortageMessage(amount - playerTicketAmount));
            return false;
        }

        for(int i = 0, size = inventory.getSize(); i < size; ++i){
            final ItemStack item = inventory.getItem(i);
            if(item == null || (isRequireVoteTicket && !VOTE_TICKET.isSimilar(playerName, item) || (!isRequireVoteTicket && !isEventTicket(playerName, item)))){
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

    private static boolean isEventTicket(String playerName, ItemStack item){
        return EVENT_TICKET.isSimilar(playerName, item) || VOTE_TICKET.isSimilar(playerName, item);
    }

    private static Predicate<ItemStack> getTicketPredicate(String playerName, boolean isRequireVoteTicket){
        if(isRequireVoteTicket){
            return itemStack -> VOTE_TICKET.isSimilar(playerName, itemStack);
        }else {
            return itemStack -> EVENT_TICKET.isSimilar(playerName, itemStack) || VOTE_TICKET.isSimilar(playerName, itemStack);
        }
    }
}
