package jp.kentan.minecraft.event;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
	private NekoEvent ne = null;
	private static final int kMaxLoopLimit = 100;
	
	private final Charset CONFIG_CHAREST = StandardCharsets.UTF_8;
	private String configGachaFile = null;
	
	public ConfigManager(NekoEvent ne){
		this.ne = ne;
		
		configGachaFile = ne.getDataFolder() + File.separator + "gacha.yml";
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

		ne.sendInfoMessage("Successfully loaded the config file.");
		ne.getLogger().info("Minute => " + TimeManager.minute);
		ne.getLogger().info("Reward rate => " + GameManager.reward_rate);
		ne.getLogger().info("Special day => " + TimeManager.month + "/" + TimeManager.day);
		ne.getLogger().info("Buy commands => " + NekoEvent.buy_command_list.size());
		ne.getLogger().info("Trigger items => " + TriggerManager.item_list.size());
	}
	
	private List<String> readGachaData(String path) {
		List<String> list = null;

		try (Reader reader = new InputStreamReader(new FileInputStream(configGachaFile), CONFIG_CHAREST)) {

			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);

			list = conf.getStringList(path);
		} catch (Exception e) {
			ne.sendErrorMessage(e.getMessage());
		}

		return list;
	}
	
	public List<String> readGachaCommand(String strGachaID) {
		return readGachaData("Gacha." + strGachaID + ".Command");
	}
	
	public List<String> readGachaName(String strGachaID) {
		return readGachaData("Gacha." + strGachaID + ".Name");
	}
	
	public List<String> readGachaList() {
		return readGachaData("ID");
	}

	public void save() {
		ne.getLogger().info("Saving. minute:" + TimeManager.minute);
		ne.getConfig().set("minute", TimeManager.minute);
		ne.saveConfig();
		ne.getLogger().info("Save success!");
	}
	
	public void saveGachaData(String strID, String strCommand, String strName){
		try {
			File configFile = new File(ne.getDataFolder(), "gacha.yml");
			String pathCommand = "Gacha." + strID + ".Command";
			String pathName    = "Gacha." + strID + ".Name";

			if (configFile != null) {
				FileConfiguration conf = new YamlConfiguration();
				conf.load(configFile);

				List<String> commandList = conf.getStringList(pathCommand);
				List<String> nameList    = conf.getStringList(pathName);

				commandList.add(strCommand);
				nameList.add(strName);

				conf.set(pathCommand, commandList);
				conf.set(pathName, nameList);

				conf.save(configFile);
			}
		} catch (Exception e) {
			ne.sendErrorMessage(e.getMessage());
		}
	}
	
	public boolean saveGachaID(String strID){
		try {
			File configFile = new File(ne.getDataFolder(), "gacha.yml");

			if (configFile != null) {
				FileConfiguration conf = new YamlConfiguration();
				conf.load(configFile);
				
				List<String> idList = conf.getStringList("ID");
				
				if(conf.get("Gacha." + strID) != null) return false;
				
				idList.add(strID);
				
				conf.set("ID", idList);
				conf.set("Gacha." + strID, "Command");

				conf.save(configFile);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public boolean deleteGachaData(String strID, int index){
		try {
			File configFile = new File(ne.getDataFolder(), "gacha.yml");
			String pathCommand = "Gacha." + strID + ".Command";
			String pathName    = "Gacha." + strID + ".Name";

			if (configFile != null) {
				FileConfiguration conf = new YamlConfiguration();
				conf.load(configFile);

				List<String> commandList = conf.getStringList(pathCommand);
				List<String> nameList    = conf.getStringList(pathName);

				commandList.remove(index);
				nameList.remove(index);

				conf.set(pathCommand, commandList);
				conf.set(pathName, nameList);

				conf.save(configFile);
			}
		} catch (Exception e) {
			ne.sendErrorMessage(e.getMessage());
			return false;
		}
		return true;
	}
}
