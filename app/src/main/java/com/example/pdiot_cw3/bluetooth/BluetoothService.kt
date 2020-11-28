package com.example.pdiot_cw3.bluetooth

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.pdiot_cw3.common.JavaUtils
import com.example.pdiot_cw3.common.Utils
import com.example.pdiot_cw3.utils.Constants
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.disposables.Disposable
import java.util.*

class BluetoothService: Service() {

    lateinit var rxBleClient: RxBleClient
    lateinit var respekUUID:String
    var respekVersion: Int = 0
    var respekFound = false
    var respeckDevice: RxBleDevice? = null

    lateinit var scanDisposable: Disposable
    var respeckLiveSubscription: Disposable? = null

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        respekUUID = sharedPreferences.getString(Constants.RESPECK_MAC_ADDRESS_PREF,"").toString()
        respekVersion = sharedPreferences.getInt(Constants.RESPECK_VERSION, 0)
        Log.i("RESPEK VERSION _____", "$respekVersion")
    }

    override fun onDestroy() {
        Log.i("service", "BLE Service Destroyed")
        super.onDestroy()
        val respeckDisconnectedIntent = Intent(Constants.ACTION_RESPECK_DISCONNECTED)
        sendBroadcast(respeckDisconnectedIntent)
        respeckLiveSubscription?.dispose()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("service", "BLE Service Started")
        // start bluetooth service in a new thread
        Thread{
            rxBleClient = RxBleClient.create(this);
            val scanSettings: ScanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()

            scanDisposable = rxBleClient.scanBleDevices(scanSettings)
                .doFinally{ Log.i("ble", "Connection terminated")}
                .subscribe({
                    if(respekFound){
                        Log.i("ble", "Disposing of scanner")
                        scanDisposable.dispose()
                    }
                    onScanSuccess(it, respekUUID)
                }, {onScanFailure(it)})

        }.start()
        return START_STICKY
    }

    private fun onScanSuccess(scanResult: ScanResult?, respekUUID: String) {
        if (scanResult?.bleDevice?.macAddress == respekUUID){
            Log.i("ble", "Successfully found respek")
            respekFound=true
            respeckDevice = scanResult.bleDevice
            connectRespek()
        }
    }

    private fun connectRespek(){
        // observe connecting state changes
        val connectionStateChanges = respeckDevice?.observeConnectionStateChanges()
            ?.subscribe({
                when(it){
                    RxBleConnection.RxBleConnectionState.CONNECTED -> Log.i("Respek", "Connection state = connected")
                    RxBleConnection.RxBleConnectionState.CONNECTING -> Log.i("Respek", "Connection state = connecting")
                    RxBleConnection.RxBleConnectionState.DISCONNECTED -> Log.i("Respek", "Connection state = disconnected")
                    RxBleConnection.RxBleConnectionState.DISCONNECTING -> Log.i("Respek", "Connection state = disconnecting")
                    else -> Log.i("Respek", "Connection state was null")
                }
            }, {
                Log.i("Respek", "Connection state error =${it.stackTrace}")
            })


        val connectionObservable = respeckDevice?.establishConnection(false)
        var interval = 0

        respeckLiveSubscription = connectionObservable?.flatMap { it.setupNotification(
            UUID.fromString(Constants.RESPECK_CHARACTERISTIC_UUID))
        }?.doOnNext {
            Log.i("Respek", "Subscriped to Respek")
            // Send broadcast to everyone notifying of connected respek
            val respekFoundIntent = Intent(Constants.ACTION_RESPECK_CONNECTED)
            sendBroadcast(respekFoundIntent)
        }
            ?.flatMap { it }
            ?.subscribe({
                JavaUtils.processRESpeckPacket(it, respekVersion, this)
//                Utils.processRESpeckPacket(it, 6, this)
                val respekFoundIntent = Intent(Constants.ACTION_RESPECK_CONNECTED)
                sendBroadcast(respekFoundIntent)
                interval++
            }, {
                Log.i("Respek", "Error when connecting ${it.stackTrace}")
                val respekFoundIntent = Intent(Constants.ACTION_RESPECK_DISCONNECTED)
                sendBroadcast(respekFoundIntent)
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

