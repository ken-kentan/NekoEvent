package jp.kentan.minecraft.neko_event.config.provider;

import jp.kentan.minecraft.neko_event.config.ConfigManager;
import jp.kentan.minecraft.neko_event.config.listener.ConfigListener;
import jp.kentan.minecraft.neko_event.game.dungeon.model.Dungeon;
import jp.kentan.minecraft.neko_event.util.Log;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

import static jp.kentan.minecraft.neko_event.util.NekoUtil.DATE_FORMAT;

public class DungeonConfigProvider {

    private static File sConfigFile;

    private static ConfigListener<Dungeon> sListener;

    public static void setup(File dataFolder){
        sConfigFile = new File(dataFolder + File.separator + "dungeon.yml");

        ConfigManager.createIfNeed(sConfigFile);
    }

    public static void bindListener(ConfigListener<Dungeon> listener){
        sListener = listener;
    }

    public static void load(){
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            final Map<String, Dungeon> dungeonMap = new HashMap<>();

            if(!config.isConfigurationSection("Dungeon")){
                Log.warn("Dungeon section not found.");
                return;
            }

            final Set<String> dungeonIdSet = config.getConfigurationSection("Dungeon").getKeys(false);

            dungeonIdSet.forEach(id -> {
                final String path = "Dungeon." + id;

                Dungeon dungeon = new Dungeon(id,
                        config.getString(path + ".name", "ダンジョン"),
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
                        config.getString(path + ".Join.playerMsg"),
                        config.getString(path + ".Join.broadcastMsg"),
                        config.getString(path + ".Clear.playerMsg"),
                        config.getString(path + ".Clear.broadcastMsg"),
                        config.getString(path + ".Clear.titleText"),
                        config.getBoolean(path + ".Clear.enableSound"),
                        config.getString(path + ".TimedOut.broadcastMsg"),
                        config.getInt(path + ".Reward.ticketAmount"),
                        config.getString(path + ".Reward.gachaId")
                );

                dungeonMap.put(id, dungeon);
            });

            Log.info(dungeonMap.size() + "個のダンジョンを読み込みました.");

            sListener.onUpdate(dungeonMap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
        }
    }

    public static Dungeon create(String id, String name, Location joinLocation, Location clearLocation, String joinPlayerMsg,
                                           String joinBroadcastMsg, String clearPlayerMsg, String clearBroadcastMsg, String clearTitleText,
                                           Boolean enableClearSound, String timedOutBroadcastMsg, int rewardTicketAmount, String rewordGachaId) {
        final String path = "Dungeon." + id;

        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            if(config.isConfigurationSection(path)){
                Log.error("ダンジョン({id})は既に登録されています.".replace("{id}",id));
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

                put(path + ".Join.playerMsg", joinPlayerMsg);
                put(path + ".Join.broadcastMsg", joinBroadcastMsg);
                put(path + ".Clear.playerMsg", clearPlayerMsg);
                put(path + ".Clear.broadcastMsg", clearBroadcastMsg);
                put(path + ".Clear.titleText", clearTitleText);
                put(path + ".Clear.enableSound", enableClearSound);
                put(path + ".TimedOut.broadcastMsg", timedOutBroadcastMsg);

                put(path + ".Reward.ticketAmount", rewardTicketAmount);
                put(path + ".Reward.gachaId", rewordGachaId);
            }
        })){
            return null;
        }

        return new Dungeon(id, name, joinLocation, clearLocation, joinPlayerMsg, joinBroadcastMsg, clearPlayerMsg, clearBroadcastMsg, clearTitleText, enableClearSound, timedOutBroadcastMsg, rewardTicketAmount, rewordGachaId);
    }

    public static boolean update(String id, Location joinLocation, Location clearLocation, String joinPlayerMsg, String joinBroadcastMsg, String clearPlayerMsg,
                                        String clearBroadcastMsg, String clearTitleText, Boolean enableClearSound, String timedOutBroadcastMsg, int rewardTicketAmount, String rewardGachaId) {
        final String path = "Dungeon." + id;

        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sConfigFile);

            if(!config.isConfigurationSection(path)){
                Log.error("ダンジョン({id})は存在しません.".replace("{id}",id));
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
                if(enableClearSound != null){
                    put(path + ".Clear.enableSound", enableClearSound);
                }

                if(timedOutBroadcastMsg != null){
                    put(path + ".TimedOut.broadcastMsg", timedOutBroadcastMsg);
                }

                if(rewardTicketAmount > 0) {
                    put(path + ".Reward.ticketAmount", rewardTicketAmount);
                }
                if(rewardGachaId != null) {
                    put(path + ".Reward.gachaId", rewardGachaId);
                }
            }
        });
    }

    public static boolean delete(String id) {
        final String path = "Dungeon." + id;

        return save(new HashMap<String, Object>(){
            {
                put(path , null);
            }
        });
    }

    public static boolean saveClearDate(UUID uuid, String id){
        return PlayerConfigProvider.save(uuid, new HashMap<String, Object>(){
            {
                put("Dungeon." + id + ".clearDate", DATE_FORMAT.format(new Date()));
            }
        });
    }

    public static boolean hasClearedToday(UUID uuid, String id){
        try {
            final String strPrev = (String) PlayerConfigProvider.get(uuid, "Dungeon." + id + ".clearDate", null);

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
