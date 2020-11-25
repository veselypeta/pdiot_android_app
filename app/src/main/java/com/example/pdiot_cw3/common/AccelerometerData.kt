package com.example.pdiot_cw3.common

class AccelerometerData(private val length: Int, private val width: Int =3) {

    // create the array and buffer to hold the data - buffer is used when needing to pass to TFLiteModel
    private val data: Array<Array<FloatArray>> = arrayOf(Array(length){ FloatArray(width)})

    fun pushNewData(x: Float, y: Float, z: Float) {
        // move every row down one
        for (i in 0 until length - 1) {
            for (j in 0 until width) {
                data[0][i][j] = data[0][i+1][j]
            }
        }
        // add the new data to the first row
        data[0][length-1][0] = x
        data[0][length-1][1] = y
        data[0][length-1][2] = z
    }

    fun getData(): Array<Array<FloatArray>> {
        return data
    }

}