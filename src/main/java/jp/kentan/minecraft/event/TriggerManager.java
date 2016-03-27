package jp.kentan.minecraft.event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TriggerManager {
	public static String triggerItem[] = new String[10];
	
	static NekoEvent ne = NekoEvent.getInstance();

	public static boolean checkItem(String strPlayer, String strItemNum, String noItemInHand, String notMatchItem){
		int item;
		Player player;
		
		try{
			player = Bukkit.getServer().getPlayer(strPlayer);
			item = Integer.parseInt(strItemNum);
		}catch (Exception e){
			ne.showException(e);
			return false;
		}
		
		if(!ne.checkPlayer(player)) return false;
		
		ItemStack itemStack = player.getInventory().getItemInHand();
		
		if(itemStack == null){
			if(!noItemInHand.equals("null")) player.sendMessage(noItemInHand);
			return false;
		}
		
		if(itemStack.toString().indexOf(triggerItem[item]) != -1){
			int amt = itemStack.getAmount() - 1;
			
			itemStack.setAmount(amt);
			player.getInventory().setItemInHand(amt > 0 ? itemStack : null);
			player.updateInventory();
			
			return true;
		}else{
			if(!notMatchItem.equals("null")) player.sendMessage(notMatchItem);
		}
		
		return false;
	}
	
	public static void setTorch(Location thisLoc, String[] strLoc){
		double[] loc = {Double.parseDouble(strLoc[0]),Double.parseDouble(strLoc[1]),Double.parseDouble(strLoc[2])};
		Location setLoc = new Location(thisLoc.getWorld(), loc[0], loc[1], loc[2]);
		
		setLoc.getBlock().setType(Material.REDSTONE_TORCH_ON);
	}
}
