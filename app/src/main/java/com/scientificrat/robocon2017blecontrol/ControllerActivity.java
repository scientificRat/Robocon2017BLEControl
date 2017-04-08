package com.scientificrat.robocon2017blecontrol;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.scientificrat.robocon2017blecontrol.adapter.DeviceListAdapter;
import com.scientificrat.robocon2017blecontrol.connection.BluetoothConnection;
import com.scientificrat.robocon2017blecontrol.connection.OnConnectListener;
import com.scientificrat.robocon2017blecontrol.connection.OnConnectionBreakListener;
import com.scientificrat.robocon2017blecontrol.connection.OnDataInListener;
import com.scientificrat.robocon2017blecontrol.util.AppVibrator;
import com.scientificrat.robocon2017blecontrol.util.HexHelper;
import com.scientificrat.robocon2017blecontrol.util.MesurementUtility;
import com.scientificrat.robocon2017blecontrol.widget.CustomizableCommandButton;

import org.w3c.dom.Text;

public class ControllerActivity extends AppCompatActivity {

    // 成功开启蓝牙后，onActivityResult 收到的code
    private final int BLUETOOTH_REQUEST_ENABLE_BT = 2;
    // 成功开启蓝牙标志
    private boolean bluetoothOpen = false;
    // 成功连接设备
    private boolean bluetoothDeviceConnected = false;
    // 蓝牙适配器（controller）
    private BluetoothAdapter bluetoothAdapter;
    // 设备列表控制器(controller)
    DeviceListAdapter deviceListAdapter;

