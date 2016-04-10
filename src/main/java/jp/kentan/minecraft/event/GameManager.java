package jp.kentan.minecraft.event;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GameManager {
	public static int reward_rate = 0;
	
	static NekoEvent ne = NekoEvent.getInstance();
	
	public static void clearDungeon(String stage,String et_number, String strPlayer) {
		String path = strPlayer + ".dungeon." + stage;
		Player player = ne.convertToPlayer(strPlayer);
		
		if(player == null || !ne.checkPlayer(player)) return;
		
		player.sendMessage(ChatColor.RED+ stage + "ダンジョン" + ChatColor.WHITE + "を" + ChatColor.AQUA + "クリア！");
		ne.writeLog("Dungeon:" + strPlayer + " clear:" + stage);
		
		if(TimeManager.checkOverDiffMinute(path, 30)){ //if over 30m,reset
			ne.getConfig().set(path + ".clear", false);
			ne.saveConfig();
		}

		if(ne.getConfig().getBoolean(path + ".clear") == false){
			TicketManager.give(strPlayer, et_number);
			ne.getConfig().set(path + ".last_minute", TimeManager.minute);
			ne.broadcastAll(player, NekoEvent.ne_tag + ChatColor.BLUE + strPlayer + ChatColor.WHITE + "が、" + ChatColor.RED + stage + "ダンジョン" + ChatColor.WHITE +"をクリアしました！");//
		}else{
			player.sendMessage(ChatColor.YELLOW +"イベントチケットは各ダンジョンで30分おきに入手できます。");
		}
		
		ne.getConfig().set(path + ".clear", true);
		ne.saveConfig();
	}

	public static void clearParkour(String stage, String strPlayer) {
		String path = strPlayer + ".parkour." + stage;
		Player player = ne.convertToPlayer(strPlayer);
		
		if(player == null || !ne.checkPlayer(player)) return;
		
		player.sendMessage(ChatColor.GREEN + stage + "アスレ" + ChatColor.WHITE + "を" + ChatColor.AQUA + "クリア！");
		ne.writeLog("Parkour:" + strPlayer + " clear:" + stage);
		
		if(TimeManager.checkOverDiffMinute(path, 1440)){ //if over 24h,reset
			ne.getConfig().set(path + ".clear", false);
			ne.saveConfig();
		}

		if(ne.getConfig().getBoolean(path + ".clear") == false){
			TicketManager.give(strPlayer, "1");
			ne.getConfig().set(path + ".last_minute", TimeManager.minute);
			ne.broadcastAll(player, NekoEvent.ne_tag + ChatColor.BLUE + strPlayer + ChatColor.WHITE + "が、" + ChatColor.GREEN + stage + "アスレ" + ChatColor.WHITE +"をクリアしました！");
		}else{
			player.sendMessage(ChatColor.YELLOW +"イベントチケットは各アスレで24時間おきに入手できます。");
		}
		
		ne.getConfig().set(path + ".clear", true);
		ne.saveConfig();
	}
	
	public static void reward(String s_player, String number){
		int rand = (int) (Math.random()*reward_rate);//0-5
		
		ne.writeLog("Minigame:" + s_player + " rand:" + rand + " rate:" + reward_rate);
		
		if(rand == reward_rate - 1) TicketManager.give(s_player,number);
	}
	
	public static void removeItem(String strPlayer, String strItem){
		Player player = Bukkit.getServer().getPlayer(strPlayer);
		
		for(int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack itemS = player.getInventory().getItem(i);
			String strItemStack = null;
			
			try{
				strItemStack = itemS.toString();
			}catch(Exception e){
				if(itemS != null && itemS.getAmount() > 0) ne.getLogger().warning("例外が発生したためスキップしました。");
				continue;
			}

			if(itemS != null && strItemStack.indexOf(strItem) != -1){
				player.getInventory().setItem(i, null);
				player.updateInventory();
				ne.getLogger().info(player.getName() + "のインベントリから" + itemS.getType() + "を消去しました。");
			}
		}
	}
	
	public static void setItemAmount(String strPlayer, String strItem, String strAmount){
		Player player = Bukkit.getServer().getPlayer(strPlayer);
		int amount = Integer.parseInt(strAmount), sumAmount = 0;
		
		for(int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack itemS = player.getInventory().getItem(i);
			String strItemStack = null;
			
			try{
				strItemStack = itemS.toString();
			}catch(Exception e){
				if(itemS != null && itemS.getAmount() > 0) ne.getLogger().warning("例外が発生したためスキップしました。");
				continue;
			}

			if(itemS != null && strItemStack.indexOf(strItem) != -1){
				sumAmount += itemS.getAmount();
				if(sumAmount > amount){
					int setAmount = itemS.getAmount() - (sumAmount - amount);
					
					if(setAmount > 0) itemS.setAmount(setAmount);
					else player.getInventory().setItem(i, null);
					
					player.updateInventory();
					
					ne.getLogger().info(player.getName() + "のインベントリから" + itemS.getType() + "を" + setAmount +"個にしました。");
				}
			}
		}
	}
	
	public static void join(String strPlayer, String strStageName) {
		String path = "TP." + strStageName;
		Player player = ne.convertToPlayer(strPlayer);
		
		int stageNumber = TPManager.getTPLocationNumber(strStageName),
			stageTimer = ne.getConfig().getInt(path + ".Timer");
		boolean isLock = ne.getConfig().getBoolean(path + ".Lock");
		
		if(stageNumber < 0 || player == null) return;
		
		if(!isLock || (isLock && TimeManager.isCheckTPTimer(stageNumber, stageTimer))){
			if(isLock) lock(strStageName, false);
			ne.broadcastAll(player, NekoEvent.ne_tag + ChatColor.BLUE + strPlayer + ChatColor.WHITE + "が" + ChatColor.GOLD + strStageName + ChatColor.WHITE + "に参加しました。");
			TPManager.TP(strStageName, strPlayer);
		}else{
			player.sendMessage(NekoEvent.ne_tag + "現在、" + strStageName + "では参加を受け付けていません。");
			player.sendMessage(NekoEvent.ne_tag + "参加中のプレイヤーを待つか、" + (stageTimer - TimeManager.getTPLockTimer(stageNumber)) + "秒お待ちください。");
		}
	}
	
	public static void lock(String strStageName, boolean isLock) {
		String path = "TP." + strStageName;
		int stageNumber = TPManager.getTPLocationNumber(strStageName);

		ne.getConfig().set(path + ".Lock", isLock);
		ne.saveConfig();
		
		if(isLock) TimeManager.startTPLockTimer(stageNumber);
		
		ne.getLogger().info(strStageName + "のロックを" + isLock + "にしました。");
	}
}
