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

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    String[] PERMISSIONS = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    ActivityResultLauncher<String[]>
        requestPermissionsLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
            });

    //Snackbar mySnackbar = Snackbar.make(findViewById(R.id.), R.string.popup_message_for_debug_mode, Snackbar.LENGTH_LONG);

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

        Button connectButton = findViewById(R.id.button);
        TextView errorText = findViewById(R.id.errorText);

        class ConnectButtonOnClickListener implements View.OnClickListener{
            @Override
            public void onClick(View v) {
                errorText.setText("接続中...");
                try{
                    Connect connect = new Connect();
                    connect.connectAdopter();
                    connect.connectOBD();
                    Intent intent = new Intent(getApplication(), MainActivity2.class);
                    startActivity(intent);//画面遷移
                }catch (AdapterException.NoAdapterException e){
                    errorText.setText(R.string.error_log_NoAdapter);
                }catch (AdapterException.NotFoundException e1){
                    errorText.setText(R.string.error_log_NotFoundAdapter);
                }catch (NullPointerException e2){
                    errorText.setText(R.string.error_log_NullPointer);
                    Log.e("MainActivity",e2.toString());
                }
            }
        }

        class ConnectButtonOnLongClickListener implements View.OnLongClickListener{
            @Override
            public boolean onLongClick(View view) {
                Log.i("MainActivity","long clicked");
                Intent intent = new Intent(getApplication(), MainActivity2.class);
                startActivity(intent);//画面遷移
                return true;
            }
        }

        connectButton.setOnClickListener(new ConnectButtonOnClickListener());
        connectButton.setOnLongClickListener(new ConnectButtonOnLongClickListener());
    }
}