package com.example.pdiot_cw3.common

import java.nio.ByteBuffer
import java.nio.ByteOrder

class AccelerometerData(private val length: Int, private val width: Int =3) {

    // create the array and buffer to hold the data - buffer is used when needing to pass to TFLiteModel
    private val data: Array<Array<Float>> = Array (length){ Array (width){0.0f}}
    private var dataByteBuffer: ByteBuffer = ByteBuffer.allocateDirect(4 * length * width)
        .apply { order(ByteOrder.nativeOrder()) }

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
    }

    fun convertToByteBuffer(): ByteBuffer {
        dataByteBuffer.position(0)

        for (n in 0 until length){
            dataByteBuffer.putFloat(data[n][0])
            dataByteBuffer.putFloat(data[n][1])
            dataByteBuffer.putFloat(data[n][2])
        }
        return dataByteBuffer
    }
}