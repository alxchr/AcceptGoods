package ru.abch.acceptgoods2;

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
import java.util.Arrays;

public class Database {
    private static  String TAG = "Database";
    private static final String DB_NAME = "goodsdb";
    private static final int DB_VERSION = 3;
    private static final String DB_TABLE_MOVEGOODS = "movegoods";
    private static final String DB_TABLE_MOVEGOODS_DATA = "movegoodsdata";
    private static final String DB_TABLE_BARCODES = "barcodes";
    private static final String DB_TABLE_GOODS = "goods";
    private static final String DB_TABLE_ADDGOODS = "add_goods";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_WAREHOUSE_CODE = "wh_code";
    private static final String COLUMN_STOREMAN = "storeman";
    private static final String COLUMN_IN1C = "in1c";
    private static final String COLUMN_MOVEGOODS_ID = "movegoods_id";
    private static final String COLUMN_GOODS_CODE = "goods_code";
    private static final String COLUMN_INPUT_CELL = "input_cell";
    private static final String COLUMN_OUTPUT_CELL  = "output_cell";
    private static final String COLUMN_QNT  = "qnt";
    private static final String COLUMN_MOVEGOODS_SCAN_TIME = "scan_time";
    private static final String COLUMN_BARCODE  = "barcode";
    private static final String COLUMN_GOODS_DESC = "goods_desc";
    private static final String COLUMN_GOODS_ARTICLE = "goods_article";

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
    private static final String DB_CREATE_BARCODES =
                    "create table " + DB_TABLE_BARCODES + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_GOODS_CODE + " text not null, " +
                    COLUMN_BARCODE + " text not null, " +
                    COLUMN_QNT + " integer " +
                    ");";
    private static final String DB_CREATE_GOODS =
            "create table " + DB_TABLE_GOODS + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_GOODS_CODE + " text not null, " +
                    COLUMN_GOODS_DESC + " text not null, " +
                    COLUMN_OUTPUT_CELL + " text, " +
                    COLUMN_GOODS_ARTICLE + " text, " +
                    COLUMN_QNT + " integer " +
                    ");";
    private static final String DB_CREATE_ADDGOODS =
            "create table " + DB_TABLE_ADDGOODS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_STOREMAN + " integer, " +
                    COLUMN_GOODS_CODE + " text not null, " +
                    COLUMN_BARCODE + " text not null, " +
                    COLUMN_QNT + " integer, " +
                    COLUMN_INPUT_CELL + " text, " +
                    COLUMN_MOVEGOODS_SCAN_TIME + " text " +
                    ");";
    private final Context mCtx;
    private DBHelper mDBHelper;
    private static SQLiteDatabase mDB;
//    private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static ConnectionClass connectionClass;
    Database(Context ctx) {
        mCtx = ctx;
    }

