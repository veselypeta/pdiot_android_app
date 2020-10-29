package com.example.pdiot_cw3.common

import android.app.ActivityManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.pdiot_cw3.bluetooth.BluetoothService
import com.example.pdiot_cw3.utils.Constants
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or

object Utils {

    private var last_seq_number = -1

    fun processRESpeckPacket(values: ByteArray , respeckVersion: Int, bltService: BluetoothService) {
        if (respeckVersion == 5) {
            val time_array: ByteArray = byteArrayOf(values[0], values[1], values[2], values[3])
            // and try ByteBuffer:
            val buffer: ByteBuffer = ByteBuffer.wrap(time_array).order(ByteOrder.BIG_ENDIAN)
            buffer.position(0);
            val uncorrectedRESpeckTimestamp = buffer.int.toLong() and 0xffffffffL

            val newRESpeckTimestamp = uncorrectedRESpeckTimestamp * 197 * 1000 / 32768;
        } else if (respeckVersion == 6) {
            //get the respeck timestamp
            val buffer: ByteBuffer = ByteBuffer.wrap(values).order(ByteOrder.BIG_ENDIAN)
            buffer.position(0)

            val uncorrectedRESpeckTimestamp = buffer.int.toLong() and 0xffffffffL
            val newRESpeckTimestamp = uncorrectedRESpeckTimestamp * 197 * 1000 / 32768
            Log.i("RESpeckPacketHandler", "Respeck timestamp (ms): $newRESpeckTimestamp")

            // get the packet sequence number.
            // This counts from zero when the respeck is reset and is a uint32 value,
            // so we'll all be long dead by the time it wraps!

            val seqNumber = (buffer.short.toInt()) and 0xffff
            Log.i("RESpeckPacketHandler", "Respeck seq number: $seqNumber")

            if (last_seq_number >= 0 && seqNumber - last_seq_number != 1) {
                // have we just wrapped?
                if (seqNumber == 0 && last_seq_number == 65535) {
                    Log.w("RESpeckPacketHandler", "Respeck seq number wrapped");
                } else {
                    Log.w(
                        "RESpeckPacketHandler",
                        "Unexpected respeck seq number. Expected: ${last_seq_number + 1}, received: $seqNumber"
                    )
                }
            }
            last_seq_number = seqNumber

            // Read battery level and charging status
            val battLevel = values[6];
            Log.i("RESpeckPacketHandler", "Respeck battery level: $battLevel %");

            var chargingStatus = false
            if (values[7] == 0x01.toByte()) {
                chargingStatus = true
            }
            Log.i("RESpeckPacketHandler", "Respeck charging?: $chargingStatus")
        }

        for (i in 8 until values.size step 6) {
            val x = combineAccelerationBytes(values[i + 0], values[i + 1])
            val y = combineAccelerationBytes(values[i + 2], values[i + 3])
            val z = combineAccelerationBytes(values[i + 4], values[i + 5])
//
            Log.i("Debug", "(x = $x, y = $y, z = $z)")

            val liveDataIntent = Intent(Constants.ACTION_INNER_RESPECK_BROADCAST);
            liveDataIntent.putExtra(Constants.EXTRA_RESPECK_LIVE_X, x)
            liveDataIntent.putExtra(Constants.EXTRA_RESPECK_LIVE_Y, y)
            liveDataIntent.putExtra(Constants.EXTRA_RESPECK_LIVE_Z, z)
            bltService.sendBroadcast(liveDataIntent)
        }
    }

    private fun combineAccelerationBytes(upper: Byte, lower: Byte): Float{
        val unsignedLower = (lower and 0xFF.toByte()).toShort()
        val value = (upper.toInt() shl 8).toShort() or unsignedLower
        return value / 16384.0f;
    }

    fun getBluetoothDevice(context: Context, address: String): BluetoothDevice? {
        val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager;
        val ba = bm.adapter;

        if (ba != null) {
            try {
                return ba.getRemoteDevice(address)
            } catch (e : Exception) {
                return null
            }
        }
        return null;
    }

    fun isServiceRunning(serviceClass: Class<Any>, context: Context): Boolean{
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for(service in manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.name == service.service.className){
                return true
            }
        }
        return false
    }
}