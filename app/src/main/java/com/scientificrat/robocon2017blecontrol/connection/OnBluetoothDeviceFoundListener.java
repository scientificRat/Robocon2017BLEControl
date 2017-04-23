package com.scientificrat.robocon2017blecontrol.connection;

import android.bluetooth.BluetoothDevice;

/**
 * Created by huangzhengyue on 2017/4/23.
 */

public interface OnBluetoothDeviceFoundListener {
    void onBlueToothDeviceFound(BluetoothDevice bluetoothDevice);
}
