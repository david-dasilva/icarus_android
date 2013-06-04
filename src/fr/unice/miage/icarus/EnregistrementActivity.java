package fr.unice.miage.icarus;



import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class EnregistrementActivity extends SherlockActivity{

	private SensorManager sm;
	private LocationManager locationManager;
	
	private FlightLogger logger;
	
	private long lastUpdateTime = 0;
	private int updateFrequency = 1000; // In milliseconds
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_values);
		Log.d("Icarus", "TestValuesActivity started");
		
		// Acquire a reference to the system SensorManager
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		/*
		 *  Flight Logger (for the file)
		 */
		DateFormat dateFomat = new SimpleDateFormat("yyyyMMdd-HHmm");
		Date date = new Date();
		String flightname = "vol_"+dateFomat.format(date);
		String updateFreqInSeconds = String.valueOf(updateFrequency/1000.0);
		String author = "David Da Silva";
		String aircraft = "ULM";
		String depart = "Aeroport de Mandelieu"; 
		String device = android.os.Build.MODEL; 
		String misc = "Ceci est un vol de test";
		
		logger = new FlightLogger(this,flightname,author,updateFreqInSeconds, aircraft, depart, device, misc);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerListeners();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterListeners();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterListeners();
	}
	
	private void registerListeners(){
		
		/*
		 * Accelerometer
		 */
//		if (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){	
//			sm.registerListener(
//					myAccSensorListener, 
//					sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//					sm.SENSOR_DELAY_NORMAL);
//		}
		
		/*
		 * Orientation
		 */
		if (sm.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null){	
			sm.registerListener(
					myOriSensorListener, 
					sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), 
					sm.SENSOR_DELAY_NORMAL);
		}
		
		/*
		 * Position
		 */
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateFrequency, 0, myLocListener); // GPS
//		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, updateFrequency, 0, myLocListener); // NETWORK
		//TODO: disable network for quicker position lock.
	}
	
	public void unregisterListeners(){
		/*
		 * Accelerometer
		 */
//		if (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){	
//			sm.unregisterListener(myAccSensorListener);
//		}
		
		/*
		 * Orientation
		 */
		if (sm.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null){	
			sm.unregisterListener(myOriSensorListener);
		}
		
		/*
		 * Position
		 */
		locationManager.removeUpdates(myLocListener);
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
        	Intent intent3 = new Intent(this, EnregistrementActivity.class);
        	startActivity(intent3);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
		}
	}
	

	
	/**
	 * Affiche les valeurs de l'accelerometre sur l'écran de test
	 * @param x
	 * @param y
	 * @param z
	 */
//	public void updateAccValues(float x, float y, float z){
//		
//		TextView accX = (TextView) findViewById(R.id.accX);
//		accX.setText(Float.toString(x));
//		
//		TextView accY = (TextView) findViewById(R.id.accY);
//		accY.setText(Float.toString(y));
//		
//		TextView accZ = (TextView) findViewById(R.id.accZ);
//		accZ.setText(Float.toString(z));
//		
//		logger.setAcceleration(x, y, z);
//		
//	}
	
	/**
	 * Affiche les valeurs des capteurs d'orientation sur l'écran de test
	 * @param x
	 * @param y
	 * @param z
	 */
	private void updateOriValues(float x, float y, float z){
    	TextView oriX = (TextView) findViewById(R.id.oriX);
		oriX.setText(Float.toString(x));
		
		TextView oriY = (TextView) findViewById(R.id.oriY);
		oriY.setText(Float.toString(y));
		
		TextView oriZ = (TextView) findViewById(R.id.oriZ);
		oriZ.setText(Float.toString(z));
		
		logger.setOrientation(x, y, z);
	}
	/**
	 * Affiche les valeurs des capteurs de position sur l'écran de test
	 * @param loc
	 */
	private void updatePosValues(Location loc){
		TextView lat = (TextView) findViewById(R.id.lat);
		lat.setText(Double.toString(loc.getLatitude()));
		
		TextView lng = (TextView) findViewById(R.id.lng);
		lng.setText(Double.toString(loc.getLongitude()));
		
		TextView dir = (TextView) findViewById(R.id.dir);
		dir.setText(Float.toString(loc.getBearing()));
		
		TextView alt = (TextView) findViewById(R.id.alt);
		alt.setText(Double.toString(loc.getAltitude()));
		
		logger.setLocation(loc);
	}
	
	/**
	 * Custom SensorEventListener for the Accelerometer Sensor
	 */
//	private SensorEventListener myAccSensorListener = new SensorEventListener() {
//		
//		private float[] gravity = new float[10];
//		private float[] linear_acceleration = new float[10];
//		
//		@Override
//		public void onSensorChanged(SensorEvent event) {
//
//			// In this example, alpha is calculated as t / (t + dT),
//			// where t is the low-pass filter's time-constant and
//			// dT is the event delivery rate.
//		
//			/*
//			 * Filtrage passe-bas
//			 */
//			final float kFilteringFactor = 0.1f;
//
//			// Isolate the force of gravity with the low-pass filter.
//			gravity[0] = event.values[0] * kFilteringFactor + gravity[0] * (1.0f - kFilteringFactor) ;
//			gravity[1] = event.values[1] * kFilteringFactor + gravity[1] * (1.0f - kFilteringFactor) ;
//			gravity[2] = event.values[2] * kFilteringFactor + gravity[2] * (1.0f - kFilteringFactor) ;;
//
//			// Remove the gravity contribution with the high-pass filter.
//			linear_acceleration[0] = event.values[0] - gravity[0];
//			linear_acceleration[1] = event.values[1] - gravity[1];
//			linear_acceleration[2] = event.values[2] - gravity[2];
//			
//			updateAccValues(linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]);
//		}
//		@Override
//		public void onAccuracyChanged(Sensor sensor, int accuracy) {	
//		}
//	};
	
	/**
	 * Custom SensorEventListener for the Orientation Sensor
	 */
	private SensorEventListener myOriSensorListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			updateOriValues(event.values[0], event.values[1], event.values[2]);
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	/**
	 * Custom LocationListener
	 */
	private LocationListener myLocListener = new LocationListener() {
		
		@Override
		public void onLocationChanged(Location location) {
			updatePosValues(location);
		}
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
		
		@Override
		public void onProviderEnabled(String provider) {
		}
		
		@Override
		public void onProviderDisabled(String provider) {
		}
		
	};
}
