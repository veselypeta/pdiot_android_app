package com.example.pdiot_cw3.common

import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.*
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.collections.ArrayList

class TFLiteModel(assetManager: AssetManager, modelPath: String, labelPath: String, private val outputClasses: Int) {

    private var interpreter: Interpreter
    private var labelList: List<String>

    init {
        interpreter = Interpreter(this.loadModelFile(assetManager, modelPath), Interpreter.Options())
        labelList = this.loadLabelList(assetManager, labelPath)
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

    fun classify(accelerometerData: AccelerometerData): Array<FloatArray>{
        val output = arrayOf(FloatArray(outputClasses))
        interpreter.run(accelerometerData.getData(), output)
        return output
    }

    fun getLabelText(predictions: Array<FloatArray>): String{
        var max = Float.MIN_VALUE
        var maxIdx = -1
        for (i in labelList.indices){
            if(predictions[0][i] > max) {
                max = predictions[0][i]
                maxIdx = i
            }
        }
        return labelList[maxIdx]
    }
}