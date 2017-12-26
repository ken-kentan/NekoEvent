package jp.kentan.minecraft.neko_event.game.dungeon.model;

import jp.kentan.minecraft.neko_event.config.provider.DungeonConfigProvider;
import jp.kentan.minecraft.neko_event.util.Log;
import jp.kentan.minecraft.neko_event.util.NekoUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class Dungeon {
    private static BukkitScheduler sScheduler = Bukkit.getScheduler();

    private String mId, mName;
    private String mJoinPlayerMsg, mJoinBroadcastMsg;
    private String mClearPlayerMsg, mClearBroadcastMsg, mClearTitleText;
    private String mTimedOutBroadcastMsg;
    private Location mJoinLocation, mClearLocation;

    private Boolean mEnableClearSound;

    private int mRewardTicketAmount;
    private String mRewardGachaId;

    private BukkitTask mLockTask;
    private int mLockTimer = 0;

    public Dungeon(String id, String name, Location joinLocation, Location clearLocation, String joinPlayerMsg, String joinBroadcastMsg,
                   String clearPlayerMsg, String clearBroadcastMsg, String clearTitleText, Boolean enableClearSound, String timedOutBroadcastMsg, int rewardTicketAmount, String rewardGachaId){
        mId = id;
        mName = ChatColor.translateAlternateColorCodes('&', name);

        mJoinLocation = joinLocation;
        mClearLocation = clearLocation;

        mJoinPlayerMsg     = (joinPlayerMsg     != null) ? ChatColor.translateAlternateColorCodes('&', joinPlayerMsg.replace("{name}", name)) : null;
        mJoinBroadcastMsg  = (joinBroadcastMsg  != null) ? ChatColor.translateAlternateColorCodes('&', joinBroadcastMsg.replace("{name}", name)) : null;

        mClearPlayerMsg    = (clearPlayerMsg    != null) ? ChatColor.translateAlternateColorCodes('&', clearPlayerMsg.replace("{name}", name)) : null;
        mClearBroadcastMsg = (clearBroadcastMsg != null) ? ChatColor.translateAlternateColorCodes('&', clearBroadcastMsg.replace("{name}", name)) : null;
        mClearTitleText    = (clearTitleText    != null) ? ChatColor.translateAlternateColorCodes('&', clearTitleText.replace("{name}", name)) : null;

        mTimedOutBroadcastMsg = (timedOutBroadcastMsg != null) ? ChatColor.translateAlternateColorCodes('&', timedOutBroadcastMsg.replace("{name}", name)) : null;

        mEnableClearSound = enableClearSound;

        mRewardTicketAmount = rewardTicketAmount;

        mRewardGachaId = rewardGachaId;
    }

    public boolean update(Location joinLocation, Location clearLocation, String joinPlayerMsg, String joinBroadcastMsg, String clearPlayerMsg,
                           String clearBroadcastMsg, String clearTitleText, Boolean enableClearSound, String timedOutBroadcastMsg, int rewordTicketAmount, String rewordGachaId){

        if(!DungeonConfigProvider.update(mId, joinLocation, clearLocation, joinPlayerMsg, joinBroadcastMsg, clearPlayerMsg,
                clearBroadcastMsg, clearTitleText, enableClearSound, timedOutBroadcastMsg, rewordTicketAmount, rewordGachaId)){
            return false;
        }

        if(joinLocation != null){
            mJoinLocation = joinLocation;
        }

        if(clearLocation != null){
            mClearLocation = clearLocation;
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

        if(timedOutBroadcastMsg != null){
            mTimedOutBroadcastMsg = ChatColor.translateAlternateColorCodes('&', timedOutBroadcastMsg.replace("{name}", mName));
        }

        if(enableClearSound != null){
            mEnableClearSound = enableClearSound;
        }

        if(rewordTicketAmount >= 0){
            mRewardTicketAmount = rewordTicketAmount;
        }

        if(rewordGachaId != null){
            mRewardGachaId = rewordGachaId;
        }

        return true;
    }

    public void startLockTimer(Plugin plugin, int sec){
        mLockTimer = sec;

        if(mLockTask != null){
            mLockTask.cancel();
        }

        mLockTask = sScheduler.runTaskTimerAsynchronously(plugin, () -> {
            if(--mLockTimer <= 0){
                mLockTask.cancel();
                mLockTask = null;

                if(mTimedOutBroadcastMsg != null) {
                    NekoUtil.broadcastMessage(mTimedOutBroadcastMsg, null);
                }

                Log.info("ダンジョン({id})のﾛｯｸﾀｲﾏｰがﾀｲﾑｱｳﾄしました.".replace("{id}", mId));
            }
        }, 20L, 20L);

        Log.info("ダンジョン({id})にﾛｯｸﾀｲﾏｰ({sec}s)を設定しました.".replace("{id}", mId).replace("{sec}", Integer.toString(sec)));
    }

    public void resetLockTimer(){
        if(mLockTask != null){
            mLockTask.cancel();
            mLockTask = null;

            Log.info("ダンジョン({id})のﾛｯｸﾀｲﾏｰを解除しました.".replace("{id}", mId));
        }

        mLockTimer = 0;
    }

    public boolean isLock() {
        return mLockTimer > 0;
    }

    public int getLockTimer(){
        return mLockTimer;
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

    public int getRewardTicketAmount(){
        return mRewardTicketAmount;
    }

    public String getRewordGachaId() {
        return mRewardGachaId;
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

    public boolean hasRewordTicket(){
        return mRewardTicketAmount > 0;
    }

    public boolean hasRewordGacha(){
        return mRewardGachaId != null;
    }

    public boolean hasJoinLocation(){
        return mJoinLocation != null;
    }

    public boolean hasClearLocation(){
        return mClearLocation != null;
    }

    public boolean enableClearSound(){
        return mEnableClearSound != null && mEnableClearSound;
    }
}
