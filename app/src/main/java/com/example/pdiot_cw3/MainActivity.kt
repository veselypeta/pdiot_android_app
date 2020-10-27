package com.example.pdiot_cw3

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.pdiot_cw3.bluetooth.ConnectBluetoth
import com.example.pdiot_cw3.bluetooth.ThingyService
import com.example.pdiot_cw3.common.AccelerometerData
import com.example.pdiot_cw3.common.TFLiteModel
import com.example.pdiot_cw3.utils.Constants
import no.nordicsemi.android.thingylib.Thingy
import no.nordicsemi.android.thingylib.ThingyListener
import no.nordicsemi.android.thingylib.ThingyListenerHelper
import no.nordicsemi.android.thingylib.ThingySdkManager
import no.nordicsemi.android.thingylib.ThingySdkManager.ServiceConnectionListener
import com.example.pdiot_cw3.common.Utils.getBluetoothDevice


class MainActivity : AppCompatActivity(), ServiceConnectionListener {

    // buttons
    lateinit var connectBluetoothDeviceButton: Button

    // permissions
    lateinit var permissionAlertDialog: AlertDialog.Builder

    val permissionsForRequest = arrayListOf<String>()
    val accelerometerData: AccelerometerData = AccelerometerData();

    var cameraPermissionGranted = false
    var locationPermissionGranted = false
    var readStoragePermissionGranted = false
    var writeStoragePermissionGranted = false



    lateinit var classifier: TFLiteModel

    lateinit var thingySdkManager: ThingySdkManager
    lateinit var thingyBinder: ThingyService.ThingyBinder
    var mDevice: BluetoothDevice? = null
    private val mThingyListener = object : ThingyListener {
        override fun onDeviceConnected(device: BluetoothDevice?, connectionState: Int) {
            Log.i("thingyListener", "------ Device Connected!")
        }

        override fun onDeviceDisconnected(device: BluetoothDevice?, connectionState: Int) {
            Log.i("thingyListener", "------- Device Disconnected!")
        }

        override fun onServiceDiscoveryCompleted(device: BluetoothDevice?) {
            Log.i("thingyListener", "Service Discovery Completed!")
            thingySdkManager.enableMotionNotifications(device, true);
        }

        override fun onBatteryLevelChanged(bluetoothDevice: BluetoothDevice?, batteryLevel: Int) {}

        override fun onTemperatureValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            temperature: String?
        ) {}

        override fun onPressureValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            pressure: String?
        ) {}

        override fun onHumidityValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            humidity: String?
        ) {}

        override fun onAirQualityValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            eco2: Int,
            tvoc: Int
        ) {}

        override fun onColorIntensityValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            red: Float,
            green: Float,
            blue: Float,
            alpha: Float
        ) {}

        override fun onButtonStateChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            buttonState: Int
        ) {}

        override fun onTapValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            direction: Int,
            count: Int
        ) {}

        override fun onOrientationValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            orientation: Int
        ) {}

        override fun onQuaternionValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            w: Float,
            x: Float,
            y: Float,
            z: Float
        ) {}

        override fun onPedometerValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            steps: Int,
            duration: Long
        ) {}

        override fun onAccelerometerValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            x: Float,
            y: Float,
            z: Float
        ) {
            accelerometerData.pushNewData(x, y, z)
            classifier.classify(accelerometerData)
        }

        override fun onGyroscopeValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            x: Float,
            y: Float,
            z: Float
        ) {}

        override fun onCompassValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            x: Float,
            y: Float,
            z: Float
        ) {}

        override fun onEulerAngleChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            roll: Float,
            pitch: Float,
            yaw: Float
        ) {}

        override fun onRotationMatrixValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            matrix: ByteArray?
        ) {}

        override fun onHeadingValueChangedEvent(bluetoothDevice: BluetoothDevice?, heading: Float) {}

        override fun onGravityVectorChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            x: Float,
            y: Float,
            z: Float
        ) {}

        override fun onSpeakerStatusValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            status: Int
        ) {}

        override fun onMicrophoneValueChangedEvent(
            bluetoothDevice: BluetoothDevice?,
            data: ByteArray?
        ) {}
    }

    // thingy UUID - MAC address
    private val thingyUUID = "E1:FE:48:AB:C5:2A"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        classifier = TFLiteModel().create(assets, Constants.MODEL_PATH, Constants.LABEL_PATH)

        thingySdkManager = ThingySdkManager.getInstance();

        connectBluetoothDeviceButton = findViewById(R.id.ble_button)

        permissionAlertDialog = AlertDialog.Builder(this)
        setupPermissions()
        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        thingySdkManager.bindService(this, ThingyService::class.java)
        // register listener
        ThingyListenerHelper.registerThingyListener(this, mThingyListener);
    }

    override fun onStop() {
        super.onStop();
        thingySdkManager.unbindService(this)
        ThingyListenerHelper.unregisterThingyListener(this, mThingyListener)
    }

    fun setupClickListeners() {
        connectBluetoothDeviceButton.setOnClickListener {
            val intent = Intent(this, ConnectBluetoth::class.java)
            startActivity(intent)
        }
    }

    // check which permissions we need to request
    // and add to permissionsForRequest array
    // request the needed permissions
    fun setupPermissions() {

        // camera permission
        Log.i("Permissions", "Camera permission = " + cameraPermissionGranted)
        if (ActivityCompat.checkSelfPermission(applicationContext,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permissions", "Camera permission = " + cameraPermissionGranted)
            permissionsForRequest.add(Manifest.permission.CAMERA)
        }
        else {
            cameraPermissionGranted = true
        }

        // location permission
        Log.i("Permissions", "Location permission = " + locationPermissionGranted)
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsForRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsForRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        else {
            locationPermissionGranted = true
        }

        // read storage permission
        Log.i("Permissions", "Read st permission = " + readStoragePermissionGranted)
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permissions", "Read st permission = " + readStoragePermissionGranted)
            permissionsForRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        else {
            readStoragePermissionGranted = true
        }

        // write storage permission
        Log.i("Permissions", "Write storage permission = " + writeStoragePermissionGranted)
        if (ActivityCompat.checkSelfPermission(applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permissions", "Write storage permission = " + writeStoragePermissionGranted)
            permissionsForRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        else {
            writeStoragePermissionGranted = true
        }


        if (permissionsForRequest.size >= 1) {
            ActivityCompat.requestPermissions(this,
                    permissionsForRequest.toTypedArray(),
                    Constants.REQUEST_CODE_PERMISSIONS)
        }
    }

    // called after service is bound;
    override fun onServiceConnected() {
        thingyBinder = thingySdkManager.thingyBinder as ThingyService.ThingyBinder
        connect()
    }

    private fun connect(){
        val device = getBluetoothDevice(this, thingyUUID)
        mDevice = device
        thingySdkManager.connectToThingy(this, device, ThingyService::class.java)
//        val thingy = Thingy(device)
        thingySdkManager.selectedDevice = device
    }

    private fun ensureBleExists(): Boolean{
        if(packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            return true;
        }
        return false;
    }
    private fun isBleEnabled(): Boolean{
        val bm = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        return bm.adapter != null && bm.adapter.isEnabled
    }
}