    void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        try {
            mDB = mDBHelper.getWritableDatabase();
            connectionClass = new ConnectionClass();
        } catch (SQLException s) {
            new Exception("Error with DB Open");
        }
    }
    void close() {
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
            db.execSQL(DB_CREATE_BARCODES);
            db.execSQL(DB_CREATE_GOODS);
            db.execSQL(DB_CREATE_ADDGOODS);
            Log.d(TAG, "onCreate");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrade DB from " + oldVersion + " to " + newVersion);
            String dropAddGoods = "drop table if exists " + DB_TABLE_ADDGOODS;
            String dropMoveGoods = "drop table if exists " + DB_TABLE_MOVEGOODS;
            String dropMoveGoodsData = "drop table if exists " + DB_TABLE_MOVEGOODS_DATA;
            String dropGoods = "drop table if exists " + DB_TABLE_GOODS;
            String dropBarcodes = "drop table if exists " + DB_TABLE_BARCODES;
            if (oldVersion == 1 && newVersion == 2) db.execSQL(DB_CREATE_BARCODES);
            if (newVersion > 2) {
                db.execSQL(dropAddGoods);
                db.execSQL(dropMoveGoods);
                db.execSQL(dropMoveGoodsData);
                db.execSQL(dropGoods);
                db.execSQL(dropBarcodes);
                db.execSQL(DB_CREATE_MOVEGOODS);
                db.execSQL(DB_CREATE_MOVEGOODS_DATA);
                db.execSQL(DB_CREATE_BARCODES);
                db.execSQL(DB_CREATE_GOODS);
                db.execSQL(DB_CREATE_ADDGOODS);
            }
        }
    }
    /*
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

     */
    static void clearData() {
        mDB.delete(DB_TABLE_MOVEGOODS_DATA,null,null);
//        mDB.delete(DB_TABLE_ADDGOODS,null,null);
        mDB.delete(DB_TABLE_BARCODES, null, null);
        mDB.delete(DB_TABLE_GOODS, null, null);
        FL.d(TAG,"Clear tables");
    }

    private static long addBarCode(String goodsCode, String barCode, int qnt) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GOODS_CODE, goodsCode);
        cv.put(COLUMN_BARCODE, barCode);
        cv.put(COLUMN_QNT, qnt);
        try {
            ret = mDB.insert(DB_TABLE_BARCODES, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, Arrays.toString(ex.getStackTrace()));
        }
        return ret;
    }
    static void getBarCodes(String storeID) {
        Connection con = connectionClass.CONN();;
        String SQL = "exec spr.dbo.dctGetAcceptGoodsData 2,'" + storeID + "';";
        Log.d(TAG, "Get bar codes for "+ storeID);
        try {
            Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            boolean result = stmt.execute(SQL);
            if (result) {
                ResultSet rs = stmt.getResultSet();
                rs.beforeFirst();
                mDB.beginTransaction();
                while (rs.next()) {
                    String goodsCode = rs.getString(1);
                    String barCode = rs.getString(2);
                    int qnt = rs.getInt(3);
                    Log.d(TAG, "Goods code =" + goodsCode + " Barcode = " + barCode + " Qnt = " + qnt);
                    addBarCode(goodsCode, barCode, qnt);
                }
                mDB.setTransactionSuccessful();
                mDB.endTransaction();
                rs.close();
            } else Log.d(TAG, "No result");
            con.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }
    private static long addGoods(String goodsCode, String desc, String cell, String article, int total) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GOODS_CODE, goodsCode);
        cv.put(COLUMN_GOODS_DESC, desc);
        cv.put(COLUMN_OUTPUT_CELL, cell);
        cv.put(COLUMN_GOODS_ARTICLE, article);
        cv.put(COLUMN_QNT, total);
        try {
            ret = mDB.insert(DB_TABLE_GOODS, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, Arrays.toString(ex.getStackTrace()));
        }
        return ret;
    }
    static void getGoods(String storeID) {
        Connection con = connectionClass.CONN();;
        String SQL = "exec spr.dbo.dctGetAcceptGoodsData 1,'" + storeID + "';";
        Log.d(TAG, "Get goods for "+ storeID);
        try {
            Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            boolean result = stmt.execute(SQL);
            if (result) {
                ResultSet rs = stmt.getResultSet();
                rs.beforeFirst();
                mDB.beginTransaction();
                while (rs.next()) {
                    String goodsCode = rs.getString(1);
                    String desc = rs.getString(3);
                    String cell = rs.getString(7);
                    String article = rs.getString(4);
                    int total = rs.getInt(2);
                    Log.d(TAG, "Goods code =" + goodsCode + " desc = " + desc + " cell = " + cell + " article = " + article + " total = " + total);
                    addGoods(goodsCode, desc, cell, article, total);
                }
                mDB.setTransactionSuccessful();
                mDB.endTransaction();
                rs.close();
            } else Log.d(TAG, "No result");
            con.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }
    static GoodsPosition getGoodsPosition(String barcode) {
        GoodsPosition ret = null;
        String goodsCode;
        int qnt, total;
        String barcodeTable = DB_TABLE_BARCODES;
        String goodsTable = DB_TABLE_GOODS;
        String description, cell, article;
        Log.d(TAG, "Goods position barcode = " + barcode);
        /*
        Cursor c = mDB.query( barcodeTable, null,COLUMN_BARCODE + "=?", new String[]{barcode},
                null, null, null, null );

         */
//  Honeywell EDA50K trims EAN-13 last digit
        Cursor c = mDB.query( barcodeTable, null,COLUMN_BARCODE + " like ?", new String[]{barcode+"%"},
                null, null, null, null );
        if (c.moveToFirst()) {
            goodsCode = c.getString(1);
            qnt = c.getInt(3);
            Log.d(TAG, "Found goods position code = " + goodsCode + " qnt = " + qnt);
            Cursor cGoods = mDB.query( goodsTable, null,COLUMN_GOODS_CODE + "=?", new String[]{goodsCode},
                    null, null, null, null );
            if (cGoods.moveToFirst()) {
                description = cGoods.getString(2);
                cell = cGoods.getString(3);
                total = cGoods.getInt(5);
                article = cGoods.getString(4);
                ret = new GoodsPosition(goodsCode, barcode, description, cell, qnt, article, total);
                Log.d(TAG, "Found goods position desc = " + description + " cell = " + cell);
            }
        }
        return ret;
    }
    private static long insertAcceptGoods(String storage, int storeman, String goodsId, String barcode, int qnt, String cell, String time) {
        String insert = "insert into dbo.dctRcvAcceptGoods(storage,storeman,goods,goodsbarcode,qnt,cellinbarcode,scandt) values ('";
        String query = insert + storage + "'," + storeman + ",'" + goodsId + "','" + barcode + "'," + qnt + ",'" + cell +"','" + time +"');";
        FL.d(TAG, "Query =" + query);
        long ret = 0;
        Connection con;
        String[] generatedColumns = new String[] { "rowid" };
        PreparedStatement stmtInsert;
        con = connectionClass.CONN();
        ResultSet rs;
        try {
            stmtInsert  = con.prepareStatement(query, generatedColumns);
            int affectedRows = stmtInsert.executeUpdate();
            if (affectedRows == 0) {
                FL.d(TAG, "Row not inserted");
            }
            rs = stmtInsert.getGeneratedKeys();
            if (rs.next()) {
                ret = rs.getLong(1);
                Log.d(TAG,"Inserted ID = " + ret);
            }
            stmtInsert.close();
            rs.close();
            con.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }
    static long addAcceptGoods(int storeman, String goodsId, String barcode, int qnt, String cell, String datetime) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STOREMAN,storeman);
        cv.put(COLUMN_GOODS_CODE,goodsId);
        cv.put(COLUMN_BARCODE, barcode);
        cv.put(COLUMN_QNT, qnt);
        cv.put(COLUMN_INPUT_CELL, cell);
        cv.put(COLUMN_MOVEGOODS_SCAN_TIME, datetime);
        try {
            ret = mDB.insert(DB_TABLE_ADDGOODS, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, Arrays.toString(ex.getStackTrace()));
        }
        return ret;
    }
    /*
    public static long addAcceptGoods(int storeman, GoodsPosition gp) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STOREMAN,storeman);
        cv.put(COLUMN_GOODS_CODE,gp.getId());
        cv.put(COLUMN_BARCODE, gp.getBarcode());
        cv.put(COLUMN_QNT, gp.getQnt());
        cv.put(COLUMN_INPUT_CELL, gp.getCell());
        cv.put(COLUMN_MOVEGOODS_SCAN_TIME, gp.getTime());
        try {
            ret = mDB.insert(DB_TABLE_ADDGOODS, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, Arrays.toString(ex.getStackTrace()));
        }
        return ret;
    }

     */
    static void uploadGoods(){
        Cursor c = mDB.query(DB_TABLE_ADDGOODS, null, null, null, null, null, null);
        int n = c.getCount();
        if (n > 0) {
            while (c.moveToNext()) {
                insertAcceptGoods(App.getStoreId(), c.getInt(1), c.getString(2), c.getString(3), c.getInt(4), c.getString(5), c.getString(6));
            }
            Log.d(TAG, "Upload " + n + " rows from localDB to server");
        }
        c.close();
        mDB.delete(DB_TABLE_ADDGOODS, null, null);
    }
    static GoodsPosition[] searchGoods(String searchPattern) {
        GoodsPosition[] ret = null;
        int qnt, total;
        String goodsCode, description, cell, barcode, article;
        Cursor c = mDB.query( true, DB_TABLE_GOODS, new String[] {COLUMN_GOODS_CODE, COLUMN_GOODS_DESC, COLUMN_OUTPUT_CELL, COLUMN_GOODS_ARTICLE, COLUMN_QNT},
                COLUMN_GOODS_DESC + " like ? or " + COLUMN_GOODS_ARTICLE + " like ? COLLATE NOCASE",
                new String[] {"%" + searchPattern + "%","%" + searchPattern + "%"},null, null, null, null);
        int count = c.getCount();
        Log.d(TAG, "Found " + count + " rows for " + searchPattern);
        if (count > 0) {
            ret = new GoodsPosition[count];
            c.moveToFirst();
            for (int i = 0; i < count; i++) {
                goodsCode = c.getString(0);
                description = c.getString(1);
                Cursor c1 = mDB.query(true, DB_TABLE_BARCODES,null,COLUMN_GOODS_CODE+" =?", new String[] {goodsCode},
                        null, null, null,"1");
                Log.d(TAG, "Barcode count = " + c1.getCount());
                barcode = "";
                qnt = 0;
                while (c1.moveToNext()) {
                    String id = c1.getString(1);
                    barcode = c1.getString(2);
                    qnt = c1.getInt(3);
//                    Log.d(TAG, "Id =" + id + " barcode =" + barcode + " qnt=" + qnt);
                }
                cell = c.getString(2);
                article = c.getString(3);
                total = c.getInt(4);
                GoodsPosition gp = new GoodsPosition(goodsCode,barcode, description, cell, qnt, article, total);
                ret[i] = gp;
                c.moveToNext();
                c1.close();
            }
            c.close();
        }
        return ret;
    }
    static boolean insertGoodsPosition(String storage, int storeman, GoodsPosition gp) {
        boolean ret = false;
        String insert = "insert into dbo.dctRcvAcceptGoods(storage,storeman,goods,goodsbarcode,qnt,cellinbarcode,scandt) values ('";
        String query = insert + storage + "'," + storeman + ",'" + gp.getId() + "','" + gp.getBarcode() + "'," + gp.getQnt() + ",'" + gp.getCell() +"','" + gp.getTime() +"');";
        FL.d(TAG, "Query =" + query);
        Connection con;
        String[] generatedColumns = new String[] { "rowid" };
        PreparedStatement stmtInsert;
        con = connectionClass.CONN();
        ResultSet rs;
        long row;
        try {
            stmtInsert  = con.prepareStatement(query, generatedColumns);
            int affectedRows = stmtInsert.executeUpdate();
            if (affectedRows == 0) {
                FL.d(TAG, "Row not inserted");
            }
            rs = stmtInsert.getGeneratedKeys();
            if (rs.next()) {
                row = rs.getLong(1);
                Log.d(TAG,"Inserted ID = " + row);
                ret = true;
            }
            stmtInsert.close();
            rs.close();
            con.close();
        } catch (java.sql.SQLException e) {
            FL.d(TAG, "Row not inserted \n" + e.getSQLState() + " error = " + e.getErrorCode());
        }
        return ret;
    }
    /*
    public static boolean insertGoodsPosition(String storage, int storeman, GoodsPosition[] gpArray) {
        boolean ret = false;
        String insert = "insert into dbo.dctRcvAcceptGoods(storage,storeman,goods,goodsbarcode,qnt,cellinbarcode,scandt) values ('";
        String query;// = insert + storage + "'," + storeman + ",'" + gp.getId() + "','" + gp.getBarcode() + "'," + gp.getQnt() + ",'" + gp.getCell() +"','" + gp.getTime() +"');";
        Connection con;
        String[] generatedColumns = new String[] { "rowid" };
        PreparedStatement stmtInsert;
        con = connectionClass.CONN();
        ResultSet rs;
        long row;
        int len = gpArray.length;
        for (int i = 0; i < len; i++) {
            ret = false;
            query =  insert + storage + "'," + storeman + ",'" + gpArray[i].getId() + "','" + gpArray[i].getBarcode() + "'," + gpArray[i].getQnt() +
                    ",'" + gpArray[i].getCell() +"','" + gpArray[i].getTime() +"');";
            FL.d(TAG, "Query =" + query);
            try {
                stmtInsert = con.prepareStatement(query, generatedColumns);
                int affectedRows = stmtInsert.executeUpdate();
                if (affectedRows == 0) {
                    FL.d(TAG, "Row not inserted");
                }
                rs = stmtInsert.getGeneratedKeys();
                if (rs.next()) {
                    row = rs.getLong(1);
                    Log.d(TAG, "Inserted ID = " + row);
                    ret = true;
                } else
                    break;
                stmtInsert.close();
                rs.close();
            } catch (java.sql.SQLException e) {
                FL.d(TAG, "Row not inserted \n" + e.getSQLState() + " error = " + e.getErrorCode());
                break;
            }
        }
        try {
            con.close();
        } catch (java.sql.SQLException e) {
            FL.d(TAG, "Row not inserted \n" + e.getSQLState() + " error = " + e.getErrorCode());
        }
        return ret;
    }

     */
}
