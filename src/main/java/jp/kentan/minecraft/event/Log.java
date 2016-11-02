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

	public static List<String> read(int page) {
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

					if (i < start)
						continue;

					if (buff == null)
						break;

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

	private static boolean isAvailableFile() {
		if (path.exists()) {
			if (path.isFile() && path.canWrite() && path.canRead())
				return true;
		}
		return false;
	}
}
