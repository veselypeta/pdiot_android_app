package com.example.pdiot_cw3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.widget.TextView
import com.example.pdiot_cw3.utils.Constants
import kotlinx.android.synthetic.main.activity_thingy_recognition.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.math.max


private val TAG = ThingyRecognition::class.java.simpleName

class ThingyRecognition : AppCompatActivity(){
    lateinit var labelList: List<String>
    private var outputTensor = FloatArray(3)


    lateinit var predictionText: TextView
    lateinit var confidenceText: TextView

    lateinit var looper: Looper


    lateinit var thingyPredictionReceiver: BroadcastReceiver
    var thingyPredictionFilter = IntentFilter(Constants.ACTION_THINGY_DATA_AVAILABLE)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thingy_recognition)

        labelList = this.loadLabelList(assets, Constants.LABEL_PATH)

        predictionText = findViewById(R.id.thingy_prediction_text)
        confidenceText = findViewById(R.id.thingy_confidence_text)

        thingyPredictionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when(intent?.action){
                    Constants.ACTION_THINGY_DATA_AVAILABLE -> {
                        val zero = intent.getFloatExtra(Constants.EXTRA_THINGY_DATA_ZERO, 0f)
                        val one = intent.getFloatExtra(Constants.EXTRA_THINGY_DATA_ONE, 0f)
                        val two = intent.getFloatExtra(Constants.EXTRA_THINGY_DATA_TWO, 0f)

                        outputTensor[0] = zero
                        outputTensor[1] = one
                        outputTensor[2] = two

                        updateUI()
                    }
                }
            }
        }
        val handlerThread = HandlerThread("bgThread")
        handlerThread.start()
        looper = handlerThread.looper
        val handler = Handler(looper)
        this.registerReceiver(thingyPredictionReceiver, thingyPredictionFilter, null, handler)
    }

    @Throws (IOException::class)
    private fun loadLabelList(assetManager: AssetManager, labelPath: String) : List<String> {
        val labelList: MutableList<String> = ArrayList()
        val reader = BufferedReader(InputStreamReader(assetManager.open(labelPath)))
        while (true){
            val line = reader.readLine()
            if (line != null){
                labelList.add(line)
            } else {
                break
            }
        }
        reader.close()
        return labelList
    }

    private fun updateUI() {
        val maxIdx = getMaxIdx()
        val labelText = labelList[maxIdx]
        runOnUiThread{
            thingy_prediction_text.text = labelText
            thingy_confidence_text.text = "${(outputTensor[maxIdx] * 100).toInt()} %"
        }
    }

    private fun getMaxIdx(): Int {
        var max = Float.MIN_VALUE
        var maxIdx = -1
        for(i in 0 until 3){
            if( outputTensor[i] > max){
                max = outputTensor[i]
                maxIdx = i
            }
        }
        return maxIdx
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(thingyPredictionReceiver)
        looper.quit()
    }





}