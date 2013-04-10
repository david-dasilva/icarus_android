package fr.unice.miage.icarus;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

public class TestValuesActivity extends SherlockActivity implements SensorEventListener, LocationListener{

	private SensorManager sm;
	private float[] gravity = null;
	private float[] linear_acceleration = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_values);
		Log.d("Icarus", "TestValuesActivity started");
		
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		if (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){	
			sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
		}
		if (sm.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null){	
			sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
		}
		gravity = new float[10];
		linear_acceleration = new float[10];
		
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this); // GPS
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this); // NETWORK
		
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
		
		Sensor sensor = event.sensor;
        
		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            /*
             * 
             * Accelerometre
             * 
             * 
             */
			// In this example, alpha is calculated as t / (t + dT),
			// where t is the low-pass filter's time-constant and
			// dT is the event delivery rate.
		
			
			final float kFilteringFactor = 0.1f;

			// Isolate the force of gravity with the low-pass filter.
			gravity[0] = event.values[0] * kFilteringFactor + gravity[0] * (1.0f - kFilteringFactor) ;
			gravity[1] = event.values[1] * kFilteringFactor + gravity[1] * (1.0f - kFilteringFactor) ;
			gravity[2] = event.values[2] * kFilteringFactor + gravity[2] * (1.0f - kFilteringFactor) ;;

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
        }else if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
            /*
             * 
             * Orientation
             * 
             */
        	Log.d("Icarus", "Capteur d'orientation");
        	TextView oriX = (TextView) findViewById(R.id.oriX);
			oriX.setText(Float.toString(event.values[0]));
			
			TextView oriY = (TextView) findViewById(R.id.oriY);
			oriY.setText(Float.toString(event.values[1]));
			
			TextView oriZ = (TextView) findViewById(R.id.oriZ);
			oriZ.setText(Float.toString(event.values[2]));
			   	
        	
        }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		Log.d("Icarus", "onAccuracyChanged");
	}

	@Override
	public void onLocationChanged(Location loc) {
		
		
		TextView lat = (TextView) findViewById(R.id.lat);
		lat.setText(Double.toString(loc.getLatitude()));
		
		TextView lng = (TextView) findViewById(R.id.lng);
		lng.setText(Double.toString(loc.getLongitude()));
		
		TextView dir = (TextView) findViewById(R.id.dir);
		dir.setText(Float.toString(loc.getBearing()));
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}


	
}
