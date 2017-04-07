package com.scientificrat.robocon2017blecontrol.util;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by huangzhengyue on 2017/4/7.
 */

public class MesurementUtility {
    public static int convertDPtoPX(Resources r ,int dp){
        return  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
}
