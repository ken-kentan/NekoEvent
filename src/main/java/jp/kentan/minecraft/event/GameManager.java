package jp.kentan.minecraft.event;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GameManager {
	public static int reward_rate = 0;
	
	static NekoEvent ne = NekoEvent.getInstance();
	
	public static void clearDungeon(String stage,String et_number, String s_player) {
		String path = s_player + ".dungeon." + stage;
		Player player = Bukkit.getServer().getPlayer(s_player);
		
		if(ne.checkPlayer(player) == false) return;
		
		player.sendMessage(ChatColor.RED+ stage + "ダンジョン" + ChatColor.WHITE + "を" + ChatColor.AQUA + "クリア！");
		ne.writeLog("Dungeon:" + s_player + " clear:" + stage);
		
		if(TimeManager.checkOverDiffMinute(path, 30)){ //if over 30m,reset
			ne.getConfig().set(path + ".clear", false);
			ne.saveConfig();
		}

		if(ne.getConfig().getBoolean(path + ".clear") == false){
			TicketManager.give(s_player, et_number);
			ne.getConfig().set(path + ".last_minute", TimeManager.minute);
			ne.broadcastAll(player, NekoEvent.ne_tag + ChatColor.BLUE + s_player + ChatColor.WHITE + "が、" + ChatColor.RED + stage + "ダンジョン" + ChatColor.WHITE +"をクリアしました！");//
		}else{
			player.sendMessage(ChatColor.YELLOW +"イベントチケットは各ダンジョンで30分おきに入手できます。");
		}
		
		ne.getConfig().set(path + ".clear", true);
		ne.saveConfig();
		
		TimeManager.tp = 0;//reset
	}

	public static void clearParkour(String stage, String s_player) {
		String path = s_player + ".parkour." + stage;
		Player player = Bukkit.getServer().getPlayer(s_player);
		
		if(ne.checkPlayer(player) == false) return;
		
		player.sendMessage(ChatColor.GREEN + stage + "アスレ" + ChatColor.WHITE + "を" + ChatColor.AQUA + "クリア！");
		ne.writeLog("Parkour:" + s_player + " clear:" + stage);
		
		if(TimeManager.checkOverDiffMinute(path, 1440)){ //if over 24h,reset
			ne.getConfig().set(path + ".clear", false);
			ne.saveConfig();
		}

		if(ne.getConfig().getBoolean(path + ".clear") == false){
			TicketManager.give(s_player, "1");
			ne.getConfig().set(path + ".last_minute", TimeManager.minute);
			ne.broadcastAll(player, NekoEvent.ne_tag + ChatColor.BLUE + s_player + ChatColor.WHITE + "が、" + ChatColor.GREEN + stage + "アスレ" + ChatColor.WHITE +"をクリアしました！");
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
			if(itemS != null && itemS.toString().indexOf(strItem) != -1){//リンゴ
				player.getInventory().setItem(i, null);
				player.updateInventory();
				ne.getLogger().info(player.getName() + "のインベントリから" + itemS.getType() + "を消去しました。");
			}
		}
	}
}
