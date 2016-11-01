package jp.kentan.minecraft.event;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GachaManager {
	private NekoEvent ne;
	private ConfigManager config;
	private TicketManager ticket;
	
	private Server server;
	
	public GachaManager(NekoEvent ne, ConfigManager config, TicketManager ticket){
		this.ne = ne;
		this.config = config;
		this.ticket = ticket;
		
		server = ne.getServer();
	}
	
	public boolean createGacha(String strGachaID){
		return config.saveGachaID(strGachaID);
	}
	
	public void addCommand(String strGachaID, String strName, String strCommand){
		config.saveGachaData(strGachaID, strCommand, strName);
	}
	
	public boolean removeCommand(String strGachaID, String strIndex){
		return config.deleteGachaData(strGachaID, Integer.parseInt(strIndex));
	}
	
	public void infoGachaID(String strGachaID, CommandSender player){
		int index = 0;
		List<String> commandList = config.readGachaCommand(strGachaID);
		List<String> nameList    = config.readGachaName(strGachaID);
		
		player.sendMessage(NekoEvent.CHAT_TAG + "--------- Gacha(" + strGachaID + ")'s info ---------");
		
		for(String command : commandList){
			player.sendMessage(NekoEvent.CHAT_TAG + index + " - " + nameList.get(index));
			player.sendMessage(NekoEvent.CHAT_TAG + index + " - " + command);
			index++;
		}
	}
	
	public void infoGachaList(CommandSender player){
		List<String> gachaList = config.readGachaList();
		String strGachaList = "";
		
		for(String gachaString : gachaList){
			strGachaList = strGachaList.concat(gachaString).concat("  ");
		}
		
		player.sendMessage(NekoEvent.CHAT_TAG + "Gacha ID List: " + strGachaList);
	}
	
	public void gacha(String strGachaID,String strCost, String strPlayer, boolean isSilent) {
		List<String> commandList = config.readGachaCommand(strGachaID);
		List<String> nameList    = config.readGachaName(strGachaID);
		Player player = ne.convertToPlayer(strPlayer);
		int index = (int) (Math.random() * (commandList.size()));
		
		if(commandList.size() < 1 || nameList.size() < 1 || (commandList.size() != nameList.size())){
			ne.sendErrorMessage("Gacha(" + strGachaID + ") is Not Found or Invaild data set.");
			return;
		}
		
		if(!ticket.remove(strPlayer, strCost)) return;
		
		String command = commandList.get(index);
		
		command = command.replace("{player}", player.getName());

		server.dispatchCommand(server.getConsoleSender(), command);

		if(!isSilent){
			player.sendMessage(NekoEvent.CHAT_TAG + ChatColor.AQUA + nameList.get(index) + ChatColor.WHITE + "を" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "！");
			ne.broadcast(player,NekoEvent.CHAT_TAG + ChatColor.BLUE + player.getName() + ChatColor.WHITE + "が" + ChatColor.AQUA + nameList.get(index) + ChatColor.WHITE + "を" + ChatColor.GOLD + "ゲット" + ChatColor.WHITE + "しました！");
		}
		
		ne.sendInfoMessage("Gave the " + nameList.get(index) + " to " + strPlayer + " (Silent mode: " + isSilent + ").");
		ne.writeLog("Gacha:" + player.getName() + " get:" +nameList.get(index) + "(" + command + ")");
	}
}
