package com.santhoshn.androidbits.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by santhosh on 09/02/16.
 */
public class Circle extends SurfaceView implements SurfaceHolder.Callback {
    private float x;
    private float y;
    private float radius;
    private float color;

    public Circle(Context context, float x, float y, float radius, int color) {
        super(context);
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.paint = new Paint();
        this.paint.setColor(color);
    }

    private Paint paint;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void onDraw(Canvas canvas) {
        canvas.drawCircle(this.x, this.y, this.radius, this.paint);
    }
}