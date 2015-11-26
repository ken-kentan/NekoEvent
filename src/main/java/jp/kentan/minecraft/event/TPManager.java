package jp.kentan.minecraft.event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TPManager {
	static NekoEvent ne;
	static TimeManager time = new TimeManager();

	Location location;

	public void setInstance(NekoEvent _ne) {
		ne = _ne;
	}

	private boolean checkPlayer(Player player) {

		if (ne.checkInGame(player) == false) {
			ne.getLogger().info("プレイヤーが見つかりません。");
			return false;
		}
		return true;
	}

	public void set(Player player, String tp) {
		String path = "TP." + tp;

		if (checkPlayer(player) == false) return;

		location = player.getLocation();
		ne.getConfig().set(path + ".X", location.getX());
		ne.getConfig().set(path + ".Y", location.getY());
		ne.getConfig().set(path + ".Z", location.getZ());
		ne.saveConfig();
	}

	public void singleTP(String tp, String s_player) {
		Player player = Bukkit.getServer().getPlayer(s_player);
		String path = "TP." + tp;
		
		if (checkPlayer(player) == false) return;

		location = player.getLocation();
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
}
