package jp.kentan.minecraft.event;

public class ConfigManager {
	private NekoEvent ne = null;
	private static final int kMaxLoopLimit = 100;
	
	public ConfigManager(NekoEvent ne){
		this.ne = ne;
	}

	public void load() {
		ne.reloadConfig();

		TicketManager.name      = ne.getConfig().getString("ticket.ID");
		TicketManager.itemstack = ne.getConfig().getString("ticket.ItemStack");

		TimeManager.minute   = ne.getConfig().getInt("minute");
		TimeManager.month    = ne.getConfig().getInt("special.month");
		TimeManager.day      = ne.getConfig().getInt("special.day");
		
		NekoEvent.sp_itemid  = ne.getConfig().getString("special.item.ID");
		NekoEvent.sp_name    = ne.getConfig().getString("special.item.name");

		GameManager.reward_rate = ne.getConfig().getInt("reward_rate");

		for (int j = 0; j < 5; j++) {
			for (int i = 0; i < kMaxLoopLimit; i++) {
				if (ne.getConfig().getString("gacha.ID." + j + "_" + i) != null) {
					NekoEvent.gacha_list[j][i]     = ne.getConfig().getString("gacha.ID." + j + "_" + i);// 0_0
					NekoEvent.gacha_itemname[j][i] = ne.getConfig().getString("gacha.name." + j + "_" + i);
					NekoEvent.gacha_numbers[j] = i;
				}
			}
		}
		
		//All List clear
		NekoEvent.buy_command_list.clear();
		NekoEvent.buy_name_list.clear();
		TriggerManager.item_list.clear();
		
		for (int i = 0; i < kMaxLoopLimit; i++) {
			if (ne.getConfig().getString("buy.command." + i) == null) break;
			
			NekoEvent.buy_command_list.add(ne.getConfig().getString("buy.command."+ i));
			NekoEvent.buy_name_list.add(ne.getConfig().getString("buy.name." + i));
		}
		
		for (int i=0; i < kMaxLoopLimit; i++){
			if (ne.getConfig().getString("trigger.item." + i) == null) break;
			
			TriggerManager.item_list.add(ne.getConfig().getString("trigger.item." + i));
		}

		ne.sendInfoMessage("Done. getBaseConfig from config.yml");
		ne.getLogger().info("Minute => " + TimeManager.minute);
		ne.getLogger().info("Reward rate => " + GameManager.reward_rate);
		ne.getLogger().info("Gacha => " + NekoEvent.gacha_numbers[0] + "," + NekoEvent.gacha_numbers[1] + "," + NekoEvent.gacha_numbers[2] + "," + NekoEvent.gacha_numbers[3] + "," + NekoEvent.gacha_numbers[4]);
		ne.getLogger().info("Special day => " + TimeManager.month + "/" + TimeManager.day);
		ne.getLogger().info("Buy commands => " + NekoEvent.buy_command_list.size());
		ne.getLogger().info("Trigger items => " + TriggerManager.item_list.size());
	}

	public void save() {
		ne.getLogger().info("Saving. minute:" + TimeManager.minute);
		ne.getConfig().set("minute", TimeManager.minute);
		ne.saveConfig();
		ne.getLogger().info("Save success!");
	}
}
