package fr.unice.miage.icarus;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class TestValuesActivity extends SherlockActivity implements SensorEventListener{

	private SensorManager sm;
	private float[] gravity = null;
	private float[] linear_acceleration = null;
	private Sensor accelerometre;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_values);
		Log.d("Icarus", "TestValuesActivity started");
		
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		if (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
			accelerometre = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sm.registerListener(this, accelerometre, SensorManager.SENSOR_DELAY_NORMAL);
		}
		gravity = new float[10];
		linear_acceleration = new float[10];
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.activity_test, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
        case android.R.id.home:
            // app icon in action bar clicked; go home
            Intent intent = new Intent(this, TestActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;     
        case R.id.menu_capteur:
        	Intent intent2 = new Intent(this, TestActivity.class);
        	startActivity(intent2);
        	return true;   
        case R.id.menu_valeurs:
        	Intent intent3 = new Intent(this, TestValuesActivity.class);
        	startActivity(intent3);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
		}
	}
	

	
	@Override
	public void onSensorChanged(SensorEvent event){
		// In this example, alpha is calculated as t / (t + dT),
		// where t is the low-pass filter's time-constant and
		// dT is the event delivery rate.

		Log.d("Icarus", "onSensorChanged");
		Log.d("Icarus", "event :"+event.values.toString());
		
		
		final float alpha = 0.8f;

		// Isolate the force of gravity with the low-pass filter.
		gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
		gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
		gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

		// Remove the gravity contribution with the high-pass filter.
		linear_acceleration[0] = event.values[0] - gravity[0];
		linear_acceleration[1] = event.values[1] - gravity[1];
		linear_acceleration[2] = event.values[2] - gravity[2];

		TextView accX = (TextView) findViewById(R.id.accX);
		accX.setText(Float.toString(linear_acceleration[0]));
		
		TextView accY = (TextView) findViewById(R.id.accY);
		accY.setText(Float.toString(linear_acceleration[1]));
		
		TextView accZ = (TextView) findViewById(R.id.accZ);
		accZ.setText(Float.toString(linear_acceleration[2]));
		
		//Log.d("Icarus", "linear_acceleration[0] = "+linear_acceleration[0]);
		//Log.d("Icarus", "linear_acceleration[1] = "+linear_acceleration[1]);
		//Log.d("Icarus", "linear_acceleration[2] = "+linear_acceleration[2]);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		Log.d("Icarus", "onAccuracyChanged");
	}


	
}
