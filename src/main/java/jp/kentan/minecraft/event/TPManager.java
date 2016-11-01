package jp.kentan.minecraft.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TPManager {
	private NekoEvent ne = null;
	
	public TPManager(NekoEvent ne){
		this.ne = ne;
	}
	
	public void TP(Player player, Location thisLoc ,String[] strLoc){
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
	
		ne.sendInfoMessage("TP " + player.getName() + " to (" + location.getWorld().getName() + "," + (int)loc[0] + "," + (int)loc[1] + "," + (int)loc[2]+ ").");
	}

	public boolean set(Player player, String strName, String strTimer) {
		String path = "TP." + strName;

		if (!ne.isCheckPlayerOnline(player)){
			ne.sendErrorMessage("Could not found player " + player.getName() + ".");
			return false;
		}
		
		int stageTimer = Integer.parseInt(strTimer);
		
		if(stageTimer < 0 || stageTimer > 3600){
			ne.sendErrorMessage("自動解除タイマーは1~3600(秒)で指定する必要があります。");
			return false;
		}
		
		if(ne.getConfig().getString(path + ".Lock") != null){
			ne.sendErrorMessage("Already registered (" + strName + ").");
			return false;
		}
		
		ne.getConfig().set(path + ".Lock", false);
		ne.getConfig().set(path + ".Timer", stageTimer);

		Location location = player.getLocation();
		ne.getConfig().set(path + ".X", location.getX());
		ne.getConfig().set(path + ".Y", location.getY());
		ne.getConfig().set(path + ".Z", location.getZ());
		ne.getConfig().set(path + ".Yaw", location.getYaw());
		ne.getConfig().set(path + ".Pitch", location.getPitch());
		ne.saveConfig();
		
		return true;
	}
	
	public int getTPLocationNumber(String strStage){
		String path = "TP." + strStage;
		int stageNumber;
		stageNumber = ne.getConfig().getInt(path + ".No", -1);
		
		if(stageNumber < 0){
			ne.sendErrorMessage(strStage + "は登録されていません。");
			return -1;
		}
		
		return stageNumber;
	}

	public void TP(String tp, String strPlayer) {
		Player player = ne.convertToPlayer(strPlayer);
		String path = "TP." + tp;
		
		if (player == null || !ne.isCheckPlayerOnline(player)) return;

		Location location = player.getLocation();
		location.setX(ne.getConfig().getDouble(path + ".X"));
		location.setY(ne.getConfig().getDouble(path + ".Y"));
		location.setZ(ne.getConfig().getDouble(path + ".Z"));
		location.setYaw((float) ne.getConfig().getDouble(path + ".Yaw"));
		location.setPitch((float) ne.getConfig().getDouble(path + ".Pitch"));

		player.teleport(location);
		
		ne.sendInfoMessage("TP " + strPlayer + " to " + tp + ".");
	}
}
