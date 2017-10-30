package jp.kentan.minecraft.neko_event.util;

import org.bukkit.ChatColor;
import org.bukkit.Server;

import java.util.logging.Logger;

public class Log {

    private final static String PREFIX_INFO = ChatColor.translateAlternateColorCodes('&', "&7[NekoEvent &fINFO&7]&r ");
    private final static String PREFIX_WARN = ChatColor.translateAlternateColorCodes('&', "&7[NekoEvent &eWARN&7]&r ");
    private final static String PREFIX_ERROR = ChatColor.translateAlternateColorCodes('&', "&7[NekoEvent &4ERROR&7]&r ");

    private static Server sServer;
    private static Logger sLogger;

    public static void set(Server server, Logger logger){
        sServer = server;
        sLogger = logger;
    }

    public static void info(final String str){
        sLogger.info(str);
        sendMessageToOp(PREFIX_INFO + str);
    }

    public static void warn(final String str){
        sLogger.warning(str);
        sendMessageToOp(PREFIX_WARN + str);
    }

    public static void error(final String str){
        sLogger.warning(str);
        sendMessageToOp(PREFIX_ERROR + str);
    }

    private static void sendMessageToOp(final String message){
        sServer.getOnlinePlayers().forEach(p -> {
            if(p.isOp()){
                p.sendMessage(message);
            }
        });
    }
}
