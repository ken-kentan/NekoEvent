package jp.kentan.minecraft.event;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PasswordManager {
	static NekoEvent ne = NekoEvent.getInstance();
	
	private static String[] password = new String[5];
	private static String[] setPassword = new String[5];
	private static String[][] locTorch = new String[5][3];
	
	
	public static void init(int numPass, String initPass, String[] loc){
		password[numPass] = initPass;
		setPassword[numPass] = "";
		locTorch[numPass] = loc;
		ne.getLogger().info("パスワード:" + numPass + " を " + password[numPass] + " で初期化しました。");
	}
	
	public static void set(int numPass, Player player, String pass){
		StringBuilder sb = new StringBuilder();
		
		sb.append(setPassword[numPass]);
		sb.append(pass);
		
		setPassword[numPass] = new String(sb);
		
		ne.getLogger().info("パスワード:" + numPass + " に " + setPassword[numPass] + " がセットされました。");
		
		if(password[numPass].length() <= setPassword[numPass].length()) run(numPass,player);
	}
	
	private static void run(int numPass, Player player){
		if(password[numPass].equals(setPassword[numPass])){
			setPassword[numPass] = "";
			ne.getLogger().info("パスワード:" + numPass + " が一致しました。");
			TriggerManager.setTorch(player.getLocation(), locTorch[numPass]);
		}else{
			setPassword[numPass] = "";
			player.sendMessage(" " + ChatColor.GRAY + ChatColor.ITALIC + "入力が誤りです。再入力を行ってください。");
			ne.getLogger().info("パスワード:" + numPass + " が不一致です。");
		}
	}
}
