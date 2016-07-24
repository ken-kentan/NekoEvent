package jp.kentan.minecraft.event;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class NekoEvent extends JavaPlugin {
	private ConfigManager config = null;
	private GameManager gm = null;
	private TPManager tp = null;
	private PasswordManager pm = null;
	private TicketManager ticket = null;
	private TimeManager time = null;
	private TriggerManager trigger = null;
	
	public static String ne_tag = ChatColor.GRAY + "[" + ChatColor.GOLD  + "Neko" + ChatColor.YELLOW + "Event" + ChatColor.GRAY + "] " + ChatColor.WHITE;
	public static String sp_itemid, sp_name;
	public static String gacha_list[][] = new String[5][10], gacha_itemname[][] = new String[5][10];
	public static List<String> buy_command_list = new ArrayList<String>(),
							   buy_name_list    = new ArrayList<String>();
	public static int gacha_numbers[] = new int[5];

	@Override
	public void onEnable() {
		
		config = new ConfigManager(this);
		
		tp = new TPManager(this);
		ticket = new TicketManager(this);
		trigger = new TriggerManager(this);
		
		time = new TimeManager(this, config);
		pm = new PasswordManager(this, trigger);
		gm = new GameManager(this, tp, ticket, time);

		time.runTaskTimer(this, 20, 20);// 20 1s 1200 1m

		for (int i = 0; i < 5; i++) {
			gacha_numbers[i] = -1;
		}

		config.load();
		time.initTPLockTimer();

		getLogger().info("NekoEventを有効にしました。");
	}

	@Override
	public void onDisable() {
		config.save();
		
		Bukkit.getScheduler().cancelTasks(this);

		getLogger().info("NekoEventを無効にしました。");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equals("event")) {
			Player player = null;
			Block commandBlock = null;
			
			if(args.length == 0 || args[0].equals("help")){
				showCommandHelp(sender);
				return true;
			}

			switch (args[0]) {
			case "reload":

				config.load();
				sender.sendMessage(ChatColor.GREEN + "NekoEventの設定を再読み込みしました。");

				break;
			case "save":
				config.save();
				break;
			case "ticket":// event ticket <player> <number>

				if(isCheckParamLength(args.length, 3)) ticket.give(args[1], args[2]);

				break;
			case "minigame":// event minigame <player> <ticket>

				if(isCheckParamLength(args.length, 3)) gm.reward(args[1], args[2]);

				break;
			case "parkour":// event parkour <stage> <player>

				if(isCheckParamLength(args.length, 3)) gm.clearParkour(args[1], args[2]);

				break;
			case "dungeon":// event dungeon <stage> <number> <player>

				if(isCheckParamLength(args.length, 4)) gm.clearDungeon(args[1], args[2], args[3]);

				break;
			case "join":// event join <player> <stage> <join msg>, event join set <stage> <stage number> <timer> ,event join unlock <stage>
				
				if(isCheckPlayerOnline(args[1], false)){
					if(isCheckParamLength(args.length, 4)) gm.join(args[1], args[2], args[3]);
				}else{
					switch (args[1]) {
						case "set":
							if(isCheckParamLength(args.length, 5) && isCheckPlayerOnline((Player)sender)){
								tp.set((Player)sender, args[2], args[3], args[4]);
								sender.sendMessage(ChatColor.GREEN + "現在位置を" + args[2] + "(" + args[3] + ")のTP位置として自動ﾛｯｸ解除時間" + args[4] + "秒で設定しました。");
							}
							break;
						case "lock":
							if(isCheckParamLength(args.length, 3)) gm.lock(args[2], true);
							break;
						case "unlock":
							if(isCheckParamLength(args.length, 3)) gm.lock(args[2], false);
							break;
						default:
							if(isCheckParamLength(args.length, 2)) sendErrorMessage(args[1] + "は/event joinのパラメータとして不適切です。");
							break;
					}
				}

				break;
			case "tp"://event tp <player> <x ,y, z>
				if(isCheckParamLength(args.length, 5) && isCheckCommandBlock(sender)){
					player = convertToPlayer(args[1]);
					String[] strLoc = new String[3];
					
					for(int i=0; i<3; i++) strLoc[i] = args[i+2];
					
					commandBlock = ((BlockCommandSender)sender).getBlock();
					
					tp.TP(player, commandBlock.getLocation(), strLoc);
				}
				break;
			case "gacha":// event gacha <player> <type> <ticket>

				if (isCheckParamLength(args.length, 4) && ticket.remove(args[1], args[3])) {
					processGacha(Bukkit.getServer().getPlayer(args[1]), Integer.parseInt(args[2]));
				}

				break;
			case "special":// event special <player> <name>

				if(isCheckParamLength(args.length, 3) && time.checkSpecialDay()) processSpecial(args[1], args[2]);
				else convertToPlayer(args[1]).sendMessage(ChatColor.YELLOW + "今日はスペシャル対象の日ではありません。");

				break;
			case "buy":// event buy <player> <type> <ticket>

				if (isCheckParamLength(args.length, 4) && ticket.remove(args[1], args[3]) == true) {
					processBuy(Bukkit.getServer().getPlayer(args[1]), Integer.parseInt(args[2]));
				}

				break;
			case "msg"://event msg <player> <name color> <name> <message>
				if(isCheckParamLength(args.length, 5)){
					player = convertToPlayer(args[1]);
					if(player != null) player.sendMessage(" " + getChatColor(args[2]) + args[3] + ChatColor.GREEN + ": " +ChatColor.WHITE + args[4]);
				}
				break;
			case "trigger"://event trigger <x y z> <player> <item_num> <msg_no_hand> <msg_not_match>
				if(isCheckParamLength(args.length, 8) && isCheckCommandBlock(sender)){
					commandBlock = ((BlockCommandSender)sender).getBlock();
					String[] strLocTrigger = {args[1],args[2],args[3]};
				
					if(trigger.checkItem(args[4],args[5],args[6],args[7])) trigger.setTorch(commandBlock.getLocation(), strLocTrigger);
				}
				break;
			case "setAmount"://event setAmount <player> <item name> <item amount>
				if(isCheckParamLength(args.length, 4)) gm.setItemAmount(args[1], args[2], args[3]);
				break;
			case "itemStack"://event itemStack
				ItemStack itemStack = ((Player)sender).getInventory().getItemInMainHand();
				sender.sendMessage(itemStack.toString());
				break;
			case "pass":
				if(isCheckParamLength(args.length, 4)){
					int numPass = Integer.parseInt(args[1]);
					player = convertToPlayer(args[2]);
					
					switch(args[3]){
					case "init"://event pass init <pass number> @p <password> <x y z>
						if(isCheckParamLength(args.length, 8)){
							String[] loc = {args[5],args[6],args[7]};
							pm.init(numPass, args[4],loc);
						}
						break;
					case "set"://event pass set <pass number> @p <password>
						if(isCheckParamLength(args.length, 5)) pm.set(numPass, player, args[4]);
						break;
					}
				}
				break;
			}
		}

		return true;
	}

	public boolean isCheckPlayerOnline(Player player) {
		if (player.isOnline()) return true;
		
		sendErrorMessage(player + "が見つかりません。");

		return false;
	}
	
	public boolean isCheckPlayerOnline(String strPlayer, boolean isSendErrorMsg) {
		Player player = convertToPlayer(strPlayer);
		if (player != null && player.isOnline()) return true;
		
		if(isSendErrorMsg) sendErrorMessage(strPlayer + "が見つかりません。");

		return false;
	}
	
	private boolean isCheckParamLength(int paramLen, int targetLen) {

		if(paramLen >= targetLen) return true;
		else{
			sendErrorMessage("コマンドのパラメータ数が不足しています。");
			return false;
		}
	}
	
	private boolean isCheckCommandBlock(CommandSender sender){
		try{
			@SuppressWarnings("unused")
			Block tmp = ((BlockCommandSender)sender).getBlock();
		}catch(Exception e){
			sendErrorMessage("このコマンドはコマンドブロックから実行してください。");
			return false;
		}
		
		return true;
	}
	
	public Player convertToPlayer(String strPlayer) {
		Player player = null;
		try{
			player = Bukkit.getServer().getPlayer(strPlayer);
		}catch(Exception e){
			sendErrorMessage(strPlayer + "をPlayer型に変換できませんでした。");
		}
		
		return player;
	}
	
	public void sendErrorMessage(String str){
		for(Player player : Bukkit.getServer().getOnlinePlayers())
        {
			if(player.isOp()){
				player.sendMessage(ne_tag + ChatColor.RED + "ERROR! " + ChatColor.WHITE + str);
			}
        }
		
		getLogger().warning(str);
	}
	
	public void sendInfoMessage(String str){
		for(Player player : Bukkit.getServer().getOnlinePlayers())
        {
			if(player.isOp()){
				player.sendMessage(ne_tag + ChatColor.AQUA + "Info " + ChatColor.WHITE + str);
			}
        }
		
		getLogger().info(str);
	}
	
	private void showCommandHelp(CommandSender _sender){
		_sender.sendMessage("---------- NekoEventコマンドヘルプ ----------");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event ticket <player> <number>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event minigame <player> <ticket>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event parkour <stage> <player>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event dungeon <stage> <number> <player>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event join <player> <stage> <join msg>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event join set <stage> <stage number> <timer>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event join lock <stage>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event join unlock <stage>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event tp <player> <x y z>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event gacha <player> <type> <ticket>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event special <player> <name>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event buy <player> <type> <ticket>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event msg <player> <name color> <name> <message>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event trigger <x y z> <player> <item_num> <msg_no_hand> <msg_not_match>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event setAmount <player> <item name> <item amount>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event itemStack");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event pass init <pass number> @p <password> <x y z>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event pass set <pass number> @p <password>");
		_sender.sendMessage("| " + ChatColor.GRAY + "文字装飾は節記号を使用して下さい。");
		_sender.sendMessage("---------------------------------------");
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
				sendInfoMessage("ログファイルが見つかりません");
			}
		} catch (IOException e) {
			sendErrorMessage("ログをファイルに書き込めませんでした");
		}
	}

	private void processGacha(Player player, int type) {
		int rand = (int) (Math.random() * (gacha_numbers[type] + 1));// 0-gacha_numbers

		getServer().dispatchCommand(getServer().getConsoleSender(), "give " + player.getName() + gacha_list[type][rand]);

		player.sendMessage(ChatColor.AQUA + gacha_itemname[type][rand] + ChatColor.WHITE + "を" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
		broadcastAll(player,ne_tag + ChatColor.BLUE + player.getName() + ChatColor.WHITE + "が,ガチャで" + ChatColor.AQUA + gacha_itemname[type][rand] + ChatColor.WHITE + "を" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
		sendInfoMessage(player.getName() + "にガチャ景品 " + gacha_list[type][rand] + " を追加しました。");
		writeLog("Gacha:" + player.getName() + " get:" + gacha_itemname[type][rand] + "(" + gacha_list[type][rand] + ")");
	}
	
	private void processSpecial(String strPlayer, String name) {
		Player player = convertToPlayer(strPlayer);
		if(!isCheckPlayerOnline(player)) return;
		
		String path = strPlayer + ".special." + name;
		
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
		sendInfoMessage(player.getName() + "にスペシャル景品 " + sp_itemid + " を追加しました。");
		writeLog("Special:" + player.getName() + " get:" + sp_name + "(" + sp_itemid + ")");
	}
	
	private void processBuy(Player player, int type) {
		if(type >= buy_command_list.size() || type >= buy_name_list.size()){
			sendErrorMessage(type + "はbuy_commandに登録されていません。");
			return;
		}
		String command = buy_command_list.get(type).replace("{player}", player.getName()),
			   name    = buy_name_list.get(type);

		getServer().dispatchCommand(getServer().getConsoleSender(), command);

		player.sendMessage(ChatColor.AQUA + buy_name_list.get(type) + ChatColor.WHITE + "を" + ChatColor.GOLD + "購入" + ChatColor.WHITE + "しました！");
		broadcastAll(player,ne_tag + ChatColor.BLUE + player.getName() + ChatColor.WHITE + "が," + ChatColor.AQUA + name + ChatColor.WHITE + "を" + ChatColor.GOLD + "購入" + ChatColor.WHITE + "しました！");
		sendInfoMessage(player.getName() + "にコマンド [" + command + "]を実行しました。");
		writeLog("Buy:" + player.getName() + " detail:" + name + "(" + command + ")");
	}
	
	public void broadcastAll(Player me, String str){
		int rand = (int) (Math.random() * 5);
		
		if(rand == 0) str = str.replace("Neko", "(^・ω・^)");
		
		str = str.replace("{player}", me.getName());
		
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
