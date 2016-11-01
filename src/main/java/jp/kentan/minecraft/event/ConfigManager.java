package jp.kentan.minecraft.event;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
	final public static SimpleDateFormat FORMATER_DAY = new SimpleDateFormat("yyyy/MM/dd");
	final public static SimpleDateFormat FORMATER_SEC = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	final private static Charset CONFIG_CHAREST = StandardCharsets.UTF_8;
	final private static int MAX_LOOP_LIMIT = 100;
	
	private NekoEvent neko;
	private String configGachaFile;
	private Logger logger;
	
	public ConfigManager(NekoEvent neko){
		this.neko = neko;
		this.logger = neko.getLogger();
		
		configGachaFile = neko.getDataFolder() + File.separator + "gacha.yml";
	}

	public void load() {
		neko.reloadConfig();
		FileConfiguration config = neko.getConfig();

		String ticket_name      = config.getString("ticket.ID");
		String ticket_itemstack = config.getString("ticket.ItemStack");
		
		TicketManager.setup(ticket_name, ticket_itemstack);
		
		Calendar specialDay = new GregorianCalendar();
		
		try{
			specialDay.setTime(FORMATER_DAY.parse(config.getString("special.date")));	
		}catch (ParseException e) {
			logger.warning(e.getMessage());
		}
		
		TimeManager.setup(specialDay);
		
		NekoEvent.sp_itemid  = config.getString("special.item.ID");
		NekoEvent.sp_name    = config.getString("special.item.name");

		GameManager.reward_rate = config.getInt("reward_rate");
		
		//All List clear
		NekoEvent.buy_command_list.clear();
		NekoEvent.buy_name_list.clear();
		TriggerManager.item_list.clear();
		
		for (int i = 0; i < MAX_LOOP_LIMIT; i++) {
			if (config.getString("buy.command." + i) == null) break;
			
			NekoEvent.buy_command_list.add(config.getString("buy.command."+ i));
			NekoEvent.buy_name_list.add(config.getString("buy.name." + i));
		}
		
		for (int i=0; i < MAX_LOOP_LIMIT; i++){
			if (config.getString("trigger.item." + i) == null) break;
			
			TriggerManager.item_list.add(config.getString("trigger.item." + i));
		}

		neko.sendInfoMessage("Successfully loaded the config file.");
		logger.info("Reward rate => " + GameManager.reward_rate);
		logger.info("Special day => " + FORMATER_DAY.format(specialDay.getTime()));
		logger.info("Buy commands => " + NekoEvent.buy_command_list.size());
		logger.info("Trigger items => " + TriggerManager.item_list.size());
	}
	
	private List<String> readGachaData(String path) {
		List<String> list = null;

		try (Reader reader = new InputStreamReader(new FileInputStream(configGachaFile), CONFIG_CHAREST)) {

			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);

			list = conf.getStringList(path);
		} catch (Exception e) {
			neko.sendErrorMessage(e.getMessage());
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
		
//		logger.info("Saving. minute:" + TimeManager.minute);
//		neko.getConfig().set("now", TimeManager.minute);
		neko.saveConfig();
		logger.info("Save success!");
	}
	
	public String readString(String path){
		return neko.getConfig().getString(path);
	}
	
	public void saveGachaData(String strID, String strCommand, String strName){
		try {
			File configFile = new File(neko.getDataFolder(), "gacha.yml");
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
			neko.sendErrorMessage(e.getMessage());
		}
	}
	
	public boolean saveGachaID(String strID){
		try {
			File configFile = new File(neko.getDataFolder(), "gacha.yml");

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
			File configFile = new File(neko.getDataFolder(), "gacha.yml");
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
			neko.sendErrorMessage(e.getMessage());
			return false;
		}
		return true;
	}
}
