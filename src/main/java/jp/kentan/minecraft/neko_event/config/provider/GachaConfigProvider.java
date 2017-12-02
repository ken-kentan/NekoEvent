package jp.kentan.minecraft.neko_event.config.provider;

import jp.kentan.minecraft.neko_event.config.ConfigManager;
import jp.kentan.minecraft.neko_event.config.listener.ConfigListener;
import jp.kentan.minecraft.neko_event.gacha.model.Gacha;
import jp.kentan.minecraft.neko_event.util.Log;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class GachaConfigProvider {

    private static File sConfigFile;

    private static ConfigListener<Gacha> sListener;

    public static void setup(File dataFolder){
        sConfigFile = new File(dataFolder + File.separator + "gacha.yml");

        ConfigManager.createIfNeed(sConfigFile);
    }

    public static void bindListener(ConfigListener<Gacha> listener){
        sListener = listener;
    }

    public static void load(){
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            final Map<String, Gacha> gachaMap = new HashMap<>();

            if(!config.isConfigurationSection("Gacha")){
                Log.warn("Gacha section not found.");
                return;
            }

            final Set<String> gachaIdSet = config.getConfigurationSection("Gacha").getKeys(false);

            gachaIdSet.forEach(id -> {
                final String path = "Gacha." + id;

                Gacha gacha = new Gacha(id,
                        config.getString(path + ".name", "ガチャ"),
                        config.getString(path + ".getMsg"),
                        config.getString(path + ".missMsg"),
                        config.getString(path + ".broadcastMsg"),
                        config.getBoolean(path + ".enableEffect", true),
                        config.getBoolean(path + ".requireVoteTicket", false)
                );

                final String componentsPath = "Gacha." + id + ".components";
                final Set<String> components = config.getConfigurationSection(componentsPath).getKeys(false);
                components.forEach(index -> {
                    final String indexPath = componentsPath + "." + index;

                    gacha.add(
                            config.getString(indexPath + ".name"),
                            config.getDouble(indexPath + ".probability"),
                            config.getStringList(indexPath + ".commands"));
                });

                gacha.normalizeIfNeed();
                gachaMap.put(id, gacha);
            });

            Log.info(gachaMap.size() + "個のガチャを読み込みました.");

            sListener.onUpdate(gachaMap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
        }
    }
}
