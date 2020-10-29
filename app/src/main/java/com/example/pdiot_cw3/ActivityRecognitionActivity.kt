package com.example.pdiot_cw3

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.widget.TextView
import com.example.pdiot_cw3.common.AccelerometerData
import com.example.pdiot_cw3.common.TFLiteModel
import com.example.pdiot_cw3.utils.Constants
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import kotlin.math.roundToInt
import com.github.mikephil.charting.data.LineDataSet
import kotlin.math.sqrt

class ActivityRecognitionActivity : AppCompatActivity() {

    // prediction text
    lateinit var predictionText: TextView
    lateinit var confidenceText: TextView

    // receive broadcast
    lateinit var accelDataReceiver: BroadcastReceiver
    var respekDataFilter = IntentFilter(Constants.ACTION_INNER_RESPECK_BROADCAST)
    var accelData = AccelerometerData(50)
    lateinit var looper: Looper

    // global graph variables
    lateinit var dataSet_x: LineDataSet
    lateinit var dataSet_y: LineDataSet
    lateinit var dataSet_z: LineDataSet
    lateinit var dataSet_mag: LineDataSet
    var time = 0f
    lateinit var allAccelData: LineData
    lateinit var chart: LineChart


    // tensorflow model
    lateinit var tfLiteModel: TFLiteModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognition2)

        predictionText = findViewById(R.id.prediction_text)
        confidenceText = findViewById(R.id.confidence_text)

        tfLiteModel = TFLiteModel(assets, Constants.MODEL_PATH, Constants.LABEL_PATH)

        setupGraph()

        accelDataReceiver = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent?.action == Constants.ACTION_INNER_RESPECK_BROADCAST){
                    val x = intent.getFloatExtra(Constants.EXTRA_RESPECK_LIVE_X, 0f)
                    val y = intent.getFloatExtra(Constants.EXTRA_RESPECK_LIVE_Y, 0f)
                    val z = intent.getFloatExtra(Constants.EXTRA_RESPECK_LIVE_Z, 0f)

                    val mag = sqrt((x*x + y*y + z*z).toDouble())

                    accelData.pushNewData(x, y, z)
                    val predictions = tfLiteModel.classify(accelData)
                    val confidence = predictions[0].maxOrNull()?.times(100)?.roundToInt()
                    updateUI(tfLiteModel.getLabelText(predictions), confidence)


                    //  ---  Graph  ---
                    time++
                    updateGraph()

                }
            }
        }
        val handlerThread = HandlerThread("bgThread")
        handlerThread.start()
        looper = handlerThread.looper
        val handler = Handler(looper)
//        respekDataFilter.addAction(Constants.ACTION_INNER_RESPECK_BROADCAST)
        this.registerReceiver(accelDataReceiver, respekDataFilter, null, handler)
    }

    fun setupGraph(){

    }

    fun updateGraph(){

    }

    fun updateUI(prediction: String, confidence: Int?){
        runOnUiThread{
            predictionText.text = prediction
            confidenceText.text = "$confidence %"
        }
    }
}