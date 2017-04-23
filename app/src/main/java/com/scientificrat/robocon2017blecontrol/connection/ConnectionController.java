package com.scientificrat.robocon2017blecontrol.connection;

import java.io.IOException;

/**
 * Created by huangzhengyue on 2017/4/22.
 */

public interface ConnectionController {
    void sendRawData(byte[] data) throws IOException;
    void cancel();
    boolean isConnected();
}
