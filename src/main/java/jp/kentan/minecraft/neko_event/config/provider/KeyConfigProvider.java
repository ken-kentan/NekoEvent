package jp.kentan.minecraft.neko_event.config.provider;

import jp.kentan.minecraft.neko_event.config.ConfigManager;
import jp.kentan.minecraft.neko_event.config.listener.ConfigListener;
import jp.kentan.minecraft.neko_event.module.key.model.Key;
import jp.kentan.minecraft.neko_event.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;


public class KeyConfigProvider {

    private static File sConfigFile;

    private static ConfigListener<Key> sListener;

    public static void setup(File dataFolder){
        sConfigFile = new File(dataFolder + File.separator + "key.yml");

        ConfigManager.createIfNeed(sConfigFile);
    }

    public static void bindListener(ConfigListener<Key> listener){
        sListener = listener;
    }

    public static void load() {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            final Map<String, Key> keyMap = new HashMap<>();

            if(!config.isConfigurationSection("Key")){
                Log.warn("Key section not found.");
                return;
            }

            final Set<String> keyIdSet = config.getConfigurationSection("Key").getKeys(false);

            keyIdSet.forEach(id -> {
                final String path = "Key." + id;

                Key key = new Key(id,
                        ItemStack.deserialize(config.getConfigurationSection(path + ".ItemStack").getValues(true)),
                        config.getBoolean(path + ".take"),
                        config.getInt(path + ".periodMinutes"),
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
                        config.getString(path + ".expiredMsg"),
                        config.getString(path + ".shortAmountMsg")
                );

                keyMap.put(id, key);
            });

            Log.info(keyMap.size() + "個のキーを読み込みました.");

            sListener.onUpdate(keyMap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
        }
    }

    public static Key create(String id, ItemStack itemStack, boolean isTake, int periodMinutes, Material blockMaterial, Location blockLocation,
                             String matchMsg, String notMatchMsg, String expiredMsg, String shortAmountMsg) {
        final String path = "Key." + id;

        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            if(config.isConfigurationSection(path)){
                Log.error("キー({id})は既に登録されています.".replace("{id}",id));
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
            return null;
        }

        final String itemStackPath = path + ".ItemStack.";

        if(!save(new LinkedHashMap<String, Object>(){
            {
                itemStack.serialize().forEach((key, val) -> put(itemStackPath + key, val));

                put(path + ".take", isTake);
                put(path + ".periodMinutes", periodMinutes);

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
                put(path + ".expiredMsg", expiredMsg);
                put(path + ".shotAmountMsg", shortAmountMsg);
            }
        })){
            return null;
        }

        return new Key(id, itemStack, isTake, periodMinutes, blockMaterial, blockLocation, matchMsg, notMatchMsg, expiredMsg, shortAmountMsg);
    }

    public static boolean delete(String id) {
        final String path = "Key." + id;

        return save(new HashMap<String, Object>(){
            {
                put(path , null);
            }
        });
    }

    public static boolean update(String id, ItemStack itemStack, Boolean isTake, int periodMinutes, Material blockMaterial, Location blockLocation,
                                 String matchMsg, String notMatchMsg, String expiredMsg, String shortAmountMsg) {
        final String path = "Key." + id;

        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            if(!config.isConfigurationSection(path)){
                Log.error("キー({id})は存在しません.".replace("{id}",id));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
            return false;
        }

        if(itemStack != null){
            save(new HashMap<String, Object>(){
                {
                    put(path + ".ItemStack", null);
                }
            });
        }

        return save(new LinkedHashMap<String, Object>(){
            {
                if(itemStack != null){
                    final String itemStackPath = path + ".ItemStack.";
                    itemStack.serialize().forEach((key, val) -> put(itemStackPath + key, val));
                }

                if(isTake != null) {
                    put(path + ".take", isTake);
                }

                if(periodMinutes > 0){
                    put(path + ".periodMinutes", periodMinutes);
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
                if(expiredMsg != null) {
                    put(path + ".expiredMsg", expiredMsg);
                }
                if(shortAmountMsg != null){
                    put(path + ".shortAmountMsg", shortAmountMsg);
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
