package jp.kentan.minecraft.event;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TicketManager {
	private static String name, itemstack;
	
	private NekoEvent ne = null;
	
	public TicketManager(NekoEvent ne){
		this.ne = ne;
	}
	
	public static void setup(String _name, String _itemstack){
		name = _name;
		itemstack = _itemstack;
	}
	
	public void give(String strPlayer, int number){
		give(strPlayer, Integer.toString(number));
	}
	
	public void give(String strPlayer, String number){
		int ticket_number = 0;
		Player player = ne.convertToPlayer(strPlayer);
		
		if(player == null || !ne.isCheckPlayerOnline(player)) return;
		
		try {
			ticket_number = Integer.parseInt(number);
		} catch (NumberFormatException nfex) {
			ne.sendErrorMessage("Could not convert " + number + " to integer.");
			return;
		}
		
		if(ticket_number > 0){			
			ne.getServer().dispatchCommand(ne.getServer().getConsoleSender(), "give " + strPlayer + name.replace("{number}", Integer.toString(ticket_number)));
			
			player.sendMessage(NekoEvent.CHAT_TAG + ChatColor.AQUA +" イベントチケット" + ChatColor.WHITE + "を" + ticket_number + "枚" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
			ne.sendInfoMessage("Gave the " + ticket_number + " Event Tickets to " + strPlayer + ".");
			ne.writeLog("Ticket:" + strPlayer + " +" + ticket_number );
		}
		
	}
	
	public boolean remove(String strPlayer, String number) {
		int ticket_number = 0, player_ticket_amt = 0;
		Player player = ne.convertToPlayer(strPlayer);
		
		if(player == null || !ne.isCheckPlayerOnline(player)) return false;
		
		try {
			ticket_number = Integer.parseInt(number);
		} catch (NumberFormatException nfex) {
			ne.sendErrorMessage("Could not convert " + number + " to integer.");
			return false;
		}
		
		if(ticket_number <= 0) return true;
		
		//Create EventTickets ItemStack
		String itemS_str = itemstack.replace("{number}", Integer.toString(ticket_number));
		
		//Get amount of EventTickets
		for(int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack itemStack = player.getInventory().getItem(i);
			String strItemStack = null;
			
			try{
				strItemStack = itemStack.toString();
			}catch(Exception e){
				if(itemStack != null && itemStack.getAmount() > 0) ne.getLogger().warning("Skip An exception.");
				continue;
			}
			
			if(itemStack != null && strItemStack.indexOf(itemS_str) != -1){
				player_ticket_amt += itemStack.getAmount();
			}
		}
		
		//reject (shortage)
		if(player_ticket_amt < ticket_number){
			player.sendMessage(NekoEvent.CHAT_TAG + ChatColor.YELLOW +"イベントチケットが" + (ticket_number - player_ticket_amt) + "枚不足しています。");
			return false;
		}

		for(int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack itemS = player.getInventory().getItem(i);
			String strItemStack = null;
			
			try{
				strItemStack = itemS.toString();
			}catch(Exception e){
				if(itemS != null && itemS.getAmount() > 0) ne.getLogger().warning("Skip An exception.");
				continue;
			}
			
			if(itemS != null && strItemStack.indexOf(itemS_str) != -1){
				int amt = itemS.getAmount() - ticket_number;
				
				//over 1S
				if(amt < 0){
					ticket_number = Math.abs(amt);
					amt = 0;
				}else{
					ticket_number = 0;
				}
				
				itemS.setAmount(amt);
				player.getInventory().setItem(i, amt > 0 ? itemS : null);
				player.updateInventory();
			}
		}
		
		ne.writeLog("Ticket:" + player.getName() + " -" + number );
		return true;
	}

}
