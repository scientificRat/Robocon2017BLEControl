package com.scientificrat.robocon2017blecontrol.widget;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.support.annotation.NonNull;

import android.util.AttributeSet;
import android.support.v7.widget.*;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by huangzhengyue on 2017/4/5.
 */

public class CustomizableCommandButton extends AppCompatButton {

    private final static int NORMAL_STATE = 0;
    private final static int EDITING_STATE = 1;
    private int state = NORMAL_STATE;
    private String buttonText = "";
    private byte[] sendBuffer = new byte[100];
    private int sendBufferLength = 0;

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
    private void init(){

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // vibrate
                Vibrator vibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
                state = EDITING_STATE;
                showInputDialog();
                vibrator.vibrate(100);
                return false;
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state==NORMAL_STATE){
                    // vibrate
                    Vibrator vibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
                    vibrator.vibrate(40);
                }
            }
        });
    }

    private void showInputDialog() {
        new MaterialDialog.Builder(getContext()).title("shit").content("hello").positiveText("ok").input("s", "s", false, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                state = NORMAL_STATE;
            }
        }).cancelable(false).show();
    }

    /**
     * Get state
     * @return state, 0 for NORMAL_STATE, 1 for EDITING_STATE
     */
    public int getState() {
        return state;
    }
}
