package com.scientificrat.robocon2017blecontrol;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.scientificrat.robocon2017blecontrol.adapter.DeviceListAdapter;
import com.scientificrat.robocon2017blecontrol.connection.BluetoothConnectionController;
import com.scientificrat.robocon2017blecontrol.connection.OnBluetoothDeviceFoundListener;
import com.scientificrat.robocon2017blecontrol.connection.OnConnectListener;
import com.scientificrat.robocon2017blecontrol.connection.OnConnectionBreakListener;
import com.scientificrat.robocon2017blecontrol.connection.OnDataInListener;
import com.scientificrat.robocon2017blecontrol.util.AppVibrator;
import com.scientificrat.robocon2017blecontrol.util.HexHelper;
import com.scientificrat.robocon2017blecontrol.util.MeasurementUtility;
import com.scientificrat.robocon2017blecontrol.widget.CustomizableCommandButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ControllerActivity extends AppCompatActivity {

    private static String CUSTOMIZE_BUTTON_INFO_FILE_NAME = "CustomizeButtons.info";

    // 蓝牙连接控制器
    private BluetoothConnectionController bluetoothConnectionController = BluetoothConnectionController.getInstance();
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
    private ToggleButton receiveDisplayToggleButton;
    private Button clearReceiveButton;
    private ScrollView dataReceiveScrollView;
    private Button rotateForwardButton;
    private Button rotateBackwardButton;
    private Button rotateLeftButton;
    private Button rotateRightButton;
    private Button launchButton;
    private BottomSheetBehavior bottomHidePanel;
    private ImageButton showHiddenPanelButton;
    private Button exitHiddenPanelButton;


    /**
     * 初始化 views (UI Widgets)
     */
    private void getViewsReferences() {
        deviceListView = (ListView) findViewById(R.id.device_list);
        customizeButtonContainer = (LinearLayout) findViewById(R.id.customize_command_button_container);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        bluetoothSettingButton = (ImageButton) findViewById(R.id.bluetooth_setting);
        connectButton = (Button) findViewById(R.id.connect);
        leftDrawer = (LinearLayout) findViewById(R.id.left_drawer);
        dataReceiveTextView = (TextView) findViewById(R.id.data_receive);
        connectionStateTextView = (TextView) findViewById(R.id.text_view_connection_state);
        receiveDisplayToggleButton = (ToggleButton) findViewById(R.id.toggle_ascii);
        clearReceiveButton = (Button) findViewById(R.id.clear_receive);
        dataReceiveScrollView = (ScrollView) findViewById(R.id.data_receive_scrollview);

        rotateForwardButton = (Button) findViewById(R.id.left_up);
        rotateBackwardButton = (Button) findViewById(R.id.left_down);
        rotateLeftButton = (Button) findViewById(R.id.left_left);
        rotateRightButton = (Button) findViewById(R.id.left_right);
        launchButton = (Button) findViewById(R.id.launch);
        bottomHidePanel = BottomSheetBehavior.from(findViewById(R.id.hide_panel));
        showHiddenPanelButton = (ImageButton) findViewById(R.id.show_hidden_panel);
        exitHiddenPanelButton = (Button) findViewById(R.id.exit_hidden_panel_button);

    }

    /**
     * OnCreate 方法
     *
     * @param savedInstanceState 传入的状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 从layout.xml 构建界面
        super.onCreate(savedInstanceState);
        // 不休眠
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_controller);

        // 获取所有view的引用
        this.getViewsReferences();

        // 异步初始化蓝牙设备
        this.initBluetoothInBackground();

        // 构建所有自定义发送按钮
        this.constructCustomizedButton();

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

        // 底部滑入控制面板
        bottomHidePanel.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // disable dragging
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomHidePanel.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        showHiddenPanelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomHidePanel.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        exitHiddenPanelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomHidePanel.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

        // 清除接收
        clearReceiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataReceiveTextView.setText("");
            }
        });

        // 下方黄色命令按钮
        rotateForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBytes("\n\r1a\r\n".getBytes());
            }
        });

        rotateBackwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBytes("\n\r1b\r\n".getBytes());
            }
        });

        rotateLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBytes("\n\r1c\r\n".getBytes());
            }
        });

        rotateRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBytes("\n\r1d\r\n".getBytes());
            }
        });

        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBytes("\n\r1k\r\n".getBytes());
            }
        });

    }


    /**
     * 添加自定义发送按钮动作
     *
     * @param view 当前点击的视图
     */
    public void addNewCustomizeCommandButton(View view) {
        // vibrate
        AppVibrator.vibrateShort(this);
        addCustomizeCommandButton(null);
    }

    private void sendBytes(byte[] buffer) {
        try {
            if (!bluetoothConnectionController.isConnected()) {
                Toast.makeText(ControllerActivity.this, "蓝牙未连接", Toast.LENGTH_SHORT).show();
            } else {
                bluetoothConnectionController.sendRawData(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加自定义发送按钮动作
     */
    private void addCustomizeCommandButton(CustomizableCommandButton.CustomizableInfo info) {
        CustomizableCommandButton button;
        if (info == null) {
            button = new CustomizableCommandButton(this);
        } else {
            button = new CustomizableCommandButton(this, info);
        }
        button.setBackground(getDrawable(R.drawable.blue_command_button));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 1.0f;
        params.setMarginEnd(MeasurementUtility.convertDPtoPX(getResources(), 8));
        customizeButtonContainer.addView(button, params);
    }

    /**
     * 构建所有自定义发送按钮
     */
    private void constructCustomizedButton() {
        File inputFile = new File(getCacheDir(), CUSTOMIZE_BUTTON_INFO_FILE_NAME);
        if (!inputFile.exists()) {
            constructCustomizedButtonInDefault();
            return;
        }
        ArrayList<CustomizableCommandButton.CustomizableInfo> infos = null;
        // 根据文件内容创建
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(inputFile))) {
            infos = (ArrayList<CustomizableCommandButton.CustomizableInfo>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (infos == null || infos.isEmpty()) {
            constructCustomizedButtonInDefault();
        } else {
            for (CustomizableCommandButton.CustomizableInfo info : infos) {
                addCustomizeCommandButton(info);
            }
        }
    }

    private void constructCustomizedButtonInDefault() {
        // 默认构建7个按钮
        CustomizableCommandButton.CustomizableInfo info;
        Log.d("func in", "constructCustomizedButtonInDefault: in!");
        for (int i = 0; i < 7; i++) {
            info = new CustomizableCommandButton.CustomizableInfo();
            info.setButtonText("位置" + i);
            addCustomizeCommandButton(info);
        }
    }


    /**
     * 打开右边抽屉菜单相应动作
     */
    private void openRightDrawer() {
        // 震动
        AppVibrator.vibrateShort(ControllerActivity.this);
        if (!bluetoothConnectionController.isOpen()) {
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
        if (!bluetoothConnectionController.isOpen()) {
            Toast.makeText(ControllerActivity.this,
                    "正在连接设备,请稍后再试",
                    Toast.LENGTH_SHORT).show();
            initBluetooth();
            return;
        }
        // 根据状态执行连接／断开连接操作
        if (bluetoothConnectionController.isConnected()) {
            // 断开连接
            bluetoothConnectionController.cancel();
            connectButton.setText("连接");
            connectionStateTextView.setText("未连接");
            deviceListView.setEnabled(true);

        } else {
            BluetoothDevice selectedDevice = deviceListAdapter.getSelectedDevice();
            if (selectedDevice != null) {
                // Change UI
                connectButton.setText("WAIT");
                connectButton.setEnabled(false);
                deviceListView.setEnabled(false);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, Gravity.RIGHT);


                // Do connecting in background
                bluetoothConnectionController.connectInBackground(selectedDevice, new OnConnectListener() {
                    @Override
                    public void onConnectSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 连接成功
                                connectionStateTextView.setText("已连接");
                                deviceListView.setEnabled(false);
                                connectButton.setText("断开");
                                connectButton.setEnabled(true);
                                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
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
     * 后台初始化蓝牙
     */
    private void initBluetoothInBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initBluetooth();
            }
        }).start();
    }


    /**
     * 初始化蓝牙设备
     */
    private void initBluetooth() {
        if (!bluetoothConnectionController.openBluetooth()) {
            // Device does not support Bluetooth then alert
            Toast.makeText(getApplicationContext(), "无法打开蓝牙TAT～", Toast.LENGTH_LONG).show();
        }

        // set on dataInListener
        bluetoothConnectionController.setOnDataInListener(new OnDataInListener() {
            @Override
            public void onDataIn(final byte[] data, final int size) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (receiveDisplayToggleButton.isChecked()) {
                            Charset charset = Charset.forName("GBK");
                            charset.decode(ByteBuffer.wrap(data, 0, size));
                            dataReceiveTextView.append(charset.decode(ByteBuffer.wrap(data, 0, size)));
                        } else {
                            dataReceiveTextView.append(HexHelper.byte2hexString(data, size));
                        }
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                dataReceiveScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                    }
                });
            }
        });

        // set on connectionBreakListener
        bluetoothConnectionController.setOnConnectionBreakListener(new OnConnectionBreakListener() {
            @Override
            public void onConnectionBreak() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ControllerActivity.this, "连接中断", Toast.LENGTH_SHORT).show();
                        connectionStateTextView.setText("未连接");
                        deviceListView.setEnabled(true);
                        connectButton.setText("连接");
                        connectButton.setEnabled(true);
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    }
                });
            }
        });

