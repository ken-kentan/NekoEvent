package jp.kentan.minecraft.event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TPManager {

	static NekoEvent ne = NekoEvent.getInstance();
	
	public static void TP(Player player, Location thisLoc ,String[] strLoc){
		boolean[] isRelative = {false,false,false};
		double[] loc = {thisLoc.getX(),thisLoc.getY(),thisLoc.getZ()};
		
		if(player == null){
			ne.sendErrorMessage("変数playerが空です。");
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
	
		ne.getLogger().info(player.getName() + "を(" + location.getWorld().getName() + "," + loc[0] + "," + loc[1] + "," + loc[2]+ ")にTPしました。");
	}

	public static void set(Player player,String strNo, String strName, String strTimer) {
		String path = "TP." + strName;

		if (ne.checkPlayer(player) == false) return;
		
		int stageNumber = Integer.parseInt(strNo);
		int stageTimer = Integer.parseInt(strTimer);
		
		if(stageNumber < 0 || stageNumber >= 20){
			ne.sendErrorMessage("ステージナンバーは0~19で指定する必要があります。");
			return;
		}
		
		if(stageTimer < 0 || stageNumber > 5000){
			ne.sendErrorMessage("自動解除タイマーは0~5000で指定する必要があります。");
			return;
		}
		
		ne.getConfig().set(path + ".No", strNo);
		ne.getConfig().set(path + ".Lock", false);
		ne.getConfig().set(path + ".Timer", strTimer);

		Location location = player.getLocation();
		ne.getConfig().set(path + ".X", location.getX());
		ne.getConfig().set(path + ".Y", location.getY());
		ne.getConfig().set(path + ".Z", location.getZ());
		ne.getConfig().set(path + ".Yaw", location.getYaw());
		ne.getConfig().set(path + ".Pitch", location.getPitch());
		ne.saveConfig();
	}
	
	public static int getTPLocationNumber(String strStage){
		String path = "TP." + strStage;
		int stageNumber;
		stageNumber = ne.getConfig().getInt(path + ".No");
		
		return stageNumber;
	}

	public static void TP(String tp, String strPlayer) {
		Player player = ne.convertToPlayer(strPlayer);
		String path = "TP." + tp;
		
		if (player == null || !ne.checkPlayer(player)) return;

		Location location = player.getLocation();
		location.setX(ne.getConfig().getDouble(path + ".X"));
		location.setY(ne.getConfig().getDouble(path + ".Y"));
		location.setZ(ne.getConfig().getDouble(path + ".Z"));
		location.setYaw((float) ne.getConfig().getDouble(path + ".Yaw"));
		location.setPitch((float) ne.getConfig().getDouble(path + ".Pitch"));

		player.teleport(location);

		// count startne
		if (TimeManager.tp == 0)
			TimeManager.tp = 1;
		
		ne.getLogger().info(player.getName() + "を" + tp + "にTPしました。");
	}
	
	public static void areaTP(float range, Location thisLoc , String[] strLoc){
		boolean[] isRelative = {false,false,false};
		double[] loc = new double[3];
		
		loc[0] = thisLoc.getX();
		loc[1] = thisLoc.getY();
		loc[2] = thisLoc.getZ();
		
		for(int i=0; i<3; i++){
			if(strLoc[i].indexOf("~") != -1){
				isRelative[i] = true;
				loc[i] = Double.parseDouble(strLoc[i].replace("~", ""));
			}
			else {
				loc[i] = Double.parseDouble(strLoc[i]);
			}
		}
		
		
		for(Player player : Bukkit.getServer().getOnlinePlayers()){
			if(player.getWorld() == thisLoc.getWorld() && thisLoc.distance(player.getLocation()) <= (double)range){
				Location playerLoc = player.getLocation();
				
				if(isRelative[0]) loc[0] += playerLoc.getX();
				if(isRelative[1]) loc[1] += playerLoc.getY();
				if(isRelative[2]) loc[2] += playerLoc.getZ();
				
				Location location = new Location(thisLoc.getWorld(), loc[0], loc[1], loc[2], playerLoc.getYaw(), playerLoc.getPitch());
				
				player.teleport(location);
				ne.getLogger().info(player.getName() + "を(" + location.getWorld().getName() + "," + loc[0] + "," + loc[1] + "," + loc[2] + ")にareaTPしました。");
				
				if(isRelative[0]) loc[0] -= playerLoc.getX();
				if(isRelative[1]) loc[1] -= playerLoc.getY();
				if(isRelative[2]) loc[2] -= playerLoc.getZ();
			}
		}
	}
}
