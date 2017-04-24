package com.scientificrat.robocon2017blecontrol.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import com.scientificrat.robocon2017blecontrol.connection.ConnectionController;
import com.scientificrat.robocon2017blecontrol.sender.CommandSender;
import com.scientificrat.robocon2017blecontrol.util.HexHelper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by huangzhengyue on 2017/4/21.
 */

@SuppressLint("AppCompatCustomView")
public class MovementCommandButton extends Button {

    private CommandSender commandSender = CommandSender.getInstance();
    private String sendFailToastString = "发送失败";
    private short leftX = 0;
    private short leftY = 0;
    private short rightX = 0;
    private short rightY = 0;

    public MovementCommandButton(Context context) {
        super(context);
    }

    public MovementCommandButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.dealAttrs(attrs);
    }

    public MovementCommandButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.dealAttrs(attrs);
    }

    public MovementCommandButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.dealAttrs(attrs);
    }

    private void dealAttrs(AttributeSet attrs) {
        // parse from xml
        String command = attrs.getAttributeValue(null, "command");
        if (command == null) return;
        String[] split = command.split(",");
        if (split == null || split.length != 4) return;
        try {
            leftX = Short.parseShort(split[0]);
            leftY = Short.parseShort(split[1]);
            rightX = Short.parseShort(split[2]);
            rightY = Short.parseShort(split[3]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                commandSender.setSpeed(leftX, leftY, rightX, rightY);
                break;
            case MotionEvent.ACTION_UP:
                commandSender.setSpeedZero();
                break;
        }
        return super.onTouchEvent(event);
    }

    // getter setters:

    public String getSendFailToastString() {
        return sendFailToastString;
    }

    public void setSendFailToastString(String sendFailToastString) {
        this.sendFailToastString = sendFailToastString;
    }

}
