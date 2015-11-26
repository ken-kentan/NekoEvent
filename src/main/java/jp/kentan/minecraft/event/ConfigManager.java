package jp.kentan.minecraft.event;

public class ConfigManager {
	NekoEvent ne;
	TicketManager ticket = new TicketManager();
	TimeManager time = new TimeManager();
	GameManager game = new GameManager();
	
	public void setInstance(NekoEvent _ne){
		ne = _ne;
	}
	
	public void setBase(){
		ne.reloadConfig();
		
		TicketManager.name       = ne.getConfig().getString("ticket.ID");
		TicketManager.itemstack  = ne.getConfig().getString("ticket.ItemStack");
		
		TimeManager.minute = ne.getConfig().getInt("minute");
		GameManager.reward_rate = ne.getConfig().getInt("reward_rate");
		
		for(int j = 0; j < 5; j++){
			for(int i = 0; i < 10; i++){
				if(ne.getConfig().getString("gacha.ID." + j + "_" + i) != null){
					NekoEvent.gacha_list[j][i] = ne.getConfig().getString("gacha.ID." + j + "_" + i);//0_0
					NekoEvent.gacha_itemname[j][i] = ne.getConfig().getString("gacha.name." + j + "_" + i);
					NekoEvent.gacha_numbers[j] = i;
				}
			}
		}
		
		ne.getLogger().info("Done. getBaseConfig from config.yml");
		ne.getLogger().info("Result. minute:" + TimeManager.minute + " reward_rate:" + GameManager.reward_rate + " gacha_numbers:" + NekoEvent.gacha_numbers[0] + "," + NekoEvent.gacha_numbers[1] + "," + NekoEvent.gacha_numbers[2] + "," + NekoEvent.gacha_numbers[3] + "," + NekoEvent.gacha_numbers[4]);
	}
	
	public void save(){
		ne.getLogger().info("Saved. minute:" + TimeManager.minute);
		ne.getConfig().set("minute", TimeManager.minute);
		ne.saveConfig();
	}
}
