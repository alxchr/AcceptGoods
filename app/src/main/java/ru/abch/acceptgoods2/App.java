package ru.abch.acceptgoods2;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bosphere.filelogger.FL;
import com.bosphere.filelogger.FLConfig;
import com.bosphere.filelogger.FLConst;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class App extends Application {
    static App instance = null;
    static String TAG = "App";
    static SharedPreferences sp;
    private static int storeIndex, storeMan;
    private static String storeId, storeName;
    private static String storeIndexKey = "store_index", storeIdKey = "store_id", storeNameKey = "store_name", storeManKey = "store_man";
    GregorianCalendar calendar;
    public static Database db;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        sp = getSharedPreferences();
        storeIndex = sp.getInt(storeIndexKey, -1);
        storeId = sp.getString(storeIdKey,"");
        storeName = sp.getString(storeNameKey,"");
        storeMan = sp.getInt(storeManKey, 0);
        Log.d(TAG,"Start application store id =" + storeId + " store index =" + storeIndex);
        FL.init(new FLConfig.Builder(this)
                .minLevel(FLConst.Level.V)
                .logToFile(true)
                .dir(new File(Environment.getExternalStorageDirectory(), "AcceptGoods2"))
                .retentionPolicy(FLConst.RetentionPolicy.FILE_COUNT)
                .build());
        FL.setEnabled(true);
        calendar = new GregorianCalendar(2000, Calendar.JANUARY,1);
        Config.timeShift = calendar.getTime().getTime();
        db = new Database(this);
        db.open();
    }
    public static SharedPreferences getSharedPreferences() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.get());
        return sharedPrefs;
    }
    public static App get() {
        return instance;
    }
    public static int getStoreIndex() {return storeIndex;};
    public static String getStoreId() {
        Log.d(TAG, "Get store id =" + storeId);
        return storeId;
    }
    public static String getStoreName(){return storeName;}
    public static int getStoreMan() { return storeMan;}
    public static void setStoreIndex(int i) {
        storeIndex = i;
        sp.edit().putInt(storeIndexKey, i).apply();
    }
    public static void setStoreId(String id) {
        storeId = id;
        Log.d(TAG, "Set store id =" + id);
        sp.edit().putString(storeIdKey, id).apply();
    }
    public static void setStoreName(String name) {
        storeName = name;
        sp.edit().putString(storeNameKey, name).apply();
    }
    public static void setStoreMan(int sm) {
        storeMan = sm;
        sp.edit().putInt(storeManKey, sm).apply();
    }
}
