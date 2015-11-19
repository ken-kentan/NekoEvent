package jp.kentan.minecraft.event;

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
	private String ticket_str, ticket_itemstack;
	private Location location;
	private int sec_tp = 0;
	
	@Override
	public void onEnable() {
		new BukkitRunnable()
		{
		    @Override
		    public void run()
		    {
		    	if(sec_tp > 660) sec_tp = 0; //reset over 10m
		    	if(sec_tp > 0) sec_tp++;     //count 0-1m
		    }
		}.runTaskTimer(this, 20, 20);//20 1s　1200 1m
		
		getBaseConfig();
		
		getLogger().info("NekoEventを有効にしました");
	}

	@Override
	public void onDisable() {
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
			case "create":
				if(args.length < 1){
					getLogger().info("対象プレイヤーを入力してください。");
					return true;
				}
				
				createPlayer(args[1]);
				
				break;
			case "ticket":
				if(args.length < 3){
					getLogger().info("対象プレイヤーまたはチケット数が指定されていません。");
					return true;
				}
				giveTicket(args[1],args[2]);
				break;
			case "athletic":
				if(args.length < 3){
					getLogger().info("ステージまたは対象プレイヤーが指定されていません。");
					return true;
				}
				
				clearAthletics(args[1],args[2]);//stage, name
				break;
			case "dungeon":
				if(args.length < 3){
					getLogger().info("ステージまたは対象プレイヤーが指定されていません。");
					return true;
				}
				
				clearDungeon(args[1],args[2]);//stage, name
				break;
			case "tp":
				if(args.length < 3){
					getLogger().info("パラメータが不足しています。");
					return true;
				}
				
				if(args[1].equals("set")){ //event tp set test
					if(checkInGame(sender) == false){
						sender.sendMessage(ChatColor.RED + "このコマンドはゲーム内から実行してください。");
						return false;
					}
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
			case "test":
				checkTicket((Player)sender,1);
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
	
	private boolean checkTicket(Player player, int ticket_number) {
		String itemS_str = ticket_itemstack.replace("{number}", Integer.toString(ticket_number));;

		for(ItemStack itemS : player.getInventory().getContents()) {
			  // isにはインベントリのアイテムが順々に入ります。
			if(itemS != null || itemS_str.equals(itemS.toString())){
				return true;
			}
		}

		return false;
	}
	
	private void giveTicket(String player,String number){
		int ticket_number = 0;
		
		try {
			ticket_number = Integer.parseInt(number);
		} catch (NumberFormatException nfex) {
			showException(nfex);
		}
		
		if(ticket_number > 0){
			getServer().dispatchCommand(getServer().getConsoleSender(), "give " + player + ticket_str.replace("{number}", Integer.toString(ticket_number)));
			
			getServer().dispatchCommand(getServer().getConsoleSender(), "tell " + player + " &bイベントチケット&fを&a" + ticket_number + "&f枚&6ゲット&fしました！");
			getLogger().info(player + "に、イベントチケットを" + ticket_number + "枚追加しました。");
		}
		
	}
	
	private void getBaseConfig(){
		reloadConfig();
		ticket_str = getConfig().getString("ticket.ID");
		ticket_itemstack = getConfig().getString("ticket.ItemStack");
		
		getLogger().info("Done. getBaseConfig from config.yml");
	}
	
	private boolean checkString(String str) {

		if (str.length() < 1)
			return false;

		return true;
	}

	private void createPlayer(String name) {

		if (checkString(name) == false) return;

		// create Key of player
		getConfig().set(name + "." + "check", true);
		saveConfig();
	}
	
	private void setTP(Player player, String tp) {
		String path = "TP." + tp;

		if (checkString(path) == false) return;
		
		location = player.getLocation();
		getConfig().set(path + ".X", location.getX());
		getConfig().set(path + ".Y", location.getY());
		getConfig().set(path + ".Z", location.getZ());
		saveConfig();
	}
	
	private void clearDungeon(String stage, String name) {
		String path = name + ".dungeon." + stage;

		if (checkString(name) == false || checkString(stage) == false) return;

		//give EventTicket when first clear
		if(getConfig().getBoolean(path) == false){
			giveTicket(name, "5");
		}
		
		getConfig().set(path, true);
		saveConfig();
		
		sec_tp = 0;//reset
	}
	
	private void clearAthletics(String stage, String name) {
		String path = name + ".athletic." + stage;
		Player player = Bukkit.getServer().getPlayer(name);
		
		player.sendMessage(ChatColor.RED + stage +"クリア！");

		if (checkString(name) == false || checkString(stage) == false){
			player.sendMessage(ChatColor.YELLOW +"イベチケは各アスレにつき1つまで入手できます。");
			return;
		}

		//give EventTicket when first clear
		if(getConfig().getBoolean(path) == false){
			giveTicket(name, "1");
		}
		
		getConfig().set(path, true);
		saveConfig();
	}
	
	private void singleTP(String tp, Player player) {
		String path = "TP." + tp;

		if (checkString(path) == false) return;
		
		location = player.getLocation();
		location.setX(getConfig().getDouble(path + ".X"));
		location.setY(getConfig().getDouble(path + ".Y"));
		location.setZ(getConfig().getDouble(path + ".Z"));
		
		player.teleport(location);
		
		//count start
		if(sec_tp == 0) sec_tp = 1;
		
		getLogger().info(player.getName() + "を" + tp + "にsingleTPしました。");
	}

}
