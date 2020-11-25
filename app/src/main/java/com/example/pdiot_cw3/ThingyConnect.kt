package com.example.pdiot_cw3

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.pdiot_cw3.bluetooth.ThingyRxConnectService
import com.example.pdiot_cw3.utils.Constants

class ThingyConnect : AppCompatActivity(){

    private lateinit var connectButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var thingyMacInput: EditText

    lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thingy_connect)

        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE)

        connectButton = findViewById(R.id.connect_button)
        disconnectButton = findViewById(R.id.disconnect_button)
        thingyMacInput = findViewById(R.id.thingy_code)

        thingyMacInput.setText(sharedPreferences.getString(Constants.THINGY_MAC_ADDRESS_PREF,""))
        setupClickListeners()
        setupEditText()
    }

    private fun setupClickListeners(){
        connectButton.setOnClickListener{
            Log.i("ThingyConnect", "Connect Button Pressed")

            saveMacToSharedPref()
            val simpleIntent = Intent(this, ThingyRxConnectService::class.java)
            this.startService(simpleIntent)
            finish()
        }

        disconnectButton.setOnClickListener{
            Log.i("ThingyConnect", "Disconnect Button Pressed")

            //mThingySdkManager.disconnectFromAllThingies()
            val simpleIntent = Intent(this, ThingyRxConnectService::class.java)
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

}