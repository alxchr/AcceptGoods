package ru.abch.acceptgoods2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bosphere.filelogger.FL;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    AlertDialog.Builder adbSettings, adbError, adbUpload;
    private static String TAG = "MainActivity";
    private static final int REQ_PERMISSION = 1233;
    private static TextToSpeech mTTS;
    TextView tvStore;
    static  EditText etStoreMan;
    String sStoreMan;
    static int storeMan;
    TextView  tvPrompt, tvBoxLabel, tvDescription, tvCell, tvGoods;
    EditText etScan, etQnt;
    private String input;
    private final int WAIT_QNT = 0, ERROR = 1, WAIT_GOODS_CODE = 2, WAIT_GOODS_BARCODE = 3, WAIT_CELL = 4;
    private int state = WAIT_GOODS_BARCODE;


    Button process;
    ConnectivityManager cm;
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    IntentFilter intentFilter;
    public static boolean online = false;
    private static boolean updateMoveGoodsData = false;
    final String[] ids = new String[] {"     2   ", "     JCTR", "    10SSR", "    12SPR", "    1ASPR", "    1BSPR", "    1ISPR", "    1LSPR",
    "    1OSPR", "    1PSPR", "    1CSPR", "    1SSPR", "    1USPR", "    15SPR", "    1TSPR"};
    int qnt;
    GoodsPosition gp;
    String cell;
    AlertDialog.Builder ad, adScan, adbGoods;
    final String[] storeCode = new String[] {"1908","1909","1907","1901","1900","1902","1906","1904","1903","1905","1900","1900","1900","1900","1900"};
    String[] names;
    static ProgressBar pbbar;
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkChangeReceiver, intentFilter);
        if(cm.getActiveNetworkInfo() != null) {
            FL.d(TAG,"Network " + cm.getActiveNetworkInfo().getExtraInfo() + " " + cm.getActiveNetworkInfo().getDetailedState());
        }
    }
    @Override
    public void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale locale = new Locale("ru");
            int result = mTTS.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS: language not supported");
            }
            if (result == TextToSpeech.SUCCESS) {
                Log.d(TAG, "TTS OK");
                say(getResources().getString(R.string.storeman_number_tts));
            }
        } else {
            Log.e(TAG, "TTS: error");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION) {
            if (grantResults.length > 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_one);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mTTS = new TextToSpeech(this, this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_PERMISSION);
        }
        names = getResources().getStringArray(R.array.store_names);
