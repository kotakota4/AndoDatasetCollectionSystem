package com.example.andodatasetcollectionsystem;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class Connect extends AppCompatActivity implements Runnable{

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice obd;
    private BluetoothSocket socket;

    private final int SUCCESS_ADAPTER = 1;
    private final int FAILURE_ADAPTER = 0;
    private final int SUCCESS_FIND_OBD = 1;
    private final int FAILURE_FIND_OBD = 0;


    private final UUID OBD_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected String TAG = "BluetoothConnectThread";

    public int connectAdopter() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e("Connect", "Failure to connect adapter.");
            return FAILURE_ADAPTER;
        }
        Log.i("Connect", "Success to connect adapter");
        return SUCCESS_ADAPTER;
    }

    @SuppressLint("MissingPermission")
    public int connectOBD() {

        Set<BluetoothDevice> pairedDevices;

        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.i("Connect","Success to find " + device.getName());
                if (device.getName().equals("OBDII")) {//DESKTOP-ATC5ELK     OBDII
                    obd = device;
                    return SUCCESS_FIND_OBD;
                }
            }
        }
        Log.e("Connect", "Failure to find obd");
        return FAILURE_FIND_OBD;
    }

    public BluetoothSocket getSocket() {
        return socket;

    }

    @SuppressLint("MissingPermission")
    public void run(){
        bluetoothAdapter.cancelDiscovery();

        try {
            socket = obd.createRfcommSocketToServiceRecord(OBD_UUID);
            socket.connect();

        } catch (IOException e) {
            Log.e("Connect","Failure in connection" + e.toString());
            e.printStackTrace();
            try{
                Log.i("Connect", "closing the socket");
                socket.close();
            } catch (IOException e1) {
                Log.e("Connect", "Failure to close socket");
                e1.printStackTrace();
            }
            return;
        }
        //Log.e("Connect", "Failure to get socket");
    }
}
