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
	
	private NekoEvent ne = null;
	
	public TriggerManager(NekoEvent ne){
		this.ne = ne;
	}

	public boolean checkItem(String strPlayer, String strItemNum, String noItemInHand, String notMatchItem){
		int item;
		Player player = ne.convertToPlayer(strPlayer);
		
		try{
			item = Integer.parseInt(strItemNum);
		}catch (Exception e){
			ne.sendErrorMessage(strItemNum + "を整数型に変換できませんでした。");
			return false;
		}
		
		if(player  == null || !ne.isCheckPlayerOnline(player)) return false;
		
		if(item >= item_list.size()){
			ne.sendErrorMessage("TriggerItem(" + item + ")は登録されていません。");
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
		
		ne.sendInfoMessage("(" + thisLoc.getWorld().getName() + "," + loc[0] + "," + loc[1] + "," + loc[2] + ")にREDSTONE_TORCH_ONをセットしました。");
	}
}
