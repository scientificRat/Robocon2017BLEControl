package com.scientificrat.robocon2017blecontrol.trasmitter;

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

    private byte key = 0;

    private short leftX;

    private short leftY;

    private short rightX;

    private short rightY;

    private boolean inEmergencyState;

    private int state = STATE_STOP;

    private ConnectionController connectionController = BluetoothConnectionController.getInstance();

    private Timer timer = new Timer(true);

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (inEmergencyState) {
                leftX = leftY = rightX = rightY = 0;
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(13);
            //小端序
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.putShort((short) 0x0d0a)
                    .put(key)
                    .putShort(leftX)
                    .putShort(leftY)
                    .putShort(rightX)
                    .putShort(rightY)
                    .putShort((short) 0x0a0d);
            try {
                connectionController.sendRawData(byteBuffer.array());
            } catch (IOException e) {
                e.printStackTrace();
            }
            // reset key
            key = 0;
        }
    };

    public static CommandSender getInstance() {
        return instance;
    }

    // private constructor
    private CommandSender() {
    }

    public void start() {
        timer.schedule(this.timerTask, this.delayInMillis);
        this.state = STATE_START;
    }

    public void stop() {
        timer.cancel();
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
}
