package jp.kentan.minecraft.event;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class NekoEvent extends JavaPlugin {
	private String ticket_str;
	private String ticket_itemstack;
	private String gacha_list[][] = new String[5][10], gacha_itemname[][] = new String[5][10];
	private Location location;
	private int sec_tp = 0, sec = 0, sec_m = 0, gacha_numbers[] = new int[5];
	
	@Override
	public void onEnable() {
		
		new BukkitRunnable()
		{
		    @Override
		    public void run()
		    {
		    	if(sec_tp > 660) sec_tp = 0; //reset over 10m
		    	if(sec_tp > 0) sec_tp++;     //count 0-1m
		    	
		    	if(sec >= 59){
		    		sec = 0;
		    		sec_m++;
		    	}
		    	sec++;
		    }
		}.runTaskTimer(this, 20, 20);//20 1s　1200 1m
		
		for(int i = 0;i < 5;i++){
			gacha_numbers[i] = -1;
		}
		
		getBaseConfig();
		
		getLogger().info("NekoEventを有効にしました");
	}

	@Override
	public void onDisable() {
		getConfig().set("sec_m", sec_m);
		saveConfig();
		getLogger().info("NekoEventを無効にしました");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if(cmd.getName().equals("event")){
			
			switch (args[0]){
			case "reload":
				
				getBaseConfig();
				
				sender.sendMessage(ChatColor.GREEN + "NekoEventの設定を再読み込みしました。");
				
				break;
			case "ticket":
				if(args.length < 3){
					getLogger().info("対象プレイヤーまたはチケット数が指定されていません。");
					return true;
				}
				giveTicket(args[1],args[2]);
				
				break;
			case "minigame":
				int rand = (int) (Math.random()*5);//0-5
				
				if(rand == 5){
					giveTicket(args[1],"1");
				}
				break;
			case "parkour":
				if(args.length < 3){
					getLogger().info("ステージまたは対象プレイヤーが指定されていません。");
					return true;
				}
				
				clearParkour(args[1],args[2]);//event parkour <stage> <player>
				
				break;
			case "dungeon":
				if(args.length < 3){
					getLogger().info("ステージまたは対象プレイヤーが指定されていません。");
					return true;
				}
				
				clearDungeon(args[1],args[2]);//event dungeon <stage> <player>
				
				break;
			case "tp":
				if(args.length < 3){
					getLogger().info("パラメータが不足しています。");
					return true;
				}
				
				if(args[1].equals("set")){ //event tp set <name>
					setTP((Player)sender, args[2]);
					return true;
				}
				
				//singleTP
				if(sec_tp > 60){
					sender.sendMessage(ChatColor.RED + "初回のプレイヤー参加から１分が経過しました。");
					sender.sendMessage(ChatColor.YELLOW + "プレイヤーがダンジョンをクリアするか、" + (600 - (sec_tp - 60)) + "秒経過するまで参加できません。");
					
					getLogger().info(sender.getName() + "がダンジョンへの参加をリジェクトされました。(over 1m)");
					return true;
				}
				
				singleTP(args[1], Bukkit.getServer().getPlayer(args[2]));//tp player
				
				break;
			case "gacha"://event gacha <player> <type> <ticket>
				if(args.length < 3){
					getLogger().info("パラメータが不足しています。");
					return true;
				}
				
				int type         = Integer.parseInt(args[2]),
					need_tickers = Integer.parseInt(args[3]);
				
				if(removeTicket(Bukkit.getServer().getPlayer(args[1]),need_tickers) == true){
					processGacha(Bukkit.getServer().getPlayer(args[1]),type);
				}
				break;
			}
			
		}
		
		return true;
	}
	
	public void showException(Exception _e) {
		getLogger().info(_e.toString());
	}
	
	private boolean checkInGame(CommandSender _sender){
		if (!(_sender instanceof Player)) return false;
		else                              return true;
	}
	
	private void getBaseConfig(){
		reloadConfig();
		ticket_str = getConfig().getString("ticket.ID");
		ticket_itemstack = getConfig().getString("ticket.ItemStack");
		
		sec_m = getConfig().getInt("sec_m");
		
		for(int j = 0; j < 5; j++){
			for(int i = 0; i < 10; i++){
				if(getConfig().getString("gacha.ID." + j + "_" + i) != null){
					gacha_list[j][i] = getConfig().getString("gacha.ID." + j + "_" + i);//0_0
					gacha_itemname[j][i] = getConfig().getString("gacha.name." + j + "_" + i);
					gacha_numbers[j] = i;
				}
			}
		}
		
		getLogger().info("Done. getBaseConfig from config.yml");
		getLogger().info("Result. gacha_numbers:" + gacha_numbers[0] + "," + gacha_numbers[1] + "," + gacha_numbers[2] + "," + gacha_numbers[3] + "," + gacha_numbers[4]);
	}
	
	private static boolean checkBeforeWritefile(File file) {
		if (file.exists()) {
			if (file.isFile() && file.canWrite()) return true;
		}
		return false;
	}
	
	private void giveTicket(String player, String number){
		int ticket_number = 0;
		
		try {
			ticket_number = Integer.parseInt(number);
		} catch (NumberFormatException nfex) {
			showException(nfex);
			return;
		}
		
		if(ticket_number > 0){			
			getServer().dispatchCommand(getServer().getConsoleSender(), "give " + player + ticket_str.replace("{number}", Integer.toString(ticket_number)));
			
			Bukkit.getServer().getPlayer(player).sendMessage(ChatColor.AQUA +" イベントチケット" + ChatColor.WHITE + "を" + ticket_number + "枚" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
			getLogger().info(player + "に、イベントチケットを" + ticket_number + "枚追加しました。");
			writeLog("Ticket:" + player + " +" + ticket_number );
		}
		
	}
	
	private boolean removeTicket(Player player, int ticket_number) {
		String itemS_str = ticket_itemstack.replace("{number}", Integer.toString(ticket_number));;

		for(int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack itemS = player.getInventory().getItem(i);
			if(itemS != null && itemS.toString().indexOf(itemS_str) != -1){
				int amt = itemS.getAmount() - ticket_number;
				
				if(amt < 0){
					player.sendMessage(ChatColor.YELLOW +"イベントチケットが" + Math.abs(amt) + "枚不足しています。");
					return false;
				}
				
				itemS.setAmount(amt);
				player.getInventory().setItem(i, amt > 0 ? itemS : null);
				player.updateInventory();
				
				writeLog("Ticket:" + player + " -" + ticket_number );
				return true;
			}
		}
		
		player.sendMessage(ChatColor.YELLOW +"イベントチケットが" + ticket_number + "枚不足しています。");

		return false;
	}
	
	public void writeLog(String _str){
		try {
			File file = new File("plugins/NekoEvent/log.txt");

			if (checkBeforeWritefile(file)) {
				FileWriter filewriter = new FileWriter(file, true);

				Calendar calendar = Calendar.getInstance();

				filewriter.write("[" + calendar.getTime().toString() + "]" + _str + "\r\n");

				filewriter.close();
			} else {
				getLogger().info("ログをファイルに書き込めませんでした");
			}
		} catch (IOException e) {
			showException(e);
			getLogger().info("ログをファイルに書き込めませんでした");
		}
	}
	
	private void setTP(Player player, String tp) {
		String path = "TP." + tp;
		
		if(checkInGame(player) == false){
			player.sendMessage(ChatColor.RED + "このコマンドはゲーム内から実行してください。");
			return;
		}
		
		location = player.getLocation();
		getConfig().set(path + ".X", location.getX());
		getConfig().set(path + ".Y", location.getY());
		getConfig().set(path + ".Z", location.getZ());
		saveConfig();
	}
	
	private void clearDungeon(String stage, String s_player) {
		String path = s_player + ".dungeon." + stage;
		Player player = Bukkit.getServer().getPlayer(s_player);
		
		player.sendMessage(ChatColor.BLUE + stage + ChatColor.AQUA + "ダンジョンをクリア！");
		writeLog("Dungeon:" + player + " clear:" + stage);
		
		if(checkOverDiffMinute(path, 1440)){ //if over 24h,reset
			getConfig().set(path + "clear", false);
			saveConfig();
		}

		if(getConfig().getBoolean(path + "clear") == false){
			giveTicket(s_player, "5");
			getConfig().set(path + "sec_m", sec_m);
		}else{
			player.sendMessage(ChatColor.YELLOW +"イベントチケットは各ダンジョンで24時間おきに入手できます。");
		}
		
		getConfig().set(path + "clear", true);
		saveConfig();
		
		sec_tp = 0;//reset
	}
	
	private void clearParkour(String stage, String s_player) {
		String path = s_player + ".parkour." + stage;
		Player player = Bukkit.getServer().getPlayer(s_player);
		
		player.sendMessage(ChatColor.GREEN + stage + ChatColor.AQUA + "アスレをクリア！");
		writeLog("Parkour:" + player + " clear" + stage);
		
		if(checkOverDiffMinute(path, 1440)){ //if over 24h,reset
			getConfig().set(path + "clear", false);
			saveConfig();
		}

		if(getConfig().getBoolean(path + "clear") == false){
			giveTicket(s_player, "1");
			getConfig().set(path + "sec_m", sec_m);
		}else{
			player.sendMessage(ChatColor.YELLOW +"イベントチケットは各アスレで24時間おきに入手できます。");
		}
		
		getConfig().set(path + "clear", true);
		saveConfig();
	}
	
	private void singleTP(String tp, Player player) {
		String path = "TP." + tp;
		
		location = player.getLocation();
		location.setX(getConfig().getDouble(path + ".X"));
		location.setY(getConfig().getDouble(path + ".Y"));
		location.setZ(getConfig().getDouble(path + ".Z"));
		
		player.teleport(location);
		
		//count start
		if(sec_tp == 0) sec_tp = 1;
		
		getLogger().info(player.getName() + "を" + tp + "にsingleTPしました。");
		writeLog("TP:" + player + " tp:" + tp );
	}
	
	private void processGacha(Player player,int type){
		int rand = (int) (Math.random()*(gacha_numbers[type] + 1));//0-gacha_numbers
		
		getServer().dispatchCommand(getServer().getConsoleSender(), "give " + player.getName() + gacha_list[type][rand]);
		
		player.sendMessage(ChatColor.AQUA + gacha_itemname[type][rand] + ChatColor.WHITE + "を" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
		getLogger().info(player.getName() + "にガチャ景品 " + gacha_list[type][rand] + " を追加しました。");
		writeLog("Gacha:" + player + " get:" + gacha_itemname[type][rand] );
	}
	
	private boolean checkOverDiffMinute(String _path, int baseDiff){
		int old_sec_m = 0;
		
		if(getConfig().getString(_path + "sec_m") != null){
			old_sec_m = getConfig().getInt(_path + "sec_m");
			if(Math.abs(sec_m - old_sec_m) > baseDiff) return true;
		}
		return false;
	}

}
