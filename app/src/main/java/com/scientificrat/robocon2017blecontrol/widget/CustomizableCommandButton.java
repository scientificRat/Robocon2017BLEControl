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

/**
 * Created by huangzhengyue on 2017/4/5.
 */

public class CustomizableCommandButton extends AppCompatButton implements Serializable {

    // ------------------Const variables-------------------
    private final static int NORMAL_STATE = 0;
    private final static int EDITING_STATE = 1;
    private final static int ASCII_FORMAT = 0;
    private final static int HEX_FORMAT = 1;
    // 创建按键时的默认文字
    private final static String DEFAULT_BUTTON_TEXT = "";
    //-----------------------------------------------------

    private int state = NORMAL_STATE;
    // 自定义信息
    private CustomizableInfo customizableInfo = null;
    // 弹出输入框
    private transient MaterialDialog mInputDialog = null;

    // 保存到文件的部分
    public static class CustomizableInfo implements Serializable {
        private String buttonText = DEFAULT_BUTTON_TEXT;
        private byte[] sendBuffer = null;
        private int dataFormat = ASCII_FORMAT;

        public String getButtonText() {
            return buttonText;
        }

        public CustomizableInfo setButtonText(String buttonText) {
            this.buttonText = buttonText;
            return this;
        }

        public byte[] getSendBuffer() {
            return sendBuffer;
        }

        public CustomizableInfo setSendBuffer(byte[] sendBuffer) {
            this.sendBuffer = sendBuffer;
            return this;
        }

        public int getDataFormat() {
            return dataFormat;
        }

        public CustomizableInfo setDataFormat(int dataFormat) {
            this.dataFormat = dataFormat;
            return this;
        }
    }


    //------------------Constructors------------------------
    public CustomizableCommandButton(Context context) {
        super(context);
        // 新建button自定义信息
        customizableInfo = new CustomizableInfo();
        this.setText(customizableInfo.buttonText);
        initDefaultListeners();
    }

    public CustomizableCommandButton(Context context, CustomizableInfo customizableInfo) {
        super(context);
        // 构建button自定义信息
        this.customizableInfo = customizableInfo;
        this.setText(customizableInfo.buttonText);
        initDefaultListeners();
    }


    public CustomizableCommandButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 新建button自定义信息
        customizableInfo = new CustomizableInfo();
        this.setText(customizableInfo.buttonText);
        initDefaultListeners();
        attrs.getAttributeValue("app","");
    }

    public CustomizableCommandButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 新建button自定义信息
        customizableInfo = new CustomizableInfo();
        this.setText(customizableInfo.buttonText);
        initDefaultListeners();
    }
    //------------------------------------------------------

    /**
     * Do initialization, setting listeners
     */
    private void initDefaultListeners() {
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
                        bluetoothConnection.sendRawData(customizableInfo.sendBuffer);
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
                        customizableInfo.buttonText = editTextButtonName.getText().toString();
                        CustomizableCommandButton.this.setText(customizableInfo.buttonText);
                        // 设置发送内容
                        EditText editTextOfButtonSendCommand = (EditText) dialog.findViewById(R.id.editText_send_command);
                        RadioButton radioButtonOfASC = (RadioButton) dialog.findViewById(R.id.button_ascii);
                        if (radioButtonOfASC.isChecked()) {
                            customizableInfo.sendBuffer = editTextOfButtonSendCommand.getText().toString().getBytes();
                        } else {
                            try {
                                String input = editTextOfButtonSendCommand.getText().toString();
                                // 去除空格
                                input = input.replace(" ", "");
                                customizableInfo.sendBuffer = HexHelper.hexString2byte(input);
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
                customizableInfo.dataFormat = ASCII_FORMAT;
                if (customizableInfo.sendBuffer != null && customizableInfo.sendBuffer.length != 0) {
                    editTextOfButtonSendCommand.setText(new String(customizableInfo.sendBuffer));
                }
            }
        });

        radioButtonOfHex.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                customizableInfo.dataFormat = HEX_FORMAT;
                if (customizableInfo.sendBuffer != null && customizableInfo.sendBuffer.length != 0) {
                    editTextOfButtonSendCommand.setText(HexHelper.byte2hexString(customizableInfo.sendBuffer));
                }
            }
        });


    }

    private void showInputDialog() {
        EditText editTextButtonName = (EditText) mInputDialog.findViewById(R.id.editText_button_name);
        editTextButtonName.setText(this.getText());
        EditText editTextOfButtonSendCommand = (EditText) mInputDialog.findViewById(R.id.editText_send_command);
        // TODO: 应该添加判断是否为可显示的 ASCII ，然后以不同形式显示
        if (customizableInfo.sendBuffer != null) {
            if (customizableInfo.dataFormat == ASCII_FORMAT) {
                editTextOfButtonSendCommand.setText(new String(customizableInfo.sendBuffer));
            } else {
                editTextOfButtonSendCommand.setText(HexHelper.byte2hexString(customizableInfo.sendBuffer));
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

    public CustomizableInfo getCustomizableInfo() {
        return customizableInfo;
    }
}
