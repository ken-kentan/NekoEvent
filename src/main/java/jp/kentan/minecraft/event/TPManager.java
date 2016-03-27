package jp.kentan.minecraft.event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TPManager {

	static NekoEvent ne = NekoEvent.getInstance();
	
	public static void TP(Player player, Location thisLoc ,String[] strLoc){
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

		Location playerLoc = player.getLocation();
		
		if(isRelative[0]) loc[0] += playerLoc.getX();
		if(isRelative[1]) loc[1] += playerLoc.getY();
		if(isRelative[2]) loc[2] += playerLoc.getZ();
		
		Location location = new Location(thisLoc.getWorld(), loc[0], loc[1], loc[2]);
		
		player.teleport(location);
	
		ne.getLogger().info(player.getName() + "を(" + location.getWorld().getName() + "," + loc[0] + "," + loc[1] + "," + loc[2]+ ")にTPしました。");
	}

	public static void set(Player player, String tp) {
		String path = "TP." + tp;

		if (ne.checkPlayer(player) == false) return;

		Location location = player.getLocation();
		ne.getConfig().set(path + ".X", location.getX());
		ne.getConfig().set(path + ".Y", location.getY());
		ne.getConfig().set(path + ".Z", location.getZ());
		ne.saveConfig();
	}

	public static void singleTP(String tp, String s_player) {
		Player player = Bukkit.getServer().getPlayer(s_player);
		String path = "TP." + tp;
		
		if (ne.checkPlayer(player) == false) return;

		Location location = player.getLocation();
		location.setX(ne.getConfig().getDouble(path + ".X"));
		location.setY(ne.getConfig().getDouble(path + ".Y"));
		location.setZ(ne.getConfig().getDouble(path + ".Z"));

		player.teleport(location);

		// count startne
		if (TimeManager.tp == 0)
			TimeManager.tp = 1;
		
		ne.getLogger().info(player.getName() + "を" + tp + "にsingleTPしました。");
		ne.writeLog("TP:" + player.getName() + " tp:" + tp);
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
				
				Location location = new Location(thisLoc.getWorld(), loc[0], loc[1], loc[2]);
				
				player.teleport(location);
				ne.getLogger().info(player.getName() + "を(" + location.getWorld().getName() + "," + loc[0] + "," + loc[1] + "," + loc[2] + ")にareaTPしました。");
				
				if(isRelative[0]) loc[0] -= playerLoc.getX();
				if(isRelative[1]) loc[1] -= playerLoc.getY();
				if(isRelative[2]) loc[2] -= playerLoc.getZ();
			}
		}
	}
}
