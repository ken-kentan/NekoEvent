package jp.kentan.minecraft.neko_event.config.provider;

import jp.kentan.minecraft.neko_event.config.ConfigManager;
import jp.kentan.minecraft.neko_event.config.listener.ConfigListener;
import jp.kentan.minecraft.neko_event.game.parkour.model.Parkour;
import jp.kentan.minecraft.neko_event.util.Log;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

import static jp.kentan.minecraft.neko_event.util.NekoUtil.DATE_FORMAT;

public class ParkourConfigProvider {

    private static File sConfigFile;

    private static ConfigListener<Parkour> sListener;

    public static void setup(File dataFolder){
        sConfigFile = new File(dataFolder + File.separator + "parkour.yml");

        ConfigManager.createIfNeed(sConfigFile);
    }

    public static void bindListener(ConfigListener<Parkour> listener){
        sListener = listener;
    }

    public static void load() {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            final Map<String, Parkour> parkourMap = new HashMap<>();

            if(!config.isConfigurationSection("Parkour")){
                Log.warn("Parkour section not found.");
                return;
            }

            final Set<String> parkourIdSet = config.getConfigurationSection("Parkour").getKeys(false);

            parkourIdSet.forEach(id -> {
                final String path = "Parkour." + id;

                Parkour parkour = new Parkour(id,
                        config.getString(path + ".name", "アスレチック"),
                        config.isConfigurationSection(path + ".Join.Location") ?
                                new Location(
                                        Bukkit.getWorld(config.getString(path + ".Join.Location.world")),
                                        config.getDouble(path + ".Join.Location.x"),
                                        config.getDouble(path + ".Join.Location.y"),
                                        config.getDouble(path + ".Join.Location.z"),
                                        (float)config.getDouble(path + ".Join.Location.yaw"),
                                        (float)config.getDouble(path + ".Join.Location.pitch")
                                ) : null,
                        config.isConfigurationSection(path + ".Clear.Location") ?
                                new Location(
                                        Bukkit.getWorld(config.getString(path + ".Clear.Location.world")),
                                        config.getDouble(path + ".Clear.Location.x"),
                                        config.getDouble(path + ".Clear.Location.y"),
                                        config.getDouble(path + ".Clear.Location.z"),
                                        (float)config.getDouble(path + ".Clear.Location.yaw"),
                                        (float)config.getDouble(path + ".Clear.Location.pitch")
                                ) : null,
                        config.isConfigurationSection(path + ".Back.Location") ?
                                new Location(
                                        Bukkit.getWorld(config.getString(path + ".Back.Location.world")),
                                        config.getDouble(path + ".Back.Location.x"),
                                        config.getDouble(path + ".Back.Location.y"),
                                        config.getDouble(path + ".Back.Location.z"),
                                        (float)config.getDouble(path + ".Back.Location.yaw"),
                                        (float)config.getDouble(path + ".Back.Location.pitch")
                                ) : null,
                        config.getString(path + ".Join.playerMsg"),
                        config.getString(path + ".Join.broadcastMsg"),
                        config.getString(path + ".Clear.playerMsg"),
                        config.getString(path + ".Clear.broadcastMsg"),
                        config.getString(path + ".Clear.titleText"),
                        config.getString(path + ".Back.playerMsg")
                );

                parkourMap.put(id, parkour);
            });

            Log.info(parkourMap.size() + "個のアスレチックを読み込みました.");

            sListener.onUpdate(parkourMap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
        }
    }

    public static Parkour create(String id, String name, Location joinLocation, Location clearLocation, Location backLocation, String joinPlayerMsg,
                                 String joinBroadcastMsg, String clearPlayerMsg, String clearBroadcastMsg, String clearTitleText, String baclPlayerMsg) {
        final String path = "Parkour." + id;

        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            if(config.isConfigurationSection(path)){
                Log.error("パルクール({id})は既に登録されています.".replace("{id}",id));
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
            return null;
        }

        if(!save(new LinkedHashMap<String, Object>(){
            {
                put(path + ".name", name);

                if(joinLocation != null) {
                    put(path + ".Join.Location.world", joinLocation.getWorld().getName());
                    put(path + ".Join.Location.x", joinLocation.getX());
                    put(path + ".Join.Location.y", joinLocation.getY());
                    put(path + ".Join.Location.z", joinLocation.getZ());
                    put(path + ".Join.Location.yaw", (double) joinLocation.getYaw());
                    put(path + ".Join.Location.pitch", (double) joinLocation.getPitch());
                }

                if(clearLocation != null) {
                    put(path + ".Clear.Location.world", clearLocation.getWorld().getName());
                    put(path + ".Clear.Location.x", clearLocation.getX());
                    put(path + ".Clear.Location.y", clearLocation.getY());
                    put(path + ".Clear.Location.z", clearLocation.getZ());
                    put(path + ".Clear.Location.yaw", (double) clearLocation.getYaw());
                    put(path + ".Clear.Location.pitch", (double) clearLocation.getPitch());
                }

                if(backLocation != null) {
                    put(path + ".Back.Location.world", backLocation.getWorld().getName());
                    put(path + ".Back.Location.x", backLocation.getX());
                    put(path + ".Back.Location.y", backLocation.getY());
                    put(path + ".Back.Location.z", backLocation.getZ());
                    put(path + ".Back.Location.yaw", (double) backLocation.getYaw());
                    put(path + ".Back.Location.pitch", (double) backLocation.getPitch());
                }

                put(path + ".Join.playerMsg", joinPlayerMsg);
                put(path + ".Join.broadcastMsg", joinBroadcastMsg);
                put(path + ".Clear.playerMsg", clearPlayerMsg);
                put(path + ".Clear.broadcastMsg", clearBroadcastMsg);
                put(path + ".Clear.titleText", clearTitleText);
                put(path + ".Back.playerMsg", baclPlayerMsg);
            }
        })){
            return null;
        }

        return new Parkour(id, name, joinLocation, clearLocation, backLocation, joinPlayerMsg, joinBroadcastMsg, clearPlayerMsg, clearBroadcastMsg, clearTitleText, baclPlayerMsg);
    }

