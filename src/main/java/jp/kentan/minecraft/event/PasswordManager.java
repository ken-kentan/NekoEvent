package jp.kentan.minecraft.event;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PasswordManager {
	private NekoEvent ne = null;
	private TriggerManager trigger = null;
	
	private static String[] password = new String[5];
	private static String[] setPassword = new String[5];
	private static String[][] locTorch = new String[5][3];
	
	public PasswordManager(NekoEvent ne, TriggerManager trigger){
		this.ne = ne;
		this.trigger = trigger;
	}
	
	
	public void init(int numPass, String initPass, String[] loc){
		password[numPass] = initPass;
		setPassword[numPass] = "";
		locTorch[numPass] = loc;
		ne.sendInfoMessage("パスワード:" + numPass + " を " + password[numPass] + " で初期化しました。");
	}
	
	public void set(int numPass, Player player, String pass){
		StringBuilder sb = new StringBuilder();
		
		if(setPassword[numPass] == null){
			ne.sendErrorMessage("ﾊﾟｽﾜｰﾄﾞ:" + numPass + " が初期化されていないためset出来ませんでした。");
			ne.sendErrorMessage("setコマンド以前にinitコマンドが実行されている必要があります。");
			return;
		}
		
		sb.append(setPassword[numPass]);
		sb.append(pass);
		
		setPassword[numPass] = new String(sb);
		
		ne.sendInfoMessage("パスワード:" + numPass + " に " + setPassword[numPass] + " がセットされました。");
		
		if(password[numPass].length() <= setPassword[numPass].length()) run(numPass,player);
	}
	
	private void run(int numPass, Player player){
		if(password[numPass].equals(setPassword[numPass])){
			setPassword[numPass] = "";
			ne.sendInfoMessage("パスワード:" + numPass + " が一致しました。");
			trigger.setTorch(player.getLocation(), locTorch[numPass]);
		}else{
			setPassword[numPass] = "";
			player.sendMessage(" " + ChatColor.GRAY + ChatColor.ITALIC + "入力が誤りです。再入力を行ってください。");
			ne.sendInfoMessage("パスワード:" + numPass + " が不一致です。");
		}
	}
}
