package com.example.pdiot_cw3

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.pdiot_cw3.bluetooth.BluetoothService
import com.example.pdiot_cw3.bluetooth.ThingyBluetoothService
import com.example.pdiot_cw3.utils.Constants

class ThingyConnect : AppCompatActivity(){

    private lateinit var connectButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var thingyMacInput: EditText

    lateinit var sharedPreferences: SharedPreferences

//    lateinit var mThingyListener: ThingyListener
//    lateinit var mThingySdkManager: ThingySdkManager
//    lateinit var mThingyBinder: ThingyService.ThingyBinder
//    var mDevice: BluetoothDevice? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thingy_connect)

        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)

        connectButton = findViewById(R.id.connect_button)
        disconnectButton = findViewById(R.id.disconnect_button)
        thingyMacInput = findViewById(R.id.thingy_code)

        thingyMacInput.setText(sharedPreferences.getString(Constants.THINGY_MAC_ADDRESS_PREF,""))




        /*
        mThingySdkManager = ThingySdkManager.getInstance()

        mThingyListener = object: ThingyListener {
            override fun onDeviceConnected(device: BluetoothDevice?, connectionState: Int) {
            }

            override fun onDeviceDisconnected(device: BluetoothDevice?, connectionState: Int) {
            }

            override fun onServiceDiscoveryCompleted(device: BluetoothDevice?) {
            }

            override fun onBatteryLevelChanged(
                bluetoothDevice: BluetoothDevice?,
                batteryLevel: Int
            ) {
            }

            override fun onTemperatureValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                temperature: String?
            ) {
            }

            override fun onPressureValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                pressure: String?
            ) {
            }

            override fun onHumidityValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                humidity: String?
            ) {
            }

            override fun onAirQualityValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                eco2: Int,
                tvoc: Int
            ) {
            }

            override fun onColorIntensityValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                red: Float,
                green: Float,
                blue: Float,
                alpha: Float
            ) {
            }

            override fun onButtonStateChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                buttonState: Int
            ) {
            }

            override fun onTapValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                direction: Int,
                count: Int
            ) {
            }

            override fun onOrientationValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                orientation: Int
            ) {
            }

            override fun onQuaternionValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                w: Float,
                x: Float,
                y: Float,
                z: Float
            ) {
            }

            override fun onPedometerValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                steps: Int,
                duration: Long
            ) {
            }

            override fun onAccelerometerValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                x: Float,
                y: Float,
                z: Float
            ) {
            }

            override fun onGyroscopeValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                x: Float,
                y: Float,
                z: Float
            ) {
            }

            override fun onCompassValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                x: Float,
                y: Float,
                z: Float
            ) {
            }

            override fun onEulerAngleChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                roll: Float,
                pitch: Float,
                yaw: Float
            ) {
            }

            override fun onRotationMatrixValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                matrix: ByteArray?
            ) {
            }

            override fun onHeadingValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                heading: Float
            ) {
            }

            override fun onGravityVectorChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                x: Float,
                y: Float,
                z: Float
            ) {
            }

            override fun onSpeakerStatusValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                status: Int
            ) {
            }

            override fun onMicrophoneValueChangedEvent(
                bluetoothDevice: BluetoothDevice?,
                data: ByteArray?
            ) {
            }

        }

         */
        setupClickListeners()
        setupEditText()
    }

    /*
    override fun onStart() {
        super.onStart()
        mThingySdkManager.bindService(this, ThingyService::class.java)
    }

    override fun onStop() {
        super.onStop()
        mThingySdkManager.unbindService(this)
    }


     */



    private fun setupClickListeners(){
        connectButton.setOnClickListener{
            Log.i("ThingyConnect", "Connect Button Pressed")

            saveMacToSharedPref()
            val simpleIntent = Intent(this, ThingyBluetoothService::class.java)
            this.startService(simpleIntent)
            //connectToThingy()
            finish()
        }

        disconnectButton.setOnClickListener{
            Log.i("ThingyConnect", "Disconnect Button Pressed")

            //mThingySdkManager.disconnectFromAllThingies()
            val simpleIntent = Intent(this, ThingyBluetoothService::class.java)
            this.stopService(simpleIntent)
            Log.i("ThingyConnect", "ThingyConnect Service Disconnected")
            finish()
        }
    }

    private fun setupEditText() {
        thingyMacInput.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        thingyMacInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s.toString().trim().length
                if (length != 17) {
                    connectButton.isEnabled = false
                    connectButton.isClickable = false
                } else {
                    connectButton.isEnabled = true
                    connectButton.isClickable = true
                }
            }
        })
    }

    private fun saveMacToSharedPref() {
        sharedPreferences.edit().putString(
            Constants.THINGY_MAC_ADDRESS_PREF,
            thingyMacInput.text.toString()
        ).apply()
    }

    /*
    override fun onServiceConnected() {
        mThingyBinder = mThingySdkManager.thingyBinder as ThingyService.ThingyBinder
    }

    private fun connectToThingy(){
        val thingyUUID = sharedPreferences.getString(Constants.THINGY_MAC_ADDRESS_PREF,"")
        val device = thingyUUID?.let { Utils.getBluetoothDevice(this, it) }
        mDevice = device
        mThingySdkManager.connectToThingy(this, device, ThingyService::class.java)
        mThingySdkManager.selectedDevice = device
    }
     */

}