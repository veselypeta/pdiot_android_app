package com.example.pdiot_cw3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.pdiot_cw3.common.AccelerometerData
import com.example.pdiot_cw3.common.TFLiteModel
import com.example.pdiot_cw3.utils.Constants

class ActivityRecognitionActivity : AppCompatActivity() {

    // prediction text
    lateinit var predictionText: TextView

    // data receiver
    lateinit var accelDataReceiver: BroadcastReceiver

   // data store
    var accelData = AccelerometerData(50)

    var respekDataFilter = IntentFilter()

    // tensorflow model
    lateinit var tfLiteModel: TFLiteModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognition2)

        predictionText = findViewById(R.id.prediction_text)

        tfLiteModel = TFLiteModel(assets, Constants.MODEL_PATH, Constants.LABEL_PATH)

        accelDataReceiver = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent?.action == Constants.ACTION_INNER_RESPECK_BROADCAST){
                    val x = intent.getFloatExtra(Constants.EXTRA_RESPECK_LIVE_X, 0f)
                    val y = intent.getFloatExtra(Constants.EXTRA_RESPECK_LIVE_Y, 0f)
                    val z = intent.getFloatExtra(Constants.EXTRA_RESPECK_LIVE_Z, 0f)

                    accelData.pushNewData(x, y, z)
                    predictionText.text = tfLiteModel.classify(accelData)
                }
            }
        }
        respekDataFilter.addAction(Constants.ACTION_INNER_RESPECK_BROADCAST)
        this.registerReceiver(accelDataReceiver, respekDataFilter)
    }
}