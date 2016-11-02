package jp.kentan.minecraft.event;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class Log {
	final private static File path = new File("./plugins/NekoEvent/log.txt");
	
	public static void write(String str) {
		try {
			if (isAvailableFile()) {
				FileWriter filewriter = new FileWriter(path, true);

				filewriter.write("[" + ConfigManager.FORMATER_SEC.format(Calendar.getInstance().getTime()) + "]" + str + "\r\n");

				filewriter.close();
			} else {
				NekoEvent.sendInfoMessage("ログファイルが見つかりません.");
			}
		} catch (IOException e) {
			NekoEvent.sendErrorMessage("ログをファイルに書き込めませんでした.");
		}
	}
	
	private static boolean isAvailableFile() {
		if (path.exists()) {
			if (path.isFile() && path.canWrite())
				return true;
		}
		return false;
	}
}
