package jp.kentan.minecraft.neko_event.config.provider;

import jp.kentan.minecraft.neko_event.config.ConfigManager;
import jp.kentan.minecraft.neko_event.config.listener.ConfigListener;
import jp.kentan.minecraft.neko_event.module.password.model.Password;
import jp.kentan.minecraft.neko_event.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class PasswordConfigProvider {

    private static File sConfigFile;

    private static ConfigListener<Password> sListener;

    public static void setup(File dataFolder){
        sConfigFile = new File(dataFolder + File.separator + "password.yml");

        ConfigManager.createIfNeed(sConfigFile);
    }

    public static void bindListener(ConfigListener<Password> listener){
        sListener = listener;
    }

    public static void load() {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            final Map<String, Password> passMap = new HashMap<>();

            if(!config.isConfigurationSection("Password")){
                Log.warn("Password section not found.");
                return;
            }

            final Set<String> passIdSet = config.getConfigurationSection("Password").getKeys(false);

            passIdSet.forEach(id -> {
                final String path = "Password." + id;

                Password pass = new Password(id,
                        config.getString(path + ".default"),
                        config.isString(path + ".Block.material") ?
                                Material.matchMaterial(config.getString(path + ".Block.material")) : null,
                        config.isConfigurationSection(path + ".Block.Location") ?
                                new Location(
                                        Bukkit.getWorld(config.getString(path + ".Block.Location.world")),
                                        config.getDouble(path + ".Block.Location.x"),
                                        config.getDouble(path + ".Block.Location.y"),
                                        config.getDouble(path + ".Block.Location.z")
                                ) : null,
                        config.getString(path + ".matchMsg"),
                        config.getString(path + ".notMatchMsg"),
                        config.getString(path + ".inputMsg"),
                        config.getString(path + ".clearMsg")
                );

                passMap.put(id, pass);
            });

            Log.info(passMap.size() + "個のパスワードを読み込みました.");

            sListener.onUpdate(passMap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
        }
    }

    public static Password create(String id, String defaultPass,
                                  Material blockMaterial, Location blockLocation,
                                  String matchMsg, String notMatchMsg, String inputMsg, String clearMsg) {
        final String path = "Password." + id;

        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            if(config.isConfigurationSection(path)){
                Log.error("パスワード({id})は既に登録されています.".replace("{id}",id));
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
            return null;
        }

        if(!save(new LinkedHashMap<String, Object>(){
            {
                put(path + ".default", defaultPass);

                if(blockMaterial != null){
                    put(path + ".Block.material", blockMaterial.toString());
                }

                if(blockLocation != null){
                    put(path + ".Block.Location.world", blockLocation.getWorld().getName());
                    put(path + ".Block.Location.x", blockLocation.getBlockX());
                    put(path + ".Block.Location.y", blockLocation.getBlockY());
                    put(path + ".Block.Location.z", blockLocation.getBlockZ());
                }

                put(path + ".matchMsg", matchMsg);
                put(path + ".notMatchMsg", notMatchMsg);
                put(path + ".inputMsg", inputMsg);
                put(path + ".clearMsg", clearMsg);
            }
        })){
            return null;
        }

        return new Password(id, defaultPass, blockMaterial, blockLocation, matchMsg, notMatchMsg, inputMsg, clearMsg);
    }

    public static boolean delete(String id) {
        final String path = "Password." + id;

        return save(new HashMap<String, Object>(){
            {
                put(path , null);
            }
        });
    }

    public static boolean update(String id, String defaultPass,
                                 Material blockMaterial, Location blockLocation,
                                 String matchMsg, String notMatchMsg, String inputMsg, String clearMsg) {
        final String path = "Password." + id;

        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            if(!config.isConfigurationSection(path)){
                Log.error("パスワード({id})は存在しません.".replace("{id}",id));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
            return false;
        }

        return save(new LinkedHashMap<String, Object>(){
            {
                if(defaultPass != null) {
                    put(path + ".default", defaultPass);
                }

                if(blockMaterial != null){
                    put(path + ".Block.material", blockMaterial.toString());
                }

                if(blockLocation != null){
                    put(path + ".Block.Location.world", blockLocation.getWorld().getName());
                    put(path + ".Block.Location.x", blockLocation.getBlockX());
                    put(path + ".Block.Location.y", blockLocation.getBlockY());
                    put(path + ".Block.Location.z", blockLocation.getBlockZ());
                }

                if(matchMsg != null) {
                    put(path + ".matchMsg", matchMsg);
                }
                if(notMatchMsg != null) {
                    put(path + ".notMatchMsg", notMatchMsg);
                }
                if(inputMsg != null) {
                    put(path + ".inputMsg", inputMsg);
                }
                if(clearMsg != null) {
                    put(path + ".clearMsg", clearMsg);
                }
            }
        });
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
}
