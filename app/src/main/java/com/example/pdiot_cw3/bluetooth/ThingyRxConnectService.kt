package com.example.pdiot_cw3.bluetooth

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.pdiot_cw3.common.Utils
import com.example.pdiot_cw3.utils.Constants
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.RxBleScanResult
import com.polidea.rxandroidble2.scan.ScanResult
import io.reactivex.disposables.Disposable
import com.polidea.rxandroidble2.scan.ScanSettings
import java.util.*


class ThingyRxConnectService: Service() {
    private val TAG = ThingyRxConnectService::class.java.simpleName
    lateinit var rxBleClient: RxBleClient
    lateinit var thingyUUID: String

    var thingyFound = false
    var thingyDevice: RxBleDevice? = null

    lateinit var scanDisposable: Disposable
    var thingyLiveSubscription: Disposable? = null

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        thingyUUID = sharedPreferences.getString(Constants.THINGY_MAC_ADDRESS_PREF,"").toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        val thingDisconnectedIntent = Intent(Constants.ACTION_GATT_DISCONNECTED)
        sendBroadcast(thingDisconnectedIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread{
            Log.i(TAG, "Thingy RX Service Started")
            rxBleClient = RxBleClient.create(this)
            val scanSettings: ScanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()

            scanDisposable = rxBleClient.scanBleDevices(scanSettings)
                .doFinally{ Log.i(TAG, "Connection Terminated")}
                .subscribe({
                    if(thingyFound){
                        scanDisposable.dispose()
                    }
                    onScanSuccess(it, thingyUUID)
                }, {onScanFailure(it)})
        }.start()
        return START_STICKY
    }

    private fun onScanSuccess(scanResult: ScanResult?, thingyUUID: String){
        if (scanResult?.bleDevice?.macAddress == thingyUUID){
            thingyFound = true
            thingyDevice = scanResult.bleDevice
            connectThingy()
        }
    }

    private fun connectThingy(){
        // observe connecting state changes
        val connectionStateChanges = thingyDevice?.observeConnectionStateChanges()
            ?.subscribe({
                when(it){
                    RxBleConnection.RxBleConnectionState.CONNECTED -> Log.i(TAG, "Connection state = connected")
                    RxBleConnection.RxBleConnectionState.CONNECTING -> Log.i(TAG, "Connection state = connecting")
                    RxBleConnection.RxBleConnectionState.DISCONNECTED -> Log.i(TAG, "Connection state = disconnected")
                    RxBleConnection.RxBleConnectionState.DISCONNECTING -> Log.i(TAG, "Connection state = disconnecting")
                    else -> Log.i(TAG, "Connection state was null")
                }
            }, {
                Log.i(TAG, "Connection state error =${it.stackTrace}")
            })

        val connectionObservable = thingyDevice?.establishConnection(false)

        thingyLiveSubscription = connectionObservable?.flatMap { it.setupNotification(
            UUID.fromString(Constants.THINGY_CHARACTERISITC_UUID))
        }?.doOnNext{
            Log.i(TAG, "subscribed to thingy")
            val thingyFoundIntent = Intent(Constants.ACTION_GATT_CONNECTED)
            sendBroadcast(thingyFoundIntent)
        }?.flatMap { it }
            ?.subscribe({
                val processedData = Utils.processThingyPacket(it)
                val thingyDataIntent = Intent(Constants.ACTION_THINGY_DATA_AVAILABLE);
                thingyDataIntent.putExtra(Constants.EXTRA_THINGY_DATA_ZERO, processedData[0])
                thingyDataIntent.putExtra(Constants.EXTRA_THINGY_DATA_ONE, processedData[1])
                thingyDataIntent.putExtra(Constants.EXTRA_THINGY_DATA_TWO, processedData[2])
                sendBroadcast(thingyDataIntent)

                val thingyFoundIntent = Intent(Constants.ACTION_GATT_CONNECTED)
                sendBroadcast(thingyFoundIntent)
            }, {
                val thingyDisconnectedIntent = Intent(Constants.ACTION_GATT_DISCONNECTED)
                sendBroadcast(thingyDisconnectedIntent)
        })
    }

    private fun onScanFailure(throwable: Throwable? ){
        Log.i(TAG, "Scan failure: ${throwable?.stackTrace}")
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}