package jp.kentan.minecraft.event;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TicketManager {
	public static String name, itemstack;
	
	private NekoEvent ne = null;
	
	public TicketManager(NekoEvent ne){
		this.ne = ne;
	}
	
	public void give(String strPlayer, String number){
		int ticket_number = 0;
		Player player = ne.convertToPlayer(strPlayer);
		
		if(player == null || !ne.isCheckPlayerOnline(player)) return;
		
		try {
			ticket_number = Integer.parseInt(number);
		} catch (NumberFormatException nfex) {
			ne.sendErrorMessage(number + "を整数型に変換できませんでした。");
			return;
		}
		
		if(ticket_number > 0){			
			ne.getServer().dispatchCommand(ne.getServer().getConsoleSender(), "give " + strPlayer + name.replace("{number}", Integer.toString(ticket_number)));
			
			player.sendMessage(ChatColor.AQUA +" イベントチケット" + ChatColor.WHITE + "を" + ticket_number + "枚" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
			ne.sendInfoMessage(strPlayer + "に、イベントチケットを" + ticket_number + "枚追加しました。");
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
			ne.sendErrorMessage(number + "を整数型に変換できませんでした。");
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
				if(itemStack != null && itemStack.getAmount() > 0) ne.getLogger().warning("例外が発生したためスキップしました。");
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
				if(itemS != null && itemS.getAmount() > 0) ne.getLogger().warning("例外が発生したためスキップしました。");
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
