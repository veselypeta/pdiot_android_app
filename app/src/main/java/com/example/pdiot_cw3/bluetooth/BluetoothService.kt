package com.example.pdiot_cw3.bluetooth

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.pdiot_cw3.utils.Constants
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.disposables.Disposable
import java.util.*

class BluetoothService: Service() {

    lateinit var rxBleClient: RxBleClient;
    lateinit var respekUUID:String;
    var respekFound = false;
    var respeckDevice: RxBleDevice? = null

    lateinit var scanDisposable: Disposable

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);
        respekUUID = sharedPreferences.getString(Constants.RESPECK_MAC_ADDRESS_PREF,"").toString();
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
                        Log.i("ble", "Disposing of scanner")
                        scanDisposable.dispose();
                    }
                    onScanSuccess(it, respekUUID);
                }, {onScanFailure(it)})

        }.start();
        return START_STICKY
    }

    private fun onScanSuccess(scanResult: ScanResult?, respekUUID: String) {
        if (scanResult?.bleDevice?.macAddress == respekUUID){
            Log.i("ble", "Successfully connected to respek");
            respekFound=true;
            respeckDevice = scanResult.bleDevice;
            connectRespek()
        }
    }

    private fun connectRespek(){
        val connectionObservable = respeckDevice?.establishConnection(false);
        val interval = 0
        connectionObservable?.flatMap { it.setupNotification(
            UUID.fromString(
                Constants.RESPECK_CHARACTERISTIC_UUID,
            )
        ) }?.doOnNext{
            Log.i("ble", "Subscribed to Respek")
        }
            ?.flatMap { it }
            ?.subscribe({
                Log.i("ble", it.toString())
            })
    }

    private fun onScanFailure(throwable: Throwable?) {
        Log.i("ble", "Scan failure: " + throwable?.stackTrace)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i("service", "BLE Service Bound")
        return null;
    }

}

