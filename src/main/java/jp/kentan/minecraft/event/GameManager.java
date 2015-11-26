package jp.kentan.minecraft.event;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GameManager {
	public static int reward_rate = 0;
	
	NekoEvent ne;
	TicketManager ticket = new TicketManager();
	TimeManager time = new TimeManager();
	
	public void setInstance(NekoEvent _ne){
		ne = _ne;
	}
	
	private boolean checkPlayer(Player player) {

		if (ne.checkInGame(player) == false) {
			ne.getLogger().info("プレイヤーが見つかりません。");
			return false;
		}
		return true;
	}
	
	public void clearDungeon(String stage, String s_player) {
		String path = s_player + ".dungeon." + stage;
		Player player = Bukkit.getServer().getPlayer(s_player);
		
		if(checkPlayer(player) == false) return;
		
		player.sendMessage(ChatColor.BLUE + stage + ChatColor.AQUA + "ダンジョンをクリア！");
		ne.writeLog("Dungeon:" + s_player + " clear:" + stage);
		
		if(time.checkOverDiffMinute(path, 1440)){ //if over 24h,reset
			ne.getConfig().set(path + "clear", false);
			ne.saveConfig();
		}

		if(ne.getConfig().getBoolean(path + ".clear") == false){
			ticket.give(s_player, "5");
			ne.getConfig().set(path + ".last_minute", TimeManager.minute);
		}else{
			player.sendMessage(ChatColor.YELLOW +"イベントチケットは各ダンジョンで24時間おきに入手できます。");
		}
		
		ne.getConfig().set(path + "clear", true);
		ne.saveConfig();
		
		TimeManager.tp = 0;//reset
	}

	public void clearParkour(String stage, String s_player) {
		String path = s_player + ".parkour." + stage;
		Player player = Bukkit.getServer().getPlayer(s_player);
		
		if(checkPlayer(player) == false) return;
		
		player.sendMessage(ChatColor.GREEN + stage + ChatColor.AQUA + "アスレをクリア！");
		ne.writeLog("Parkour:" + s_player + " clear:" + stage);
		
		if(time.checkOverDiffMinute(path, 1440)){ //if over 24h,reset
			ne.getConfig().set(path + "clear", false);
			ne.saveConfig();
		}

		if(ne.getConfig().getBoolean(path + ".clear") == false){
			ticket.give(s_player, "1");
			ne.getConfig().set(path + ".last_minute", TimeManager.minute);
		}else{
			player.sendMessage(ChatColor.YELLOW +"イベントチケットは各アスレで24時間おきに入手できます。");
		}
		
		ne.getConfig().set(path + "clear", true);
		ne.saveConfig();
	}
	
	public void reward(String s_player, String number){
		int rand = (int) (Math.random()*reward_rate);//0-5
		
		ne.writeLog("Minigame:" + s_player + " rand:" + rand + " rate:" + reward_rate);
		
		if(rand == reward_rate - 1) ticket.give(s_player,number);
	}
}
