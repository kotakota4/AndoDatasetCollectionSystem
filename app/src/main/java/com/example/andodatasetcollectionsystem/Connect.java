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

    @SuppressLint("MissingPermission")
    public Connect() throws NullPointerException{
        UUID OBD_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        try{
            this.connectAdopter();
            this.connectOBD();
            socket = obd.createRfcommSocketToServiceRecord(OBD_UUID);
        }catch(AdapterException.NotFoundException | IOException e1){
            Log.e("Connect", e1.toString());
        } catch (AdapterException.NoAdapterException e3) {
            Log.e("Connect", e3.toString());
            e3.printStackTrace();
        }


    }

    public void connectAdopter() throws AdapterException.NoAdapterException {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e("Connect", "Failure to connect adapter.");
            throw new AdapterException.NoAdapterException();
        }
        Log.i("Connect", "Success to connect adapter");
    }

    @SuppressLint("MissingPermission")
    public void connectOBD() throws AdapterException.NotFoundException{

        Set<BluetoothDevice> pairedDevices;

        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.i("Connect","Success to find " + device.getName());
                if (device.getName().equals("JOHN")) {//DESKTOP-ATC5ELK     OBDII  UMPC-03-SR JOHN
                    obd = device;
                    return;
                }
            }
        }
        throw new AdapterException.NotFoundException();
    }

    public BluetoothSocket getSocket() throws IOException{
        if(socket == null) throw new IOException();
        return socket;
    }

    @SuppressLint("MissingPermission")
    public void run() {
        bluetoothAdapter.cancelDiscovery();

        try {
            socket.connect();

        } catch (IOException e) {
            Log.e("Connect","Failure in connection" + e);
            e.printStackTrace();
            try{
                Log.i("Connect", "closing the socket");
                socket.close();
            } catch (IOException e1) {
                Log.e("Connect", "Failure to close socket");
                e1.printStackTrace();
            }
        }
    }
}


