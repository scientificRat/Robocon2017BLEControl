package com.scientificrat.robocon2017blecontrol.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

import android.util.AttributeSet;
import android.support.v7.widget.*;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.scientificrat.robocon2017blecontrol.R;
import com.scientificrat.robocon2017blecontrol.connection.BluetoothConnection;
import com.scientificrat.robocon2017blecontrol.util.AppVibrator;
import com.scientificrat.robocon2017blecontrol.util.HexHelper;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by huangzhengyue on 2017/4/5.
 */

public class CustomizableCommandButton extends AppCompatButton implements Serializable {

    private final static int NORMAL_STATE = 0;
    private final static int EDITING_STATE = 1;
    private final static int ASCII_FORMAT = 0;
    private final static int HEX_FORMAT = 1;
    private int state = NORMAL_STATE;
    private String buttonText = "";
    private byte[] sendBuffer;

    private int dataFormat = ASCII_FORMAT;

    private transient MaterialDialog mInputDialog = null;


    //constructors
    public CustomizableCommandButton(Context context) {
        super(context);
        init();
    }

    public CustomizableCommandButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomizableCommandButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Do initialization, setting listeners
     */
    private void init() {
        // 为了在layout编辑器中正确显示
        if (isInEditMode()) {
            return;
        }
        // 初始化对话框
        initDialog();
        // 设置长按事件监听
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                state = EDITING_STATE;
                showInputDialog();
                //vibrate
                AppVibrator.vibrateLong(getContext());
                return false;
            }
        });
        // 设置短按事件监听
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == NORMAL_STATE) {
                    AppVibrator.vibrateShort(getContext());
                    BluetoothConnection bluetoothConnection = BluetoothConnection.getInstance();
                    if (bluetoothConnection == null) {
                        Toast.makeText(getContext(), "蓝牙未连接", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        bluetoothConnection.sendRawData(sendBuffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "发送失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void initDialog() {

        mInputDialog = new MaterialDialog.Builder(getContext())
                .backgroundColorRes(R.color.colorBack)
                .positiveText("确定")
                .customView(R.layout.dialog_edit_command_layout, false)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        state = NORMAL_STATE;
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        state = NORMAL_STATE;
                        // 设置按钮显示文本
                        EditText editTextButtonName = (EditText) dialog.findViewById(R.id.editText_button_name);
                        buttonText = editTextButtonName.getText().toString();
                        CustomizableCommandButton.this.setText(buttonText);
                        // 设置发送内容
                        EditText editTextOfButtonSendCommand = (EditText) dialog.findViewById(R.id.editText_send_command);
                        RadioButton radioButtonOfASC = (RadioButton) dialog.findViewById(R.id.button_ascii);
                        if (radioButtonOfASC.isChecked()) {
                            sendBuffer = editTextOfButtonSendCommand.getText().toString().getBytes();
                        } else {
                            try {
                                String input = editTextOfButtonSendCommand.getText().toString();
                                // 去除空格
                                input = input.replace(" ", "");
                                sendBuffer = HexHelper.hexString2byte(input);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "输入格式错误", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).build();


        // 删除按键
        // FIXME: 2017/4/7 为了简单，并没有真正删除按键，而是隐藏，这里存在潜在的泄漏风险
        final Button button = (Button) mInputDialog.findViewById(R.id.button_delete);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomizableCommandButton.this.setVisibility(GONE);
                mInputDialog.hide();
            }
        });


        RadioButton radioButtonOfASC = (RadioButton) mInputDialog.findViewById(R.id.button_ascii);
        RadioButton radioButtonOfHex = (RadioButton) mInputDialog.findViewById(R.id.button_hex);
        final EditText editTextOfButtonSendCommand = (EditText) mInputDialog.findViewById(R.id.editText_send_command);

        radioButtonOfASC.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dataFormat = ASCII_FORMAT;
                if (sendBuffer != null && sendBuffer.length != 0) {
                    editTextOfButtonSendCommand.setText(new String(sendBuffer));
                }
            }
        });

        radioButtonOfHex.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dataFormat = HEX_FORMAT;
                if (sendBuffer != null && sendBuffer.length != 0) {
                    editTextOfButtonSendCommand.setText(HexHelper.byte2hexString(sendBuffer));
                }
            }
        });


    }

    private void showInputDialog() {
        EditText editTextButtonName = (EditText) mInputDialog.findViewById(R.id.editText_button_name);
        editTextButtonName.setText(this.getText());
        EditText editTextOfButtonSendCommand = (EditText) mInputDialog.findViewById(R.id.editText_send_command);
        // TODO: 应该添加判断是否为可显示的ascii，然后以不同形式显示
        if (sendBuffer != null) {
            if (dataFormat == ASCII_FORMAT) {
                editTextOfButtonSendCommand.setText(new String(sendBuffer));
            } else {
                editTextOfButtonSendCommand.setText(HexHelper.byte2hexString(sendBuffer));
            }

        }
        mInputDialog.show();
    }

    /**
     * Get state
     *
     * @return state, 0 for NORMAL_STATE, 1 for EDITING_STATE
     */
    public int getState() {
        return state;
    }
}