    public static boolean delete(String id) {
        final String path = "Parkour." + id;

        return save(new HashMap<String, Object>(){
            {
                put(path , null);
            }
        });
    }

    public static boolean update(String id, Location joinLocation, Location clearLocation, Location backLocation, String joinPlayerMsg, String joinBroadcastMsg, String clearPlayerMsg,
                                 String clearBroadcastMsg, String clearTitleText, String backPlayerMsg) {
        final String path = "Parkour." + id;

        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            if(!config.isConfigurationSection(path)){
                Log.error("パルクール({id})は存在しません.".replace("{id}",id));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
            return false;
        }

        return save(new LinkedHashMap<String, Object>(){
            {
                if(joinLocation != null) {
                    put(path + ".Join.Location.world", joinLocation.getWorld().getName());
                    put(path + ".Join.Location.x", joinLocation.getX());
                    put(path + ".Join.Location.y", joinLocation.getY());
                    put(path + ".Join.Location.z", joinLocation.getZ());
                    put(path + ".Join.Location.yaw", (double) joinLocation.getYaw());
                    put(path + ".Join.Location.pitch", (double) joinLocation.getPitch());
                }

                if(clearLocation != null){
                    put(path + ".Clear.Location.world", clearLocation.getWorld().getName());
                    put(path + ".Clear.Location.x", clearLocation.getX());
                    put(path + ".Clear.Location.y", clearLocation.getY());
                    put(path + ".Clear.Location.z", clearLocation.getZ());
                    put(path + ".Clear.Location.yaw", (double)clearLocation.getYaw());
                    put(path + ".Clear.Location.pitch", (double)clearLocation.getPitch());
                }

                if(backLocation != null){
                    put(path + ".Back.Location.world", backLocation.getWorld().getName());
                    put(path + ".Back.Location.x", backLocation.getX());
                    put(path + ".Back.Location.y", backLocation.getY());
                    put(path + ".Back.Location.z", backLocation.getZ());
                    put(path + ".Back.Location.yaw", (double)backLocation.getYaw());
                    put(path + ".Back.Location.pitch", (double)backLocation.getPitch());
                }

                if(joinPlayerMsg != null) {
                    put(path + ".Join.playerMsg", joinPlayerMsg);
                }
                if(joinBroadcastMsg != null) {
                    put(path + ".Join.broadcastMsg", joinBroadcastMsg);
                }

                if(clearPlayerMsg != null) {
                    put(path + ".Clear.playerMsg", clearPlayerMsg);
                }
                if(clearBroadcastMsg != null) {
                    put(path + ".Clear.broadcastMsg", clearBroadcastMsg);
                }
                if(clearTitleText != null){
                    put(path + ".Clear.titleText", clearTitleText);
                }

                if(backPlayerMsg != null) {
                    put(path + ".Back.playerMsg", backPlayerMsg);
                }
            }
        });
    }

    public static boolean saveClearDate(UUID uuid, String id){
        return PlayerConfigProvider.save(uuid, new HashMap<String, Object>(){
            {
                put("Parkour." + id + ".clearDate", DATE_FORMAT.format(new Date()));
            }
        });
    }

    public static boolean hasClearedToday(UUID uuid, String id){
        try {
            final String strPrev = (String) PlayerConfigProvider.get(uuid, "Parkour." + id + ".clearDate", null);

            if(strPrev == null){
                return false;
            }

            final Date prev = DATE_FORMAT.parse(strPrev);
            final Date today = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);

            return prev != null && prev.compareTo(today) == 0;
        }catch (Exception e){
            e.printStackTrace();
            Log.error(e.getMessage());
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
}
