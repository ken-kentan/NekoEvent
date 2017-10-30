package jp.kentan.minecraft.neko_event.config.provider;

import jp.kentan.minecraft.neko_event.config.ConfigManager;
import jp.kentan.minecraft.neko_event.util.Log;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class PlayerConfigProvider {
    private static String sFolderPath;

    public static void setup(File dataFolder){
        sFolderPath = dataFolder + File.separator + "players" + File.separator;

        ConfigManager.createIfNeed(new File(sFolderPath));
    }

    static Object get(UUID uuid, String path, Object def) {
        final File file = new File(sFolderPath + uuid + ".yml");

        try {
            ConfigManager.createIfNeed(file);

            FileConfiguration config = new YamlConfiguration();
            config.load(file);

            return config.get(path, def);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
            return def;
        }
    }

    static boolean save(UUID uuid, Map<String, Object> dataList) {
        final File file = new File(sFolderPath + uuid + ".yml");

        try {
            ConfigManager.createIfNeed(file);

            FileConfiguration config = new YamlConfiguration();
            config.load(file);

            dataList.forEach(config::set);

            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
            return false;
        }

        return true;
    }
}
