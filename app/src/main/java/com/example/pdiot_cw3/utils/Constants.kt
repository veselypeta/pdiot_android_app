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

    const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
    const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
    const val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
    const val ACTION_THINGY_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
    const val EXTRA_THINGY_DATA_ZERO = "com.example.bluetooth.le.EXTRA_THINGY_DATA_ZERO"
    const val EXTRA_THINGY_DATA_ONE = "com.example.bluetooth.le.EXTRA_THINGY_DATA_ONE"
    const val EXTRA_THINGY_DATA_TWO = "com.example.bluetooth.le.EXTRA_THINGY_DATA_TWO"


    const val THINGY_SERVICE_UUID = "0000fe40-cc7a-482a-984a-7f2ed5b3e58f"
    const val THINGY_CHARACTERISITC_UUID = "0000fe41-8e22-4541-9d4c-21edae82ed19"
    const val THINGY_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805F9B34FB"


    // Notifications Constants
    const val ACTION_DISCONNECT = "DISCONNECT_ACTION"


    // TFLite constnat
    const val MODEL_PATH = "gyroscope_model.tflite"
    const val LABEL_PATH = "labels.txt"
}
