package jp.kentan.minecraft.event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bukkit.command.CommandSender;

public class Log {
	final private static File path = new File("./plugins/NekoEvent/log.txt");

	public static void write(String str) {
		try {
			if (isAvailableFile()) {
				FileWriter file = new FileWriter(path, true);

				StringBuilder builder = new StringBuilder("[");
				builder.append(ConfigManager.FORMATER_SEC.format(Calendar.getInstance().getTime()));
				builder.append("]");
				builder.append(str);
				builder.append("\r\n");

				file.write(builder.toString());
				file.close();
			} else {
				NekoEvent.sendInfoMessage("ログファイルが見つかりません.");
			}
		} catch (IOException e) {
			NekoEvent.sendErrorMessage("ログをファイルに書き込めませんでした.");
		}
	}

	private static List<String> read(int page) {
		List<String> list = new ArrayList<String>();

		try {
			if (isAvailableFile()) {
				LineNumberReader lineReader = new LineNumberReader(new FileReader(path));
				BufferedReader buffReader = new BufferedReader(new FileReader(path));

				while (lineReader.readLine() != null) {
				}
				
				int start = lineReader.getLineNumber() - ++page * 10;
				int end = start + 10;

				for (int i = 0; i < end; ++i) {
					String buff = buffReader.readLine();

					if (i < start) continue;

					if (buff == null) break;

					list.add(buff);
				}

				lineReader.close();
				buffReader.close();
			}
		} catch (IOException e) {
			NekoEvent.sendErrorMessage("ログ読み込みｴﾗｰ:" + e.getMessage());
			return null;
		}

		return list;
	}
	
	public static void refer(CommandSender sender, int page){
		int cnt = (page + 1) * 10 + 1;
		
		sender.sendMessage(NekoEvent.CHAT_TAG + "イベントログ<" + page + "ページ>");
		List<String> log = Log.read(page);
		StringBuilder builder = new StringBuilder();
		
		String format = "%" + String.valueOf(cnt).length() + "s";
		
		for(String line : log){
			builder.append(String.format(format, --cnt));
			builder.append(":");
			builder.append(line);
			
			sender.sendMessage(builder.toString());
			
			builder.setLength(0);
		}
	}

	private static boolean isAvailableFile() {
		if (path.exists()) {
			if (path.isFile() && path.canWrite() && path.canRead())
				return true;
		}
		return false;
	}
}
