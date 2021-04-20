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
import android.widget.ImageView
import android.widget.ProgressBar
import com.example.pdiot_cw3.utils.Constants
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList


private val TAG = ThingyRecognition::class.java.simpleName

class ThingyRecognition : AppCompatActivity(){
    lateinit var labelList: List<String>
    private var outputTensor = FloatArray(3)


    lateinit var predictionProgress: ProgressBar
    lateinit var predictionImage: ImageView

    lateinit var looper: Looper


    lateinit var thingyPredictionReceiver: BroadcastReceiver
    var thingyPredictionFilter = IntentFilter(Constants.ACTION_THINGY_DATA_AVAILABLE)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thingy_recognition)

        labelList = this.loadLabelList(assets, Constants.LABEL_PATH)

        predictionProgress = findViewById(R.id.predicted_activity)
        predictionImage = findViewById(R.id.predicted_activity_logo)

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
            predictionProgress.progress = (outputTensor[maxIdx] * 100).toInt()
            when(labelText.toLowerCase(Locale.ROOT)){
                "running" -> {
                    predictionImage.setImageResource(R.drawable.ic_baseline_directions_run_24)
                }
                "standing" -> {
                    predictionImage.setImageResource(R.drawable.ic_baseline_accessibility_24)
                }
                "walking at normal speed" -> {
                    predictionImage.setImageResource(R.drawable.ic_baseline_directions_walk_24)
                }
            }
        }
        updateAllPredictions()
    }

    private fun updateAllPredictions(){
        runOnUiThread {
            val walkProb = (outputTensor[0]*100).toInt()
            val runProb = (outputTensor[1]*100).toInt()
            val standProb = (outputTensor[2]*100).toInt()

            val walkingProgress = findViewById<ProgressBar>(R.id.walking_prediction_progress)
            val runningProgress = findViewById<ProgressBar>(R.id.running_prediction_progress)
            val standingProgress = findViewById<ProgressBar>(R.id.standing_prediction_progress)

            val upDownStairsProgress = findViewById<ProgressBar>(R.id.up_down_stairs_progress)
            val sittingProgress = findViewById<ProgressBar>(R.id.sitting_prediction_progress)


            walkingProgress.progress = walkProb
            runningProgress.progress = runProb
            standingProgress.progress = standProb

            upDownStairsProgress.progress = 2
            sittingProgress.progress = 3


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