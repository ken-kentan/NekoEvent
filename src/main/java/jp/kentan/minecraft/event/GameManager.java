package jp.kentan.minecraft.event;

import java.util.Calendar;

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
	
	final private static String MSG_CLEAR = ChatColor.WHITE + "を" + ChatColor.AQUA + "クリア！";
	
	public GameManager(NekoEvent ne, TPManager tp, TicketManager ticket, TimeManager time){
		this.ne = ne;
		this.tp = tp;
		this.ticket = ticket;
		this.time = time;
	}
	
	public void clearDungeon(String stage,String et_number, String strPlayer) {
		String path = strPlayer + ".dungeon." + stage;
		Player player = Utils.toPlayer(strPlayer);
		
		if(player == null || !Utils.isOnline(player)) return;
		
		player.sendMessage(NekoEvent.CHAT_TAG + ChatColor.RED + stage + "ダンジョン" + MSG_CLEAR);
		Log.write("Dungeon:" + strPlayer + " clear:" + stage);
		
		if(time.checkOverDiffMinute(path + ".last_date", 30)){ //if over 30m,reset
			ne.getConfig().set(path + ".last_date", ConfigManager.FORMATER_SEC.format(Calendar.getInstance().getTime()));
			ne.saveConfig();
			
			ticket.give(strPlayer, et_number);
			ne.broadcast(player, NekoEvent.CHAT_TAG + ChatColor.BLUE + strPlayer + ChatColor.WHITE + "が、" + ChatColor.RED + stage + "ダンジョン" + ChatColor.WHITE +"をクリアしました！");
		}else{
			player.sendMessage(NekoEvent.CHAT_TAG + ChatColor.YELLOW +"イベントチケットは各ダンジョンで30分おきに入手できます。");
		}
	}

	public void clearParkour(String stage, String strPlayer) {
		String path = strPlayer + ".parkour." + stage;
		Player player = Utils.toPlayer(strPlayer);
		
		if(player == null || !Utils.isOnline(player)) return;
		
		player.sendMessage(NekoEvent.CHAT_TAG + ChatColor.GREEN + stage + "アスレ" + MSG_CLEAR);
		Log.write("Parkour:" + strPlayer + " clear:" + stage);
		
		if(time.checkOverDiffMinute(path + ".last_date", 1440)){ //if over 24h,reset
			ne.getConfig().set(path + ".last_date", ConfigManager.FORMATER_SEC.format(Calendar.getInstance().getTime()));
			ne.saveConfig();
			
			ticket.give(strPlayer, 1);
			ne.broadcast(player, NekoEvent.CHAT_TAG + ChatColor.BLUE + strPlayer + ChatColor.WHITE + "が、" + ChatColor.GREEN + stage + "アスレ" + ChatColor.WHITE +"をクリアしました！");
		}else{
			player.sendMessage(NekoEvent.CHAT_TAG + ChatColor.YELLOW +"イベントチケットは各アスレで24時間おきに入手できます。");
		}
	}
	
	public void reward(String s_player, String number){
		int rand = (int) (Math.random()*reward_rate);//0-5
		
		Log.write("Minigame:" + s_player + " rand:" + rand + " rate:" + reward_rate);
		
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
				if(itemS != null && itemS.getAmount() > 0) ne.getLogger().warning("Skip unknown item.");
				continue;
			}

			if(itemS != null && strItemStack.indexOf(strItem) != -1){
				player.getInventory().setItem(i, null);
				player.updateInventory();
				NekoEvent.sendInfoMessage(player.getName() + "のインベントリから" + itemS.getType() + "を消去しました.");
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
				if(itemS != null && itemS.getAmount() > 0) ne.getLogger().warning("Skip unknown item.");
				continue;
			}

			if(itemS != null && strItemStack.indexOf(strItem) != -1){
				sumAmount += itemS.getAmount();
				if(sumAmount > amount){
					int setAmount = itemS.getAmount() - (sumAmount - amount);
					
					if(setAmount > 0) itemS.setAmount(setAmount);
					else player.getInventory().setItem(i, null);
					
					player.updateInventory();
					
					NekoEvent.sendInfoMessage(player.getName() + "のインベントリから" + itemS.getType() + "を" + amount +"個にしました。");
				}
			}
		}
	}
	
	public void join(String strPlayer, String strStageName, String strJoinMsg) {
		String path = "TP." + strStageName;
		Player player = Utils.toPlayer(strPlayer);
		
		int stageTimer = ne.getConfig().getInt(path + ".Timer");
		boolean isLock = ne.getConfig().getBoolean(path + ".Lock");
		
		if(player == null) return;
		
		if(!isLock || (isLock && time.isCheckTPTimer(strStageName, stageTimer))){
			if(isLock) lock(strStageName, false);
			ne.broadcast(player, NekoEvent.CHAT_TAG + strJoinMsg);
			tp.TP(strStageName, strPlayer);
			player.sendMessage(NekoEvent.CHAT_TAG + strStageName + ChatColor.WHITE + "に参加しました。");
		}else{
			player.sendMessage(NekoEvent.CHAT_TAG + "現在、" + strStageName + "では参加を受け付けていません。");
			player.sendMessage(NekoEvent.CHAT_TAG + "参加中のプレイヤーを待つか、" + (stageTimer - time.getTPLockTimer(strStageName)) + "秒お待ちください。");
		}
	}
	
	public void lock(String strStageName, boolean isLock) {
		String path = "TP." + strStageName;

		ne.getConfig().set(path + ".Lock", isLock);
		ne.saveConfig();
		
		if(isLock) time.startTPLockTimer(strStageName);
		
		if(isLock){
			NekoEvent.sendInfoMessage(strStageName + "をロック.");
		}else{
			NekoEvent.sendInfoMessage(strStageName + "のロックを解除.");
		}
		
	}
	
	public void setSpawn(String strPlayer){
		Player player = Utils.toPlayer(strPlayer);
		
		if(player == null) return;
		
		Location location = player.getLocation();

		player.setBedSpawnLocation(location, true);
		player.sendMessage(NekoEvent.CHAT_TAG + "セーブしました!");
		NekoEvent.sendInfoMessage(strPlayer + "のスポーンを(" + location.getWorld().getName() + "," + (int)location.getX() + "," + (int)location.getY() + "," + (int)location.getZ() + ")にセット."
				+ "にセット.");
	}
	
	public void setSpawn(String strPlayer, String strX, String strY, String strZ){
		Player player = Utils.toPlayer(strPlayer);
		
		if(player == null) return;
		
		Location location = Utils.toLocation(player, strX, strY, strZ);
		
		if(location == null) return;

		player.setBedSpawnLocation(location, true);
		player.sendMessage(NekoEvent.CHAT_TAG + "セーブしました!");
		NekoEvent.sendInfoMessage(strPlayer + "のスポーンを(" + location.getWorld().getName() + "," + (int)location.getX() + "," + (int)location.getY() + "," + (int)location.getZ() + ")にセット.");
	}
}
