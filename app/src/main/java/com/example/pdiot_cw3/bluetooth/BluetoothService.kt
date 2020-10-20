package com.example.pdiot_cw3.bluetooth

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.disposables.Disposable

class BluetoothService: Service() {

    lateinit var rxBleClient: RxBleClient;
    lateinit var respekUUID:String;

    lateinit var scanDisposable: Disposable


    override fun onCreate() {
        super.onCreate()
        Log.i("service", "BLE Service Created")
    }

    override fun onDestroy() {
        Log.i("service", "BLE Service Destroyed")
        super.onDestroy()
        // TODO - clean up service
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("service", "BLE Service Started")
        Thread{
            Log.i("service", "Service thread created Successfully")
            rxBleClient = RxBleClient.create(this);
            val scanSettings: ScanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            scanDisposable = rxBleClient.scanBleDevices(scanSettings)
                .doFinally{ Log.i("ble", "Connection terminated")}
                .subscribe({
                    if(respekFound){
                        scanDisposable.dispose();
                    }
                    onScanSuccess(it, respekUUID);
                }, {onScanFailure(it)})

        }.start();
        return START_STICKY
    }

    private fun onScanSuccess(it: ScanResult?, respekUUID: String) {


    }

    private fun onScanFailure(it: Throwable?) {


    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i("service", "BLE Service Bound")
        return null;
    }

}

