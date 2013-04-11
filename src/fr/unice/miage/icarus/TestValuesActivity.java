package fr.unice.miage.icarus;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

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
	private LocationManager locationManager;
	private float[] gravity = new float[10];
	private float[] linear_acceleration = new float[10];
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_values);
		Log.d("Icarus", "TestValuesActivity started");
		
		// Acquire a reference to the system SensorManager
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		registerListeners();
	}

	private void registerListeners(){
		
		if (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){	
			sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
		}
		if (sm.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null){	
			sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
		}
		
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
	

	/**
	 * A defaut de faire un SensorEventListener différent par capteur, on utilise le même
	 * sur lequel on va tester le type d'evenement
	 */
	@Override
	public void onSensorChanged(SensorEvent event){
		
		// Récupere le type de capteur qui a généré l'évenement
		Sensor sensor = event.sensor;
        
		/*
		 * Accelerometre
		 */
		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			// In this example, alpha is calculated as t / (t + dT),
			// where t is the low-pass filter's time-constant and
			// dT is the event delivery rate.
		
			/*
			 * Filtrage passe-bas
			 */
			final float kFilteringFactor = 0.1f;

			// Isolate the force of gravity with the low-pass filter.
			gravity[0] = event.values[0] * kFilteringFactor + gravity[0] * (1.0f - kFilteringFactor) ;
			gravity[1] = event.values[1] * kFilteringFactor + gravity[1] * (1.0f - kFilteringFactor) ;
			gravity[2] = event.values[2] * kFilteringFactor + gravity[2] * (1.0f - kFilteringFactor) ;;

			// Remove the gravity contribution with the high-pass filter.
			linear_acceleration[0] = event.values[0] - gravity[0];
			linear_acceleration[1] = event.values[1] - gravity[1];
			linear_acceleration[2] = event.values[2] - gravity[2];

			// affichage des données
			displayAccValues(linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]);
			
			
			
        }
		else if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
			/*
             * Orientation
             */
			displayOriValues(event.values[0], event.values[1], event.values[2]);
        }
	}

	/**
	 * Affiche les valeurs de l'accelerometre sur l'écran de test
	 * @param x
	 * @param y
	 * @param z
	 */
	private void displayAccValues(float x, float y, float z){
		
		TextView accX = (TextView) findViewById(R.id.accX);
		accX.setText(Float.toString(x));
		
		TextView accY = (TextView) findViewById(R.id.accY);
		accY.setText(Float.toString(y));
		
		TextView accZ = (TextView) findViewById(R.id.accZ);
		accZ.setText(Float.toString(z));
		
	}
	
	/**
	 * Affiche les valeurs des capteurs d'orientation sur l'écran de test
	 * @param x
	 * @param y
	 * @param z
	 */
	private void displayOriValues(float x, float y, float z){
		Log.d("Icarus", "Capteur d'orientation");
    	TextView oriX = (TextView) findViewById(R.id.oriX);
		oriX.setText(Float.toString(x));
		
		TextView oriY = (TextView) findViewById(R.id.oriY);
		oriY.setText(Float.toString(y));
		
		TextView oriZ = (TextView) findViewById(R.id.oriZ);
		oriZ.setText(Float.toString(z));
	}
	/**
	 * Affiche les valeurs des capteurs de position sur l'écran de test
	 * @param loc
	 */
	private void displayPosValues(Location loc){
		TextView lat = (TextView) findViewById(R.id.lat);
		lat.setText(Double.toString(loc.getLatitude()));
		
		TextView lng = (TextView) findViewById(R.id.lng);
		lng.setText(Double.toString(loc.getLongitude()));
		
		TextView dir = (TextView) findViewById(R.id.dir);
		dir.setText(Float.toString(loc.getBearing()));
		
		TextView alt = (TextView) findViewById(R.id.alt);
		alt.setText(Double.toString(loc.getAltitude()));
	}
	
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		Log.d("Icarus", "onAccuracyChanged");
	}

	@Override
	public void onLocationChanged(Location loc) {
		
		displayPosValues(loc);
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


	
	private void writeData(Context ctx, String data){
		FileOutputStream fOut = null;
		OutputStreamWriter osw = null;

		try{
			fOut = ctx.openFileOutput("enregistreurvol.txt",MODE_APPEND);
			osw = new OutputStreamWriter(fOut);
			osw.write(data);
			osw.flush();
			osw.close();
			fOut.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
}
