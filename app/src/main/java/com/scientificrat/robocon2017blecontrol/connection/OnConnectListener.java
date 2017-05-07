package com.scientificrat.robocon2017blecontrol.connection;

import java.io.IOException;

/**
 * Created by huangzhengyue on 2016/10/28.
 */

public interface OnConnectListener {
    void onConnectSuccess();
    void onConnectFail(IOException e);
}
