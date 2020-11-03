package com.example.pdiot_cw3

import android.bluetooth.BluetoothDevice
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.pdiot_cw3.bluetooth.ThingyService
import com.example.pdiot_cw3.common.AccelerometerData
import com.example.pdiot_cw3.common.TFLiteModel
import com.example.pdiot_cw3.utils.Constants
import no.nordicsemi.android.thingylib.ThingyListener
import no.nordicsemi.android.thingylib.ThingyListenerHelper
import no.nordicsemi.android.thingylib.ThingySdkManager

class ThingyRecognition : AppCompatActivity(), ThingySdkManager.ServiceConnectionListener {

    lateinit var mThingySdkManager: ThingySdkManager
    lateinit var mThingyBinder: Binder
    val accelerometerData = AccelerometerData(50, 3)
    val mThingyListener = object: ThingyListener{
        override fun onDeviceConnected(device: BluetoothDevice?, connectionState: Int) {
        }

        override fun onDeviceDisconnected(device: BluetoothDevice?, connectionState: Int) {
        }

        override fun onServiceDiscoveryCompleted(device: BluetoothDevice?) {
            mThingySdkManager.enableMotionNotifications(device, true)
        }

        override fun onBatteryLevelChanged(bluetoothDevice: BluetoothDevice?, batteryLevel: Int) {
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
            accelerometerData.pushNewData(x, y, z)
            updatePrediction()
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

        override fun onHeadingValueChangedEvent(bluetoothDevice: BluetoothDevice?, heading: Float) {
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

    val tfLiteModel = TFLiteModel(assets, Constants.MODEL_PATH, Constants.LABEL_PATH)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thingy_recognition)
        mThingySdkManager = ThingySdkManager.getInstance()
    }

    override fun onStart() {
        super.onStart()
        mThingySdkManager.bindService(this, ThingyService::class.java)
        ThingyListenerHelper.registerThingyListener(this, mThingyListener)
    }

    override fun onStop() {
        super.onStop()
        mThingySdkManager.unbindService(this)
        ThingyListenerHelper.unregisterThingyListener(this, mThingyListener)
    }

    override fun onServiceConnected() {
        mThingyBinder = mThingySdkManager.thingyBinder
    }

    private fun updatePrediction(){
        val predictions = tfLiteModel.classify(accelerometerData)
        val label = tfLiteModel.getLabelText(predictions)
        Log.i("Thingy Prediction", "$label")
    }
}