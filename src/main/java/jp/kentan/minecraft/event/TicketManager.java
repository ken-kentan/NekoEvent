package jp.kentan.minecraft.event;

import static org.hamcrest.CoreMatchers.nullValue;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TicketManager {
	public static String name, itemstack;
	
	static NekoEvent ne = NekoEvent.getInstance();
	
	public static void give(String s_player, String number){
		int ticket_number = 0;
		Player player = null;
		
		try{
			player = Bukkit.getServer().getPlayer(s_player);
		}catch (Exception e){
			ne.showException(e);
			return;
		}
		
		if(ne.checkPlayer(player) == false) return;
		
		try {
			ticket_number = Integer.parseInt(number);
		} catch (NumberFormatException nfex) {
			ne.showException(nfex);
			return;
		}
		
		if(ticket_number > 0){			
			ne.getServer().dispatchCommand(ne.getServer().getConsoleSender(), "give " + s_player + name.replace("{number}", Integer.toString(ticket_number)));
			
			player.sendMessage(ChatColor.AQUA +" イベントチケット" + ChatColor.WHITE + "を" + ticket_number + "枚" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
			ne.getLogger().info(s_player + "に、イベントチケットを" + ticket_number + "枚追加しました。");
			ne.writeLog("Ticket:" + s_player + " +" + ticket_number );
		}
		
	}
	
	public static boolean remove(String s_player, String number) {
		int ticket_number = 0, player_ticket_amt = 0;
		Player player = Bukkit.getServer().getPlayer(s_player);
		
		if(ne.checkPlayer(player) == false) return false;
		
		try {
			ticket_number = Integer.parseInt(number);
		} catch (NumberFormatException nfex) {
			ne.showException(nfex);
			return false;
		}
		
		//Create EventTickets ItemStack
		String itemS_str = itemstack.replace("{number}", Integer.toString(ticket_number));
		
		//Get amount of EventTickets
		for(int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack itemStack = player.getInventory().getItem(i);
			String strItemStack = null;
			
			try{
				strItemStack = itemStack.toString();
			}catch(Exception e){
				ne.getLogger().warning("例外が発生したためスキップしました。");
				continue;
			}
			
			if(itemStack != null && strItemStack.indexOf(itemS_str) != -1){
				player_ticket_amt += itemStack.getAmount();
			}
		}
		
		//reject (shortage)
		if(player_ticket_amt < ticket_number){
			player.sendMessage( NekoEvent.ne_tag + ChatColor.YELLOW +"イベントチケットが" + (ticket_number - player_ticket_amt) + "枚不足しています。");
			return false;
		}

		for(int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack itemS = player.getInventory().getItem(i);
			String strItemStack = null;
			
			try{
				strItemStack = itemS.toString();
			}catch(Exception e){
				ne.getLogger().warning("例外が発生したためスキップしました。");
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
		
		ne.writeLog("Ticket:" + player.getName() + " -" + ticket_number );
		return true;
	}

}
