package com.example.pdiot_cw3.common

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import java.lang.Exception

object Utils {

    fun getBluetoothDevice(context: Context, address: String): BluetoothDevice? {
        val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager;
        val ba = bm.adapter;

        if(ba != null){
            try {
                return ba.getRemoteDevice(address)
            } catch (e : Exception){
                return null
            }
        }
        return null;
    }
}