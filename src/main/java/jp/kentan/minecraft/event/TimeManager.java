package jp.kentan.minecraft.event;

import java.util.Calendar;

import org.bukkit.scheduler.BukkitRunnable;

public class TimeManager extends BukkitRunnable{
	public static int sec = 0, minute = 0, month = 0, day = 0;
	private static int tpLockTimer[] = new int[20];
	
	static NekoEvent ne = NekoEvent.getInstance();
	
	@Override
    public void run() {    	
    	if(sec++ >= 59){
    		sec = 0;
    		minute++;
    		if(minute % 60 == 0) ConfigManager.save();
    	}
    	
    	for(int i=0; i<20; i++) if(tpLockTimer[i] > 0 && tpLockTimer[i] < 5000) tpLockTimer[i]++;
    }
	
	public static void initTPLockTimer(){
		for(int i=0;i<20;i++) tpLockTimer[i] = -1;
		ne.getLogger().info("Initialized All TP Lock Timer.");
	}
	
	public static boolean checkOverDiffMinute(String _path, int baseDiff){
		int last_minute = 0;
		
		if(ne.getConfig().getString(_path + ".clear") != null){
			last_minute = ne.getConfig().getInt(_path + ".last_minute");
			if(Math.abs(minute - last_minute) > baseDiff) return true;
		}
		return false;
	}
	
	public static boolean checkSpecialDay(){
		Calendar calendar = Calendar.getInstance();
		
		if(calendar.get(Calendar.MONTH) + 1 == month && calendar.get(Calendar.DATE) == day) return true;
				
		return false;
	}
	
	public static void startTPLockTimer(int stageNumber){
		tpLockTimer[stageNumber] = 1;
	}
	
	public static int getTPLockTimer(int stageNumber){
		return tpLockTimer[stageNumber];
	}
	
	public static boolean isCheckTPTimer(int stageNumber, int unlockTimer){
		if(tpLockTimer[stageNumber] == -1 || tpLockTimer[stageNumber] > unlockTimer){
			tpLockTimer[stageNumber] = 0;
			return true;
		}else {return false;}
	}
}
