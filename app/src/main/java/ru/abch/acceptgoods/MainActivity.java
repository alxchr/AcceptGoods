package ru.abch.acceptgoods;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;


import android.Manifest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bosphere.filelogger.FL;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener,Button.OnClickListener{
    AlertDialog.Builder adbSettings, adbCell, adbUpload;
    private String TAG = "MainActivity";
    private static final int REQ_PERMISSION = 1233;
    private static TextToSpeech mTTS;
    TextView tvStore;
    EditText etStoreMan;
    String sStoreMan;
    int storeMan;
    TextView tvInputCell, tvPrompt, tvBoxLabel, tvDescription, tvCell, tvGoods;
    EditText etScan, etQnt;
    private String input;
    private final int WAIT_INPUT_CELL = 0, WAIT_OUTPUT_CELL = 1, WAIT_GOODS_CODE = 2, WAIT_GOODS_BARCODE = 3, WAIT_CELL = 4, WAIT_QTY = 5;
//    private int state = WAIT_INPUT_CELL;
    private int state = WAIT_GOODS_BARCODE;
    private String inputCell, outputCell, goodsCode;
    private long moveGoodsId, moveGoodsRow;
//    ArrayMap<String,String> goodsItem, goods;
//    ArrayList<ArrayMap<String,String>> goodsArray;
//    ArrayAdapter<ArrayMap<String, String>> itemsAdapter;

    Button process;
    ConnectivityManager cm;
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    IntentFilter intentFilter;
    public static boolean online = false;
    /*
    BlankFragment1 fragment1;
    BlankFragment2 fragment2;
    BlankFragment3 fragment3;
    BlankFragment4 fragment4;
    BlankFragment5 fragment5;
    FragmentTransaction fTrans;

     */
    private static boolean updateMoveGoodsData = false;
    final String[] ids = new String[] {"     2   ", "     JCTR", "    10SSR", "    12SPR", "    1ASPR", "    1BSPR", "    1ISPR", "    1LSPR",
    "    1OSPR", "    1PSPR", "    1CSPR", "    1SSPR", "    1USPR", "    15SPR", "    1TSPR"};
    int qnt;
    GoodsPosition gp;
    String cell;
    final String[] storeCode = new String[] {"1908","1909","1907","1901","1900","1902","1906","1904","1903","1905","1900","1900","1900","1900","1900"};
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
        final String[] names = getResources().getStringArray(R.array.store_names);
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
//                        say(getResources().getString(R.string.ready));
//                        tvPrompt.setText(getResources().getString(R.string.scan_box));
//                        fTrans = getSupportFragmentManager().beginTransaction();
//                        fTrans.replace(R.id.fragment_placeholder,fragment1);
//                        fTrans.replace(R.id.fragment_placeholder,fragment5);
//                        fTrans.commit();
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
        etScan.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN && (i == KeyEvent.KEYCODE_ENTER)) {
                    int entIndex;
                    Log.d(TAG, "Before cleaning text length =" + etScan.getText().toString().length());
                    input = etScan.getText().toString();
                    Log.d(TAG, "Input =" + input);
                    etScan.getText().clear();
                    Log.d(TAG, "After cleaning text length =" + etScan.getText().toString().length());
                    if (input.contains("\n")) {
                        entIndex = input.indexOf("\n");
                        Log.d(TAG, "Enter char index = " + entIndex);
                        if (entIndex == 0) {
                            input = input.substring(1);
                        } else input = input.substring(0, entIndex);
                    }
                    processScan(input);
                }
                return false;
            }
        });

/*
        process = findViewById(R.id.button_process);
        process.setOnClickListener(this);
        process.setEnabled(false);

 */
