package com.example.andodatasetcollectionsystem;

import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity2 extends AppCompatActivity {

    BluetoothSocket socket;

    private final Handler handler = new Handler(Looper.getMainLooper());

    TextView rpm;
    TextView status;

    private RPMCommand rpmCommand;
    private SQLiteDatabase db;

    Timer t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(this);
        db = dbHelper.getWritableDatabase();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ImageButton imageButton = findViewById(R.id.imageButton);
        TextView textView = findViewById(R.id.textView4);
        ImageButton imageButton1 = findViewById(R.id.imageButton2);

        status = findViewById(R.id.textView2);
        rpm = findViewById(R.id.textView3);

        Button button = findViewById(R.id.button2);
        TextView textView1 = findViewById(R.id.textView5);

        class ObdOnClickListener implements View.OnClickListener {
            public void onClick(View v) {

                ContentValues value = new ContentValues();
                rpmCommand = new RPMCommand();

                Connect connect = new Connect();
                connect.connectAdopter();
                connect.connectOBD();
                new Thread(connect).start();

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                socket = connect.getSocket();
                if (socket == null) {
                    textView.setText("接続失敗, socketが空だった");
                    return;
                }

                try {
                    status.setText("ステータス: connecting...");
                    new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                    new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                    new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());

                    Log.i("send","success connecting obd");
                    status.setText("ステータス: collecting");
                    t = new Timer();
                    t.schedule(new TimerTaskRPM(), 0, 1000);

//                    try {
//                        Thread.sleep(5000);   // 定期実行を待って休止
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

                    //textView.setText("complete");

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
            public void onClick(View view){
                status.setText("ステータス: stopped");
                if(t == null) return;
                t.cancel();
            }
        }

        class DbShowOnClickListener implements View.OnClickListener {

            @Override
            public void onClick(View view) {
                db = dbHelper.getReadableDatabase();

                String[] projection = {
                        BaseColumns._ID,
                        FeedReaderContract.FeedEntry.COLUMN_NAME_TIME,
                        FeedReaderContract.FeedEntry.COLUMN_NAME_RPM
                };

                String selection = BaseColumns._ID + " > ?";
                String[] selectionArgs = {"1"};

                String sortOrder =
                        FeedReaderContract.FeedEntry.COLUMN_NAME_RPM + " DESC";

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
                    text.append(milliToString(datetime) + ": " + rpm + "\n");
                    Log.i("db", "loop");
                }
                cursor.close();
                textView1.setText(text.toString());
            }
        }

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

    class TimerTaskRPM extends TimerTask {
        int numRPM;
        @Override
        public void run() {
            // handlerを使って処理をキューイングする
            handler.post(() -> {
                try {
                    rpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                } catch (IOException e){
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                numRPM = rpmCommand.getRPM();
                Log.i("send","sending rpmcommand" + Integer.valueOf(numRPM).toString());
                rpm.setText("回転数" + Integer.valueOf(numRPM).toString());

                ContentValues values = new ContentValues();
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TIME, System.currentTimeMillis());
                values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_RPM, numRPM);
                db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
            });
        }
    }
}