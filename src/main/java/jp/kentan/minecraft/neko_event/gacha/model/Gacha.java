package jp.kentan.minecraft.neko_event.gacha.model;

import jp.kentan.minecraft.neko_event.util.Log;
import jp.kentan.minecraft.neko_event.util.math.MersenneTwisterFast;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.stream.Collectors;

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

    public void add(String name, double probability, List<String> command){
        mGacha.add(new Component(name, probability, command));
    }

    public void normalizeIfNeed(){
        final double sumProbability = mGacha.stream().mapToDouble(Component::getProbability).sum();
        final List<Component> emptyProbabilityList = mGacha.stream().filter(c -> c.getProbability() <= 0D).collect(Collectors.toList());

        if(sumProbability < 1D){
            if(emptyProbabilityList.size() > 0){
                final double probability = (1D - sumProbability) / emptyProbabilityList.size();

                emptyProbabilityList.forEach(c -> c.mProbability = probability);
            }else{
                double gain = 1D / sumProbability;
                mGacha.forEach(g -> g.mProbability *= gain);
            }
        }else if(sumProbability > 1D){
            Log.error("ｶﾞﾁｬ(" + mId + ") の合計確率が100%を超えています.");
        }
    }

    public Component getByRandom(){
        double sumProbability = 0D;
        double random = RANDOM.nextDouble(false, true);

        for(Component component : mGacha){
            sumProbability += component.getProbability();

            if(sumProbability >= random){
                return component;
            }
        }

        Log.error("ｶﾞﾁｬ(" + mId + ") の確率が正常に動作しませんでした. [sum:" + sumProbability + ", rand:" + random + "]");

        return mGacha.get(mGacha.size() - 1);
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

        private double mProbability;

        private Component(String name, double probability, List<String> commands){
            NAME = ChatColor.translateAlternateColorCodes('&', name);
            mProbability = probability;
            COMMANDS.addAll(commands);
        }

        public String getName(){
            return NAME;
        }

        double getProbability() {
            return mProbability;
        }

        public List<String> getCommands(){
            return COMMANDS;
        }

        public boolean hasCommands(){
            return !COMMANDS.isEmpty();
        }
    }
}
