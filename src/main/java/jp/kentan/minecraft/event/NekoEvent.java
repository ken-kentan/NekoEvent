package jp.kentan.minecraft.event;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NekoEvent extends JavaPlugin {
	public static String ne_tag = ChatColor.GRAY + "[" + ChatColor.GOLD  + "Neko" + ChatColor.YELLOW + "Event" + ChatColor.GRAY + "] " + ChatColor.WHITE;
	public static String sp_itemid, sp_name;
	public static String gacha_list[][] = new String[5][10], gacha_itemname[][] = new String[5][10];
	public static String buy_list[] = new String[20], buy_name[] = new String[20];
	public static int gacha_numbers[] = new int[5];
	
	private static NekoEvent instance;

	@Override
	public void onEnable() {
		
		instance = this;

		new TimeManager().runTaskTimer(this, 20, 20);// 20 1s 1200 1m

		for (int i = 0; i < 5; i++) {
			gacha_numbers[i] = -1;
		}

		ConfigManager.setBase();

		getLogger().info("NekoEventを有効にしました。");
	}

	@Override
	public void onDisable() {
		ConfigManager.save();
		
		Bukkit.getScheduler().cancelTasks(this);

		getLogger().info("NekoEventを無効にしました。");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (cmd.getName().equals("event") && args.length > 0) {

			switch (args[0]) {
			case "reload":

				ConfigManager.setBase();

				sender.sendMessage(ChatColor.GREEN + "NekoEventの設定を再読み込みしました。");

				break;
			case "save":

				ConfigManager.save();

				break;
			case "ticket":// event ticket <player> <number>

				TicketManager.give(args[1], args[2]);

				break;
			case "minigame":// event minigame <player> <ticket>

				GameManager.reward(args[1], args[2]);

				break;
			case "parkour":// event parkour <stage> <player>

				GameManager.clearParkour(args[1], args[2]);

				break;
			case "dungeon":// event dungeon <stage> <number> <player>

				GameManager.clearDungeon(args[1], args[2], args[3]);

				break;
			case "join":// event join <player> <stage> , event join set <stage>

				if (args[1].equals("set")) { // event tp set <name>
					if (checkInGame(sender) == true)
						TPManager.set((Player) sender, args[2]);
					return true;
				}

				if (TimeManager.checkOverTPTime(args[2]) == true)
					TPManager.singleTP(args[1], args[2]);

				break;
			case "tp"://event tp <range> <x ,y, z> or /event tp 0 <x ,y, z> <player>
				float range = Float.parseFloat(args[1]);
				
				String[] strLoc = new String[3];
				
				for(int i=0; i<3; i++) strLoc[i] = args[i+2];
				
				Block thisCommandBlock = ((BlockCommandSender)sender).getBlock();
				
				if(range > 0) TPManager.areaTP(range, thisCommandBlock.getLocation(), strLoc);
				else TPManager.TP(Bukkit.getServer().getPlayer(args[5]), thisCommandBlock.getLocation(), strLoc);

				break;
			case "gacha":// event gacha <player> <type> <ticket>

				if (TicketManager.remove(args[1], args[3]) == true) {
					processGacha(Bukkit.getServer().getPlayer(args[1]), Integer.parseInt(args[2]));
				}

				break;
			case "special":// event special <player> <name>

				if(TimeManager.checkSpecialDay() == true) processSpecial(args[1], args[2]);
				else Bukkit.getServer().getPlayer(args[1]).sendMessage(ChatColor.YELLOW + "今日はスペシャル対象の日ではありません。");

				break;
			case "buy":// event buy <player> <type> <ticket>

				if (TicketManager.remove(args[1], args[3]) == true) {
					processBuy(Bukkit.getServer().getPlayer(args[1]), Integer.parseInt(args[2]));
				}

				break;
			case "msg"://event msg <player> <name color> <name> <message>
				Player player = (Player)Bukkit.getServer().getPlayer(args[1]);
				
				player.sendMessage(" " + getChatColor(args[2]) + args[3] + ChatColor.GREEN + ": " +ChatColor.WHITE + args[4]);
				break;
			case "trigger"://event trigger <x y z> <player> <item_num> <msg_no_hand> <msg_not_match>
				Block thisCommandBlock1 = ((BlockCommandSender)sender).getBlock();
				
				String[] strLoc1 = {args[1],args[2],args[3]};
				
				if(TriggerManager.checkItem(args[4],args[5],args[6],args[7])) TriggerManager.setTorch(thisCommandBlock1.getLocation(), strLoc1);
				break;
			case "give"://event give <player> <item_num> <cycle>
				break;
			}
		}

		return true;
	}

	public void showException(Exception _e) {
		getLogger().warning(_e.toString());
	}
	
	public static NekoEvent getInstance(){
		return instance;
	}

	public boolean checkInGame(CommandSender _sender) {
		if (!(_sender instanceof Player))
			return false;
		else
			return true;
	}

	public boolean checkPlayer(Player player) {

		if (checkInGame(player) == false) {
			getLogger().info("プレイヤー(" + player.getName() + ")が見つかりません。");
			return false;
		}
		return true;
	}

	private static boolean checkBeforeWritefile(File file) {
		if (file.exists()) {
			if (file.isFile() && file.canWrite())
				return true;
		}
		return false;
	}

	public void writeLog(String _str) {
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

	private void processGacha(Player player, int type) {
		int rand = (int) (Math.random() * (gacha_numbers[type] + 1));// 0-gacha_numbers

		getServer().dispatchCommand(getServer().getConsoleSender(), "give " + player.getName() + gacha_list[type][rand]);

		player.sendMessage(ChatColor.AQUA + gacha_itemname[type][rand] + ChatColor.WHITE + "を" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
		broadcastAll(player,ne_tag + ChatColor.BLUE + player.getName() + ChatColor.WHITE + "が,ガチャで" + ChatColor.AQUA + gacha_itemname[type][rand] + ChatColor.WHITE + "を" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
		getLogger().info(player.getName() + "にガチャ景品 " + gacha_list[type][rand] + " を追加しました。");
		writeLog("Gacha:" + player.getName() + " get:" + gacha_itemname[type][rand] + "(" + gacha_list[type][rand] + ")");
	}
	
	private void processSpecial(String s_player, String name) {
		Player player = Bukkit.getServer().getPlayer(s_player);
		if(checkPlayer(player) == false) return;
		
		String path = s_player + ".special." + name;
		
		if(getConfig().getBoolean(path) == false){
			getConfig().set(path, true);
			saveConfig();
		}
		else {
			player.sendMessage(ChatColor.YELLOW + "あなたはすでに" + name + "景品を入手しています。");
			return;
		}

		getServer().dispatchCommand(getServer().getConsoleSender(), "give " + player.getName() + sp_itemid);

		player.sendMessage(ChatColor.AQUA + sp_name + ChatColor.WHITE + "を" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
		broadcastAll(player,ne_tag + ChatColor.BLUE + player.getName() + ChatColor.WHITE + "が," + ChatColor.AQUA + sp_name + ChatColor.WHITE + "を" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
		getLogger().info(player.getName() + "にスペシャル景品 " + sp_itemid + " を追加しました。");
		writeLog("Special:" + player.getName() + " get:" + sp_name + "(" + sp_itemid + ")");
	}
	
	private void processBuy(Player player, int type) {
		String _command = buy_list[type].replace("{player}", player.getName());

		getServer().dispatchCommand(getServer().getConsoleSender(), _command);

		player.sendMessage(ChatColor.AQUA + buy_name[type] + ChatColor.WHITE + "を" + ChatColor.GOLD + "購入" + ChatColor.WHITE + "しました！");
		broadcastAll(player,ne_tag + ChatColor.BLUE + player.getName() + ChatColor.WHITE + "が," + ChatColor.AQUA + buy_name[type] + ChatColor.WHITE + "を" + ChatColor.GOLD + "購入" + ChatColor.WHITE + "しました！");
		getLogger().info(player.getName() + "にコマンド [" + buy_list[type] + "]を実行しました。");
		writeLog("Buy:" + player.getName() + " detail:" + buy_name[type] + "(" + buy_list[type] + ")");
	}
	
	public void broadcastAll(Player me, String str){
		int rand = (int) (Math.random() * 5);
		
		if(rand == 0) str = str.replace("Neko", "(^・ω・^)");
		
		for(Player player : Bukkit.getServer().getOnlinePlayers())
        {
			if(player!= me) player.sendMessage(str);
        }
	}
	
	public ChatColor getChatColor(String str){
		switch(str){
		case "&0":
			return ChatColor.BLACK;
		case "&1":
			return ChatColor.DARK_BLUE;
		case "&2":
			return ChatColor.DARK_GREEN;
		case "&3":
			return ChatColor.DARK_AQUA;
		case "&4":
			return ChatColor.DARK_RED;
		case "&5":
			return ChatColor.DARK_PURPLE;
		case "&6":
			return ChatColor.GOLD;
		case "&7":
			return ChatColor.GRAY;
		case "&8":
			return ChatColor.DARK_GRAY;
		case "&a":
			return ChatColor.GREEN;
		case "&b":
			return ChatColor.AQUA;
		case "&c":
			return ChatColor.RED;
		case "&d":
			return ChatColor.LIGHT_PURPLE;
		case "&e":
			return ChatColor.YELLOW;
		case "&f":
		default:
			return ChatColor.WHITE;
		}
	}
}
