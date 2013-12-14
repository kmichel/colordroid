package info.kmichel;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MovementDetector implements SensorEventListener {

    public interface MovementListener {
        void onMoveStart();

        void onMoveStop();
    }

    private final SensorManager sensor_manager;
    private final int delay_milliseconds;
    private final MovementListener listener;
    private final float[] gravity;
    private final float[] linear_acceleration;
    private boolean was_moving;
    private boolean is_moving;
    private long state_change_start_time;
    private boolean state_change_is_pending;


    public MovementDetector(final Context context, final int delay_milliseconds, final MovementListener listener) {
        sensor_manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.delay_milliseconds = delay_milliseconds;
        this.listener = listener;
        gravity = new float[3];
        linear_acceleration = new float[3];
        was_moving = false;
        is_moving = false;
        state_change_is_pending = false;
    }

    public void start() {
        sensor_manager.registerListener(this, sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        checkPendingStageChange();
        sensor_manager.unregisterListener(this);
    }

    public void onSensorChanged(final SensorEvent sensor_event) {
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate

        final float alpha = 0.8f;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * sensor_event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * sensor_event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * sensor_event.values[2];

        linear_acceleration[0] = sensor_event.values[0] - gravity[0];
        linear_acceleration[1] = sensor_event.values[1] - gravity[1];
        linear_acceleration[2] = sensor_event.values[2] - gravity[2];

        final float x = linear_acceleration[0];
        final float y = linear_acceleration[1];
        final float z = linear_acceleration[2];

        final double acceleration = Math.sqrt(x * x + y * y + z + z);

        is_moving = acceleration > 0.8;

        if (is_moving != was_moving) {
            state_change_start_time = sensor_event.timestamp;
            state_change_is_pending = true;
        }

        if (is_moving == was_moving && sensor_event.timestamp - state_change_start_time > 1000 * delay_milliseconds)
            checkPendingStageChange();

        was_moving = is_moving;
    }

    private void checkPendingStageChange() {
        if (state_change_is_pending) {
            state_change_is_pending = false;
            if (is_moving)
                listener.onMoveStart();
            else
                listener.onMoveStop();
        }
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int i) {
    }
}
