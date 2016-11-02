package jp.kentan.minecraft.event;

import java.util.ArrayList;
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
	private ConfigManager config ;
	private GameManager gm;
	private TPManager tp;
	private PasswordManager pm;
	private TicketManager ticket;
	private TimeManager time;
	private TriggerManager trigger;
	private GachaManager gacha;
	private static NekoEvent neko;
	
	final public static String CHAT_TAG = ChatColor.GRAY + "[" + ChatColor.GOLD  + "Neko" + ChatColor.YELLOW + "Event" + ChatColor.GRAY + "] " + ChatColor.WHITE;
	final private static String CHAT_OP_TAG = ChatColor.GRAY + "[" + ChatColor.GOLD  + "Neko" + ChatColor.YELLOW + "Event ";
	
	public static String sp_itemid, sp_name;
	public static List<String> buy_command_list = new ArrayList<String>(),
							   buy_name_list    = new ArrayList<String>();

	@Override
	public void onEnable() {
		neko = this;
		
		config = new ConfigManager(this);
		
		tp = new TPManager(this, config);
		ticket = new TicketManager(this);
		trigger = new TriggerManager();
		
		time = new TimeManager(this, config);
		pm = new PasswordManager(trigger);
		gm = new GameManager(this, tp, ticket, time);
		gacha = new GachaManager(this, config, ticket);

		time.runTaskTimer(this, 20, 20);// 20 1s 1200 1m

		config.load();
		time.initTPLockTimer();

		getLogger().info("NekoEventを有効にしました.");
	}

	@Override
	public void onDisable() {
		config.save();
		
		Bukkit.getScheduler().cancelTasks(this);

		getLogger().info("NekoEventを無効にしました.");
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
			case "join":// event join <player> <stage> <join msg>, event join set <stage> <timer> ,event join unlock <stage>
				
				if(Utils.isOnline(args[1])){
					if(isCheckParamLength(args.length, 4)) gm.join(args[1], args[2], args[3]);
				}else{
					switch (args[1]) {
						case "set":
							if(isCheckParamLength(args.length, 4) && Utils.isOnline((Player)sender)){
								if(tp.set((Player)sender, args[2], args[3])){
									sender.sendMessage(ChatColor.GREEN + "現在位置を" + args[2] + "のTP位置として自動ﾛｯｸ解除時間 " + args[3] + "秒で設定しました。");
								}
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
				if(isCheckParamLength(args.length, 5) && Utils.isCommandBlock(sender)){
					player = Utils.toPlayer(args[1]);
					String[] strLoc = new String[3];
					
					for(int i=0; i<3; i++) strLoc[i] = args[i+2];
					
					commandBlock = ((BlockCommandSender)sender).getBlock();
					
					tp.TP(player, commandBlock.getLocation(), strLoc);
				}
				break;
			case "gacha":
				if(!isCheckParamLength(args.length, 2)) return true;
				
				switch(args[1]){
				case "create":
					if(!isCheckParamLength(args.length, 3)) return true;
					
					if(gacha.createGacha(args[2])){
						sender.sendMessage(CHAT_TAG + "Successfully create Gacha(" + args[2] + ").");
					}else{
						sendErrorMessage("Gacha(" + args[2] + ")は既に存在するか不正なIDです。");
					}
					break;
				case "info":
					if(!isCheckParamLength(args.length, 3)) return true;
					
					gacha.infoGachaID(args[2], sender);
					break;
				case "list":
					gacha.infoGachaList(sender);
					break;
				case "add":
					if(!isCheckParamLength(args.length, 4)) return true;
					
					String strCommand = "";
					
					for(int i = 4; i < args.length; ++i){
						strCommand = strCommand.concat(args[i]).concat(" ");
					}
					
					gacha.addCommand(args[2], args[3], strCommand);
					
					sender.sendMessage(CHAT_TAG + "Successfully add command to Gacha(" + args[2] + ")");
					sender.sendMessage(CHAT_TAG + "Name: " + args[3]);
					sender.sendMessage(CHAT_TAG + "Command: " + strCommand);
					break;
				case "remove":
					if(!isCheckParamLength(args.length, 4)) return true;
					
					if(gacha.removeCommand(args[2], args[3])){
						sender.sendMessage(CHAT_TAG + "Gacha(" + args[2] + ")からindex:" + args[3] + "を消去しました.");
					}
					break;
				default:
					if(!isCheckParamLength(args.length, 4)) return true;
					
					gacha.gacha(args[1], args[2], args[3], (args.length > 4 && args[4].equals("silent")));
					break;
				}

				break;
			case "special":// event special <player> <name>

				if(isCheckParamLength(args.length, 3) && time.checkSpecialDay()) processSpecial(args[1], args[2]);
				else Utils.toPlayer(args[1]).sendMessage(ChatColor.YELLOW + "今日はスペシャル対象の日ではありません。");

				break;
			case "buy":// event buy <player> <type> <ticket>

				if (isCheckParamLength(args.length, 4) && ticket.remove(args[1], args[3]) == true) {
					processBuy(Bukkit.getServer().getPlayer(args[1]), Integer.parseInt(args[2]));
				}

				break;
			case "msg"://event msg <player> <name color> <name> <message>
				if(isCheckParamLength(args.length, 5)){
					player = Utils.toPlayer(args[1]);
					if(player != null) player.sendMessage(" " + getChatColor(args[2]) + args[3] + ChatColor.GREEN + ": " +ChatColor.WHITE + args[4]);
				}
				break;
			case "trigger"://event trigger <x y z> <player> <item_num> <msg_no_hand> <msg_not_match>
				if(isCheckParamLength(args.length, 8) && Utils.isCommandBlock(sender)){
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
					player = Utils.toPlayer(args[2]);
					
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
			case "setspawn":
				if(isCheckParamLength(args.length, 2) && args.length == 2){
					gm.setSpawn(args[1]);
				}else if(isCheckParamLength(args.length, 5)){
					gm.setSpawn(args[1],args[2],args[3],args[4]);
				}
				break;
			case "log"://event log <page>
				int page = 0, cnt = 1 + page * 10;
				
				if(args.length >= 2){
					page = Utils.parseInt(args[1]);
					if(page < 0) page = 0;
				}
				
				sender.sendMessage(CHAT_TAG + "イベントログ " + page + "ページ");
				List<String> log = Log.read(page);
				StringBuilder builder = new StringBuilder();
				
				for(String line : log){
					builder.append(cnt++);
					builder.append(":");
					builder.append(line);
					
					sender.sendMessage(builder.toString());
					
					builder.setLength(0);
				}
				break;
			}
		}

		return true;
	}
	
	private boolean isCheckParamLength(int paramLen, int targetLen) {

		if(paramLen >= targetLen) return true;
		else{
			sendErrorMessage("Missing the number of parameters.");
			return false;
		}
	}
	
	public static void sendErrorMessage(String str){
		for(Player player : Bukkit.getServer().getOnlinePlayers())
        {
			if(player.isOp()){
				player.sendMessage(CHAT_OP_TAG + ChatColor.RED + "ERROR" + ChatColor.GRAY + "] " + ChatColor.WHITE + str);
			}
        }
		
		neko.getLogger().warning(str);
	}
	
	public static void sendInfoMessage(String str){
		for(Player player : Bukkit.getServer().getOnlinePlayers())
        {
			if(player.isOp()){
				player.sendMessage(CHAT_OP_TAG + ChatColor.AQUA + "Info" + ChatColor.GRAY + "] " + ChatColor.WHITE + str);
			}
        }
		
		neko.getLogger().info(str);
	}
	
	private void showCommandHelp(CommandSender _sender){
		_sender.sendMessage("---------- NekoEventコマンドヘルプ ----------");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event ticket <player> <number>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event minigame <player> <ticket>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event parkour <stage> <player>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event dungeon <stage> <number> <player>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event join <player> <stage> <join msg>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event join set <stage> <timer>");
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
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event setspawn @p <x y z>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event gacha list");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event gacha create <gachaID>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event gacha info <gachaID>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event gacha add <gachaID> <name> <command>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event gacha remove <gachaID> <index>");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event gacha <gachaID> <ticket cost> @p");
		_sender.sendMessage("| " + ChatColor.YELLOW + "/event gacha <gachaID> <ticket cost> @p silent");
		_sender.sendMessage("| " + ChatColor.GRAY + "文字装飾は節記号を使用して下さい。");
		_sender.sendMessage("---------------------------------------");
	}
	
	private void processSpecial(String strPlayer, String name) {
		Player player = Utils.toPlayer(strPlayer);
		if(!Utils.isOnline(player)) return;
		
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
		broadcast(player,CHAT_TAG + ChatColor.BLUE + player.getName() + ChatColor.WHITE + "が," + ChatColor.AQUA + sp_name + ChatColor.WHITE + "を" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
		sendInfoMessage(player.getName() + "にスペシャル景品 " + sp_itemid + " を追加しました。");
		Log.write("Special:" + player.getName() + " get:" + sp_name + "(" + sp_itemid + ")");
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
		broadcast(player,CHAT_TAG + ChatColor.BLUE + player.getName() + ChatColor.WHITE + "が," + ChatColor.AQUA + name + ChatColor.WHITE + "を" + ChatColor.GOLD + "購入" + ChatColor.WHITE + "しました！");
		sendInfoMessage(player.getName() + "にコマンド [" + command + "]を実行しました。");
		Log.write("Buy:" + player.getName() + " detail:" + name + "(" + command + ")");
	}
	
	public void broadcast(Player me, String str){
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
