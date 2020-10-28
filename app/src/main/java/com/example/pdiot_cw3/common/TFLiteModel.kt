package com.example.pdiot_cw3.common

import android.content.res.AssetManager
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.*
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.collections.ArrayList

class TFLiteModel {

    private lateinit var interpreter: Interpreter
    private lateinit var labelList: List<String>

    fun create(assetManager: AssetManager, modelPath: String, labelPath: String): TFLiteModel{
        val classifier = TFLiteModel()
        classifier.interpreter = Interpreter(classifier.loadModelFile(assetManager, modelPath), Interpreter.Options())
        classifier.labelList = classifier.loadLabelList(assetManager, labelPath)
        return classifier
    }

    @Throws (IOException::class)
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
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

    fun classify(accelerometerData: AccelerometerData){

        val output = arrayOf(FloatArray(11))
        interpreter.run(accelerometerData.convertToByteBuffer(), output)

        var max = 0f
        var maxIdx = 0
        for (i in labelList.indices){
            if(output[0][i] > max) {
                max = output[0][i]
                maxIdx = i
            }
        }
        val label = labelList.get(maxIdx)
        Log.i("TFLite", "PREDICTION: $label")
    }
}