package jp.kentan.minecraft.event;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TimeManager extends BukkitRunnable{
	public static int tp = 0, tp_limit = 600, sec = 0, minute = 0, month = 0, day = 0;
	
	static NekoEvent ne = NekoEvent.getInstance();
	
	@Override
    public void run() {
		if(tp > tp_limit + 60) tp = 0; //reset over 10m
    	if(tp > 0) tp++;     //count 0-1m
    	
    	if(sec >= 59){
    		sec = -1;
    		minute++;
    		if(minute % 60 == 0) ConfigManager.save();
    	}
    	sec++;
    }
	
	public static boolean checkOverDiffMinute(String _path, int baseDiff){
		int last_minute = 0;
		
		if(ne.getConfig().getString(_path + ".clear") != null){
			last_minute = ne.getConfig().getInt(_path + ".last_minute");
			if(Math.abs(minute - last_minute) > baseDiff) return true;
		}
		return false;
	}

	public static boolean checkOverTPTime(String s_player){
		Player player = Bukkit.getServer().getPlayer(s_player);
		
		if(tp > 60){
			player.sendMessage(ChatColor.RED + "初回のプレイヤー参加から１分が経過しました。");
			player.sendMessage(ChatColor.YELLOW + "プレイヤーがダンジョンをクリアするか、" + (tp_limit - tp + 61) + "秒経過するまで参加できません。");
			
			ne.getLogger().info(player.getName() + "がダンジョンへの参加をリジェクトされました。(over 1m)");
			return false;
		}
		return true;
	}
	
	public static boolean checkSpecialDay(){
		Calendar calendar = Calendar.getInstance();
		
		if(calendar.get(Calendar.MONTH) + 1 == month && calendar.get(Calendar.DATE) == day) return true;
				
		return false;
	}
}
