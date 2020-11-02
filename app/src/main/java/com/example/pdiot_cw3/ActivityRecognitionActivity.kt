package com.example.pdiot_cw3

import android.annotation.SuppressLint
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.pdiot_cw3.common.AccelerometerData
import com.example.pdiot_cw3.common.DelayRespek
import com.example.pdiot_cw3.common.RespekData
import com.example.pdiot_cw3.common.TFLiteModel
import com.example.pdiot_cw3.utils.Constants
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import kotlin.math.roundToInt
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.concurrent.BlockingQueue
import java.util.concurrent.DelayQueue
import kotlin.math.sqrt

class ActivityRecognitionActivity : AppCompatActivity() {

    // display queue to update the graph smoothly
    private var mDelayRespeckQueue: BlockingQueue<DelayRespek> = DelayQueue()

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

        // get the accel fields
        val accel_x = findViewById<TextView>(R.id.live_x_accel_data)
        val accel_y = findViewById<TextView>(R.id.live_y_accel_data)
        val accel_z = findViewById<TextView>(R.id.live_z_accel_data)

        setupGraph()

        accelDataReceiver = object: BroadcastReceiver(){
            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent?.action == Constants.ACTION_INNER_RESPECK_BROADCAST){
                    val x = intent.getFloatExtra(Constants.EXTRA_RESPECK_LIVE_X, 0f)
                    val y = intent.getFloatExtra(Constants.EXTRA_RESPECK_LIVE_Y, 0f)
                    val z = intent.getFloatExtra(Constants.EXTRA_RESPECK_LIVE_Z, 0f)

                    val mag = sqrt((x*x + y*y + z*z).toDouble())

                    // TFLite - stuff
                    accelData.pushNewData(x, y, z)
                    val predictions = tfLiteModel.classify(accelData)
                    val confidence = predictions[0].maxOrNull()?.times(100)?.roundToInt()
                    updateUI(tfLiteModel.getLabelText(predictions), confidence)


                    //  ---  Graph  ---
                    val data = RespekData(0L, x, y, z, mag.toFloat(), 0f)
                    val delayRespek = DelayRespek(data, 79)
                    mDelayRespeckQueue.add(delayRespek)

                    runOnUiThread{
                        accel_x.text = "accel_x = $x"
                        accel_y.text = "accel_y = $y"
                        accel_z.text = "accel_z = $z"
                    }

                    time++
                    updateGraph()

                }
            }
        }
        val handlerThread = HandlerThread("bgThread")
        handlerThread.start()
        looper = handlerThread.looper
        val handler = Handler(looper)
        this.registerReceiver(accelDataReceiver, respekDataFilter, null, handler)
    }

    private fun setupGraph(){
        chart = findViewById(R.id.chart)
        time = 0f
        val entries_x = ArrayList<Entry>()
        val entries_y = ArrayList<Entry>()
        val entries_z = ArrayList<Entry>()
        val entries_mag = ArrayList<Entry>()

        dataSet_x = LineDataSet(entries_x, "Accel X")
        dataSet_y = LineDataSet(entries_y, "Accel Y")
        dataSet_z = LineDataSet(entries_z, "Accel Z")
        dataSet_mag = LineDataSet(entries_mag, "Magnitude")

        dataSet_x.setDrawCircles(false)
        dataSet_y.setDrawCircles(false)
        dataSet_z.setDrawCircles(false)
        dataSet_mag.setDrawCircles(false)

        dataSet_x.setColor(
            ContextCompat.getColor(this,
            R.color.red
        ))
        dataSet_y.setColor(
            ContextCompat.getColor(this,
            R.color.green
        ))
        dataSet_z.setColor(
            ContextCompat.getColor(this,
            R.color.blue
        ))
        dataSet_mag.setColor(
            ContextCompat.getColor(this,
            R.color.yellow
        ))

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSet_x)
        dataSets.add(dataSet_y)
        dataSets.add(dataSet_z)
        dataSets.add(dataSet_mag)

        allAccelData = LineData(dataSets)
        chart.data = allAccelData
        chart.invalidate()
    }

    fun updateGraph(){
        // take the first element from the queue
        // and update the graph with it
        val respeckData = mDelayRespeckQueue.take().getData()

        dataSet_x.addEntry(Entry(time, respeckData.accel_x))
        dataSet_y.addEntry(Entry(time, respeckData.accel_y))
        dataSet_z.addEntry(Entry(time, respeckData.accel_z))
//        dataSet_mag.addEntry(Entry(time, respeckData.accel_mag))

        runOnUiThread {
            allAccelData.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
            chart.setVisibleXRangeMaximum(150f)
//            Log.i("Chart", "Lowest X = " + chart.lowestVisibleX.toString())
            chart.moveViewToX(chart.lowestVisibleX + 40)
//            Log.i("Chart", "Lowest X after = " + chart.lowestVisibleX.toString())
        }
    }

    fun updateUI(prediction: String, confidence: Int?){
        runOnUiThread{
            predictionText.text = prediction
            confidenceText.text = "$confidence %"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(accelDataReceiver)
        looper.quit()
    }
}