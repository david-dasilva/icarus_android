package fr.unice.miage.icarus.activities;



import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import fr.unice.miage.icarus.FlightLogger;
import fr.unice.miage.icarus.FlightSettings;
import fr.unice.miage.icarus.R;
import fr.unice.miage.icarus.R.id;
import fr.unice.miage.icarus.R.layout;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class EnregistrementActivity extends Activity{

	private SensorManager sm;
	private LocationManager locationManager;
	
	private FlightLogger logger;
	
	
	private FlightSettings flightSettings;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enregistrement);
		Log.d("Icarus", "TestValuesActivity started");
		
		// Acquire a reference to the system SensorManager
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		/*
		 *  récupération des settings
		 */
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			flightSettings  = (FlightSettings)extras.get("settings");
		}
		
		logger = new FlightLogger(this,
				flightSettings.getFlightName(),
				flightSettings.getPilot(),
				String.valueOf(flightSettings.getUpdateIntervalInSeconds()),
				flightSettings.getAircraft(),
				flightSettings.getDepart(),
				flightSettings.getDevice(),
				flightSettings.getNotes());
		
		// Bouton arreter
		final Button boutonArreter = (Button) findViewById(R.id.buttonStopRecording);
		boutonArreter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopRecording();
			}
		});
		
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
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, flightSettings.getUpdateIntervalInMillis(), 0, myLocListener); // GPS
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
	
	
	
	private void stopRecording() {
		unregisterListeners();
		logger.close();
		//TODO : créer activité + interface fin d'enregistrement
	}
	
	

	
	/**
	 * Affiche les valeurs des capteurs d'orientation sur l'écran de test
	 * @param x
	 * @param y
	 * @param z
	 */
	private void updateOriValues(float x, float y, float z){
    	TextView oriX = (TextView) findViewById(R.id.textViewAzimuth);
		oriX.setText(String.format("%.2f",x));
		
		TextView oriY = (TextView) findViewById(R.id.textViewPitch);
		oriY.setText(String.format("%.2f",y));
		
		TextView oriZ = (TextView) findViewById(R.id.textViewRoll);
		oriZ.setText(String.format("%.2f",z));
		
		logger.setOrientation(x, y, z);
	}
	/**
	 * Affiche les valeurs des capteurs de position sur l'écran de test
	 * @param loc
	 */
	private void updatePosValues(Location loc){
		TextView lat = (TextView) findViewById(R.id.textViewLat);
		lat.setText(Double.toString(loc.getLatitude()));
		
		TextView lng = (TextView) findViewById(R.id.textViewLng);
		lng.setText(Double.toString(loc.getLongitude()));
		
		TextView dir = (TextView) findViewById(R.id.textViewDirection);
		dir.setText(String.format("%.2f",loc.getBearing()));
		
		TextView alt = (TextView) findViewById(R.id.textViewAltitude);
		alt.setText( String.format("%.2f", loc.getAltitude()));
		
		logger.setLocation(loc);
	}
	
	
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