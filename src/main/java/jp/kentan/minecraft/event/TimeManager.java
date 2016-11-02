package jp.kentan.minecraft.event;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.scheduler.BukkitRunnable;

public class TimeManager extends BukkitRunnable{
	final static long MILLIS_TO_MINUTE = 60000;
	
	private static Calendar specialDay;
	private static HashMap<String, Integer> lockTimerMap = new HashMap<String, Integer>();
	
	private NekoEvent neko = null;
	private ConfigManager config = null;
	
	public TimeManager(NekoEvent neko, ConfigManager config){
		this.neko = neko;
		this.config = config;
	}
	
	@Override
    public void run() {
    	for(Map.Entry<String, Integer> entry : lockTimerMap.entrySet()){
    		int value = entry.getValue();
    		entry.setValue(value + 1);
    		if(value > 3600){ //over 1h
    			lockTimerMap.remove(entry);
    		}
    	}
    }
	
	public static void setup(Calendar _specialDay){
		specialDay = _specialDay;
	}
	
	public void initTPLockTimer(){
		lockTimerMap.clear();
		NekoEvent.sendInfoMessage("lockTimerを初期化.");
	}
	
	public boolean checkOverDiffMinute(String _path, int diff){
		long nowTime = Calendar.getInstance().getTimeInMillis(), lastTime = 0;
		
		String strLastDate = config.readString(_path);
		
		if(strLastDate != null){
			try {
				lastTime = ConfigManager.FORMATER_SEC.parse(strLastDate).getTime();
				
				if((int)((nowTime - lastTime) / MILLIS_TO_MINUTE) > diff){
					return true;
				}
			} catch (ParseException e) {
				neko.getLogger().warning(e.getMessage());
			}
		}else{
			return true;
		}
		return false;
	}
	
	public boolean checkSpecialDay(){
		Calendar today = Calendar.getInstance();
		
		if(today.get(Calendar.MONTH) == specialDay.get(Calendar.MONTH) && today.get(Calendar.DATE) == specialDay.get(Calendar.DATE)){
			return true;
		}
				
		return false;
	}
	
	public void startTPLockTimer(String stage){
		lockTimerMap.put(stage, 0);
		NekoEvent.sendInfoMessage("lockTimer(" + stage + ")を開始.");
	}
	
	public int getTPLockTimer(String stage){
		return lockTimerMap.get(stage);
	}
	
	public boolean isCheckTPTimer(String stage, int unlockTimer){
		if(lockTimerMap.get(stage) == null || lockTimerMap.get(stage) > unlockTimer){
			lockTimerMap.remove(stage);
			return true;
		}else {
			return false;
		}
	}
}
