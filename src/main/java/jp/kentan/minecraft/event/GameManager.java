package jp.kentan.minecraft.event;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GameManager {
	public static int reward_rate = 0;
	
	private NekoEvent ne = null;
	private TPManager tp = null;
	private TicketManager ticket = null;
	private TimeManager time = null;
	
	public GameManager(NekoEvent ne, TPManager tp, TicketManager ticket, TimeManager time){
		this.ne = ne;
		this.tp = tp;
		this.ticket = ticket;
		this.time = time;
	}
	
	public void clearDungeon(String stage,String et_number, String strPlayer) {
		String path = strPlayer + ".dungeon." + stage;
		Player player = ne.convertToPlayer(strPlayer);
		
		if(player == null || !ne.isCheckPlayerOnline(player)) return;
		
		player.sendMessage(ChatColor.RED+ stage + "ダンジョン" + ChatColor.WHITE + "を" + ChatColor.AQUA + "クリア！");
		ne.writeLog("Dungeon:" + strPlayer + " clear:" + stage);
		
		if(time.checkOverDiffMinute(path, 30)){ //if over 30m,reset
			ne.getConfig().set(path + ".clear", false);
			ne.saveConfig();
		}

		if(ne.getConfig().getBoolean(path + ".clear") == false){
			ticket.give(strPlayer, et_number);
			ne.getConfig().set(path + ".last_minute", TimeManager.minute);
			ne.broadcastAll(player, NekoEvent.ne_tag + ChatColor.BLUE + strPlayer + ChatColor.WHITE + "が、" + ChatColor.RED + stage + "ダンジョン" + ChatColor.WHITE +"をクリアしました！");//
		}else{
			player.sendMessage(ChatColor.YELLOW +"イベントチケットは各ダンジョンで30分おきに入手できます。");
		}
		
		ne.getConfig().set(path + ".clear", true);
		ne.saveConfig();
	}

	public void clearParkour(String stage, String strPlayer) {
		String path = strPlayer + ".parkour." + stage;
		Player player = ne.convertToPlayer(strPlayer);
		
		if(player == null || !ne.isCheckPlayerOnline(player)) return;
		
		player.sendMessage(ChatColor.GREEN + stage + "アスレ" + ChatColor.WHITE + "を" + ChatColor.AQUA + "クリア！");
		ne.writeLog("Parkour:" + strPlayer + " clear:" + stage);
		
		if(time.checkOverDiffMinute(path, 1440)){ //if over 24h,reset
			ne.getConfig().set(path + ".clear", false);
			ne.saveConfig();
		}

		if(!ne.getConfig().getBoolean(path + ".clear")){
			ticket.give(strPlayer, "1");
			ne.getConfig().set(path + ".last_minute", TimeManager.minute);
			ne.broadcastAll(player, NekoEvent.ne_tag + ChatColor.BLUE + strPlayer + ChatColor.WHITE + "が、" + ChatColor.GREEN + stage + "アスレ" + ChatColor.WHITE +"をクリアしました！");
		}else{
			player.sendMessage(ChatColor.YELLOW +"イベントチケットは各アスレで24時間おきに入手できます。");
		}
		
		ne.getConfig().set(path + ".clear", true);
		ne.saveConfig();
	}
	
	public void reward(String s_player, String number){
		int rand = (int) (Math.random()*reward_rate);//0-5
		
		ne.writeLog("Minigame:" + s_player + " rand:" + rand + " rate:" + reward_rate);
		
		if(rand == reward_rate - 1) ticket.give(s_player,number);
	}
	
	public void removeItem(String strPlayer, String strItem){
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
				ne.sendInfoMessage(player.getName() + "のインベントリから" + itemS.getType() + "を消去しました。");
			}
		}
	}
	
	public void setItemAmount(String strPlayer, String strItem, String strAmount){
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
					
					ne.sendInfoMessage(player.getName() + "のインベントリから" + itemS.getType() + "を" + setAmount +"個にしました。");
				}
			}
		}
	}
	
	public void join(String strPlayer, String strStageName, String strJoinMsg) {
		String path = "TP." + strStageName;
		Player player = ne.convertToPlayer(strPlayer);
		
		int stageNumber = tp.getTPLocationNumber(strStageName),
			stageTimer = ne.getConfig().getInt(path + ".Timer");
		boolean isLock = ne.getConfig().getBoolean(path + ".Lock");
		
		if(stageNumber < 0 || player == null) return;
		
		if(!isLock || (isLock && time.isCheckTPTimer(stageNumber, stageTimer))){
			if(isLock) lock(strStageName, false);
			ne.broadcastAll(player, NekoEvent.ne_tag + strJoinMsg);
			tp.TP(strStageName, strPlayer);
			player.sendMessage(NekoEvent.ne_tag + strStageName + ChatColor.WHITE + "に参加しました。");
		}else{
			player.sendMessage(NekoEvent.ne_tag + "現在、" + strStageName + "では参加を受け付けていません。");
			player.sendMessage(NekoEvent.ne_tag + "参加中のプレイヤーを待つか、" + (stageTimer - time.getTPLockTimer(stageNumber)) + "秒お待ちください。");
		}
	}
	
	public void lock(String strStageName, boolean isLock) {
		String path = "TP." + strStageName;
		int stageNumber = tp.getTPLocationNumber(strStageName);
		
		if(stageNumber < 0 || stageNumber >= 20) return;

		ne.getConfig().set(path + ".Lock", isLock);
		ne.saveConfig();
		
		if(isLock) time.startTPLockTimer(stageNumber);
		
		ne.sendInfoMessage(strStageName + "のロックを" + isLock + "にしました。");
	}
	
	public void setSpawn(String strPlayer){
		Player player = ne.convertToPlayer(strPlayer);
		
		if(player == null) return;
		
		Location location = player.getLocation();

		player.setBedSpawnLocation(location, true);
		player.sendMessage(NekoEvent.ne_tag + "セーブしました!");
		ne.sendInfoMessage(strPlayer + "のスポーンを(" + location.getWorld().getName() + "," + (int)location.getX() + "," + (int)location.getY() + "," + (int)location.getZ() + 
				")にセット。");
	}
	
	public void setSpawn(String strPlayer, String strX, String strY, String strZ){
		Player player = ne.convertToPlayer(strPlayer);
		Double x = 0.0D, y = 0.0D, z = 0.0D;
		
		if(player == null) return;
		
		Location location = player.getLocation();
		
		try {
			x = Double.parseDouble(strX);
			y = Double.parseDouble(strY);
			z = Double.parseDouble(strZ);
		} catch (NumberFormatException e) {
			ne.sendErrorMessage("(" + strX + "," + strY + "," + strZ + ")を座標に変換できませんでした。");
			return;
		}
		
		location.setX(x);
		location.setY(y);
		location.setZ(z);

		player.setBedSpawnLocation(location, true);
		player.sendMessage(NekoEvent.ne_tag + "セーブしました!");
		ne.sendInfoMessage(strPlayer + "のスポーンを(" + location.getWorld().getName() + "," + (int)location.getX() + "," + (int)location.getY() + "," + (int)location.getZ() + 
				")にセット。");
	}
}