//        // 请求定位权限，垃圾安卓在6.0 不开启这个权限不能扫描周边蓝牙设备
//        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }

    /**
     * 开始扫描蓝牙设备
     */
    private void startDiscovery() {
        // 如果已经连接设备则不扫描
        if (bluetoothConnectionController.isConnected()) {
            return;
        }
        deviceListAdapter.clear();
        // 扫描设备
        bluetoothConnectionController.scan(new OnBluetoothDeviceFoundListener() {
            @Override
            public void onBlueToothDeviceFound(BluetoothDevice bluetoothDevice) {
                deviceListAdapter.addDevice(bluetoothDevice);
            }
        });
    }

    /**
     * 停止扫描蓝牙设备
     */
    private void stopDiscovery() {
        bluetoothConnectionController.stopScanning();
    }

    /**
     * 退出时保存状态
     */
    @Override
    protected void onDestroy() {
        // Save customize button state to file
        // Gather information
        ArrayList<CustomizableCommandButton.CustomizableInfo> customizableInfoArrayList = new ArrayList<>();
        int buttonCount = customizeButtonContainer.getChildCount();
        for (int i = 0; i < buttonCount; i++) {
            CustomizableCommandButton commandButton = (CustomizableCommandButton) customizeButtonContainer.getChildAt(i);
            if (commandButton.getVisibility() == View.GONE) continue;
            customizableInfoArrayList.add(commandButton.getCustomizableInfo());
        }
        // save
        File cacheDir = getCacheDir();
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File outputFile = new File(cacheDir, CUSTOMIZE_BUTTON_INFO_FILE_NAME);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(outputFile))) {
            objectOutputStream.writeObject(customizableInfoArrayList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * 返回键进入后台程序
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
