package com.scientificrat.robocon2017blecontrol.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by huangzhengyue on 2016/10/27.
 */

public class BluetoothConnection extends Thread {
    // 常量
    private static final String DEFAULT_DEVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    // singleton 单例模式
    private static BluetoothConnection instance;

    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;

    private OutputStream outputStream;
    private InputStream inputStream;

    // 为了效率只设定了一个onDataInListener
    private OnDataInListener onDataInListener = null;

    private OnConnectionBreakListener onConnectionBreakListener = null;

    private OnConnectListener onConnectListener = null;

    /**
     * 创建蓝牙连接（阻塞方法）
     *
     * @param bluetoothDevice 待连接蓝牙设备
     * @return 连接实例
     */
    public static synchronized BluetoothConnection createInstance(BluetoothDevice bluetoothDevice) {
        // 每次调用直接创建新的连接，因为可能连接多次到不同对象
        BluetoothConnection connection = new BluetoothConnection(bluetoothDevice);
        try {
            connection.connect();
            instance = connection;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return instance;
    }

    /**
     * 创建蓝牙连接（非阻塞方法）
     *
     * @param bluetoothDevice           待连接蓝牙设备
     * @param onConnectListener         连接事件监听器
     * @param onDataInListener          数据输入监听器
     * @param onConnectionBreakListener 连接中断监听器
     */
    public static synchronized void createInstance(BluetoothDevice bluetoothDevice,
                                                   OnConnectListener onConnectListener,
                                                   OnDataInListener onDataInListener,
                                                   OnConnectionBreakListener onConnectionBreakListener) {
        // 每次调用直接创建新的连接，因为可能连接多次到不同对象
        final BluetoothConnection connection = new BluetoothConnection(bluetoothDevice);
        connection.onConnectListener = onConnectListener;
        connection.onConnectionBreakListener = onConnectionBreakListener;
        connection.onDataInListener = onDataInListener;
        // 异步连接, 避免阻塞
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connection.connect();
                    instance = connection;
                    if (connection.onConnectListener != null) {
                        connection.onConnectListener.onConnectSuccess();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (connection.onConnectListener != null) {
                        connection.onConnectListener.onConnectFail();
                    }
                }
            }
        }).start();
    }


    /**
     * 返回蓝牙连接实例（可能为空）
     *
     * @return BluetoothConnection (may be null)
     */
    public static BluetoothConnection getInstance() {
        return instance;
    }


    /**
     * 发送数据接口
     *
     * @param data 数据
     * @throws IOException 异常
     */
    public synchronized void sendRawData(byte[] data) throws IOException {
        if (data == null) {
            return;
        }
        this.outputStream.write(data);
    }

    /**
     * 关闭接口
     */
    public void cancel() {
        try {
            this.bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            instance = null;
        }
    }

    //setters
    public void setOnDataInListener(OnDataInListener onDataInListener) {
        this.onDataInListener = onDataInListener;
    }

    public void setOnConnectionBreakListener(OnConnectionBreakListener onConnectionBreakListener) {
        this.onConnectionBreakListener = onConnectionBreakListener;
    }

    //getters
    public BluetoothDevice getConnectedBluetoothDevice() {
        return bluetoothDevice;
    }


    @Override
    public void run() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                int bytes = inputStream.read(buffer);
                //listener method callback
                if (onDataInListener != null) {
                    onDataInListener.onDataIn(buffer, bytes);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // notify listeners
            if (onConnectionBreakListener != null) {
                onConnectionBreakListener.onConnectionBreak();
            }
        }
    }


    /**
     * 私有化构造函数
     *
     * @param bluetoothDevice 待连接蓝牙设备
     */
    private BluetoothConnection(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
        try {
            bluetoothSocket = this.bluetoothDevice
                    .createRfcommSocketToServiceRecord(UUID.fromString(DEFAULT_DEVICE_UUID));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("construction", "BluetoothConnection: success");
    }

    /**
     * 连接(阻塞方法)
     *
     * @throws IOException 连接异常
     */
    private void connect() throws IOException {
        Log.d("connect:", "start to connect");
        // block until connect
        bluetoothSocket.connect();
        this.outputStream = bluetoothSocket.getOutputStream();
        this.inputStream = bluetoothSocket.getInputStream();
        Log.d("connect:", "connected");
    }
}
