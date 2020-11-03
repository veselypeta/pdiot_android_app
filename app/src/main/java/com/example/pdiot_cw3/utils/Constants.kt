package com.example.pdiot_cw3.utils

object Constants
{
    const val PREFERENCES_FILE = "com.specknet.pdiotapp.PREFERENCE_FILE"
    const val REQUEST_CODE_PERMISSIONS = 4
    const val RESPECK_VERSION = "respeck_version"
    const val RESPECK_MAC_ADDRESS_PREF = "respeck_id_pref"
    const val RESPECK_CHARACTERISTIC_UUID = "00001524-1212-efde-1523-785feabcd125"

    // Status Broadcast Constant
    const val ACTION_RESPECK_CONNECTED = "com.example.pdiot_cw3.RESPECK_CONNECTED"
    const val ACTION_RESPECK_DISCONNECTED = "om.example.pdiot_cw3.RESPECK_DISCONNECTED"

    // Accel data broadcast
    const val ACTION_INNER_RESPECK_BROADCAST = "com.example.pdiot_cw3.RESPECK_BROADCAST"
    const val EXTRA_RESPECK_LIVE_X = "respeck_x"
    const val EXTRA_RESPECK_LIVE_Y = "respeck_y"
    const val EXTRA_RESPECK_LIVE_Z = "respeck_z"

    // Thingy Constants
    const val THINGY_MAC_ADDRESS_PREF = "thingy_id_pref"



    // Notifications Constants
    const val ACTION_DISCONNECT = "DISCONNECT_ACTION"


    // TFLite constnat
    const val MODEL_PATH = "gyroscope_model.tflite"
    const val LABEL_PATH = "labels.txt"
}
