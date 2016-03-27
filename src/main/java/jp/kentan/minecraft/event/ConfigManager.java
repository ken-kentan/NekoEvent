package jp.kentan.minecraft.event;

public class ConfigManager {
	static NekoEvent ne = NekoEvent.getInstance();

	public static void setBase() {
		ne.reloadConfig();

		TicketManager.name      = ne.getConfig().getString("ticket.ID");
		TicketManager.itemstack = ne.getConfig().getString("ticket.ItemStack");

		TimeManager.minute   = ne.getConfig().getInt("minute");
		TimeManager.month    = ne.getConfig().getInt("special.month");
		TimeManager.day      = ne.getConfig().getInt("special.day");
		TimeManager.tp_limit = ne.getConfig().getInt("TP.tp_limit");
		
		NekoEvent.sp_itemid  = ne.getConfig().getString("special.item.ID");
		NekoEvent.sp_name    = ne.getConfig().getString("special.item.name");

		GameManager.reward_rate = ne.getConfig().getInt("reward_rate");

		for (int j = 0; j < 5; j++) {
			for (int i = 0; i < 10; i++) {
				if (ne.getConfig().getString("gacha.ID." + j + "_" + i) != null) {
					NekoEvent.gacha_list[j][i]     = ne.getConfig().getString("gacha.ID." + j + "_" + i);// 0_0
					NekoEvent.gacha_itemname[j][i] = ne.getConfig().getString("gacha.name." + j + "_" + i);
					NekoEvent.gacha_numbers[j] = i;
				}
			}
		}
		
		for (int i = 0; i < 20; i++) {
			if (ne.getConfig().getString("buy.command." + i) != null) {
				NekoEvent.buy_list[i] = ne.getConfig().getString("buy.command."+ i);
				NekoEvent.buy_name[i] = ne.getConfig().getString("buy.name." + i);
			}
		}
		
		for (int i=0; i<10; i++){
			if (ne.getConfig().getString("trigger.item." + i) != null) {
				TriggerManager.triggerItem[i] = ne.getConfig().getString("trigger.item." + i);
			}
		}

		ne.getLogger().info("Done. getBaseConfig from config.yml");
		ne.getLogger().info("Result. minute:" + TimeManager.minute + " reward_rate:" + GameManager.reward_rate
				+ " gacha_numbers:" + NekoEvent.gacha_numbers[0] + "," + NekoEvent.gacha_numbers[1] + ","
				+ NekoEvent.gacha_numbers[2] + "," + NekoEvent.gacha_numbers[3] + "," + NekoEvent.gacha_numbers[4] + " special m,d:"
				+ TimeManager.month + "," + TimeManager.day + " TP_limit:" + TimeManager.tp_limit);
	}

	public static void save() {
		ne.getLogger().info("Saving. minute:" + TimeManager.minute);
		ne.getConfig().set("minute", TimeManager.minute);
		ne.getLogger().info("Save success!");
		ne.saveConfig();
	}
}
