package jp.kentan.minecraft.event;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TicketManager {
	public String name;
	public String itemstack;
	
	NekoEvent ne;

	public void setInstance(NekoEvent _ne) {
		ne = _ne;
	}
	
	private boolean checkPlayer(Player player){
		
		if(ne.checkInGame(player) == false){
			ne.getLogger().info("プレイヤーが見つかりません。");
			return false;
		}
		return true;
	}
	
	public void give(String s_player, String number){
		int ticket_number = 0;
		
		Player player = Bukkit.getServer().getPlayer(s_player);
		
		if(checkPlayer(player) == false) return;
		
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
	
	public boolean remove(String s_player, String number) {
		int ticket_number = 0;
		
		Player player = Bukkit.getServer().getPlayer(s_player);
		
		if(checkPlayer(player) == false) return true;
		
		try {
			ticket_number = Integer.parseInt(number);
		} catch (NumberFormatException nfex) {
			ne.showException(nfex);
			return false;
		}
		
		String itemS_str = itemstack.replace("{number}", Integer.toString(ticket_number));

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
				
				ne.writeLog("Ticket:" + player.getName() + " -" + ticket_number );
				return true;
			}
		}
		
		player.sendMessage(ChatColor.YELLOW +"イベントチケットが" + ticket_number + "枚不足しています。");

		return false;
	}

}
