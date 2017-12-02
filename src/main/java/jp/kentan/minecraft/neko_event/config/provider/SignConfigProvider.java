package jp.kentan.minecraft.neko_event.config.provider;

import jp.kentan.minecraft.neko_event.config.ConfigManager;
import jp.kentan.minecraft.neko_event.util.Log;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class SignConfigProvider {

    private static File sConfigFile;

    private static Map<Location, Metadata> sSignMetadataCacheMap = new HashMap<>();

    public static void setup(File dataFolder){
        sConfigFile = new File(dataFolder + File.separator + "sign.yml");

        ConfigManager.createIfNeed(sConfigFile);

        load();
    }

    private static void load(){
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            if(!config.isConfigurationSection("Sign")){
                Log.warn("Sign section not found.");
                return;
            }

            final Map<Location, Metadata> signMetadataMap = new HashMap<>();

            final Set<String> indexSet = config.getConfigurationSection("Sign").getKeys(false);

            indexSet.forEach(index -> {
                final String path = "Sign." + index;

                Location location = new Location(
                        Bukkit.getWorld(config.getString(path + ".Location.world")),
                        config.getDouble(path + ".Location.x"),
                        config.getDouble(path + ".Location.y"),
                        config.getDouble(path + ".Location.z")
                );

                if(!(location.getBlock().getState() instanceof Sign)){
                    config.set(path, null);
                    Log.warn("看板" + NekoUtil.toString(location) + "が見つかりませんでした.");
                    return;
                }

                Map<String, Object> metadataMap = new HashMap<>();

                final Set<String> metadataSet = config.getConfigurationSection(path + ".Metadata").getKeys(false);
                final String metadataPath = path + ".Metadata.";
                metadataSet.forEach(key -> metadataMap.put(key, config.get(metadataPath + key)));

                if(signMetadataMap.containsKey(location)){
                    Log.error("看板ﾒﾀﾃﾞｰﾀが重複しています. (index:" + index + ")");
                    return;
                }

                signMetadataMap.put(location, new Metadata(index, metadataMap));
            });

            config.save(sConfigFile);

            sSignMetadataCacheMap.clear();
            sSignMetadataCacheMap.putAll(signMetadataMap);

            Log.info(sSignMetadataCacheMap.size() + "件の看板ﾒﾀﾃﾞｰﾀを読み込みました.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
        }
    }

    public static boolean hasMetadata(Location location, String key){
        return sSignMetadataCacheMap.containsKey(location) && sSignMetadataCacheMap.get(location).containsKey(key);
    }

    public static Object getMetadata(Location location, String key){
        return sSignMetadataCacheMap.get(location).get(key);
    }

    public static boolean saveMetadata(Location location, Map<String, Object> metadataMap) {
        final String index, path;

        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            if(sSignMetadataCacheMap.containsKey(location)){
                save(new HashMap<String, Object>() {
                    {
                        put("Sign." + sSignMetadataCacheMap.get(location).getIndex(), null);
                    }
                });
            }

            String signIndex = null;
            for(int i = 0; i < 500; ++i){
                if(!config.isConfigurationSection("Sign." + i)){
                    signIndex = Integer.toString(i);
                    break;
                }
            }

            if(signIndex == null){
                Log.error("看板が保存上限に到達しています.");
                return false;
            }

            index = signIndex;
            path = "Sign." + signIndex;
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
            return false;
        }

        if (save(new LinkedHashMap<String, Object>(){
            {
                put(path + ".Location.world", location.getWorld().getName());
                put(path + ".Location.x", location.getX());
                put(path + ".Location.y", location.getY());
                put(path + ".Location.z", location.getZ());

                final String metadataPath = path + ".Metadata.";

                metadataMap.forEach((key, val) -> put(metadataPath + key, val));
            }
        })){
            sSignMetadataCacheMap.put(location, new Metadata(index, metadataMap));
            return true;
        }else{
            return false;
        }
    }

    private static boolean save(Map<String, Object> dataList) {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            dataList.forEach(config::set);

            config.save(sConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());

            return false;
        }
        return true;
    }


    private static class Metadata{
        private String mIndex;
        private Map<String, Object> mMetadataMap = new HashMap<>();

        Metadata(String index, Map<String, Object> metadataMap){
            mIndex = index;
            mMetadataMap.putAll(metadataMap);
        }

        Object get(String key){
            return mMetadataMap.get(key);
        }

        String getIndex(){
            return mIndex;
        }

        boolean containsKey(String key){
            return mMetadataMap.containsKey(key);
        }
    }
}
