package jp.kentan.minecraft.event;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class NekoEvent extends JavaPlugin {
	
	@Override
	public void onEnable() {
		new BukkitRunnable()
		{
		    @Override
		    public void run()
		    {
		    	//run
		    }
		}.runTaskTimer(this, 20, 20);//20 1s
		
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
			case "ticket":
				if(args.length < 3){
					getLogger().info("対象プレイヤーまたはチケット数が指定されていません。");
					return true;
				}
				giveTicket(args[1],args[2]);
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
			getServer().dispatchCommand(getServer().getConsoleSender(), "give " + player + "~~");
			
			getServer().dispatchCommand(getServer().getConsoleSender(), "tell " + player + " &bイベントチケット&fを&a" + ticket_number + "枚&6ゲット&fしました！");
			getLogger().info(player + "に、イベントチケットを" + ticket_number + "枚追加しました。");
		}
		
	}

}
