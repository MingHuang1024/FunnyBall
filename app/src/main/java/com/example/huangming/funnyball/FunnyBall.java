package com.example.huangming.funnyball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 可爱的小球
 *
 * @author Huangming  2017/10/18
 */
public class FunnyBall extends View {
    /** 小球的半径 */
    private final float RADIUS = 100;

    /** 当前小球的位置 */
    private PointF currrentPosition = new PointF(RADIUS, RADIUS);

    /** 手指触摸起点坐标 */
    private PointF moveStartPosition = new PointF(0, 0);

    /** 当前手指位置坐标 */
    private PointF moveEndPosition = new PointF(0, 0);

    /** 本控件的高宽 */
    private PointF me = new PointF(0, 0);

    /** 手指是否触摸在小球上 */
    private boolean isTouchInBall;

    private Paint paint;

    /** 小球是否自动移动，以区别用手指拖着走 */
    private boolean isAutoMove;

    /** 小球是否走到了左边缘，false表示走到了右边缘 */
    private boolean isOnLeftBoundary;

    /** 小球是否走到了上边缘，false表示走到了下边缘 */
    private boolean isOnTopBoundary;

    /** 小球自动移动时的步长 */
    private PointF increment = new PointF();

    public FunnyBall(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FunnyBall(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        isAutoMove = true;
        postDelayed(new AutoRunnnable(), 500);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = fixMeasureSpec(widthMeasureSpec);
        heightMeasureSpec = fixMeasureSpec(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        me.x = getWidth();
        me.y = getHeight();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isAutoMove) {
            autoDraw(canvas);
        } else {
            manualDraw(canvas);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAutoMove = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                moveStartPosition.x = event.getX();
                moveStartPosition.y = event.getY();
                boolean inX = moveStartPosition.x >= currrentPosition.x - RADIUS &&
                    moveStartPosition.x <= currrentPosition.x + RADIUS;
                boolean inY = moveStartPosition.y >= currrentPosition.y - RADIUS &&
                    moveStartPosition.y <= currrentPosition.y + RADIUS;
                isTouchInBall = inX && inY;
                break;
            case MotionEvent.ACTION_MOVE:
                // 小球跟着手指走
                if (isTouchInBall) {
                    isAutoMove = false;
                    moveEndPosition.x = event.getX();
                    moveEndPosition.y = event.getY();
                    this.postInvalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isTouchInBall && !isAutoMove) {
                    float x = currrentPosition.x + (moveEndPosition.x - moveStartPosition.x);
                    float y = currrentPosition.y + (moveEndPosition.y - moveStartPosition.y);
                    if (x <= RADIUS) {
                        currrentPosition.x = RADIUS;
                    } else if (x >= me.x - RADIUS) {
                        currrentPosition.x = me.x - RADIUS;
                    } else {
                        currrentPosition.x = x;
                    }

                    if (y <= RADIUS) {
                        currrentPosition.y = RADIUS;
                    } else if (y >= me.y - RADIUS) {
                        currrentPosition.y = me.y - RADIUS;
                    } else {
                        currrentPosition.y = y;
                    }
                    isTouchInBall = false;
                    System.out.println("抬起手指。。");
                    post(new AutoRunnnable());
                    isAutoMove = true;
                }
                break;
            default:
        }
        return true;
    }

    /**
     * 调整尺寸
     *
     * @param measureSpec
     * @return
     */
    private int fixMeasureSpec(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        if (MeasureSpec.AT_MOST == mode) {
            return measureSpec;
        } else {
            // 如果父控件没有给本控件的高/宽做任何限制
            // 或父控件已经确切的指定了本控件的高/宽但高/宽小于小球半径的三倍
            // 则手动设置本控件的高/宽为小球半径的三倍
            int size = MeasureSpec.getSize(measureSpec);
            if (size < 3 * RADIUS) {
                size = (int) (3 * RADIUS);
            }
            return MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST);
        }
    }

    private void manualDraw(Canvas canvas) {
        if (!isTouchInBall) {
            canvas.drawCircle(currrentPosition.x, currrentPosition.y, RADIUS, paint);
            return;
        }

        float x = currrentPosition.x + (moveEndPosition.x - moveStartPosition.x);
        float y = currrentPosition.y + (moveEndPosition.y - moveStartPosition.y);
        if (x <= RADIUS) {
            x = RADIUS;
        } else if (x >= me.x - RADIUS) {
            x = me.x - RADIUS;
        }
        if (y <= RADIUS) {
            y = RADIUS;
        } else if (y >= me.y - RADIUS) {
            y = me.y - RADIUS;
        }
        canvas.drawCircle(x, y, RADIUS, paint);
    }

    private void autoDraw(Canvas canvas) {
        float x = currrentPosition.x + increment.x;
        float y = currrentPosition.y + increment.y;
        if (x <= RADIUS) {
            // 到了边缘就转向
            x = RADIUS;
            isOnLeftBoundary = true;
        } else if (x >= me.x - RADIUS) {
            x = me.x - RADIUS;
            isOnLeftBoundary = false;
        }

        if (y <= RADIUS) {
            y = RADIUS;
            isOnTopBoundary = true;
        } else if (y >= me.y - RADIUS) {
            y = me.y - RADIUS;
            isOnTopBoundary = false;
        }
        currrentPosition.x = x;
        currrentPosition.y = y;
        canvas.drawCircle(x, y, RADIUS, paint);
    }


    private class AutoRunnnable implements Runnable {

        @Override
        public void run() {
            try {
                // System.out.println("线程在跑。。");
                if (isOnLeftBoundary) {
                    increment.x = 5;
                } else {
                    increment.x = -5;
                }
                if (isOnTopBoundary) {
                    increment.y = 5;
                } else {
                    increment.y = -5;
                }
                postInvalidate();
                Thread.sleep(30);
                if (isAutoMove) {
                    post(this);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
