package com.example.pdiot_cw3.bluetooth

import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import com.example.pdiot_cw3.utils.Constants
import java.util.*



class ThingyBluetoothService : Service() {
    private val TAG = ThingyBluetoothService::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences

    private enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
    }

    // bluetooth connection stuff
    lateinit var mBluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter

    private val bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
    private var thingyFound = false


    var mBluetoothGatt: BluetoothGatt? = null
    var mDevice: BluetoothDevice? = null

    var enabled = true

    // Connection State Initially Disconnected
    private var connectionState:ConnectionState = ConnectionState.DISCONNECTED;

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager;
        bluetoothAdapter = mBluetoothManager.adapter
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Thingy Service Started")
        Thread {
            scanLeDevice()
        }.start()
        return START_STICKY
    }

    private fun enableGATTNotifications(char: BluetoothGattCharacteristic){
        Log.i(TAG, "Enabling GATT Notifications for Characteristic")
        mBluetoothGatt?.setCharacteristicNotification(char, enabled)
        val uuid = UUID.fromString(Constants.THINGY_DESCRIPTOR_UUID)
        val descriptor = char.getDescriptor(uuid).apply {
            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }
        mBluetoothGatt?.writeDescriptor(descriptor)
    }


    private fun scanLeDevice(){
        bluetoothLeScanner.startScan(leScanCallback)
    }

    private val leScanCallback: ScanCallback = object: ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (thingyFound) {
                bluetoothLeScanner.stopScan(this)
            }
            onScanSuccess(result)
        }
    }

    private fun onScanSuccess(scanResult: ScanResult?){
        val thingyUUID = sharedPreferences.getString(Constants.THINGY_MAC_ADDRESS_PREF,"")
        if(scanResult?.device?.address == thingyUUID){
            mDevice = scanResult?.device
            thingyFound = true
            connectThingyGatt()
        }
    }


    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val intentAction: String
            when (newState){
                BluetoothProfile.STATE_CONNECTED -> {
                    intentAction = Constants.ACTION_GATT_CONNECTED
                    connectionState = ConnectionState.CONNECTED
                    broadcastUpdate(intentAction)
                    Log.i(TAG, "Connected to GATT server.")
                    Log.i(TAG, "Attempting to start service discovery: " +
                            mBluetoothGatt?.discoverServices())
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    intentAction = Constants.ACTION_GATT_DISCONNECTED
                    connectionState = ConnectionState.DISCONNECTED
                    Log.i(TAG, "Disconnected from GATT Server.")
                    broadcastUpdate(intentAction)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {

            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    val characteristic = gatt?.getService(UUID.fromString(Constants.THINGY_SERVICE_UUID))
                        ?.getCharacteristic(UUID.fromString(Constants.THINGY_CHARACTERISITC_UUID))
                    enableGATTNotifications(characteristic!!)
                    broadcastUpdate(Constants.ACTION_GATT_SERVICES_DISCOVERED)
                }
                else -> Log.w(TAG, "onServicesDiscovered received $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            val data = characteristic?.value
            val processedData = processThingyPacket(data!!)

            val thingyDataIntent = Intent(Constants.ACTION_THINGY_DATA_AVAILABLE);
            thingyDataIntent.putExtra(Constants.EXTRA_THINGY_DATA_ZERO, processedData[0])
            thingyDataIntent.putExtra(Constants.EXTRA_THINGY_DATA_ONE, processedData[1])
            thingyDataIntent.putExtra(Constants.EXTRA_THINGY_DATA_TWO, processedData[2])
            sendBroadcast(thingyDataIntent)

            val thingyFoundIntent = Intent(Constants.ACTION_GATT_CONNECTED)
            sendBroadcast(thingyFoundIntent)

            Log.i(TAG, "Sending BLE Data Broadcast")
        }
    }


    private fun connectThingyGatt(){
        Log.i(TAG, "Device Discovered and Starting GATT Service")
        mBluetoothGatt = mDevice?.connectGatt(this, false, gattCallback)
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun processThingyPacket(data: ByteArray): FloatArray{
        val packet = FloatArray(3)
        for(i in 0 until 3){
            packet[i] = unsignedShortToInt(data[(2*i)], data[(2*i) + 1]).toFloat() / 1000
        }
        return packet
    }

    private fun unsignedShortToInt (byte1: Byte, byte2: Byte): Int{
        val t1 = byte1.toInt() and 255
        val t2 = byte2.toInt() and 255

        val l = t2 shl 8
        val r = l or t1
        return r
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Service Destroyed")
        broadcastUpdate(Constants.ACTION_GATT_DISCONNECTED)
        mBluetoothGatt?.disconnect()
    }

    // Not a bindable service yet
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
