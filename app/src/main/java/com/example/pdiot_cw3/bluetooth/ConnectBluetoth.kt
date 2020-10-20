package com.example.pdiot_cw3.bluetooth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.pdiot_cw3.R
import com.example.pdiot_cw3.utils.PREFERENCES_FILE
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.exceptions.BleException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins

class ConnectBluetoth : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var connectButton: Button;
    private lateinit var disconnectButton: Button;
    private lateinit var macInput: EditText;


    lateinit var rxBleClient: RxBleClient

    private var respekMAC = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_bluetoth)

        // fetch shared preferences
        sharedPreferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

        // get respek mac
        macInput = findViewById(R.id.respeck_code)
        connectButton = findViewById(R.id.connect_button)
        disconnectButton = findViewById(R.id.disconnect_button)


        // necessary workaround for weird errors
        // https://github.com/Polidea/RxAndroidBle/wiki/FAQ:-UndeliverableException
        RxJavaPlugins.setErrorHandler { throwable ->
            if (throwable is UndeliverableException && throwable.cause is BleException) {
                return@setErrorHandler // ignore BleExceptions as they were surely delivered at least once
            }
            // add other custom handlers if needed
            throw RuntimeException("Unexpected Throwable in RxJavaPlugins error handler", throwable)
        }

        // disable connect button unless mac is correct length
        macInput.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim().length != 17) {
                    connectButton.isEnabled = false
                    connectButton.isClickable = false
                } else {
                    connectButton.isEnabled = true
                    connectButton.isClickable = true
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


        connectButton.setOnClickListener{
            val simpleIntent = Intent(this, BluetoothService::class.java)
            this.startService(simpleIntent)
            Log.i("service", "BLE Service Clicked")

        }

        disconnectButton.setOnClickListener{
            val simpleIntent = Intent(this, BluetoothService::class.java)
            this.stopService(simpleIntent)
            Log.i("service", "BLE Service Disconnected")

        }

    }
}