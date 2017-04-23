package com.scientificrat.robocon2017blecontrol.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
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
public class ClickTriggerCommandButton extends Button {

    private ConnectionController connection = null;
    private String sendFailToastString = "发送失败";
    private String notConnectedToastString = "蓝牙未连接";
    private byte[] sendBuffer = null;

    public ClickTriggerCommandButton(Context context) {
        super(context);
    }

    public ClickTriggerCommandButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.dealAttrs(attrs);
    }

    public ClickTriggerCommandButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.dealAttrs(attrs);
    }

    public ClickTriggerCommandButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.dealAttrs(attrs);
    }

    private void dealAttrs(AttributeSet attrs) {
        this.sendBuffer = HexHelper.hexString2byte(attrs.getAttributeValue(null, "clickedCommand"));
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
    public boolean performClick() {
        if(!connection.isConnected()){
            Toast.makeText(getContext(), notConnectedToastString, Toast.LENGTH_SHORT).show();
        }
        if (sendBuffer != null) {
            try {
                connection.sendRawData(sendBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), sendFailToastString, Toast.LENGTH_SHORT).show();
            }
        }
        return super.performClick();
    }

    public ConnectionController getConnection() {
        return connection;
    }

    public void setConnection(ConnectionController connection) {
        this.connection = connection;
    }

    public String getSendFailToastString() {
        return sendFailToastString;
    }

    public void setSendFailToastString(String sendFailToastString) {
        this.sendFailToastString = sendFailToastString;
    }

    public byte[] getSendBuffer() {
        return sendBuffer;
    }

    public void setSendBuffer(byte[] sendBuffer) {
        this.sendBuffer = sendBuffer;
    }

    public String getNotConnectedToastString() {
        return notConnectedToastString;
    }

    public void setNotConnectedToastString(String notConnectedToastString) {
        this.notConnectedToastString = notConnectedToastString;
    }
}
