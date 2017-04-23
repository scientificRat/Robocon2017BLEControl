package com.scientificrat.robocon2017blecontrol;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.scientificrat.robocon2017blecontrol.widget.OnRockerChangeListener;
import com.scientificrat.robocon2017blecontrol.widget.Rocker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ControllerActivity extends AppCompatActivity {

    private static String CUSTOMIZE_BUTTON_INFO_FILE_NAME = "CustomizeButtons.info";

    // 蓝牙连接控制器
    private BluetoothConnectionController bluetoothConnectionController
            = BluetoothConnectionController.getInstance();
    // 设备列表控制器(controller)
    DeviceListAdapter deviceListAdapter;
    // 紧急制动状态
    private boolean inEmergencyState = false;

    // UI Widgets
    @BindView(R.id.customize_command_button_container)
    LinearLayout customizeButtonContainer;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.device_listview)
    ListView deviceListView;

    @BindView(R.id.connect_btn)
    Button connectButton;

    @BindView(R.id.data_receive_textview)
    TextView dataReceiveTextView;

    @BindView(R.id.connection_state_text_view)
    TextView connectionStateTextView;

    @BindView(R.id.receive_panel_ascii_toggle_button)
    ToggleButton receivePanelAsciiToggleButton;

    @BindView(R.id.data_receive_scrollview)
    ScrollView dataReceiveScrollView;

    @BindView(R.id.bottom_hide_panel)
    View bottomHidePanelView;
    // 左摇杆
    @BindView(R.id.left_rocker)
    Rocker leftRocker;
    // 右摇杆
    @BindView(R.id.right_rocker)
    Rocker rightRocker;

    BottomSheetBehavior bottomHidePanel;


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

        // 异步初始化蓝牙设备
        this.initBluetoothInBackground();

        // 初始化UI
        this.initUI();

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
        java.util.Timer timer = new java.util.Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                short left_x = (short) (leftRocker.getOutputX() / leftRocker.getRange() * 1000);
                short left_y = (short) (-leftRocker.getOutputY() / leftRocker.getRange() * 1000);
                short right_x = (short) (rightRocker.getOutputX() / rightRocker.getRange() * 1000);
                short right_y = (short) (-rightRocker.getOutputY() / rightRocker.getRange() * 1000);
                if (inEmergencyState) {
                    left_x = 0;
                    left_y = 0;
                    right_x = 0;
                    right_y = 0;
                }
                ByteBuffer byteBuffer = ByteBuffer.allocate(13);
                //小端序
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.putShort((short) 0x0d0a);
                byteBuffer.put((byte) '0');
                byteBuffer.putShort(left_x);
                byteBuffer.putShort(left_y);
                byteBuffer.putShort(right_x);
                byteBuffer.putShort(right_y);
                byteBuffer.putShort((short) 0x0a0d);
                sendBytes(byteBuffer.array());
            }
        };
        // 从现在起20ms 发送一次指令
        timer.schedule(timerTask, 1, 100);


    }

    /**
     * 初始化UI
     */
    private void initUI() {
        // 获取所有ui控件的引用
        ButterKnife.bind(this);
        // 底部上滑菜单
        this.bottomHidePanel = BottomSheetBehavior.from(bottomHidePanelView);
        // 构建所有自定义发送按钮
        this.constructCustomizedButton();
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
        for (int i = 0; i < 7; i++) {
            info = new CustomizableCommandButton.CustomizableInfo();
            info.setButtonText("位置" + i);
            addCustomizeCommandButton(info);
        }
    }


    /**
     * 添加自定义发送按钮动作
     */
    @OnClick(R.id.button_add_command_button)
    public void addNewCustomizeCommandButton() {
        // vibrate
        AppVibrator.vibrateShort(this);
        addCustomizeCommandButton(null);
    }

    /**
     * 显示底部隐藏面板
     */
    @OnClick(R.id.show_hidden_panel_btn)
    public void showBottomHidePanel() {
        AppVibrator.vibrateShort(ControllerActivity.this);
        bottomHidePanel.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * 退出底部隐藏面板
     */
    @OnClick(R.id.exit_hidden_panel_btn)
    public void exitBottomHidePanel() {
        AppVibrator.vibrateShort(ControllerActivity.this);
        bottomHidePanel.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /**
     * 打开右边抽屉菜单相应动作
     */
    @OnClick(R.id.bluetooth_setting_btn)
    public void openRightDrawer() {
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
    @OnClick(R.id.connect_btn)
    public void connectButtonOnClick() {
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

    @OnClick(R.id.clear_receive_btn)
    public void clearReceive() {
        dataReceiveTextView.setText("");
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
                        if (receivePanelAsciiToggleButton.isChecked()) {
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
        // Save
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
