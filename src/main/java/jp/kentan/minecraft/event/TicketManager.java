package jp.kentan.minecraft.event;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TicketManager extends NekoEvent{
	
	public void give(String player, String number){
		int ticket_number = 0;
		
		try {
			ticket_number = Integer.parseInt(number);
		} catch (NumberFormatException nfex) {
			showException(nfex);
			return;
		}
		
		if(ticket_number > 0){			
			getServer().dispatchCommand(getServer().getConsoleSender(), "give " + player + ticket_str.replace("{number}", Integer.toString(ticket_number)));
			
			Bukkit.getServer().getPlayer(player).sendMessage(ChatColor.AQUA +" イベントチケット" + ChatColor.WHITE + "を" + ticket_number + "枚" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
			getLogger().info(player + "に、イベントチケットを" + ticket_number + "枚追加しました。");
			writeLog("Ticket:" + player + " +" + ticket_number );
		}
		
	}
	
	public boolean remove(Player player, int ticket_number) {
		String itemS_str = ticket_itemstack.replace("{number}", Integer.toString(ticket_number));;

		for(int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack itemS = player.getInventory().getItem(i);
			if(itemS != null && itemS.toString().indexOf(itemS_str) != -1){
				int amt = itemS.getAmount() - ticket_number;
				
				if(amt < 0){
					player.sendMessage(ChatColor.YELLOW +"イベントチケットが" + Math.abs(amt) + "枚不足しています。");
					return false;
				}
				
				itemS.setAmount(amt);
				player.getInventory().setItem(i, amt > 0 ? itemS : null);
				player.updateInventory();
				
				writeLog("Ticket:" + player + " -" + ticket_number );
				return true;
			}
		}
		
		player.sendMessage(ChatColor.YELLOW +"イベントチケットが" + ticket_number + "枚不足しています。");

		return false;
	}
}
