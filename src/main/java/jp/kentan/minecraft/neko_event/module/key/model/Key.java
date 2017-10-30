package jp.kentan.minecraft.neko_event.module.key.model;

import jp.kentan.minecraft.neko_event.config.provider.KeyConfigProvider;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Key {
    private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final String ID;
    private ItemStack mItemStack;
    private String mItemName;

    //Option
    private boolean mIsTake;
    private int mPeriodMinutes;
    private Material mBlockMaterial;
    private Location mBlockLocation;
    private String mMatchMessage, mNotMatchMessage, mExpiredMessage, mShortAmountMessage;

    public Key(String id, ItemStack itemStack, boolean isTake, int periodMinutes, Material blockMaterial, Location blockLocation,
               String matchMsg, String notMatchMsg, String expiredMsg, String shortAmountMsg){
        ID = id;
        mItemStack = itemStack;


        ItemMeta meta = itemStack.getItemMeta();
        if(meta != null && meta.hasDisplayName()){
            mItemName = meta.getDisplayName();
        }else{
            mItemName = itemStack.getType().toString();
        }

        mIsTake = isTake;
        mPeriodMinutes = periodMinutes;
        mBlockMaterial = blockMaterial;
        mBlockLocation = blockLocation;

        mMatchMessage     = formatMessage(matchMsg);
        mNotMatchMessage  = formatMessage(notMatchMsg);
        mExpiredMessage     = formatMessage(expiredMsg);
        mShortAmountMessage = formatMessage(shortAmountMsg);
    }

    public boolean update(ItemStack itemStack, Boolean isTake, int periodMinutes, Material blockMaterial, Location blockLocation,
                          String matchMsg, String notMatchMsg, String expiredMsg, String shortAmountMsg){

        if(!KeyConfigProvider.update(ID, itemStack, isTake, periodMinutes, blockMaterial, blockLocation,
                matchMsg, notMatchMsg, expiredMsg, shortAmountMsg)){
            return false;
        }

        if(itemStack != null){
            mItemStack = itemStack;

            ItemMeta meta = itemStack.getItemMeta();
            if(meta != null && meta.hasDisplayName()){
                mItemName = meta.getDisplayName();
            }else{
                mItemName = itemStack.getType().toString();
            }

            mMatchMessage     = formatMessage(mMatchMessage);
            mNotMatchMessage  = formatMessage(mNotMatchMessage);
            mExpiredMessage     = formatMessage(mExpiredMessage);
            mShortAmountMessage = formatMessage(mShortAmountMessage);
        }

        if(isTake != null){
            mIsTake = isTake;
        }

        if(periodMinutes > 0){
            mPeriodMinutes = periodMinutes;
        }

        if(blockMaterial != null){
            mBlockMaterial = blockMaterial;
        }

        if(blockLocation != null){
            mBlockLocation = blockLocation;
        }

        if(matchMsg != null){
            mMatchMessage = formatMessage(matchMsg);
        }

        if(notMatchMsg != null){
            mNotMatchMessage = formatMessage(notMatchMsg);
        }

        if(expiredMsg != null){
            mExpiredMessage = formatMessage(expiredMsg);
        }

        if(shortAmountMsg != null){
            mShortAmountMessage = formatMessage(shortAmountMsg);
        }

        return true;
    }

    public ItemStack getItemStack(int amount) {
        ItemStack itemStack = new ItemStack(mItemStack);

        if(mPeriodMinutes > 0){
            ItemMeta meta = itemStack.getItemMeta();

            List<String> lore = new ArrayList<>();

            if(meta.hasLore()){
                lore.addAll(meta.getLore());
            }
            lore.add("expired @ " + getPeriodTime(mPeriodMinutes));

            meta.setLore(lore);

            itemStack.setItemMeta(meta);
        }

        itemStack.setAmount(amount);

        return itemStack;
    }

    public int getAmount(){
        return mItemStack.getAmount();
    }

    public String getName(){
        return mItemName;
    }

    public Material getBlockMaterial() {
        return mBlockMaterial;
    }

    public Location getBlockLocation() {
        return mBlockLocation;
    }

    public String getMatchMessage() {
        return mMatchMessage;
    }

    public String getNotMatchMessage() {
        return mNotMatchMessage;
    }

    public String getExpiredMessage() {
        return mExpiredMessage;
    }

    public String getShortAmountMessage() {
        return mShortAmountMessage;
    }

    public boolean isTake() {
        return mIsTake;
    }

    public boolean hasBlockOption(){
        return (mBlockMaterial != null) && (mBlockLocation != null);
    }

    public boolean hasMatchMessage(){
        return mMatchMessage != null;
    }

    public boolean hasNotMatchMessage(){
        return mNotMatchMessage != null;
    }

    public boolean hasExpiredMessage(){
        return mExpiredMessage != null;
    }

    public boolean hasShortAmountMessage(){
        return mShortAmountMessage != null;
    }

    public KeyResult match(ItemStack itemStack){
        if(itemStack == null){
            return KeyResult.NOT_MATCH;
        }

        if(mPeriodMinutes > 0){
            if(!itemStack.hasItemMeta()){
                return KeyResult.NOT_MATCH;
            }

            itemStack = new ItemStack(itemStack);
            ItemMeta meta = itemStack.getItemMeta();

            if(!meta.hasLore()){
                return KeyResult.NOT_MATCH;
            }

            List<String> lore = meta.getLore();
            String strPeriod = lore.get(lore.size()-1);

            lore.remove(lore.size() - 1);
            meta.setLore(lore);

            itemStack.setItemMeta(meta);

            if(!mItemStack.isSimilar(itemStack) || strPeriod.length() < 26){
                return KeyResult.NOT_MATCH;
            }

            if(!withinPeriod(strPeriod.substring(10))){
                return KeyResult.EXPIRED;
            }
        }else if(!mItemStack.isSimilar(itemStack)){
            return KeyResult.NOT_MATCH;
        }

        return (itemStack.getAmount() >= mItemStack.getAmount()) ? KeyResult.MATCH : KeyResult.SHORT_AMOUNT;
    }

    private boolean withinPeriod(String strPeriod){
        ZonedDateTime period;

        try {
            period = ZonedDateTime.parse(strPeriod, DATE_FORMAT);
        } catch (Exception e){
            return false;
        }

        return period.compareTo(ZonedDateTime.now()) >= 0;
    }

    private String formatMessage(String message){
        if(message == null){
            return null;
        }

        return ChatColor.translateAlternateColorCodes('&', message.replace("{name}", mItemName));
    }

    private static String getPeriodTime(int offsetMinutes){
        return DATE_FORMAT.format(ZonedDateTime.now().plusMinutes(offsetMinutes));
    }
}
