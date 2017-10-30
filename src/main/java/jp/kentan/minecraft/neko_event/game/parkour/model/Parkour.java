package jp.kentan.minecraft.neko_event.game.parkour.model;

import jp.kentan.minecraft.neko_event.config.provider.ParkourConfigProvider;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Parkour {
    private String mId, mName;
    private String mJoinPlayerMsg, mJoinBroadcastMsg;
    private String mClearPlayerMsg, mClearBroadcastMsg, mClearTitleText;
    private String mBackPlayerMsg;
    private Location mJoinLocation, mClearLocation, mBackLocation;

    public Parkour(String id, String name, Location joinLocation, Location clearLocation, Location backLocation, String joinPlayerMsg, String joinBroadcastMsg,
                   String clearPlayerMsg, String clearBroadcastMsg, String clearTitleText, String backPlayerMsg){
        mId = id;
        mName = ChatColor.translateAlternateColorCodes('&', name);

        mJoinLocation = joinLocation;
        mClearLocation = clearLocation;
        mBackLocation = backLocation;

        mJoinPlayerMsg     = (joinPlayerMsg     != null) ? ChatColor.translateAlternateColorCodes('&', joinPlayerMsg.replace("{name}", name)) : null;
        mJoinBroadcastMsg  = (joinBroadcastMsg  != null) ? ChatColor.translateAlternateColorCodes('&', joinBroadcastMsg.replace("{name}", name)) : null;

        mClearPlayerMsg    = (clearPlayerMsg    != null) ? ChatColor.translateAlternateColorCodes('&', clearPlayerMsg.replace("{name}", name)) : null;
        mClearBroadcastMsg = (clearBroadcastMsg != null) ? ChatColor.translateAlternateColorCodes('&', clearBroadcastMsg.replace("{name}", name)) : null;
        mClearTitleText    = (clearTitleText    != null) ? ChatColor.translateAlternateColorCodes('&', clearTitleText.replace("{name}", name)) : null;

        mBackPlayerMsg = (backPlayerMsg != null) ? ChatColor.translateAlternateColorCodes('&', backPlayerMsg.replace("{name}", name)) : null;
    }

    public boolean update(Location joinLocation, Location clearLocation, Location backLocation, String joinPlayerMsg, String joinBroadcastMsg, String clearPlayerMsg,
                          String clearBroadcastMsg, String clearTitleText, String backPlayerMsg){

        if(!ParkourConfigProvider.update(mId, joinLocation, clearLocation, backLocation, joinPlayerMsg, joinBroadcastMsg, clearPlayerMsg,
                clearBroadcastMsg, clearTitleText, backPlayerMsg)){
            return false;
        }

        if(joinLocation != null){
            mJoinLocation = joinLocation;
        }

        if(clearLocation != null){
            mClearLocation = clearLocation;
        }

        if(backLocation != null){
            mBackLocation = backLocation;
        }

        if(joinPlayerMsg != null){
            mJoinPlayerMsg = ChatColor.translateAlternateColorCodes('&', joinPlayerMsg.replace("{name}", mName));
        }

        if(joinBroadcastMsg != null){
            mJoinBroadcastMsg = ChatColor.translateAlternateColorCodes('&', joinBroadcastMsg.replace("{name}", mName));
        }

        if(clearPlayerMsg != null){
            mClearPlayerMsg = ChatColor.translateAlternateColorCodes('&', clearPlayerMsg.replace("{name}", mName));
        }

        if(clearBroadcastMsg != null){
            mClearBroadcastMsg = ChatColor.translateAlternateColorCodes('&', clearBroadcastMsg.replace("{name}", mName));
        }

        if(clearTitleText != null){
            mClearTitleText = ChatColor.translateAlternateColorCodes('&', clearTitleText.replace("{name}", mName));
        }

        if(backPlayerMsg != null){
            mBackPlayerMsg = ChatColor.translateAlternateColorCodes('&', backPlayerMsg.replace("{name}", mName));
        }

        return true;
    }

    public String getId(){
        return mId;
    }

    public String getName(){
        return mName;
    }

    public Location getJoinLocation() {
        return mJoinLocation;
    }

    public Location getClearLocation() {
        return mClearLocation;
    }

    public Location getBackLocation() {
        return mBackLocation;
    }

    public String getJoinPlayerMessage(){
        return mJoinPlayerMsg;
    }

    public String getJoinBroadcastMessage() {
        return mJoinBroadcastMsg;
    }

    public String getClearPlayerMessage(){
        return mClearPlayerMsg;
    }

    public String getClearBroadcastMessage() {
        return mClearBroadcastMsg;
    }

    public String getClearTitleText() {
        return mClearTitleText;
    }

    public String getBackPlayerMessage() {
        return mBackPlayerMsg;
    }

    public boolean hasJoinPlayerMessage(){
        return mJoinPlayerMsg != null;
    }

    public boolean hasJoinBroadcastMessage(){
        return mJoinBroadcastMsg != null;
    }

    public boolean hasClearPlayerMessage(){
        return mClearPlayerMsg != null;
    }

    public boolean hasClearBroadcastMessage(){
        return mClearBroadcastMsg != null;
    }

    public boolean hasClearTitleText(){
        return mClearTitleText != null;
    }

    public boolean hasBackPlayerMessage(){
        return mBackPlayerMsg != null;
    }

    public boolean hasJoinLocation(){
        return mJoinLocation != null;
    }

    public boolean hasClearLocation(){
        return mClearLocation != null;
    }

    public boolean hasBackLocation(){
        return mBackLocation != null;
    }
}
