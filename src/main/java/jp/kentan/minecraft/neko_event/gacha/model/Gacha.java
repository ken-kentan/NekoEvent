package jp.kentan.minecraft.neko_event.gacha.model;

import jp.kentan.minecraft.neko_event.util.math.MersenneTwisterFast;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Gacha {
    private final static MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    private List<Component> mGacha = new ArrayList<>();
    private String mId, mName, mGetMsg, mMissMsg, mBroadcastMsg;
    private boolean mEnableEffect, mRequireVoteTicket;

    public Gacha(String id, String name, String getMsg, String missMsg, String broadcastMsg, boolean enableEffect, boolean requireVoteTicket){
        mId = id;
        mName = ChatColor.translateAlternateColorCodes('&', name);
        mGetMsg  = (getMsg != null) ? ChatColor.translateAlternateColorCodes('&', getMsg) : null;
        mMissMsg = (missMsg != null) ? ChatColor.translateAlternateColorCodes('&', missMsg) : null;
        mBroadcastMsg = (broadcastMsg != null) ? ChatColor.translateAlternateColorCodes('&', broadcastMsg) : null;
        mEnableEffect = enableEffect;
        mRequireVoteTicket = requireVoteTicket;
    }

    public void add(String name, List<String> command){
        mGacha.add(new Component(name, command));
    }

    public Component getByRandom(){
        return mGacha.get(RANDOM.nextInt(mGacha.size()));
    }

    public String getId() {
        return mId;
    }

    public String getName(){
        return mName;
    }

    public int getSize(){
        return mGacha.size();
    }

    public String getMessage(){
        return mGetMsg;
    }

    public String getMissMessage(){
        return mMissMsg;
    }

    public String getBroadcastMessage(){
        return mBroadcastMsg;
    }

    public boolean enableEffect(){
        return mEnableEffect;
    }

    public boolean isRequireVoteTicket() {
        return mRequireVoteTicket;
    }

    public boolean hasGetMessage(){
        return mGetMsg != null;
    }

    public boolean hasMissMessage(){
        return mMissMsg != null;
    }

    public boolean hasBroadcastMessage(){
        return mBroadcastMsg != null;
    }

    public static class Component{
        private final String NAME;
        private final List<String> COMMANDS = new ArrayList<>();

        private Component(String name, List<String> commands){
            NAME = ChatColor.translateAlternateColorCodes('&', name);
            COMMANDS.addAll(commands);
        }

        public String getName(){
            return NAME;
        }

        public List<String> getCommands(){
            return COMMANDS;
        }

        public boolean hasCommands(){
            return !COMMANDS.isEmpty();
        }
    }
}