    /**
     * 这个将被注册到系统广播
     * Create a BroadcastReceiver for BluetoothDevice.ACTION_FOUND
     */
    private final BroadcastReceiver mBlueToothDeviceFoundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add to the device to my listView
                ControllerActivity.this.deviceListAdapter.addDevice(device);
            }
        }
    };

    // UI Widgets
    private LinearLayout customizeButtonContainer;
    private DrawerLayout drawerLayout;
    private ListView deviceListView;
    private ImageButton bluetoothSettingButton;
    private Button connectButton;
    private LinearLayout leftDrawer;
    private TextView dataReceiveTextView;
    private TextView connectionStateTextView;


    /**
     * 初始化 views (UI Widgets)
     */
    private void initViews() {
        deviceListView = (ListView) findViewById(R.id.device_list);
        customizeButtonContainer = (LinearLayout) findViewById(R.id.customize_command_button_container);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        bluetoothSettingButton = (ImageButton) findViewById(R.id.bluetooth_setsting);
        connectButton = (Button) findViewById(R.id.connect);
        leftDrawer = (LinearLayout) findViewById(R.id.left_drawer);
        dataReceiveTextView = (TextView) findViewById(R.id.data_receive);
        connectionStateTextView = (TextView) findViewById(R.id.text_view_connection_state);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 加载xml视图
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        // 初始化蓝牙设备
        initBluetooth();

        // 初始化view
        initViews();

        // 设备列表绑定适配器
        deviceListAdapter = new DeviceListAdapter(this);
        deviceListView.setAdapter(deviceListAdapter);

        // 设备列表响应
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deviceListAdapter.setSelectedPosition(position);
                deviceListAdapter.notifyDataSetInvalidated();
            }
        });

        // 禁止右侧直接滑入
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        // 滑入菜单绑定事件
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                drawerView.setClickable(true);
                if (drawerView.getId() == R.id.right_drawer) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
                    startDiscovery();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (drawerView.getId() == R.id.right_drawer) {
                    stopDiscovery();
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });


        // 右上角蓝牙设置button 绑定事件
        bluetoothSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRightDrawer();
            }
        });


        // 连接按键绑定事件
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectButtonOnClick();
            }
        });


    }


    /**
     * 开启蓝牙外部程序返回会调用这个函数
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 如果蓝牙开启成功, 设置当前状态为开启
        if (requestCode == BLUETOOTH_REQUEST_ENABLE_BT) {
            // FIXME: 2016/10/28 有可能返回失败，还没有处理resultCode
            this.bluetoothOpen = true;
        }
    }


    /**
     * 添加命令按钮动作
     *
     * @param view 当前点击的视图
     */
    public void addCommandButton(View view) {
        //vibrate
        AppVibrator.vibrateShort(this);
        CustomizableCommandButton button = new CustomizableCommandButton(this);
        button.setBackground(getDrawable(R.drawable.blue_command_button));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 1.0f;
        params.setMarginEnd(MesurementUtility.convertDPtoPX(getResources(), 8));
        customizeButtonContainer.addView(button, params);
    }


    /**
     * 打开右边抽屉菜单相应动作
     */
    private void openRightDrawer() {
        // 震动
        AppVibrator.vibrateShort(ControllerActivity.this);
        if (!bluetoothOpen) {
            Toast.makeText(ControllerActivity.this,
                    "正在初始化蓝牙设备,请等待",
                    Toast.LENGTH_SHORT).show();
            initBluetooth();
            return;
        }
        drawerLayout.openDrawer(Gravity.RIGHT);
    }


    /**
     * 连接按钮对应动作
     */
    private void connectButtonOnClick() {
        if (!bluetoothOpen) {
            Toast.makeText(ControllerActivity.this,
                    "正在连接设备,请稍后再试",
                    Toast.LENGTH_SHORT).show();
            initBluetooth();
            return;
        }
        // 根据状态执行连接／断开连接操作
        if (bluetoothDeviceConnected) {
            // 检查是否连接还在(此时状态是已连接)
            BluetoothConnection bluetoothConnection = BluetoothConnection.getInstance();
            if (bluetoothConnection == null) {
                bluetoothDeviceConnected = false;
                connectionStateTextView.setText("未连接");
                return;
            }
            // 断开连接
            bluetoothConnection.cancel();
            connectButton.setText("连接");
            connectionStateTextView.setText("未连接");
            deviceListView.setEnabled(true);
            bluetoothDeviceConnected = false;

        } else {
            BluetoothDevice selectedDevice = deviceListAdapter.getSelectedDevice();
            if (selectedDevice != null) {
                // Change UI
                connectButton.setText("WAIT");
                connectButton.setEnabled(false);
                deviceListView.setEnabled(false);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, Gravity.RIGHT);

                // 连接设备
                BluetoothConnection.createInstance(selectedDevice, new OnConnectListener() {
                    @Override
                    public void onConnectSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 连接成功
                                bluetoothDeviceConnected = true;
                                connectionStateTextView.setText("已连接");
                                deviceListView.setEnabled(false);
                                connectButton.setText("断开");
                                connectButton.setEnabled(true);
                                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
                                BluetoothConnection bluetoothConnection = BluetoothConnection.getInstance();
                                if(bluetoothConnection!=null){
                                    bluetoothConnection.start();
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnectFail() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 连接失败
                                Toast.makeText(ControllerActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                                connectionStateTextView.setText("未连接");
                                bluetoothDeviceConnected = false;
                                deviceListView.setEnabled(true);
                                connectButton.setText("连接");
                                connectButton.setEnabled(true);
                                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                            }
                        });

                    }
                }, new OnDataInListener() {
                    @Override
                    public void onDataIn(final byte[] data, int size) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dataReceiveTextView.setText(HexHelper.byte2hexString(data));
                            }
                        });
                    }
                }, new OnConnectionBreakListener() {
                    @Override
                    public void onConnectionBreak() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ControllerActivity.this, "连接中断", Toast.LENGTH_SHORT).show();
                                connectionStateTextView.setText("未连接");
                                bluetoothDeviceConnected = false;
                                deviceListView.setEnabled(true);
                                connectButton.setText("连接");
                                connectButton.setEnabled(true);
                                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                            }
                        });
                    }
                });

            } else {
                Toast.makeText(ControllerActivity.this,
                        "请先选择设备",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * 初始化蓝牙设备
     */
    private boolean initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth then alert
            Toast.makeText(getApplicationContext(), "你的手机不支持蓝牙TAT～", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_ENABLE_BT);
            return false;
        } else {
            this.bluetoothOpen = true;
        }

        // 请求定位权限，垃圾安卓在6.0 不开启这个权限不能扫描周边蓝牙设备
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        return true;
    }

    /**
     * 开始扫描蓝牙设备
     */
    private void startDiscovery() {
        // 如果已经连接设备则不扫描
        if (bluetoothDeviceConnected) {
            return;
        }
        deviceListAdapter.clear();
        // 扫描设备
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 开启扫描,如果已经开启，先关闭
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
        // 注册 BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBlueToothDeviceFoundReceiver, filter);
    }

    /**
     * 停止扫描蓝牙设备
     */
    private void stopDiscovery() {
        // 停止扫描蓝牙设备
        try {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            unregisterReceiver(mBlueToothDeviceFoundReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