//        say(getResources().getString(R.string.storeman_number_tts));
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.addDefaultNetworkActiveListener(new ConnectivityManager.OnNetworkActiveListener() {
            @Override
            public void onNetworkActive() {
                FL.d(TAG,"Network active");
            }
        });
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        /*
        tvPrompt = findViewById(R.id.tvPrompt);
        tvPrompt.setText(getResources().getString(R.string.storeman_number_tts));


        fragment1 = new BlankFragment1();
        fragment2 = new BlankFragment2();
        fragment3 = new BlankFragment3();
        fragment4 = new BlankFragment4();
        fragment5 = new BlankFragment5();

         */
        tvBoxLabel = findViewById(R.id.tvBoxLabel);
        adbCell = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        adbCell.setMessage(R.string.other_cell);
        adbCell.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                updateMoveGoodsData = false;
            }
        });
        adbCell.setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                state = WAIT_OUTPUT_CELL;
                updateMoveGoodsData = true;
                /*
                fTrans = getSupportFragmentManager().beginTransaction();
                fTrans.replace(R.id.fragment_placeholder, fragment3);
                fTrans.commit();

                 */
                getSupportFragmentManager().executePendingTransactions();
                BlankFragment3 f3 = (BlankFragment3) getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
                if (f3 == null) Log.e(TAG, "Fragment3 not found");
                else {
                    f3.setGoods(goodsCode);
                    f3.setQty(Config.getQty(goodsCode));
                }
            }
        });
        adbCell.setCancelable(false);
        adbUpload = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        adbUpload.setMessage(R.string.force_upload);
        adbUpload.setPositiveButton(R.string.upload,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if (online) {
                    Database.transferMoveGoods();
                    etStoreMan.requestFocus();
                    state = WAIT_INPUT_CELL;
                    tvBoxLabel.setText("");
                    tvInputCell.setText("");
                    /*
                    fTrans = getSupportFragmentManager().beginTransaction();
                    fTrans.replace(R.id.fragment_placeholder,fragment1);
                    fTrans.commit();

                     */
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
        Database.getBarCodes(App.getStoreId());
        Database.getGoods(App.getStoreId());
        etQnt.setOnKeyListener(new View.OnKeyListener() {
            long row = 0;
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        (i == KeyEvent.KEYCODE_ENTER)){
//                    qnt = etStoreMan.getText().toString();
                    try {
                        qnt = Integer.parseInt(etQnt.getText().toString());
                        FL.d(TAG, "qnt = " + storeMan);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        qnt = -1;
                    }
                    if (qnt > 0 && qnt < 1000) {
                        etQnt.setEnabled(false);
                        etScan.setEnabled(true);
                        etScan.requestFocus();
                        etScan.getText().clear();
                        tvPrompt.setText(getResources().getString(R.string.scan_goods));
                        long scanTime = Config.toComttTime(System.currentTimeMillis());
                        FL.d(TAG, "Insert storage =" + App.getStoreId() +
                                " storeman=" + storeMan + " goods id =" + gp.id + " barcode =" + gp.barcode +
                                " qnt =" + qnt + " cell =" + cell + " time =" + getCurrentTime());
                        if (online) row = Database.insertAcceptGoods(App.getStoreId(), storeMan, gp.id, gp.barcode, qnt, cell, getCurrentTime());
                        if (row == 0) {
                            Database.addAcceptGoods(storeMan, gp.id, gp.barcode, qnt, cell, getCurrentTime());
                        }
                    } else {
                        etQnt.getText().clear();
                        say(getResources().getString(R.string.wrong_qnt));
                    }
                }
                return false;
            }
        });
    }
    public static void say(String text) {
        if (Config.tts) mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, null);
    }
    private void processScan(String scan) {
        Log.d(TAG, "Scanned " + scan);
        switch (state) {
            case WAIT_INPUT_CELL:
                if (CheckCode.checkCell(scan)) {
                    tvInputCell.setText(Config.formatCell(scan));
                    tvBoxLabel.setText(getResources().getString(R.string.input_cell_label));
                    inputCell = scan;
                    moveGoodsId = Database.addMoveGoods(App.getStoreId(), storeMan);
                    /*
                    fTrans = getSupportFragmentManager().beginTransaction();
                    fTrans.replace(R.id.fragment_placeholder, fragment2);
                    fTrans.commit();

                     */
                    state = WAIT_GOODS_CODE;
                } else {
                    say(getResources().getString(R.string.wrong_cell));
                }
                break;
            case WAIT_OUTPUT_CELL:
                if (CheckCode.checkCell(scan)) {
                    outputCell = scan;
                    state = WAIT_GOODS_CODE;
                    addGoodsToCell(outputCell, goodsCode);
                    if (updateMoveGoodsData) {
                        Database.updateGoodsData(moveGoodsRow, moveGoodsId, goodsCode, inputCell, outputCell, Config.getQty(goodsCode));
                        updateMoveGoodsData = false;
                    } else {
                        Database.addMoveGoodsData(moveGoodsId, goodsCode, inputCell, outputCell, Config.getQty(goodsCode));
                    }
                    /*
                    fTrans = getSupportFragmentManager().beginTransaction();
                    fTrans.replace(R.id.fragment_placeholder,fragment4);
                    fTrans.commit();

                     */
                    getSupportFragmentManager().executePendingTransactions();
                    BlankFragment4 f4 = (BlankFragment4) getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
                    if (f4 == null) Log.e(TAG, "Fragment4 not found");
                    else {
                        f4.setCell(Config.formatCell(scan));
                    }
                } else {
                    say(getResources().getString(R.string.wrong_cell));
                }
                break;
            case WAIT_GOODS_CODE:
                if (CheckCode.checkGoods(scan)) {
                    goodsCode = scan;
                    moveGoodsRow = Database.findGoods(moveGoodsId,goodsCode);
                    if (moveGoodsRow == 0) {    //unique code
                        state = WAIT_OUTPUT_CELL;
                        updateMoveGoodsData = false;
                        /*
                        fTrans = getSupportFragmentManager().beginTransaction();
                        fTrans.replace(R.id.fragment_placeholder, fragment3);
                        fTrans.commit();

                         */
                        getSupportFragmentManager().executePendingTransactions();
                        BlankFragment3 f3 = (BlankFragment3) getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
                        if (f3 == null) Log.e(TAG, "Fragment3 not found");
                        else {
                            f3.setGoods(scan);
                            f3.setQty(Config.getQty(scan));
                        }
                    } else {                //not unique code
                        say(getResources().getString(R.string.repeat_scan));
                        adbCell.create().show();
                    }
                } else {
                    say(getResources().getString(R.string.wrong_goods));
                }
                break;
            case WAIT_GOODS_BARCODE:
                gp = Database.getGoodsPosition(scan);
                if (gp == null) {
                    say(getResources().getString(R.string.wrong_goods));
                } else {
                    etQnt.setText(String.valueOf(gp.qnt));
                    tvGoods.setText(gp.barcode);
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
                    state = WAIT_GOODS_BARCODE;
                } else say(getResources().getString(R.string.wrong_cell));
                break;
            default:
                Log.d(TAG,"WTF switch");
                state = WAIT_GOODS_BARCODE;
//                tvPrompt.setText(getResources().getString(R.string.scan_box));
                break;
        }
    }
    private void addGoodsToCell(String cellCode, String goodsCode) {
        Log.d(TAG, "Add goods " + goodsCode + " to cell " + cellCode);
    }

    @Override
    public void onClick(View view) {
        FL.d(TAG, "Process button pressed");
        view.setEnabled(false);
        if (online) {
            Database.transferMoveGoods();
        } else {
            say(getResources().getString(R.string.upload_deferred));
        }
        if (Database.getDataCount() < Config.maxDataCount) {
            say(getResources().getString(R.string.storeman_number_tts));
            etStoreMan.requestFocus();
            state = WAIT_INPUT_CELL;
            tvBoxLabel.setText("");
            tvInputCell.setText("");
            /*
            fTrans = getSupportFragmentManager().beginTransaction();
            fTrans.replace(R.id.fragment_placeholder,fragment1);
            fTrans.commit();

             */
        } else {
            say(getResources().getString(R.string.force_upload));
            adbUpload.create().show();
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
                Database.uploadGoods();
            } else {
                FL.d(TAG, "Network disconnected");
                phrase = "wifi отключен";
                online = false;
            }
//            say(phrase);
            Toast.makeText(context, phrase, Toast.LENGTH_LONG).show();
        }
    };
    public static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Yekaterinburg"));
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }
}
