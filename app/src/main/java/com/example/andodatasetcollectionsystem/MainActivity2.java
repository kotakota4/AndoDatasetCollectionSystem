package com.example.andodatasetcollectionsystem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity2 extends AppCompatActivity implements LocationListener, SensorEventListener {

    BluetoothSocket socket;
    LocationManager locationManager;

    private final Handler handler = new Handler(Looper.getMainLooper());

    TextView rpm;
    TextView status;
    TextView locationText;
    TextView throttle;

    private RPMCommand2 rpmCommand;
    private ThrottlePositionCommand2 throttlePositionCommand;
    private SQLiteDatabase db;
    private CreateCsv createCsv;

    int CREATE_DOCUMENT_REQUEST = 0;
    int RESULT_OK = 1;
    int RESULT_FAILED = 2;

    double latitude;
    double longitude;

    ImageView rpmmeter;
    ImageView pointer;

    ImageView frictionCircle;
    ImageView grippointer;

    Timer t;


    float sensorX;
    float sensorY;
    float sensorZ;
    float sensorX2;
    float sensorY2;
    float sensorZ2;
    private SensorManager sensorManager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

                Uri create_file = data.getData();  //取得した保存先のパスなど。

                //出力処理を実行。その際の引数に上記のUri変数をセットする。
                if (createCsv.exportCsv_for_SAF(create_file)) {
                    //出力に成功した時の処理。
                    Log.e("CreateCsv","Success to create csv");
                } else {
                    //出力に失敗した時の処理。
                    Log.e("CreateCsv", "Fail to create csv");
                }

        //そもそもアクセスに失敗したなど、保存処理の前に失敗した時の処理。
        Log.e("CreateCsv", "Fail to access to the db");

        //リストの再読み込み
        //this.LoadData();

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onResume() {
        super.onResume();

        Sensor accel = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Listenerを解除
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            sensorX = sensorX2;
            sensorY = sensorY2;
            sensorZ = sensorZ2;
            sensorX2 = event.values[0];
            sensorY2 = event.values[1];
            sensorZ2 = event.values[2];
            TranslateAnimation trance = new TranslateAnimation(sensorX*256, sensorX2*256, sensorZ*256, sensorZ2*256);
            //TranslateAnimation trance = new TranslateAnimation(sensorX*100, sensorX2*100, sensorY*100, sensorY2*100);
            trance.setFillAfter(true);
            trance.setDuration(100);
            grippointer.startAnimation(trance);

            Log.i("acceleration",Float.valueOf(sensorX).toString()+" "+Float.valueOf(sensorY).toString());
        }
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
        rpmmeter = findViewById(R.id.imageView1);
        frictionCircle = findViewById(R.id.imageView2);
        pointer = findViewById(R.id.imageView3);
        grippointer = findViewById(R.id.imageView4);

        status = findViewById(R.id.textView2);
        rpm = findViewById(R.id.textView3);
        locationText = findViewById(R.id.textView6);
        throttle = findViewById(R.id.textView8);

        Button button = findViewById(R.id.button2);
        Button buttonForSave = findViewById(R.id.button3);
        TextView textView1 = findViewById(R.id.textView5);
        rpmmeter.setImageResource(R.drawable.rpm_meter);
        frictionCircle.setImageResource(R.drawable.grip_circle);
        pointer.setImageResource(R.drawable.pointer);
        grippointer.setImageResource(R.drawable.grippointer);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        class ObdOnClickListener implements View.OnClickListener {
            public void onClick(View v) {

                rpmCommand = new RPMCommand2();
                throttlePositionCommand = new ThrottlePositionCommand2();

                try {
                    Connect connect = new Connect();
                    connect.connectAdopter();
                    connect.connectOBD();
                    new Thread(connect).start();
                    Thread.sleep(2000);
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
                    new LineFeedOffCommand().run(inputStream, outputStream);
                    Thread.sleep(1000);
                    //new TimeoutCommand(125).run(inputStream, outputStream);
                    //Thread.sleep(1000);
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(inputStream, outputStream);
                    Thread.sleep(1000);
                    Log.i("send", "success connecting obd");
                    t = new Timer();
                    t.scheduleAtFixedRate(new TimerTaskRPM(inputStream, outputStream), new Date(), 1000);
                    status.setText("ステータス: collecting");

                } catch (Exception e) {
                    // handle errors
                    textView.setText("OBDにアクセスできんかった");
                    Log.e("MainActivity2", "Error at connection between OBD and smartphone" + e.toString());
                    return;
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

        class ButtonForSaveListener implements View.OnClickListener {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("/data/data/com.example.andodatasetcorrectionsystem/databases/");
                createCsv = new CreateCsv(MainActivity2.this, db);
                createCsv.fileOpen();
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

        ButtonForSaveListener buttonForSaveListener = new ButtonForSaveListener();
        buttonForSave.setOnClickListener(buttonForSaveListener);

        ObdOnClickListener onClickListener = new ObdOnClickListener();
        imageButton.setOnClickListener(onClickListener);

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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        locationText.setText("緯度 " + latitude + " 経度 " + longitude);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
                } catch (IOException e){
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                Log.i("time", "time is " + Long.valueOf(throttlePositionCommand.getEnd()-rpmCommand.getStart()).toString());
                numRPM = rpmCommand.getRPM();
                numThrottle = throttlePositionCommand.getPercentage();
                Log.i("send","sending rpmcommand" + Integer.valueOf(numRPM).toString());
                rpm.setText("回転数" + Integer.valueOf(numRPM).toString());
                throttle.setText("スロットルポジション" + Float.valueOf(numThrottle).toString());
                pointer.setRotation(-25+(int)numRPM*150/7000);


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