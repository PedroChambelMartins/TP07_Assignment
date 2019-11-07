package pt.cm.paintapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.print.PrinterId;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Random;
import java.util.Set;

//TODO
// Detect a double tap
// Detect a long press
//TODO
// when double tap is detected the app should enter in "erase mode"
// this mode changes the color of the paint to color of the background
//TODO
// when a long press is detect change the background color with a random one

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String DEBUG_TAG = "Gestures";
    private static final int SHAKE_THRESHOLD = 800;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Sensor sensorLight;
    private Sensor sensorShake;
    private float vibrateTreshold = 0;
    private int brightness;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    private float ax = 0;
    private float ay = 0;
    private float az = 0;

    private float mLightQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SingleTouchEventView paintCanvas = new SingleTouchEventView(getApplicationContext(), null);
        setContentView(paintCanvas);// adds the created view to the screen

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
        {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_FASTEST);

            vibrateTreshold = sensor.getMaximumRange();

        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null)
        {
            sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            sensorManager.registerListener(this, sensorLight, sensorManager.SENSOR_DELAY_NORMAL);

        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            mAccel = 0.00f;
            mAccelCurrent = SensorManager.GRAVITY_EARTH;
            mAccelLast = SensorManager.GRAVITY_EARTH;
            sensorShake = sensorManager.getDefaultSensor((Sensor.TYPE_LINEAR_ACCELERATION));
            sensorManager.registerListener(this, sensorShake, sensorManager.SENSOR_DELAY_FASTEST);
        }


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
            View view = this.getWindow().getDecorView();


            if (ax > 0.0 && ax < 0.1) {
                view.setBackgroundColor(Color.RED);
            }
            else if (ay > 0.0 && ay < 0.1) {
                view.setBackgroundColor(Color.GREEN);
            }
            else if (az > 0.0 && az < 0.1) {
                view.setBackgroundColor(Color.BLUE);
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            mLightQuantity = event.values[0];

            WindowManager.LayoutParams laoyout = getWindow().getAttributes();

            laoyout.screenBrightness = mLightQuantity;

            getWindow().setAttributes(laoyout);
//            Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//
//            Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
//
////            brightness = map(mLightQuantity, 0, sensorLight.getMaximumRange(), 0, 255);
//
//            Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, (int) mLightQuantity);

        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Paint paint = new Paint();
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            if (mAccel > 2) {
//                Toast toast = Toast.makeText(getApplicationContext(), "Device has shaken.", Toast.LENGTH_LONG);
//                toast.show();
                paint.setColor(Color.WHITE);
            }
        }
    }

    long map(long x, long in_min, long in_max, long out_min, long out_max) {
        return (x - in_min) *  (out_max - out_min) / (in_max - in_min) + out_min;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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