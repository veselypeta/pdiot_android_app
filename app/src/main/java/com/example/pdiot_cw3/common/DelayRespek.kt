package com.example.pdiot_cw3.common

import java.util.concurrent.Delayed
import java.util.concurrent.TimeUnit

class DelayRespek(private var data: RespekData, delayTime: Long): Delayed {
    private var time = System.currentTimeMillis() + delayTime

    fun getData(): RespekData {return data}

    override fun compareTo(other: Delayed?): Int {
        if (this.time < (other as DelayRespek).time) {
            return -1
        }
        if (this.time > (other as DelayRespek).time) {
            return 1
        }
        return 0
    }

    override fun getDelay(unit: TimeUnit?): Long {
        val diff = time - System.currentTimeMillis()
        return unit!!.convert(diff, TimeUnit.MILLISECONDS)
    }

    override fun toString(): String {
        return "\n{data= $data, time= $time}"
    }
}