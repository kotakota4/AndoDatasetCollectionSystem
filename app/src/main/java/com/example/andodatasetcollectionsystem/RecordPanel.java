package com.example.andodatasetcollectionsystem;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.protocol.AdaptiveTimingCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity2 extends AppCompatActivity implements LocationListener {

    BluetoothSocket socket;
    LocationManager locationManager;

    private final Handler handler = new Handler(Looper.getMainLooper());

    TextView rpm;
    TextView status;
    TextView locationText;
    TextView throttle;

    //private RPMCommand rpmCommand;
    private RPMCommand2 rpmCommand;
    private ThrottlePositionCommand2 throttlePositionCommand;
    private SQLiteDatabase db;

    double latitude;
    double longitude;

    Timer t;

    private int CREATE_DOCUMENT_REQUEST = 0;
    private int RESULT_FAILED = 1;

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        /* 「CREATE_DOCUMENT_REQUEST」は、Privateで予め定義しておく。 */

        if (requestCode == CREATE_DOCUMENT_REQUEST) {
            //エクスポート
            if (resultCode == RESULT_OK) {
                Uri create_file = data.getData();  //取得した保存先のパスなど。

                //出力処理を実行。その際の引数に上記のUri変数をセットする。
                if (exportCsv_for_SAF(create_file)) {
                    //出力に成功した時の処理。
                } else {
                    //出力に失敗した時の処理。
                }
            } else if (resultCode == RESULT_FAILED) {
                //そもそもアクセスに失敗したなど、保存処理の前に失敗した時の処理。
            }
        }

        //リストの再読み込み
        //this.LoadData();

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void fileopen_for_SAF(){
        try {
            StringBuilder fileName = new StringBuilder();
            fileName.append("drivingRecord");
            fileName.append(new Date());
            fileName.append(".csv");

            Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);

            it.setType("*/*");
            it.putExtra(Intent.EXTRA_TITLE, fileName.toString());

            startActivityForResult(it, CREATE_DOCUMENT_REQUEST);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Boolean exportCsv_for_SAF(Uri openFile){
        try{
            OutputStream os = getContentResolver().openOutputStream(openFile);
            OutputStreamWriter os_write = new OutputStreamWriter(os, Charset.forName("Shift_JIS"));
            PrintWriter pw = new PrintWriter(os_write);
            FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(this);
            db = dbHelper.getReadableDatabase();

            String[] projection = {
                    BaseColumns._ID,
                    FeedReaderContract.FeedEntry.COLUMN_NAME_TIME,
                    FeedReaderContract.FeedEntry.COLUMN_NAME_RPM,
                    FeedReaderContract.FeedEntry.COLUMN_NAME_THROTTLE,
                    FeedReaderContract.FeedEntry.COLUMN_NAME_GPS_1,
                    FeedReaderContract.FeedEntry.COLUMN_NAME_GPS_2
            };

            String selection = BaseColumns._ID + " > ?";
            String[] selectionArgs = {"1"};

            String sortOrder =
                    FeedReaderContract.FeedEntry.COLUMN_NAME_TIME + " DESC";
            Cursor cursor = db.query(
                    FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );
            StringBuilder text = new StringBuilder();
            while (cursor.moveToNext()) {
                text.setLength(0);

                long datetime = cursor.getLong(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TIME));
                String rpm = cursor.getString(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_RPM));
                String throttle = cursor.getString(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_THROTTLE));
                String gps1 = cursor.getString(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_GPS_1));
                String gps2 = cursor.getString(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_GPS_2));
                //text.append(milliToString(datetime) + ": " + rpm + " " + throttle + " " + gps1 + " " + gps2 + "\n");
                text.append(milliToString(datetime));
                text.append(',');
                text.append(rpm);
                text.append(',');
                text.append(throttle);
                text.append(',');
                text.append(gps1);
                text.append(',');
                text.append(gps2);
                pw.println(text);
            }
            cursor.close();
            pw.close();
            os_write.close();
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally{
            if(db.isOpen()){
                db.endTransaction();
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        locationStart();

        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(this);
        db = dbHelper.getWritableDatabase();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ImageButton imageButton = findViewById(R.id.imageButton);
        TextView textView = findViewById(R.id.textView4);
        ImageButton imageButton1 = findViewById(R.id.imageButton2);

        status = findViewById(R.id.textView2);
        rpm = findViewById(R.id.textView3);
        locationText = findViewById(R.id.textView6);
        throttle = findViewById(R.id.textView8);

        Button button = findViewById(R.id.button2);
        Button saveButton = findViewById(R.id.button3);
        TextView textView1 = findViewById(R.id.textView5);


        class ObdOnClickListener implements View.OnClickListener {
            public void onClick(View v) {

                rpmCommand = new RPMCommand2();
                throttlePositionCommand = new ThrottlePositionCommand2();

                try {
                    Connect connect = new Connect();
                    connect.connectAdopter();
                    connect.connectOBD();
                    Thread.sleep(500);
                    new Thread(connect).start();
                    Thread.sleep(500);
                    socket = connect.getSocket();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (AdapterException.NotFoundException e1) {
                    textView.setText("OBDにアクセスできんかった");
                    Log.e("MainActivity2", "Error at OBD" + e1);
                    return;
                } catch (AdapterException.NoAdapterException e2) {
                    textView.setText("OBDが検出されなかった");
                    Log.e("MainActivity2", "Error at connection between OBD and smartphone" + e2);
                    return;
                } catch (NullPointerException | IOException e3) {
                    textView.setText("接続失敗, socketが空だった");
                    return;
                }

                try {
                    status.setText("ステータス: connecting...");
                    InputStream inputStream = socket.getInputStream();
                    OutputStream outputStream = socket.getOutputStream();
                    new EchoOffCommand().run(inputStream, outputStream);
                    Thread.sleep(1000);
                    status.setText("ステータス: connecting...(4/1)");
                    new LineFeedOffCommand().run(inputStream, outputStream);
                    Thread.sleep(1000);
                    status.setText("ステータス: connecting...(4/2)");
                    new TimeoutCommand(125).run(inputStream, outputStream);
                    Thread.sleep(1000);
                    status.setText("ステータス: connecting...(4/3)");
                    new SelectProtocolCommand(ObdProtocols.ISO_14230_4_KWP_FAST).run(inputStream, outputStream);
                    Thread.sleep(1000);
                    status.setText("ステータス: connecting...(4/4)");

                    //Thread.sleep(1000);
                    Log.i("send", "success connecting obd");
                    t = new Timer();
                    t.scheduleAtFixedRate(new TimerTaskRPM(inputStream, outputStream), new Date(), 1000);
                    status.setText("ステータス: collecting");

                } catch (Exception e) {
                    // handle errors
                    textView.setText("OBDにアクセスできんかった");
                    Log.e("MainActivity2", "Error at connection between OBD and smartphone" + e);
                }
            }
        }
        class StopRPMOnClickListener implements View.OnClickListener {

            @Override
            public void onClick(View view) {
                status.setText("ステータス: stopped");
                textView.setText("特になし");
                if (t == null) return;
                t.cancel();
            }
        }


        class SaveLogOnClickListener implements View.OnClickListener{

            @Override
            public void onClick(View view) {
                fileopen_for_SAF();
                Log.i("MainActivity2","pushed");
            }
        }
        class DbShowOnClickListener implements View.OnClickListener {

            @Override
            public void onClick(View view) {
                db = dbHelper.getReadableDatabase();

                String[] projection = {
                        BaseColumns._ID,
                        FeedReaderContract.FeedEntry.COLUMN_NAME_TIME,
                        FeedReaderContract.FeedEntry.COLUMN_NAME_RPM,
                        FeedReaderContract.FeedEntry.COLUMN_NAME_THROTTLE,
                        FeedReaderContract.FeedEntry.COLUMN_NAME_GPS_1,
                        FeedReaderContract.FeedEntry.COLUMN_NAME_GPS_2
                };

                String selection = BaseColumns._ID + " > ?";
                String[] selectionArgs = {"1"};

                String sortOrder =
                        FeedReaderContract.FeedEntry.COLUMN_NAME_TIME + " DESC";

                Cursor cursor = db.query(
                        FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                        projection,             // The array of columns to return (pass null to get all)
                        selection,              // The columns for the WHERE clause
                        selectionArgs,          // The values for the WHERE clause
                        null,                   // don't group the rows
                        null,                   // don't filter by row groups
                        sortOrder               // The sort order
                );
                StringBuilder text = new StringBuilder("");
                while (cursor.moveToNext()) {
                    long datetime = cursor.getLong(
                            cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TIME));
                    String rpm = cursor.getString(
                            cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_RPM));
                    String throttle = cursor.getString(
                            cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_THROTTLE));
                    String gps1 = cursor.getString(
                            cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_GPS_1));
                    String gps2 = cursor.getString(
                            cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_GPS_2));
                    text.append(milliToString(datetime) + ": " + rpm + " " + throttle + " " + gps1 + " " + gps2 + "\n");
                }
                cursor.close();
                textView1.setText(text.toString());
            }
        }

        ObdOnClickListener onClickListener = new ObdOnClickListener();
        imageButton.setOnClickListener(onClickListener);

        SaveLogOnClickListener saveLogOnClickListener = new SaveLogOnClickListener();
        saveButton.setOnClickListener(saveLogOnClickListener);

        StopRPMOnClickListener stopRPMOnClickListener = new StopRPMOnClickListener();
        imageButton1.setOnClickListener(stopRPMOnClickListener);

        DbShowOnClickListener dbShowOnClickListener = new DbShowOnClickListener();
        button.setOnClickListener(dbShowOnClickListener);

        ImageView connectButton = findViewById(R.id.imageView);
        // lambda式
        connectButton.setOnClickListener(v -> finish());
    }

    public static String milliToString(long mill) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        return sdf.format(mill);
    }

    @SuppressLint("MissingPermission")
    private void locationStart(){
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        locationText.setText("緯度 " + latitude + " 経度 " + longitude);
    }

    class TimerTaskRPM extends TimerTask{
        InputStream inputStream;
        OutputStream outputStream;
        int numRPM;
        float numThrottle;

        TimerTaskRPM(InputStream inputStream, OutputStream outputStream){
            super();
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public void run() {
            // handlerを使って処理をキューイングする
            handler.post(() -> {
                try {
                    rpmCommand.run(inputStream, outputStream);
                    throttlePositionCommand.run(inputStream,outputStream);
                } catch (IOException | InterruptedException e){
                    e.printStackTrace();
                }
                Log.i("time", "time is " + Long.valueOf(throttlePositionCommand.getEnd()-rpmCommand.getStart()).toString());
                numRPM = rpmCommand.getRPM();
                numThrottle = throttlePositionCommand.getPercentage();
                Log.i("send","sending rpmcommand" + Integer.valueOf(numRPM).toString());
                rpm.setText("回転数" + Integer.valueOf(numRPM).toString());
                throttle.setText("スロットルポジション" + Float.valueOf(numThrottle).toString());

                ContentValues values = new ContentValues();
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TIME, System.currentTimeMillis());
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_RPM, numRPM);
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_THROTTLE, numThrottle);
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_GPS_1, latitude);
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_GPS_2, longitude);
                db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
            });
        }
    }
}