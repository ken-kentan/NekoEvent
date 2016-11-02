package jp.kentan.minecraft.event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Utils {
	final private static Server SERVER = Bukkit.getServer();
	
	public static Player toPlayer(String strPlayer){
		Player player = null;
		
		try{
			player = SERVER.getPlayer(strPlayer);
		}catch (Exception e) {
			NekoEvent.sendErrorMessage("ﾌﾟﾚｲﾔｰ変換ｴﾗｰ:" + e.getMessage());
			return null;
		}finally {
			if(!strPlayer.equals(player.getName())){
				NekoEvent.sendErrorMessage("ﾌﾟﾚｲﾔｰ変換ｴﾗｰ: 変換不一致");
				return null;
			}
		}
		
		return player;
	}
	
	public static boolean isOnline(Player player) {		
		if (player != null && player.isOnline()){
			return true;
		}
		
		NekoEvent.sendErrorMessage(player.getName() + "が見つかりません.");
		return false;
	}
	
	public static boolean isOnline(String strPlayer) {
		Player player = toPlayer(strPlayer);
		
		if (player != null && player.isOnline()){
			return true;
		}
		
		NekoEvent.sendErrorMessage(strPlayer + "が見つかりません.");
		return false;
	}
	
	public static boolean isCommandBlock(CommandSender sender){
		try{
			@SuppressWarnings("unused")
			Block tmp = ((BlockCommandSender)sender).getBlock();
		}catch(Exception e){
			NekoEvent.sendErrorMessage("このｺﾏﾝﾄﾞはｺﾏﾝﾄﾞﾌﾞﾛｯｸで実行する必要があります.");
			return false;
		}
		
		return true;
	}
	
	public static int parseInt(String str){
		int val = 0;
		
		try{
			val = Integer.parseInt(str);
		}catch (Exception e) {
			NekoEvent.sendErrorMessage("整数変換ｴﾗｰ:" + e.getMessage());
			NekoEvent.sendErrorMessage("値 0 として処理します.");
		}
		
		return val;
	}
	
	public static Location toLocation(Player player, String strX, String strY, String strZ){
		Location location = player.getLocation();
		
		try {
			location.setX(Double.parseDouble(strX));
			location.setY(Double.parseDouble(strY));
			location.setZ(Double.parseDouble(strZ));
		} catch (Exception e) {
			NekoEvent.sendErrorMessage("浮動小数点型変換ｴﾗｰ:" + e.getMessage());
			return null;
		}
		
		return location;
	}
}
