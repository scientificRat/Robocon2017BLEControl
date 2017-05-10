package com.scientificrat.robocon2017blecontrol.sender;


import com.scientificrat.robocon2017blecontrol.connection.BluetoothConnectionController;
import com.scientificrat.robocon2017blecontrol.connection.ConnectionController;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by huangzhengyue on 2017/4/24.
 */

public class CommandSender {
    //singleton
    private static CommandSender instance = new CommandSender();

    private static final int DEFAULT_DELAY_MS = 100;

    public static final int STATE_STOP = 0;

    public static final int STATE_START = 1;

    private int delayInMillis = DEFAULT_DELAY_MS;

    private volatile byte key = 0;

    private volatile short leftX;

    private volatile short leftY;

    private volatile short rightX;

    private volatile short rightY;

    private volatile boolean inEmergencyState;

    private int state = STATE_STOP;

    private ConnectionController connectionController = BluetoothConnectionController.getInstance();

    private Timer timer = new Timer(true);

    private TimerTask timerTask = null;

    private Runnable sendDataRunnable = new Runnable() {
        @Override
        public void run() {
            ByteBuffer byteBuffer = ByteBuffer.allocate(13);
            //小端序
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.putShort((short) 0x0d0a).put(key);
            // reset key
            key = 0;
            if (inEmergencyState) {
                // 8bytes - 0
                byteBuffer.putLong(0);
            } else {
                byteBuffer.putShort(leftX)
                        .putShort(leftY)
                        .putShort(rightX)
                        .putShort(rightY);
            }

            byteBuffer.putShort((short) 0x0a0d);
            try {
                connectionController.sendRawData(byteBuffer.array());
            } catch (IOException e) {
                e.printStackTrace();
                stop();
            }
        }
    };

    public static CommandSender getInstance() {
        return instance;
    }

    // private constructor
    private CommandSender() {
    }

    /**
     * 开启发送
     *
     * @param delayInMillis 发送延时
     * @return 是否开启成功
     */
    public boolean start(int delayInMillis) {
        if (!connectionController.isConnected()) {
            return false;
        }
        if (this.state == STATE_START) {
            return true;
        }
        // 创建新的timer，注意timer一旦cancel就不能再schedule新的任务
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                sendDataRunnable.run();
            }
        };
        this.delayInMillis = delayInMillis;
        this.timer.schedule(this.timerTask, 0, this.delayInMillis);
        this.state = STATE_START;
        return true;
    }

    /**
     * 开启发送
     *
     * @return 是否开启成功
     */
    public boolean start() {
        return start(DEFAULT_DELAY_MS);
    }

    /**
     * 停止发送
     */
    public void stop() {
        this.timerTask.cancel();
        // Remove all canceled task of this timer
        this.timer.purge();
        this.state = STATE_STOP;
    }

    public byte getKey() {
        return key;
    }

    public void setKey(byte key) {
        this.key = key;
    }

    public short getLeftX() {
        return leftX;
    }

    public void setLeftX(short leftX) {
        this.leftX = leftX;
    }

    public short getLeftY() {
        return leftY;
    }

    public void setLeftY(short leftY) {
        this.leftY = leftY;
    }

    public short getRightX() {
        return rightX;
    }

    public void setRightX(short rightX) {
        this.rightX = rightX;
    }

    public short getRightY() {
        return rightY;
    }

    public void setRightY(short rightY) {
        this.rightY = rightY;
    }

    public int getDelayInMillis() {
        return delayInMillis;
    }

    public void setDelayInMillis(int delayInMillis) {
        this.delayInMillis = delayInMillis;
    }

    public boolean isInEmergencyState() {
        return inEmergencyState;
    }

    public void setInEmergencyState(boolean inEmergencyState) {
        this.inEmergencyState = inEmergencyState;
    }

    public boolean isStarted() {
        return state == STATE_START;
    }

    public void setSpeedZero() {
        leftX = leftY = rightX = rightY = 0;
    }

    public void setSpeed(short leftX, short leftY, short rightX, short rightY) {
        this.leftX = leftX;
        this.leftY = leftY;
        this.rightX = rightX;
        this.rightY = rightY;
    }
}