//        final String[] ids = getResources().getStringArray(R.array.store_ids);
        FL.d(TAG, "Store names length = " + names.length);
        adbSettings = new AlertDialog.Builder(this);
        if(App.getStoreIndex() < 0) {
            adbSettings.setTitle(R.string.store_choice)
                    .setItems(names, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            FL.d(TAG, "Index = " + which + " store id=" + ids[which]);
                            App.setStoreIndex(which);
                            App.setStoreId(ids[which]);
                            App.setStoreName(names[which]);
                        }

                    }).create().show();
        }
        pbbar = findViewById(R.id.pbbar);
        pbbar.setVisibility(View.GONE);
        tvStore = findViewById(R.id.tvStore);
        tvStore.setText(App.getStoreName());
        etStoreMan = findViewById(R.id.et_storeman);
        tvCell = findViewById(R.id.tvCell);
        etScan = findViewById(R.id.etScan);
        if(App.getStoreMan() > 0 ) {
            etStoreMan.setText(String.valueOf(App.getStoreMan()));
        }
        etStoreMan.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        (i == KeyEvent.KEYCODE_ENTER)){
                    sStoreMan = etStoreMan.getText().toString();
                    try {
                        storeMan = Integer.parseInt(sStoreMan);
                        FL.d(TAG, "Storeman = " + storeMan);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (storeMan > 0) {
                        App.setStoreMan(storeMan);
                        etStoreMan.setEnabled(false);
                        etScan.setEnabled(true);
                        etScan.requestFocus();
                        etScan.getText().clear();
                        tvPrompt.setText(getResources().getString(R.string.scan_goods));
                    }
                }
                return false;
            }
        });
        etStoreMan.requestFocus();
        etScan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "On text changed =" + charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input;
                int entIndex;
                if(editable.length() > 2) {
                    input = editable.toString();
//                    Log.d(TAG, "After text changed =" + editable.toString());
                    if (input.contains("\n") && input.indexOf("\n") == 0) {
                        input = input.substring(1);
//                        Log.d(TAG, "Enter char begins string =" + input);
                    }
                    if (input.contains("\n") && input.indexOf("\n") > 0) {
                        entIndex = input.indexOf("\n");
                        input = input.substring(0, entIndex);
//                        Log.d(TAG, "Enter at " + entIndex + " position of input =" + input);
                        if (input.length() > 2) {
                            etScan.setEnabled(false);
                            processScan(input);
                            etScan.setEnabled(true);
                        } else {
                            say(getResources().getString(R.string.enter_again));
                        }
                        etScan.setText("");
                    }
                }
            }
        });


        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.addDefaultNetworkActiveListener(new ConnectivityManager.OnNetworkActiveListener() {
            @Override
            public void onNetworkActive() {
                FL.d(TAG,"Network active");
            }
        });
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);

        tvBoxLabel = findViewById(R.id.tvBoxLabel);

        adbError = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        adbError.setMessage(R.string.error);
        adbError.setNegativeButton(R.string.dismiss,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                state = WAIT_GOODS_BARCODE;
                etQnt.setText("");
                etScan.setText("");
                tvCell.setText("");
                tvDescription.setText("");
                tvPrompt.setText(getResources().getString(R.string.scan_goods));
                tvGoods.setText("");
            }
        });
        adbError.setPositiveButton(R.string.enter_again,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                etQnt.setText("");
                etQnt.requestFocus();
            }
        });
        adbError.setCancelable(false);


        adbUpload = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        adbUpload.setMessage(R.string.force_upload);
        adbUpload.setPositiveButton(R.string.upload,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if (online) {
                    refreshData();
                } else {
                    say(getResources().getString(R.string.upload_deferred));
                }
            }
        });
        adbUpload.setCancelable(false);
        tvGoods = findViewById(R.id.tvGoods);
        etQnt = findViewById(R.id.etQty);
        tvPrompt = findViewById(R.id.tvPrompt);
        tvDescription = findViewById(R.id.tvDescription);
        refreshData();
        etQnt.setOnKeyListener(new View.OnKeyListener() {
            long row = 0;
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        (i == KeyEvent.KEYCODE_ENTER)){
                    int entIndex;
                    input = etQnt.getText().toString();
                    Log.d(TAG, "Input =" + input);
//                    etScan.getText().clear();
                    Log.d(TAG, "After cleaning text length =" + etQnt.getText().toString().length());
                    if (input.contains("\n")) {
                        entIndex = input.indexOf("\n");
                        Log.d(TAG, "Enter char index = " + entIndex);
                        if (entIndex == 0) {
                            input = input.substring(1);
                        } else input = input.substring(0, entIndex);
                    }
                    try {
                        qnt = Integer.parseInt(input);
                        FL.d(TAG, "qnt = " + qnt);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        qnt = -1;
                    }
                    FL.d(TAG, "Insert storage =" + App.getStoreId() +
                            " storeman=" + storeMan + " goods id =" + gp.id + " barcode =" + gp.barcode +
                            " qnt =" + qnt + " cell =" + cell + " time =" + getCurrentTime());
                    if (qnt > 0 && qnt < 10000) {
                        etQnt.setEnabled(false);
                        etScan.setEnabled(true);
                        etScan.requestFocus();
                        etScan.getText().clear();
                        tvPrompt.setText(getResources().getString(R.string.scan_goods));
                        if (online) {
//                            row = Database.insertAcceptGoods(App.getStoreId(), storeMan, gp.id, gp.barcode, qnt, cell, getCurrentTime());
//                            FL.d(TAG, "Row " + row + " sent to server");
                            gp.setQnt(qnt);
                            gp.setCell(cell);
                            gp.setTime(getCurrentTime());
                            uploadGoodsPosition(gp);
                        } else {
                            Database.addAcceptGoods(storeMan, gp.id, gp.barcode, qnt, cell, getCurrentTime());
                            FL.d(TAG, "Sent to local DB");
                        }
                        state = WAIT_GOODS_BARCODE;
                    } else {
                        etQnt.getText().clear();
                        say(getResources().getString(R.string.wrong_qnt));
                        FL.d(TAG,getResources().getString(R.string.wrong_qnt) + " = " + qnt);
                        etQnt.setText("");
                        etQnt.requestFocus();
                    }
                }
                return false;
            }
        });
        ad = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        ad.setMessage(R.string.exit);
        ad.setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                App.db.close();
                finish();
                System.exit(0);
            }
        });
        ad.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        ad.setCancelable(false);
        adScan = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        adScan.setMessage(R.string.close_scan);
        adScan.setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                state = WAIT_GOODS_BARCODE;
                etQnt.setText("");
                etScan.setText("");
                tvCell.setText("");
                tvDescription.setText("");
                tvPrompt.setText(getResources().getString(R.string.scan_goods));
                tvGoods.setText("");
                etScan.setEnabled(true);
                etScan.requestFocus();
                etQnt.setEnabled(false);
            }
        });
        adScan.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
