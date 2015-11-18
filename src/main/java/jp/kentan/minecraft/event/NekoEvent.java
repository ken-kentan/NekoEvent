package jp.kentan.minecraft.event;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class NekoEvent extends JavaPlugin {
	private String ticket_str;
	
	@Override
	public void onEnable() {
//		new BukkitRunnable()
//		{
//		    @Override
//		    public void run()
//		    {
//		    	//run
//		    }
//		}.runTaskTimer(this, 20, 20);//20 1s
		
		try{
			reloadConfig();
			ticket_str = getConfig().getString("ticket.ID");
		}catch(Exception e){
			showException(e);
			getLogger().info("TicketIDを正常に読み込めませんでした。");
			onDisable();
		}
		
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
				
				try{
					reloadConfig();
					ticket_str = getConfig().getString("ticket.ID");
				}catch(Exception e){
					showException(e);
					getLogger().info("TicketIDを正常に読み込めませんでした。");
					onDisable();
				}
				
				sender.sendMessage(ChatColor.GREEN + "NekoEventの設定を再読み込みしました。");
				
				break;
			case "test":
				test(args[1]);
				break;
			case "ticket":
				if(args.length < 3){
					getLogger().info("対象プレイヤーまたはチケット数が指定されていません。");
					return true;
				}
				giveTicket(args[1],args[2]);
				break;
			case "athletic":
				clearAthletics(args[1],args[2]);
				break;
			}
			
		}
		
		return true;
	}
	
	public void showException(Exception _e) {
		getLogger().info(_e.toString());
	}
	
	private void giveTicket(String player,String number){
		int ticket_number = 0;
		
		try {
			ticket_number = Integer.parseInt(number);
		} catch (NumberFormatException nfex) {
			showException(nfex);
		}
		
		if(ticket_number > 0){
			getServer().dispatchCommand(getServer().getConsoleSender(), "give " + player + ticket_str + ticket_number);
			
			getServer().dispatchCommand(getServer().getConsoleSender(), "tell " + player + " &bイベントチケット&fを&a" + ticket_number + "枚&6ゲット&fしました！");
			getLogger().info(player + "に、イベントチケットを" + ticket_number + "枚追加しました。");
		}
		
	}
	
	public void test(String name){
		
		try{
			createPlayer(name);
		}catch(Exception e){
			showException(e);
		}
	}
	
	private boolean checkString(String str) {

		if (str.length() < 1)
			return false;

		return true;
	}

	public void createPlayer(String name) {

		if (checkString(name) == false) return;

		// create Key of player
		getConfig().set(name + "." + "check", true);
		saveConfig();
	}
	
	public void clearAthletics(String stage, String name) {

		if (checkString(name) == false || checkString(stage) == false) return;

		getConfig().set(name + "." + "Athletic." + stage, true);
		saveConfig();
	}

}
