package com.scientificrat.robocon2017blecontrol.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import com.scientificrat.robocon2017blecontrol.connection.GeneralConnection;
import com.scientificrat.robocon2017blecontrol.util.HexHelper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by huangzhengyue on 2017/4/21.
 */

@SuppressLint("AppCompatCustomView")
public class TouchTriggerCommandButton extends Button {
    private String sendFailToastString = "发送失败";
    private byte[] touchDownSendBuffer;
    private byte[] touchUpSendBuffer;
    private GeneralConnection connection = null;

    public TouchTriggerCommandButton(Context context) {
        super(context);
    }

    public TouchTriggerCommandButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initSendBuffer(attrs);
    }

    public TouchTriggerCommandButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initSendBuffer(attrs);
    }

    public TouchTriggerCommandButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initSendBuffer(attrs);
    }

    private void initSendBuffer(AttributeSet attrs) {
        // parse from xml
        this.touchDownSendBuffer = HexHelper.hexString2byte(attrs.getAttributeValue(null, "touchDownCommand"));
        this.touchUpSendBuffer = HexHelper.hexString2byte(attrs.getAttributeValue(null, "touchUpCommand"));
        String connectionClass = attrs.getAttributeValue(null, "connectionClass");
        if (connectionClass != null) {
            try {
                Method method = Class.forName(connectionClass).getMethod("getInstance");
                connection = (GeneralConnection) method.invoke(null);
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
    public GeneralConnection getConnection() {
        return connection;
    }

    public void setConnection(GeneralConnection connection) {
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
}
