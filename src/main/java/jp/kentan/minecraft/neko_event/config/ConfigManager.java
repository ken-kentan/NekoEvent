package jp.kentan.minecraft.neko_event.config;

import jp.kentan.minecraft.neko_event.config.provider.*;
import jp.kentan.minecraft.neko_event.util.Log;

import java.io.*;

public class ConfigManager {

    private final String CONFIG_PATH;
    private final File mDataFolder;

    public ConfigManager(File dataFolder){
        mDataFolder = dataFolder;
        CONFIG_PATH = dataFolder + File.separator + "config.yml";

        SignConfigProvider.setup(dataFolder);
        GachaConfigProvider.setup(dataFolder);
        DungeonConfigProvider.setup(dataFolder);
        ParkourConfigProvider.setup(dataFolder);
        PlayerConfigProvider.setup(dataFolder);
        KeyConfigProvider.setup(dataFolder);
        PasswordConfigProvider.setup(dataFolder);
    }

    public static void createIfNeed(File file){
        if(!file.exists()){
            try {
                if(!file.createNewFile()){
                    Log.warn(file.getName() + "の作成に失敗しました.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.error(e.getMessage());
            }
        }
    }

    public void reload(){
        SignConfigProvider.setup(mDataFolder);
        GachaConfigProvider.setup(mDataFolder);
        DungeonConfigProvider.setup(mDataFolder);
        ParkourConfigProvider.setup(mDataFolder);
        PlayerConfigProvider.setup(mDataFolder);
        KeyConfigProvider.setup(mDataFolder);
        PasswordConfigProvider.setup(mDataFolder);
    }
}
