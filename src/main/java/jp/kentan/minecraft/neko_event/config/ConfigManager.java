package jp.kentan.minecraft.neko_event.config;

import jp.kentan.minecraft.neko_event.config.provider.*;
import jp.kentan.minecraft.neko_event.util.Log;

import java.io.*;

public class ConfigManager {

    private final File mDataFolder;

    public ConfigManager(File dataFolder){
        mDataFolder = dataFolder;

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
        PlayerConfigProvider.setup(mDataFolder);
        GachaConfigProvider.load();
        DungeonConfigProvider.load();
        ParkourConfigProvider.load();
        KeyConfigProvider.load();
        PasswordConfigProvider.load();
    }
}
