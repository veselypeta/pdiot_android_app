package com.example.pdiot_cw3.common

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AccelerometerData {

    val length = 50
    val width = 3
    private val data: Array<Array<Float>> = Array (length){ Array (width){0.0f}}

    var counter = 0

    fun pushNewData(x: Float, y: Float, z: Float) {
        for (i in 0 until length - 1) {
            for (j in 0 until width) {
                data[i + 1][j] = data[i][j]
            }
        }
        data[0][0] = x
        data[0][1] = y
        data[0][2] = z

        counter += 1
        if((counter % 50) == 0){
            for(i in 0 until length){
                Log.i("Accelerometer-Data", "i = $i x [${data[i][0]}] - y[${data[i][1]}] - z[${data[i][2]}]")
            }
        }
    }

    fun convertToByteBuffer(): ByteBuffer {
        val buffer =  ByteBuffer.allocateDirect(4 * length * width)
            .apply { order(ByteOrder.nativeOrder()) }

        for (n in 0 until length){
            buffer.putFloat(data[n][0])
            buffer.putFloat(data[n][1])
            buffer.putFloat(data[n][2])
        }
        return buffer
    }
}