package pt.cm.paintapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

//TODO
// Detect a double tap
// Detect a long press
//TODO
// when double tap is detected the app should enter in "erase mode"
// this mode changes the color of the paint to color of the background
//TODO
// when a long press is detect change the background color with a random one

public class MainActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "Gestures";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SingleTouchEventView paintCanvas = new SingleTouchEventView(getApplicationContext(), null);
        setContentView(paintCanvas);// adds the created view to the screen
    }

    class SingleTouchEventView extends View {
        private Paint paint = new Paint();
        private Path path = new Path();

        int color;
        private boolean double_tap = false;
        private long hold_time1;
        private long hold_time2;

        private long doubletap_time;

        public SingleTouchEventView(Context context, AttributeSet attrs) {
            super(context, attrs);

            paint.setAntiAlias(true);
            paint.setStrokeWidth(20f);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }


        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);// draws the path with the paint

        }

        @Override
        public boolean performClick(){
            return super.performClick();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);// updates the path initial point

                    hold_time1 = System.currentTimeMillis();

                    if(double_tap && (System.currentTimeMillis() - doubletap_time) <= 300) {
                        paint.setColor(color);
                        double_tap = false;
                    }
                    else {
                        double_tap = true;
                        doubletap_time = System.currentTimeMillis();
                    }

                    return true;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(eventX, eventY);// makes a line to the point each time this event is fired
                    break;
                case MotionEvent.ACTION_UP:// when you lift your finger

                    hold_time2 = System.currentTimeMillis();

                    if(hold_time2 - hold_time1 >= 1000) {
                        Random random = new Random();
                        color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

                        getRootView().setBackgroundColor(color);
                    }

                    performClick();
                    break;
                default:
                    return false;
            }

            // Schedules a repaint.
            invalidate();
            return true;
        }
    }
}