package com.scientificrat.robocon2017blecontrol.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by huangzhengyue on 2016/10/25.
 */

public class Rocker extends View {

    private final float DEFAULT_RADIUS = 150;
    private final float DEFAULT_BAR_RADIUS = 150 / 2.5F;
    private final long DEFAULT_TRIGGER_DELTA_TIME = 100; //ms
    // 输出的范围 [-RANGE,+RANGE]
    private final float RANGE = 1;
    // 最大半径
    private final float MAX_RADIUS = 220;
    // 画笔
    private Paint pen = new Paint();

    private float radius = DEFAULT_RADIUS;
    private float barRadius = DEFAULT_BAR_RADIUS;
    private float width = DEFAULT_RADIUS;
    private float height = DEFAULT_RADIUS;
    private long triggerDeltaTime = DEFAULT_TRIGGER_DELTA_TIME;
    // barX barY 是 【中央bar摇杆中心点】 相对于该整个控件(0,0)点的x,y偏移
    private float barX = 200;
    private float barY = 200;

    // 计时
    private long lastTriggerTime = 0;

    //输出值
    private volatile float outputX = 0;
    private volatile float outputY = 0;

    private boolean justInit = true;

    private OnRockerChangeListener onRockerChangeListener = null;

    public Rocker(Context context) {
        super(context);
    }

    public Rocker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.width = this.getWidth();
        this.height = this.getHeight();
        if (justInit) {
            barX = this.width / 2;
            barY = this.height / 2;
            justInit = false;
        }
        if (width < height) {
            this.radius = Math.min(width / 2, MAX_RADIUS);
        } else {
            this.radius = Math.min(height / 2, MAX_RADIUS);
        }
        this.barRadius = radius / 2.5f;
        //绘制外周圆
        pen.setColor(Color.argb(255, 59, 63, 71));
        pen.setStyle(Paint.Style.STROKE);
        pen.setStrokeWidth(7);
        canvas.drawCircle(width / 2, height / 2, this.radius - 4, pen);
        //绘制中心bar
        pen.setColor(Color.argb(200, 200, 200, 200));
        pen.setStyle(Paint.Style.FILL);
        // barX barY 是 【中央bar摇杆中心点】 相对于该整个控件(0,0)点的x,y偏移
        canvas.drawCircle(barX, barY, this.barRadius, pen);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //抬起或者移出区域时恢复正常位置
        int action = event.getAction();
        if (MotionEvent.ACTION_UP == action || MotionEvent.ACTION_OUTSIDE == action || MotionEvent.ACTION_CANCEL == action) {
            this.barX = width / 2;
            this.barY = height / 2;

            // 计算输出
            this.outputX = (barX - width / 2) / this.radius * RANGE;
            this.outputY = (barY - height / 2) / this.radius * RANGE;
            // listener callback
            if (this.onRockerChangeListener != null) {
                onRockerChangeListener.onRockerChange(0, 0);
            }

        } else {
            final float x = event.getX();
            final float y = event.getY();

            //触摸点相对与中心的半径的平方 r_square
            float deltaX = (x - width / 2);
            float deltaY = (y - height / 2);
            float r_square = deltaX * deltaX + deltaY * deltaY;
            // barX barY 是 【中央bar摇杆中心点】 相对于该整个控件(0,0)点的x,y偏移, 设置这个全局变量用于 onDraw绘图
            if (r_square <= this.radius * this.radius) {
                barX = x;
                barY = y;
            } else {
                float scaleFactor = this.radius / (float) Math.sqrt(r_square);
                barX = this.width / 2 + deltaX * scaleFactor;
                barY = this.height / 2 + deltaY * scaleFactor;
            }
            // 计算输出
            this.outputX = (barX - width / 2) / this.radius * RANGE;
            this.outputY = (barY - height / 2) / this.radius * RANGE;
            // listener callback
            if (onRockerChangeListener != null) {
                long currTime = System.currentTimeMillis();
                if (currTime - lastTriggerTime > triggerDeltaTime) {
                    onRockerChangeListener.onRockerChange(this.outputX, this.outputY);
                }
                lastTriggerTime = currTime;
            }
        }
        //eat this event, so I do not call the super method
        //let it redraw
        invalidate();
        return true;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setOnRockerChangeListener(OnRockerChangeListener onRockerChangeListener) {
        this.onRockerChangeListener = onRockerChangeListener;
    }

    public float getRange() {
        return RANGE;
    }

    // 获取输出
    public float getOutputX() {
        return outputX;
    }

    public float getOutputY() {
        return outputY;
    }

    public long getTriggerDeltaTime() {
        return triggerDeltaTime;
    }

    public void setTriggerDeltaTime(long triggerDeltaTime) {
        this.triggerDeltaTime = triggerDeltaTime;
    }
}
