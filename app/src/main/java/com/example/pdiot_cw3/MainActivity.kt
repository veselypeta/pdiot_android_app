package com.example.pdiot_cw3

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.pdiot_cw3.bluetooth.BluetoothService
import com.example.pdiot_cw3.bluetooth.ConnectBluetoth
import com.example.pdiot_cw3.bluetooth.ThingyRxConnectService
import com.example.pdiot_cw3.utils.Constants
import com.example.pdiot_cw3.common.Utils.isServiceRunning
import com.google.android.material.snackbar.Snackbar

private val TAG = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {

    // buttons
    lateinit var connectRespekButton: Button
    lateinit var connectThingyButton: Button
    lateinit var respekActivityRecognitionButton: Button
    lateinit var thingyActivityRecognitionButton: Button

    // status-text
    lateinit var respekStatusText: TextView
    lateinit var thingyStatusText: TextView

    // status broadcast receivers
    lateinit var respekStatusReceiver: BroadcastReceiver
    val respekStatusFilter = IntentFilter()

    // status thingy receivers
    lateinit var thingyStatusReceiver: BroadcastReceiver
    val thingyStatusFilter = IntentFilter()


    // permissions
    lateinit var permissionAlertDialog: AlertDialog.Builder

    val permissionsForRequest = arrayListOf<String>()

    // Permissions
    var cameraPermissionGranted = false
    var locationPermissionGranted = false
    var readStoragePermissionGranted = false
    var writeStoragePermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialise buttons
        connectRespekButton = findViewById(R.id.connect_respek_button)
        connectThingyButton = findViewById(R.id.connect_thingy_button)
        respekActivityRecognitionButton = findViewById(R.id.activity_recognition_button)
        thingyActivityRecognitionButton = findViewById(R.id.thingy_activity_recognition)

        // get text view ref
        respekStatusText = findViewById(R.id.respek_status)
        thingyStatusText = findViewById(R.id.thingy_status)

        respekStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i("Main activity", "received Broadcast")
                when(intent?.action) {
                    Constants.ACTION_RESPECK_CONNECTED -> respekStatusText.text = "Respeck status: Connected"
                    Constants.ACTION_RESPECK_DISCONNECTED -> respekStatusText.text = "Respeck status: Disconnected"
                    else -> respekStatusText.text = "Respeck status: Error"
                }
            }
        }
        respekStatusFilter.addAction(Constants.ACTION_RESPECK_CONNECTED)
        respekStatusFilter.addAction(Constants.ACTION_RESPECK_DISCONNECTED)
        this.registerReceiver(respekStatusReceiver, respekStatusFilter)

        thingyStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i(TAG, "Received Thingy Broadcast")
                when(intent?.action){
                    Constants.ACTION_GATT_CONNECTED -> thingyStatusText.text = "Thingy status: Connected"
                    Constants.ACTION_GATT_DISCONNECTED -> thingyStatusText.text = "Thingy status: Disconnected"
                    else -> thingyStatusText.text = "Thingy status: Error"
                }
            }
        }
        thingyStatusFilter.addAction(Constants.ACTION_GATT_CONNECTED)
        thingyStatusFilter.addAction(Constants.ACTION_GATT_DISCONNECTED)
        this.registerReceiver(thingyStatusReceiver, thingyStatusFilter)


        permissionAlertDialog = AlertDialog.Builder(this)
        setupPermissions()
        setupClickListeners()
        setupRespekStatus()
        setupThingyStatus()
    }

    fun setupClickListeners() {
        // onclick start the connect bluetooth activity
        connectRespekButton.setOnClickListener {
            val intent = Intent(this, ConnectBluetoth::class.java)
            startActivity(intent)
        }

        // TODO - setup connect thingy button
        connectThingyButton.setOnClickListener{
            val intent = Intent(this, ThingyConnect::class.java)
            startActivity(intent)
        }

        thingyActivityRecognitionButton.setOnClickListener{
            if ( thingyStatusText.text == "Thingy status: Connected"){
                val intent = Intent(this, ThingyRecognition::class.java)
                startActivity(intent)
            } else {
                val contextView = findViewById<View>(R.id.constraintLayout)
                Snackbar.make(contextView, "Please first connect to Thingy:52", Snackbar.LENGTH_LONG).show()
                Log.i("Main Activity", "not connected to thingy")
            }
        }

        respekActivityRecognitionButton.setOnClickListener{
            // TODO - Hack AF
            if(respekStatusText.text == "Respeck status: Connected"){
                val activityRecognitionActivity = Intent(this, ActivityRecognitionActivity::class.java)
                startActivity(activityRecognitionActivity)
            } else {
                val contextView = findViewById<View>(R.id.constraintLayout)
                Snackbar.make(contextView, "Please first connect to Respek", Snackbar.LENGTH_LONG).show()
                Log.i("Main Activity", "not connected to respek")
            }
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

    fun setupRespekStatus() {
        val isServiceRunning = isServiceRunning(BluetoothService::class.java as Class<Any>, applicationContext)
        Log.i("DEBUG: ", "isServiceRunning-Respek = $isServiceRunning")

        val sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        if (sharedPreferences.contains(Constants.RESPECK_MAC_ADDRESS_PREF)){
            // do something
            respekStatusText.text = "Respeck status: Connecting..."
            if(!isServiceRunning){
                Log.i("Main Activity", "Starting BLE service")
                val simpleIntent = Intent(this, BluetoothService::class.java)
                this.startService(simpleIntent)
            }
        } else {
            Log.i("sharedpref", "No Respek seen before, must pair first")
            respekStatusText.text = "Respeck status: Unpaired"

        }
    }

    fun setupThingyStatus() {
        val isServiceRunning = isServiceRunning(ThingyRxConnectService::class.java as Class<Any>, applicationContext)
        Log.i("DEBUG: ", "isServiceRunning-Thingy = $isServiceRunning")

        val sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)
        if (sharedPreferences.contains(Constants.THINGY_MAC_ADDRESS_PREF)){
            thingyStatusText.text = "Thingy Status: Connecting..."
            if(!isServiceRunning){
                Log.i(TAG, "Starting Thingy BLE Service")
                val simpleIntent = Intent(this, ThingyRxConnectService::class.java)
                this.startService(simpleIntent)
            }
        } else {
            Log.i("sharepref", "No Thingy seen before, must pair first")
            respekStatusText.text = "Thingy status: Unpaired"
        }
    }



}