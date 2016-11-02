package jp.kentan.minecraft.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TPManager {
	private NekoEvent ne;
	private ConfigManager config;
	
	public TPManager(NekoEvent ne, ConfigManager config){
		this.ne = ne;
		this.config = config;
	}
	
	public void TP(Player player, Location thisLoc ,String[] strLoc){
		boolean[] isRelative = {false,false,false};
		double[] loc = {thisLoc.getX(),thisLoc.getY(),thisLoc.getZ()};
		
		if(player == null){
			NekoEvent.sendErrorMessage("変数playerが空です.");
			return;
		}
		
		for(int i=0; i<3; i++){
			if(strLoc[i].indexOf("~") != -1){
				isRelative[i] = true;
				loc[i] = Double.parseDouble(strLoc[i].replace("~", ""));
			}
			else {
				loc[i] = Double.parseDouble(strLoc[i]);
			}
		}

		Location playerLoc = player.getLocation();
		
		if(isRelative[0]) loc[0] += playerLoc.getX();
		if(isRelative[1]) loc[1] += playerLoc.getY();
		if(isRelative[2]) loc[2] += playerLoc.getZ();
		
		Location location = new Location(thisLoc.getWorld(), loc[0], loc[1], loc[2], playerLoc.getYaw(), playerLoc.getPitch());
		
		player.teleport(location);
	
		NekoEvent.sendInfoMessage("TP " + player.getName() + "(" + location.getWorld().getName() + "," + (int)loc[0] + "," + (int)loc[1] + "," + (int)loc[2]+ ").");
	}

	public boolean set(Player player, String strName, String strTimer) {
		String path = "TP." + strName;

		if (!Utils.isOnline(player)){
			return false;
		}
		
		int stageTimer = Integer.parseInt(strTimer);
		
		if(stageTimer < 0 || stageTimer > 3600){
			NekoEvent.sendErrorMessage("自動解除タイマーは1~3600(秒)で指定する必要があります.");
			return false;
		}
		
		if(ne.getConfig().getString(path + ".Lock") != null){
			NekoEvent.sendErrorMessage(strName + "は既に登録されています.");
			return false;
		}
		
		ne.getConfig().set(path + ".Lock", false);
		ne.getConfig().set(path + ".Timer", stageTimer);
		
		config.saveLocation(player.getLocation(), path);
		
		return true;
	}
	


	public void TP(String tp, String strPlayer) {
		Player player = Utils.toPlayer(strPlayer);
		String path = "TP." + tp;
		
		if (player == null || !Utils.isOnline(player)) return;

		Location location = config.readLocation(player, path);
		
		if(location == null) return;

		player.teleport(location);
		
		NekoEvent.sendInfoMessage("TP " + strPlayer + "(" + tp + ").");
	}
}
