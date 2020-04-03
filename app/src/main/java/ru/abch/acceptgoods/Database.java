package ru.abch.acceptgoods;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bosphere.filelogger.FL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Database {
    static  String TAG = "Database";
    private static final String DB_NAME = "goodsdb";
    private static final int DB_VERSION = 1;
    private static final String DB_TABLE_MOVEGOODS = "movegoods";
    private static final String DB_TABLE_MOVEGOODS_DATA = "movegoodsdata";

    static final String COLUMN_ID = "_id";
    static final String COLUMN_WAREHOUSE_CODE = "wh_code";
    static final String COLUMN_STOREMAN = "storeman";
    static final String COLUMN_IN1C = "in1c";
    static final String COLUMN_MOVEGOODS_ID = "movegoods_id";
    static final String COLUMN_GOODS_CODE = "goods_code";
    static final String COLUMN_INPUT_CELL = "input_cell";
    static final String COLUMN_OUTPUT_CELL  = "output_cell";
    static final String COLUMN_QNT  = "qnt";
    static final String COLUMN_MOVEGOODS_SCAN_TIME = "scan_time";

    private static final String DB_CREATE_MOVEGOODS =
            "create table " + DB_TABLE_MOVEGOODS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_WAREHOUSE_CODE + " text, " +
                    COLUMN_STOREMAN + " integer, " +
                    COLUMN_IN1C + " integer, " +
                    COLUMN_MOVEGOODS_SCAN_TIME + " integer" +
                    ");";
    private static final String DB_CREATE_MOVEGOODS_DATA =
                    "create table " + DB_TABLE_MOVEGOODS_DATA + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_MOVEGOODS_ID + " integer, " +
                    COLUMN_GOODS_CODE + " text not null, " +
                    COLUMN_INPUT_CELL + " text, " +
                    COLUMN_OUTPUT_CELL + " text, " +
                    COLUMN_QNT + " integer " +
                    ");";

    private final Context mCtx;
    private DBHelper mDBHelper;
    private static SQLiteDatabase mDB;
    private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static ConnectionClass connectionClass;
    public Database(Context ctx) {
        mCtx = ctx;
    }

    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        try {
            mDB = mDBHelper.getWritableDatabase();
            connectionClass = new ConnectionClass();
        } catch (SQLException s) {
            new Exception("Error with DB Open");
        }
    }
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }


    public static void beginTransaction() {
        mDB.beginTransaction();
    }
    public static void endTransaction() {
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_MOVEGOODS);
            db.execSQL(DB_CREATE_MOVEGOODS_DATA);
            Log.d(TAG, "onCreate");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrade DB from " + oldVersion + " to " + newVersion);

        }
    }
    public static long addMoveGoods(String warehouse, int storeman) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IN1C,0);
        cv.put(COLUMN_STOREMAN,storeman);
        cv.put(COLUMN_WAREHOUSE_CODE,warehouse);
        cv.put(COLUMN_MOVEGOODS_SCAN_TIME,System.currentTimeMillis());
        try {
            ret = mDB.insert(DB_TABLE_MOVEGOODS, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, ex.getStackTrace().toString());
        }
        return ret;
    }
    public static long addMoveGoodsData(long id, String goodsCode, String inputCell, String outputCell, int qnt) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MOVEGOODS_ID,id);
        cv.put(COLUMN_INPUT_CELL,inputCell);
        cv.put(COLUMN_OUTPUT_CELL,outputCell);
        cv.put(COLUMN_QNT,qnt);
        cv.put(COLUMN_GOODS_CODE,goodsCode);
        try {
            ret = mDB.insert(DB_TABLE_MOVEGOODS_DATA, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, ex.getStackTrace().toString());
        }
        return ret;
    }
    public static void clearData() {
        mDB.delete(DB_TABLE_MOVEGOODS_DATA,null,null);
        mDB.delete(DB_TABLE_MOVEGOODS,null,null);
        FL.d(TAG,"Clear tables");
    }
    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTime();
    }
    public static void transferMoveGoods() {
        FL.d(TAG, "Transfer move goods");
        Connection con;
        String generatedColumns[] = new String[] { "rowid" };
        PreparedStatement stmtInsert;
        Statement stmtInsertData;
        ResultSet rs1;
        String insertSQL1 = "insert into dbo.dctRcvMoveGoods(storage,storeman,in1c) values ('";
        String insertSQL2 = "insert into dbo.dctRcvMoveGoodsData(rowid,shipbarcode,celloutbarcode,cellinbarcode,qnt) values (";
        String query;
        long id = 0;
        Cursor c = mDB.query(DB_TABLE_MOVEGOODS,null,null,null,null,null,null);
        if (c.getCount() > 0) {
            Log.d(TAG, DB_TABLE_MOVEGOODS + " : cursor count = " + c.getCount());
            while (c.moveToNext()) {
                id = c.getLong(0);
                Log.d(TAG, "Id = " + id +" time = " + c.getLong(4));
                Cursor c1 = mDB.query(DB_TABLE_MOVEGOODS_DATA, null, COLUMN_MOVEGOODS_ID + "=?", new String[] {String.valueOf(id),},
                        null, null, null);
                if (c1.getCount() > 0) {
                    con = connectionClass.CONN();

                    Log.d(TAG, DB_TABLE_MOVEGOODS_DATA + " : cursor count = " + c1.getCount() + " movegoods id = " + id);
                    if (con == null) {
                        FL.e(TAG, "SQL server not connected");
                    } else
                    try {
//                        con.setAutoCommit(false);
                        query = insertSQL1 + c.getString(1) + "','" + c.getString(2) + "',0);";
                        FL.d(TAG, "Query = " + query);
                        stmtInsert  = con.prepareStatement(query, generatedColumns);
                        int affectedRows = stmtInsert.executeUpdate();

                        if (affectedRows == 0) {
                            throw new SQLException("no rows affected.");
                        }
                        rs1 = stmtInsert.getGeneratedKeys();
                        if (rs1.next()) {
                            id = rs1.getLong(1);
                            Log.d(TAG,"Inserted ID = " + id);
                        }
                        stmtInsert.close();
                        rs1.close();
                    } catch (java.sql.SQLException e) {
                        e.printStackTrace();
                    }
                    if (con == null) {
                        FL.e(TAG, "SQL server not connected");
                    } else
                    if (id > 0) {
                        while (c1.moveToNext()) {
                            try {
                                query = insertSQL2 + id +",'" + c1.getString(2) + "','" + c1.getString(3) + "','" + c1.getString(4) + "'," + c1.getInt(5) + ");";
                                FL.d(TAG, "Query = " + query);
//                                stmtInsertData = con.prepareStatement(query);
                                stmtInsertData = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                                stmtInsertData.executeUpdate(query);
                                stmtInsertData.close();
                            } catch (java.sql.SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    try {
//                        con.commit();
                        con.close();
                    } catch (java.sql.SQLException e) {
                        e.printStackTrace();
                    }


                } else {
                    Log.d(TAG, DB_TABLE_MOVEGOODS_DATA +" : empty cursor");
                }
                c1.close();
            }
        } else {
            Log.d(TAG, DB_TABLE_MOVEGOODS +" : empty cursor");
        }
        c.close();
        clearData();
    }
    public static long findGoods(long moveGoodsId, String goods) {
        long ret = 0;
        Cursor c = mDB.query(DB_TABLE_MOVEGOODS_DATA,null,COLUMN_MOVEGOODS_ID + "=? and " + COLUMN_GOODS_CODE + "=?",
                new String[] {String.valueOf(moveGoodsId),goods},null,null,null);
        if (c.moveToNext()) ret = c.getLong(0);
        return ret;
    }
    public static long updateGoodsData(long row, long id, String goodsCode, String inputCell, String outputCell, int qnt) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MOVEGOODS_ID,id);
        cv.put(COLUMN_INPUT_CELL,inputCell);
        cv.put(COLUMN_OUTPUT_CELL,outputCell);
        cv.put(COLUMN_QNT,qnt);
        cv.put(COLUMN_GOODS_CODE,goodsCode);
        Log.d(TAG,"Update row "+ row + " id " + id + " goods " + goodsCode + " box " + inputCell + " cell " + outputCell + " qnt " + qnt);
        return mDB.update(DB_TABLE_MOVEGOODS_DATA, cv, COLUMN_ID + "=?", new String[]{String.valueOf(row)});
    }
    public static int getDataCount() {
        int ret;
        Cursor c1 = mDB.query(DB_TABLE_MOVEGOODS_DATA, null, null, null,
                null, null, null);
        ret = c1.getCount();
        c1.close();
        return ret;
    }
}
