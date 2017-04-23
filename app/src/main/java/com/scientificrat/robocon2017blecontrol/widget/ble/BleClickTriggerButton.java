package com.scientificrat.robocon2017blecontrol.widget.ble;

import android.content.Context;
import android.util.AttributeSet;

import com.scientificrat.robocon2017blecontrol.connection.BluetoothConnectionController;
import com.scientificrat.robocon2017blecontrol.widget.ClickTriggerCommandButton;

/**
 * Created by huangzhengyue on 2017/4/23.
 */

public class BleClickTriggerButton extends ClickTriggerCommandButton {
    public BleClickTriggerButton(Context context) {
        super(context);
        setConnection(BluetoothConnectionController.getInstance());
    }

    public BleClickTriggerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setConnection(BluetoothConnectionController.getInstance());
    }

    public BleClickTriggerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setConnection(BluetoothConnectionController.getInstance());
    }

    public BleClickTriggerButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setConnection(BluetoothConnectionController.getInstance());
    }
}
