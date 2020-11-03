package com.example.pdiot_cw3.bluetooth
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.pdiot_cw3.utils.Constants
import no.nordicsemi.android.thingylib.BaseThingyService
import no.nordicsemi.android.thingylib.ThingyConnection

class ThingyService: BaseThingyService() {

    inner class ThingyBinder: BaseThingyBinder() {
        override fun getThingyConnection(device: BluetoothDevice?): ThingyConnection? {
            return mThingyConnections[device]
        }

        // Define own api here

    }

    override fun onBind(intent: Intent?): BaseThingyBinder? {
        Log.i("ThingyService", "Thingy Service Bound!")
       return ThingyBinder();
    }

    // onUnbind
    override fun onDeviceConnected(device: BluetoothDevice?, connectionState: Int) {
        super.onDeviceConnected(device, connectionState)
        Log.i("ThingyService", "Device Connected!")
    }

    override fun onDeviceDisconnected(device: BluetoothDevice?, connectionState: Int) {
        super.onDeviceDisconnected(device, connectionState)
        Log.i("ThingyService", "Device Disconnected!")
    }


    override fun onCreate() {
        super.onCreate()
        registerReceiver(mNotificationDisconnectReceiver, IntentFilter(Constants.ACTION_DISCONNECT))
        Log.i("ThingyService", "Thingy Service Created!")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mNotificationDisconnectReceiver)
        Log.i("ThingyService", "Thingy Service Destroyed!")
    }

    private val mNotificationDisconnectReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action){
                Constants.ACTION_DISCONNECT ->  {
                    val device: BluetoothDevice? = intent.extras?.getParcelable("EXTRA_DEVICE")
                    if (device != null){
                        mThingyConnections?.get(device)?.disconnect();
                    }
                }
            }
        }

    }

}