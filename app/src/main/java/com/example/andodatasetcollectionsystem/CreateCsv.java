package com.example.andodatasetcollectionsystem;

import static com.example.andodatasetcollectionsystem.MainActivity2.milliToString;
import static java.util.Calendar.MILLISECOND;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

public class CreateCsv extends Activity{
    private final Activity activity;
    private final SQLiteDatabase db;

    public CreateCsv(Activity activity, SQLiteDatabase db){
        this.db = db;
        this.activity = activity;
    }

    int CREATE_DOCUMENT_REQUEST = 0;
    int RESULT_OK = 1;
    int RESULT_FAILED = 2;

    StringBuffer fileName;

    public void fileOpen(){
        try {
            fileName = new StringBuffer();
            fileName.append(getTime());
            fileName.append(".csv");
            Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            it.setType("*/*");
            it.putExtra(Intent.EXTRA_TITLE, fileName.toString());
            this.activity.startActivityForResult(it, CREATE_DOCUMENT_REQUEST);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private String getTime(){
        long mill = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        return sdf.format(mill);
    }

        public Boolean exportCsv_for_SAF (Uri openFile) {

            try {
                OutputStream os = this.activity.getContentResolver().openOutputStream(openFile);
                OutputStreamWriter os_write = new OutputStreamWriter(os, Charset.forName("Shift_JIS")); //後々インポートする際に困るのでエンコードを指定しておく。
                PrintWriter pw = new PrintWriter(os_write);
                StringBuilder rec = new StringBuilder();//Export_data用

                //Read Data

                /* ここにExportするデータを取得するためのDBインスタンスなどを記述する。*/

                pw.println("test\n");

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
                        FeedReaderContract.FeedEntry.COLUMN_NAME_TIME + " ASC";

                Cursor c = db.query(
                        FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                        projection,             // The array of columns to return (pass null to get all)
                        selection,              // The columns for the WHERE clause
                        selectionArgs,          // The values for the WHERE clause
                        null,                   // don't group the rows
                        null,                   // don't filter by row groups
                        sortOrder               // The sort order
                );
                while (c.moveToNext()) {
                    rec.setLength(0);
                    long datetime = c.getLong(
                            c.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TIME));

                    String rpm = c.getString(
                            c.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_RPM));
                    String throttle = c.getString(
                            c.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_THROTTLE));
                    String gps1 = c.getString(
                            c.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_GPS_1));
                    String gps2 = c.getString(
                            c.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_GPS_2));
                    rec.append(milliToString(datetime) + "," + rpm + "," + throttle + "," + gps1 + "," + gps2);
                    Log.e("test", milliToString(datetime));
                    pw.println(rec.toString()); //Export
                }


                //基本的に開けたら閉める。
                c.close();
                pw.close();
                os_write.close();
                os.close();

            } catch (SQLiteException e) {
                e.printStackTrace();
                return false;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (db.isOpen()) {
                    //db.endTransaction();
                }
            }
            return true;
        }
}
