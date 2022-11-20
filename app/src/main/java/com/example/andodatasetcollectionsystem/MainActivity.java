package com.example.andodatasetcollectionsystem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    BluetoothSocket socket;
    private final int REQUEST_CODE = 1;
    private final int REQUEST_CODE2 = 2;

    String[] PERMISSIONS = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    ActivityResultLauncher<String[]>
        requestPermissionsLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
            if (isGranted.containsValue(false)) {
                // Denied
            } else {
                // Permitted
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (String perm : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, perm)
                    == PackageManager.PERMISSION_GRANTED) {
                // It's already permitted.
            } else {
                requestPermissionsLauncher.launch(PERMISSIONS);
            }
        }

//        ActivityCompat.requestPermissions(this, new String[]{
//                Manifest.permission.BLUETOOTH_CONNECT
//        }, REQUEST_CODE);
//
//        ActivityCompat.requestPermissions(this, new String[]{
//                Manifest.permission.BLUETOOTH_SCAN
//        }, REQUEST_CODE2);

        Button connectButton = findViewById(R.id.button);
        TextView errorText = findViewById(R.id.errorText);

        class MyOnClickListener implements View.OnClickListener {
            MainActivity mainActivity;

            MyOnClickListener(MainActivity mainActivity){
                this.mainActivity = mainActivity;
            }
            @Override
            public void onClick(View v) {

                Connect connect = new Connect();
                boolean status = true;
                if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Log.e("Connect", "Failure to get permission for BLUETOOTH_CONNECT");
                    return ;
                }
                if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Log.e("Connect", "Failure to get permission for BLUETOOTH_SCAN");
                    return ;
                }
                if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("permission check", "permission dined for GPS");
                    return ;
                }

                if(connect.connectAdopter() == 0){
                    errorText.setText("アダプタと接続できんかった");
                    status = false;
                }

                if(connect.connectOBD() == 0) {
                    Intent intent = new Intent(getApplication(), MainActivity2.class);
                    startActivity(intent);
                    errorText.setText("OBD見つけられんかった");
                    status = false;
                }

                if(status) {
                    Intent intent = new Intent(getApplication(), MainActivity2.class);
                    startActivity(intent);//画面遷移
                }
            }
        }

        MyOnClickListener onClickListener = new MyOnClickListener(this);

        connectButton.setOnClickListener(onClickListener);

    }
}