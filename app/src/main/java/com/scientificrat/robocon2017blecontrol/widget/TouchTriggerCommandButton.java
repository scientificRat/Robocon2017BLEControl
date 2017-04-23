package com.scientificrat.robocon2017blecontrol.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import com.scientificrat.robocon2017blecontrol.connection.ConnectionController;
import com.scientificrat.robocon2017blecontrol.util.HexHelper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by huangzhengyue on 2017/4/21.
 */

@SuppressLint("AppCompatCustomView")
public class TouchTriggerCommandButton extends Button {

    private ConnectionController connection = null;
    private String sendFailToastString = "发送失败";
    private String notConnectedToastString = "蓝牙未连接";
    private byte[] touchDownSendBuffer;
    private byte[] touchUpSendBuffer;


    public TouchTriggerCommandButton(Context context) {
        super(context);
    }

    public TouchTriggerCommandButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.dealAttrs(attrs);
    }

    public TouchTriggerCommandButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.dealAttrs(attrs);
    }

    public TouchTriggerCommandButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.dealAttrs(attrs);
    }

    private void dealAttrs(AttributeSet attrs) {
        // parse from xml
        this.touchDownSendBuffer = HexHelper.hexString2byte(attrs.getAttributeValue(null, "touchDownCommand"));
        this.touchUpSendBuffer = HexHelper.hexString2byte(attrs.getAttributeValue(null, "touchUpCommand"));
        String connectionClass = attrs.getAttributeValue(null, "connectionClass");
        if (connectionClass != null) {
            try {
                Method method = Class.forName(connectionClass).getMethod("getInstance");
                connection = (ConnectionController) method.invoke(null);
            } catch (NoSuchMethodException |
                    ClassNotFoundException |
                    InvocationTargetException |
                    IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!connection.isConnected()) {
                    Toast.makeText(getContext(), notConnectedToastString, Toast.LENGTH_SHORT).show();
                    break;
                }
                if (touchDownSendBuffer != null) {
                    try {
                        connection.sendRawData(touchDownSendBuffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), sendFailToastString, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!connection.isConnected()) {
                    Toast.makeText(getContext(), notConnectedToastString, Toast.LENGTH_SHORT).show();
                    break;
                }
                if (touchUpSendBuffer != null) {
                    try {
                        connection.sendRawData(touchUpSendBuffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), sendFailToastString, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    // getter setters:
    public ConnectionController getConnection() {
        return connection;
    }

    public void setConnection(ConnectionController connection) {
        this.connection = connection;
    }

    public byte[] getTouchDownSendBuffer() {
        return touchDownSendBuffer;
    }

    public void setTouchDownSendBuffer(byte[] touchDownSendBuffer) {
        this.touchDownSendBuffer = touchDownSendBuffer;
    }

    public byte[] getTouchUpSendBuffer() {
        return touchUpSendBuffer;
    }

    public void setTouchUpSendBuffer(byte[] touchUpSendBuffer) {
        this.touchUpSendBuffer = touchUpSendBuffer;
    }

    public String getSendFailToastString() {
        return sendFailToastString;
    }

    public void setSendFailToastString(String sendFailToastString) {
        this.sendFailToastString = sendFailToastString;
    }

    public String getNotConnectedToastString() {
        return notConnectedToastString;
    }

    public void setNotConnectedToastString(String notConnectedToastString) {
        this.notConnectedToastString = notConnectedToastString;
    }
}
