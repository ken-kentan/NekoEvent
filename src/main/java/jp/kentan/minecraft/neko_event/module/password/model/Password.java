package jp.kentan.minecraft.neko_event.module.password.model;

import jp.kentan.minecraft.neko_event.config.provider.PasswordConfigProvider;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

public class Password {
    private final String ID;

    private String mPassword;
    private StringBuilder mInputBuffer = new StringBuilder();

    private Material mBlockMaterial;
    private Location mBlockLocation;

    private String mMatchMessage, mNotMatchMessage, mInputMessage, mClearMessage;

    public Password(String id, String defaultPass,
                    Material blockMaterial, Location blockLocation,
                    String matchMsg, String notMatchMsg, String inputMsg, String clearMsg){

        ID = id;
        mPassword = (defaultPass != null) ? defaultPass : "";

        mBlockMaterial = blockMaterial;
        mBlockLocation = blockLocation;

        mMatchMessage = formatMessage(matchMsg);
        mNotMatchMessage = formatMessage(notMatchMsg);
        mInputMessage = formatMessage(inputMsg);
        mClearMessage = formatMessage(clearMsg);
    }

    public boolean update(String defaultPass,
                          Material blockMaterial, Location blockLocation,
                          String matchMsg, String notMatchMsg, String inputMessage, String clearMsg){

        if(!PasswordConfigProvider.update(ID, defaultPass, blockMaterial, blockLocation,
                matchMsg, notMatchMsg, inputMessage, clearMsg)){
            return false;
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

        if(inputMessage != null){
            mInputMessage = formatMessage(inputMessage);
        }

        if(clearMsg != null){
            mClearMessage = formatMessage(clearMsg);
        }

        return true;
    }

    public PasswordResult input(String text){
        if(mPassword.length() <= 0){
            mInputBuffer.setLength(0);
            return PasswordResult.NOT_MATCH;
        }

        if(mInputBuffer.length() >= mPassword.length()){
            mInputBuffer.setLength(0);
        }

        mInputBuffer.append(text);

        if(mInputBuffer.length() < mPassword.length()){
            return PasswordResult.CONTINUE;
        }

        return mPassword.equals(mInputBuffer.toString()) ? PasswordResult.MATCH : PasswordResult.NOT_MATCH;
    }

    public void clearBuffer(){
        mInputBuffer.setLength(0);
    }

    public void updatePassword(String newPassword){
        mPassword = newPassword;
    }

    public String getPassword() {
        return mPassword;
    }

    public String getInputBuffer() {
        return mInputBuffer.toString();
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

    public String getInputMessage() {
        return mInputMessage.replace("{buff}", mInputBuffer.toString());
    }

    public String getClearMessage() {
        return mClearMessage;
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

    public boolean hasInputMessage(){
        return mInputMessage != null;
    }

    public boolean hasClearMessage(){
        return mClearMessage != null;
    }


    private static String formatMessage(String message){
        if(message == null){
            return null;
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
