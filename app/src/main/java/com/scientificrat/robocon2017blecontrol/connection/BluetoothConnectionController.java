package com.scientificrat.robocon2017blecontrol.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED;

/**
 * Created by huangzhengyue on 2016/10/27.
 */
public class BluetoothConnectionController implements ConnectionController {
    // Constants 常量
    private static final String DEFAULT_DEVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final int DEFAULT_RECEIVE_BUFFER_SIZE = 1024;

    public static final int STATE_CLOSED = 0;
    public static final int STATE_OPEN_AND_DISCONNECTED = 1;
    public static final int STATE_CONNECTED = 2;


    // Singleton 单例模式
    private static BluetoothConnectionController instance = new BluetoothConnectionController();

    private BluetoothLeScanner bluetoothLeScanner;

    private Context appContext = null;

    private BluetoothAdapter bluetoothAdapter;

    // The connected device
    private BluetoothDevice bluetoothDevice;
    // The bluetoothSocket
    private BluetoothSocket bluetoothSocket;

    // Input and output stream
    private OutputStream outputStream;
    private InputStream inputStream;

    private int receiveBufferSize = DEFAULT_RECEIVE_BUFFER_SIZE;

    // Connection state
    private int connectionState = STATE_CLOSED;

    // Listeners
    private OnDataInListener onDataInListener = null;

    private OnConnectionBreakListener onConnectionBreakListener = null;

    private OnBluetoothDeviceFoundListener onBluetoothDeviceFoundListener = null;


    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (onBluetoothDeviceFoundListener != null && result.getDevice() != null) {
//                Log.e("fuck", "" + result.getDevice().getAddress() + result.getDevice().getName());
                onBluetoothDeviceFoundListener.onBlueToothDeviceFound(result.getDevice());
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }
    };


    /**
     * 这个将被注册到系统广播
     * Create a BroadcastReceiver for BluetoothDevice.ACTION_FOUND
     */
    private final BroadcastReceiver blueToothDeviceFoundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (onBluetoothDeviceFoundListener != null && device != null) {
                    Log.e("fuck", "" + device.getAddress() + device.getName());
                    onBluetoothDeviceFoundListener.onBlueToothDeviceFound(device);
                }
            }
        }
    };


    // Data receiveThread
    private Thread dataReceiveThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    byte[] buffer = new byte[receiveBufferSize];
                    int bytes = inputStream.read(buffer);
                    // listener method callback
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
                // Change the connection state
                BluetoothConnectionController.this.connectionState = STATE_OPEN_AND_DISCONNECTED;
                // notify listeners
                if (onConnectionBreakListener != null) {
                    onConnectionBreakListener.onConnectionBreak();
                }
            }
        }
    });


    /**
     * 返回蓝牙连接控制器实例
     *
     * @return BluetoothConnectionController
     */
    public static BluetoothConnectionController getInstance() {
        return instance;
    }


    /**
     * 开启蓝牙设备
     *
     * @return 成功／失败
     */
    public boolean openBluetooth(Context context) {
        this.appContext = context.getApplicationContext();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            return false;
        }
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        if (!bluetoothAdapter.isEnabled()) {
            return false;
        }
//        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        this.connectionState = STATE_OPEN_AND_DISCONNECTED;
        return true;
    }

    /**
     * 扫描设备
     *
     * @param onBluetoothDeviceFoundListener 发现设备回调
     */
    public boolean scan(final OnBluetoothDeviceFoundListener onBluetoothDeviceFoundListener) {
        if (this.connectionState == STATE_CLOSED) {
            return false;
        }
        if (this.bluetoothAdapter.isDiscovering()) {
            this.bluetoothAdapter.cancelDiscovery();
        }
        this.bluetoothAdapter.startDiscovery();
        this.onBluetoothDeviceFoundListener = onBluetoothDeviceFoundListener;
        // 扫描设备
//        bluetoothLeScanner.startScan(scanCallback);
        appContext.registerReceiver(blueToothDeviceFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        return true;
    }

    /**
     * 停止扫描设备
     */
    public void stopScanning() {
        if (this.connectionState == STATE_CLOSED) {
            return;
        }
        try {
            appContext.unregisterReceiver(blueToothDeviceFoundReceiver);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        bluetoothAdapter.cancelDiscovery();
//        bluetoothLeScanner.stopScan(scanCallback);
    }


    /**
     * 连接设备 (阻塞方法)
     *
     * @param bluetoothDevice 待连接的设备
     * @throws IOException 连接异常
     */
    public void connect(BluetoothDevice bluetoothDevice) throws IOException {
        this.bluetoothDevice = bluetoothDevice;
        this.bluetoothSocket = this.bluetoothDevice
                .createRfcommSocketToServiceRecord(UUID.fromString(DEFAULT_DEVICE_UUID));
        Log.d("BLE connect:", "start to connect");
        // block until connect
        bluetoothSocket.connect();
        this.outputStream = bluetoothSocket.getOutputStream();
        this.inputStream = bluetoothSocket.getInputStream();
        // Start to receive data
        this.dataReceiveThread.start();
        this.connectionState = STATE_CONNECTED;
        Log.d("BLE connect:", "connected");
    }


    /**
     * 连接设备 (非阻塞方法)
     *
     * @param bluetoothDevice   待连接的设备
     * @param onConnectListener 连接成功／失败回调
     */
    public void connectInBackground(final BluetoothDevice bluetoothDevice, final OnConnectListener onConnectListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connect(bluetoothDevice);
                    if (onConnectListener != null) {
                        onConnectListener.onConnectSuccess();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (onConnectListener != null) {
                        onConnectListener.onConnectFail();
                    }
                }
            }
        }).start();
    }

    /**
     * 发送数据接口
     *
     * @param data 数据
     * @throws IOException 发送异常
     */
    @Override
    public synchronized void sendRawData(byte[] data) throws IOException {
        if (data == null || this.connectionState != STATE_CONNECTED) {
            return;
        }
        this.outputStream.write(data);
        this.outputStream.flush();
    }

    /**
     * 关闭连接
     */
    @Override
    public void cancel() {
        try {
            this.bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.connectionState = STATE_OPEN_AND_DISCONNECTED;
        }
    }


    //setters
    public void setOnDataInListener(OnDataInListener onDataInListener) {
        this.onDataInListener = onDataInListener;
    }

    public void setOnConnectionBreakListener(OnConnectionBreakListener onConnectionBreakListener) {
        this.onConnectionBreakListener = onConnectionBreakListener;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        if (receiveBufferSize <= 0) {
            this.receiveBufferSize = DEFAULT_RECEIVE_BUFFER_SIZE;
        } else {
            this.receiveBufferSize = receiveBufferSize;
        }

    }

    // getters
    public BluetoothDevice getConnectedBluetoothDevice() {
        return bluetoothDevice;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public int getConnectionState() {
        return connectionState;
    }

    public boolean isOpen() {
        return connectionState != STATE_CLOSED;
    }


    public boolean isConnected() {
        return connectionState == STATE_CONNECTED;
    }

    /**
     * 私有化构造函数
     */
    private BluetoothConnectionController() {
    }

}
