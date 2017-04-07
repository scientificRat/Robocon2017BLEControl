package com.scientificrat.robocon2017blecontrol.util;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

/**
 * Created by huangzhengyue on 2017/4/7.
 */

public class AppVibrator {

    private static final int VIBRATE_SHORT_IN_MILLIS = 40;
    private static final int VIBRATE_LONG_IN_MILLIS = 100;

    public static void vibrateShort(Context context) {
        vibrate(context, VIBRATE_SHORT_IN_MILLIS);
    }

    public static void vibrateLong(Context context) {
        vibrate(context, VIBRATE_LONG_IN_MILLIS);
    }


    public static void vibrate(Context context, int millis) {
        // vibrate
        Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(millis);
    }
}
