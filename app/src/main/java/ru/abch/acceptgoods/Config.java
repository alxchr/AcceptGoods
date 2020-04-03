package ru.abch.acceptgoods;

import android.util.ArrayMap;
import android.util.Log;

public class Config {
    public static final String db = "RCall";
//    public static final String ip = "10.0.1.11";
    public static final String ip = "10.0.1.10";
//    public static final String ip = "10.2.0.31";
//    public static final String classs = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String classs = "net.sourceforge.jtds.jdbc.Driver";
//    public static final String un = "sa";
    public static final String un = "tsd";
    public static final String password = "Gfhjkm12";
    public static final String deviceKey = "device";
    public static final String warehouseIdKey = "warehouse_id";
    public static String deviceName;
    public static ArrayMap<String, String> warehousesMap;
    public static long timeShift;
    public static final boolean tts = true;
    public static long toComttTime(long t) {
        return (t - timeShift)/1000;
    }
    public static long toJavaTime(long t) {
        return t*1000 + timeShift;
    }
    public static final long weekInMillis = 7*24*3600*1000;
    public static String formatCell(String c) {
        String ret ="";
        int i = 0;
        if (Integer.parseInt(c.substring(2,6)) == 0) {
            String s = c.substring(6,12);
            while (s.substring(i, i+1).equals("0") && s.length() > 0)
                s = s.substring(++i);
            i = 0;
            while (!s.substring(i, i+1).equals("0") && s.length() > 0) {
                ret += s.substring(i, i + 1);
                i++;
            }
            if (s.substring(i+1, i+2).equals("0")) {
                ret += s.substring(i, i+1);
                i++;
            }
            ret +="-";
            ret += Integer.parseInt(s.substring(++i));
        } else {
            ret = Integer.parseInt(c.substring(4, 6)) + "-" + Integer.parseInt(c.substring(6, 9));
        }
        return ret;
    }
    public static int getQty(String goods) {
        return Integer.parseInt(goods.substring(2,4));
    }
    public static final int maxDataCount = 100;
}
