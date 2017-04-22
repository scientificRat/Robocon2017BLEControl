package com.scientificrat.robocon2017blecontrol.widget.ble;

import android.content.Context;
import android.util.AttributeSet;

import com.scientificrat.robocon2017blecontrol.connection.BluetoothConnection;
import com.scientificrat.robocon2017blecontrol.widget.TouchTriggerCommandButton;

/**
 * Created by huangzhengyue on 2017/4/22.
 */

public class BleTouchTriggerButton extends TouchTriggerCommandButton {

    public BleTouchTriggerButton(Context context) {
        super(context);
        setConnection(BluetoothConnection.getInstance());
    }

    public BleTouchTriggerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setConnection(BluetoothConnection.getInstance());
    }

    public BleTouchTriggerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setConnection(BluetoothConnection.getInstance());
    }

    public BleTouchTriggerButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setConnection(BluetoothConnection.getInstance());
    }
}