//                etScan.requestFocus();
            }
        });
        adScan.setCancelable(false);
    }
    public static void say(String text) {
        if (Config.tts) mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, null);
    }
    private void processScan(String scan) {
        final String[]goodsDescriptions;
        Log.d(TAG, "Scanned " + scan);
        switch (state) {
            case WAIT_GOODS_BARCODE:
                gp = Database.getGoodsPosition(scan);
                if (gp == null) {
                    final GoodsPosition []searchResult = Database.searchGoods(scan);
                    if (searchResult != null && searchResult.length > 0){
                        if (searchResult.length == 1) {
                            gp = searchResult[0];
                            etQnt.setText(String.valueOf(gp.qnt));
                            tvGoods.setText(gp.article);
                            tvCell.setText(gp.cell);
                            tvDescription.setText(gp.description);
                            tvPrompt.setText(getResources().getString(R.string.scan_cell));
                            state = WAIT_CELL;
                        } else {
                            goodsDescriptions = new String[searchResult.length];
                            for (int j = 0; j < searchResult.length; j++) {
                                Log.d(TAG, "Goods code=" + searchResult[j].id + " desc=" + searchResult[j].description + " barcode=" + searchResult[j].barcode);
                                goodsDescriptions[j] = searchResult[j].description;
                            }
                            adbGoods = new AlertDialog.Builder(this);
                            adbGoods.setTitle(R.string.goods_choice).setItems(goodsDescriptions, new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    FL.d(TAG, "Index = " + which + "goods=" + goodsDescriptions[which]);
                                    gp = searchResult[which];
                                    etQnt.setText(String.valueOf(gp.qnt));
                                    tvGoods.setText(gp.article);
                                    tvCell.setText(gp.cell);
                                    tvDescription.setText(gp.description);
                                    tvPrompt.setText(getResources().getString(R.string.scan_cell));
                                    state = WAIT_CELL;
                                }
                            }).create().show();
                        }
                    } else {
                        say(getResources().getString(R.string.wrong_goods));
                    }
                } else {
                    etQnt.setText(String.valueOf(gp.qnt));
                    tvGoods.setText(gp.article);
                    tvCell.setText(gp.cell);
                    tvDescription.setText(gp.description);
                    tvPrompt.setText(getResources().getString(R.string.scan_cell));
                    state = WAIT_CELL;
                }
                break;
            case WAIT_CELL:
                cell = scan;

                if (CheckCode.checkCellStr(scan)){
                    int prefix, suffix;
                    String result;
                    prefix = Integer.parseInt(scan.substring(0, scan.indexOf(".")));
                    suffix = Integer.parseInt(scan.substring(scan.indexOf(".") + 1));
                    result = storeCode[App.getStoreIndex()] + String.format("%02d",prefix) + String.format("%03d",suffix) + "000";
                    int [] resDigit = new int[12];
                    for (int i = 0; i < 12; i++) {
                        resDigit[i] = Integer.parseInt(result.substring(i, i+1));
                    }
                    int e = (resDigit[1] + resDigit[3] + resDigit[5] +resDigit[7] + resDigit[9] + resDigit[11]) * 3;
                    int o = resDigit[0] + resDigit[2] + resDigit[4] +resDigit[6] + resDigit[8] + resDigit[10];
                    String r = String.valueOf(o+e);
                    int c = 10 - Integer.parseInt(r.substring(r.length() -1));
                    cell = result + c;
                    Log.d(TAG,"Manual input =" + scan + " cell =" + cell);
                }
                if (CheckCode.checkCell(cell)) {
                    etScan.setEnabled(false);
                    etQnt.setEnabled(true);
                    etQnt.requestFocus();
                    etQnt.setSelection(etQnt.getText().length());
                    tvCell.setText(Config.formatCell(cell));
                    tvPrompt.setText(getResources().getString(R.string.qnt));
                    state = WAIT_QNT;
                } else say(getResources().getString(R.string.wrong_cell));
                break;
            default:
                Log.d(TAG,"WTF switch");
                break;
        }
    }

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String phrase;
            if(cm.getActiveNetworkInfo() != null) {
                FL.d(TAG,"Network " + cm.getActiveNetworkInfo().getExtraInfo() + " " + cm.getActiveNetworkInfo().getDetailedState());
                phrase = "wifi подключен";
                online = true;
//                Database.uploadGoods();
                uploadGoods();
            } else {
                FL.d(TAG, "Network disconnected");
                phrase = "wifi отключен";
                online = false;
            }
            Toast.makeText(context, phrase, Toast.LENGTH_LONG).show();
        }
    };
    public static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Yekaterinburg"));
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Add your menu entries here
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public void onBackPressed() {
        if (state == WAIT_GOODS_BARCODE || state == WAIT_GOODS_CODE) {
            ad.show();
            FL.d(TAG, "Exit");
        }
        if (state == WAIT_CELL || state == WAIT_QNT
        ) {
            FL.d(TAG, "Finish scan");
            adScan.show();
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings_item:
                FL.d(TAG, "Settings clicked");
                adbSettings = new AlertDialog.Builder(this);
                adbSettings.setTitle(R.string.store_choice)
                            .setItems(names, new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    FL.d(TAG, "Index = " + which + " store id=" + ids[which]);
                                    App.setStoreIndex(which);
                                    App.setStoreId(ids[which]);
                                    App.setStoreName(names[which]);
                                }
                            }).create().show();
                return true;
            case R.id.refresh_item:
                FL.d(TAG, "Refresh clicked");
                refreshData();
                return true;
            default:
                break;
        }
        return false;
    }
    private void refreshData() {
//        Database.getBarCodes(App.getStoreId());
//        Database.getGoods(App.getStoreId());
        RefreshData rd = new RefreshData();
        rd.execute("");
    }
    public class RefreshData extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            Database.getBarCodes(App.getStoreId());
            Database.getGoods(App.getStoreId());
            return null;
        }
        @Override
        protected void onPreExecute() {
            FL.d(TAG, "Fetch inserted packs start");
            pbbar.setVisibility(View.VISIBLE);
        }
        @Override
        protected void onPostExecute(String r) {
            pbbar.setVisibility(View.GONE);
        }
    }
    private void uploadGoods() {
//        Database.getBarCodes(App.getStoreId());
//        Database.getGoods(App.getStoreId());
        UploadGoods ug = new UploadGoods();
        ug.execute("");
    }
    public static class UploadGoods extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            Database.uploadGoods();
            Database.clearData();
            return null;
        }
        @Override
        protected void onPreExecute() {
            FL.d(TAG, "Fetch inserted packs start");
            pbbar.setVisibility(View.VISIBLE);
        }
        @Override
        protected void onPostExecute(String r) {
            pbbar.setVisibility(View.GONE);
        }
    }
    private void uploadGoodsPosition(GoodsPosition gp) {
        UploadGoodsPosition ugp = new UploadGoodsPosition();
        ugp.execute(gp);
    }
    public static class UploadGoodsPosition extends AsyncTask<GoodsPosition,String,String> {
        boolean success;
        GoodsPosition goodsPosition;

        @Override
        protected String doInBackground(GoodsPosition... gp) {
            goodsPosition = gp[0];
            success = Database.insertGoodsPosition(App.getStoreId(), storeMan, goodsPosition);
            return null;
        }
        @Override
        protected void onPreExecute() {
            FL.d(TAG, "Start upload goods position");
            success = false;
            pbbar.setVisibility(View.VISIBLE);
        }
        @Override
        protected void onPostExecute(String r) {
            pbbar.setVisibility(View.GONE);
            if (!success) {
                FL.d(TAG, "Save to local DB gp.id =" + goodsPosition.getId());
                Database.addAcceptGoods(storeMan, goodsPosition.getId(), goodsPosition.getBarcode(), goodsPosition.getQnt(), goodsPosition.getCell(), getCurrentTime());
            } else {
                FL.d(TAG, "Successful upload goods position");
            }
        }
    }
}
