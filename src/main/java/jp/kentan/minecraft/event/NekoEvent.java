package jp.kentan.minecraft.event;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class NekoEvent extends JavaPlugin {
	public static String gacha_list[][] = new String[5][10], gacha_itemname[][] = new String[5][10];
	public static int gacha_numbers[] = new int[5];
	
	ConfigManager config = new ConfigManager();
	TicketManager ticket = new TicketManager();
	GameManager game = new GameManager();
	TimeManager time = new TimeManager();
	TPManager tp = new TPManager();
	
	@Override
	public void onEnable() {
		config.setInstance(this);
		ticket.setInstance(this);
		game.setInstance(this);
		time.setInstance(this);
		tp.setInstance(this);
		
		time.runTaskTimer(this, 20, 20);//20 1s　1200 1m
		
		for(int i = 0;i < 5;i++){
			gacha_numbers[i] = -1;
		}
		
		config.setBase();
		
		getLogger().info("NekoEventを有効化しました。");
	}

	@Override
	public void onDisable() {
		config.save();
		
		getLogger().info("NekoEventを無効化しました。");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if(cmd.getName().equals("event") && args.length > 0){
			
			switch (args[0]){
			case "reload":
				
				config.setBase();
				
				sender.sendMessage(ChatColor.GREEN + "NekoEventの設定を再読み込みしました。");
				
				break;
			case "ticket"://event ticket <player> <number>
				
				ticket.give(args[1],args[2]);
				
				break;
			case "minigame"://event minigame <player> <ticket>
				
				game.reward(args[1],args[2]);
				
				break;
			case "parkour"://event parkour <stage> <player>
				
				game.clearParkour(args[1],args[2]);
				
				break;
			case "dungeon"://event dungeon <stage> <player>
				
				game.clearDungeon(args[1],args[2]);
				
				break;
			case "tp"://event tp <player> <tp>
				
				if(args[1].equals("set")){ //event tp set <name>
					if(checkInGame(sender) == true)tp.set((Player)sender, args[2]);
					return true;
				}
				
				if(time.checkOverTPTime(args[1]) == true) tp.singleTP(args[1], args[2]);
				
				break;
			case "gacha"://event gacha <player> <type> <ticket>
				
				if(ticket.remove(args[1],args[3]) == true){
					processGacha(Bukkit.getServer().getPlayer(args[1]),Integer.parseInt(args[2]));
				}
				
				break;
			case "save":
				
				config.save();
				
				break;
			}
		}
		
		return true;
	}
	
	public void showException(Exception _e) {
		getLogger().info(_e.toString());
	}
	
	public boolean checkInGame(CommandSender _sender){
		if (!(_sender instanceof Player)) return false;
		else                              return true;
	}
	
	private static boolean checkBeforeWritefile(File file) {
		if (file.exists()) {
			if (file.isFile() && file.canWrite()) return true;
		}
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
				getLogger().info("ログファイルが見つかりません");
			}
		} catch (IOException e) {
			showException(e);
			getLogger().info("ログをファイルに書き込めませんでした");
		}
	}
	
	private void processGacha(Player player,int type){
		int rand = (int) (Math.random()*(gacha_numbers[type] + 1));//0-gacha_numbers
		
		getServer().dispatchCommand(getServer().getConsoleSender(), "give " + player.getName() + gacha_list[type][rand]);
		
		player.sendMessage(ChatColor.AQUA + gacha_itemname[type][rand] + ChatColor.WHITE + "を" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
		getLogger().info(player.getName() + "にガチャ景品 " + gacha_list[type][rand] + " を追加しました。");
		writeLog("Gacha:" + player.getName() + " get:" + gacha_itemname[type][rand] +"(" + gacha_list[type][rand] + ")");
	}
}
