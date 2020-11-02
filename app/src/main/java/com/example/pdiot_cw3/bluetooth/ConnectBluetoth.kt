package com.example.pdiot_cw3.bluetooth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.example.pdiot_cw3.MainActivity
import com.example.pdiot_cw3.R
import com.example.pdiot_cw3.utils.Constants
import com.google.android.material.snackbar.Snackbar
import com.polidea.rxandroidble2.exceptions.BleException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins

class ConnectBluetoth : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var connectButton: Button;
    private lateinit var disconnectButton: Button;
    private lateinit var macInput: EditText;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_bluetoth)

        // fetch shared preferences
        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)

        // Get widgets from the view
        connectButton = findViewById(R.id.connect_button)
        disconnectButton = findViewById(R.id.disconnect_button)
        macInput = findViewById(R.id.respeck_code)
        macInput.setText(sharedPreferences.getString(Constants.RESPECK_MAC_ADDRESS_PREF,""))


        // necessary workaround for weird errors
        // https://github.com/Polidea/RxAndroidBle/wiki/FAQ:-UndeliverableException
        RxJavaPlugins.setErrorHandler { throwable ->
            if (throwable is UndeliverableException && throwable.cause is BleException) {
                return@setErrorHandler // ignore BleExceptions as they were surely delivered at least once
            }
            // add other custom handlers if needed
            throw RuntimeException("Unexpected Throwable in RxJavaPlugins error handler", throwable)
        }

        // disable connect button unless mac is correct length - make all caps too
        macInput.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        macInput.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim().length != 17) {
                    connectButton.isEnabled = false
                    connectButton.isClickable = false
                } else {
                    connectButton.isEnabled = true
                    connectButton.isClickable = true
                }
            }
        })


        connectButton.setOnClickListener{
            // push respek mac and version to shared preferences
            sharedPreferences.edit().putString(
                Constants.RESPECK_MAC_ADDRESS_PREF,
                macInput.text.toString()
            ).apply()
            sharedPreferences.edit().putInt(Constants.RESPECK_VERSION, 6).apply()
            val simpleIntent = Intent(this, BluetoothService::class.java)
            this.startService(simpleIntent)
            Log.i("service", "BLE Service Clicked")
            finish()

        }

        disconnectButton.setOnClickListener{
            val simpleIntent = Intent(this, BluetoothService::class.java)
            this.stopService(simpleIntent)
            Log.i("service", "BLE Service Disconnected")
            finish()
        }

    }
}