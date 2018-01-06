package jp.kentan.minecraft.neko_event.util;

import jp.kentan.minecraft.neko_event.NekoEvent;
import org.bukkit.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.*;

public class NekoUtil {

    final public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

    private static Server SERVER = Bukkit.getServer();

    @SuppressWarnings("deprecation")
    public static Player toPlayer(String name){
        final Player player = SERVER.getPlayer(name);

        if(player == null){
            Log.error("プレイヤー(%name%)が見つかりませんでした.".replace("%name%", name));
        }

        return player;
    }

    public static int toInteger(String s){
        int number = 0;
        try {
            number = Integer.parseInt(s);
        } catch (Exception e){
            Log.error(s + "を整数型に変換できませんでした.");
            Log.error(s + "を0に強制的に変換して実行します.");
        }

        return number;
    }

    private static float toFloat(String s){
        float number = 0f;
        try {
            number = Float.parseFloat(s);
        } catch (Exception e){
            Log.error(s + "をFloat型に変換できませんでした.");
            Log.error(s + "を0に強制的に変換して実行します.");
        }

        return number;
    }

    public static double toDouble(String s){
        double number = 0D;
        try {
            number = Double.parseDouble(s);
        } catch (Exception e){
            Log.error(s + "をDouble型に変換できませんでした.");
            Log.error(s + "を0に強制的に変換して実行します.");
        }

        return number;
    }

    public static boolean checkParams(int length, int threshold) {
        if(length < threshold){
            Log.error("ﾊﾟﾗﾒｰﾀが不足しています.");
            return false;
        }

        return true;
    }

    public static boolean checkParams(int length, int threshold, String key) {
        if(length < threshold){
            Log.error(key + "のﾊﾟﾗﾒｰﾀが不足しています.");
            return false;
        }

        return true;
    }

    public static boolean checkCommandBlock(CommandSender sender){
        if(sender instanceof BlockCommandSender){
            return true;
        }

        sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "このコマンドはコマンドブロックから実行して下さい.");
        return false;
    }

    public static void broadcastMessage(String message, Player without){
        if(without == null){
            SERVER.broadcastMessage(message);
        }else{
            SERVER.getOnlinePlayers().forEach(p -> {
                if(!p.equals(without)){
                    p.sendMessage(message);
                }
            });
        }
    }

    public static String toString(Location location){
        return "(" + location.getWorld().getName() +
                ", XYZ:" +
                (int)location.getX() +
                "/" +
                (int)location.getY() +
                "/" +
                (int)location.getZ() +
                ")";
    }

    public static Location toLocation(Location location, String[] strLocation){
        double[] addVector3 = {0D, 0D, 0D};

        for(int i = 0; i < strLocation.length; ++i){
            if(strLocation[i].startsWith("~")){
                String str = strLocation[i].substring(1);
                if(str.length() > 0) {
                    addVector3[i] = NekoUtil.toDouble(str);
                }
            }else{
                double val = NekoUtil.toDouble(strLocation[i]);
                switch (i){
                    case 0:
                        location.setX(val);
                        break;
                    case 1:
                        location.setY(val);
                        break;
                    case 2:
                        location.setZ(val);
                        break;
                    default:
                        break;
                }
            }
        }

        return location.add(addVector3[0], addVector3[1], addVector3[2]);
    }

    public static Location toLocation(String strWorld, String strX, String strY, String strZ){
        final World world = Bukkit.getWorld(strWorld);

        if(world == null){
            Log.error("ワールド({name})が見つかりません.".replace("{name}", strWorld));
            return null;
        }

        return new Location(world, toDouble(strX), toDouble(strY), toDouble(strZ));
    }

    public static String appendStrings(String[] array, int index){
        if(index <= 0){
            return appendStrings(Arrays.asList(array));
        }else{
            return appendStrings(Arrays.asList(array), index);
        }
    }

    public static String appendStrings(Collection<String> collection){
        if(collection == null){
            return null;
        }

        StringBuilder builder = new StringBuilder();

        collection.forEach(s -> {
            builder.append(' ');
            builder.append(s);
        });

        return builder.delete(0, 1).toString();
    }

    private static String appendStrings(Collection<String> collection, int index){
        if(collection == null){
            return null;
        }

        StringBuilder builder = new StringBuilder();

        collection.stream().skip(index).forEach(s -> {
            builder.append(' ');
            builder.append(s);
        });

        return builder.delete(0, 1).toString();
    }

    public static Map<String, List<String>> getOptionMap(String[] args){
        Map<String, List<String>> optionMap = new HashMap<>();

        for(int i = 0; i < args.length; ++i){

            if(args[i].equals("-o") && i+2 < args.length){
                final String key = args[i+1];
                final List<String> argList = new ArrayList<>();

                for(int pivot = i+2; pivot < args.length; ++pivot){
                    if(args[pivot].equals("-o")){
                        break;
                    }
                    argList.add(args[i = pivot]);
                }
                optionMap.put(key, argList);
            }
        }

        return optionMap;
    }

    public static boolean isPlayer(CommandSender sender){
        if(sender instanceof Player){
            return true;
        }

        sender.sendMessage(NekoEvent.PREFIX + ChatColor.YELLOW + "チャットから実行してください.");
        return false;
    }

    public static boolean isEmpty(Inventory inventory){
        for(ItemStack item : inventory.getContents()){
            if(item != null) return false;
        }
        return true;
    }
}
