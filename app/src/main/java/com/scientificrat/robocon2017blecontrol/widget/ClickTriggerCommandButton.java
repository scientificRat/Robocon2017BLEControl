package com.scientificrat.robocon2017blecontrol.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.Toast;

import com.scientificrat.robocon2017blecontrol.sender.CommandSender;
import com.scientificrat.robocon2017blecontrol.util.HexHelper;

/**
 * Created by huangzhengyue on 2017/4/21.
 */
@SuppressLint("AppCompatCustomView")
public class ClickTriggerCommandButton extends Button {

    private CommandSender commandSender = CommandSender.getInstance();
    private String sendFailToastString = "发送失败";
    // 发送键值
    private byte command = 0;

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
        byte[] bytes = HexHelper.hexString2byte(attrs.getAttributeValue(null, "command"));
        if (bytes != null && bytes.length > 0) {
            this.command = bytes[0];
        }
    }

    @Override
    public boolean performClick() {
        if (!commandSender.isStarted()) {
            Toast.makeText(getContext(), sendFailToastString, Toast.LENGTH_SHORT).show();
        } else {
            commandSender.setKey(command);
        }
        return super.performClick();
    }

    public String getSendFailToastString() {
        return sendFailToastString;
    }

    public void setSendFailToastString(String sendFailToastString) {
        this.sendFailToastString = sendFailToastString;
    }

}
