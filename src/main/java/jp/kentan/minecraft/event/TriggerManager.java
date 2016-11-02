package jp.kentan.minecraft.event;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TriggerManager {
	public static List<String> item_list = new ArrayList<String>();
	
	public TriggerManager(){
	}

	public boolean checkItem(String strPlayer, String strItemNum, String noItemInHand, String notMatchItem){
		int item = Utils.parseInt(strItemNum);
		Player player = Utils.toPlayer(strPlayer);
		
		if(player  == null || !Utils.isOnline(player)) return false;
		
		if(item >= item_list.size()){
			NekoEvent.sendErrorMessage("TriggerItem(" + item + ")は登録されていません.");
			return false;
		}
		
		ItemStack itemStack = player.getInventory().getItemInMainHand();
		
		if(itemStack == null || itemStack.getAmount() < 1){
			if(!noItemInHand.equals("null")) player.sendMessage(" " + ChatColor.GRAY + ChatColor.ITALIC + noItemInHand);
			return false;
		}
		
		if(itemStack.toString().indexOf(item_list.get(item)) != -1){
			int amt = itemStack.getAmount() - 1;
			
			itemStack.setAmount(amt);
			player.getInventory().setItemInMainHand(amt > 0 ? itemStack : null);
			player.updateInventory();
			
			return true;
		}else{
			if(!notMatchItem.equals("null")) player.sendMessage(" " + ChatColor.GRAY + ChatColor.ITALIC + notMatchItem);
		}
		
		return false;
	}
	
	public void setTorch(Location thisLoc, String[] strLoc){
		double[] loc = {Double.parseDouble(strLoc[0]),Double.parseDouble(strLoc[1]),Double.parseDouble(strLoc[2])};
		Location setLoc = new Location(thisLoc.getWorld(), loc[0], loc[1], loc[2]);
		
		setLoc.getBlock().setType(Material.REDSTONE_TORCH_ON);
		
		NekoEvent.sendInfoMessage("(" + thisLoc.getWorld().getName() + "," + loc[0] + "," + loc[1] + "," + loc[2] + ")にREDSTONE_TORCH_ONをセット.");
	}
}
