/**
 * Une activité qui permet de tester la présence des différents capteurs présents sur l'appareil.
 * (tous ne sont pas forcément implémentés)
 * 
 * 
 * @author David Da Silva
 * @version 1.0
 */
package fr.unice.miage.icarus;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.CheckBox;

public class TestActivity extends Activity {

	private SensorManager sm;
	
	public void checkSensorAvailability(){
		
		
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		/*
		 * Types de capteurs possibles (et qui peuvent nous interesser):
		 * 
		 * TYPE_ACCELEROMETER
		 * TYPE_GRAVITY
		 * TYPE_GYROSCOPE
		 * TYPE_LINEAR_ACCELERATION
		 * TYPE_MAGNETIC_FIELD
		 * TYPE_ORIENTATION
		 * TYPE_PRESSURE
		 * TYPE_ROTATION_VECTOR
		 */
		
		if (sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
			CheckBox cb = (CheckBox) findViewById(R.id.AccelerometerCheckBox);
			cb.setChecked(true);
		}
		if (sm.getDefaultSensor(Sensor.TYPE_GRAVITY) != null){
			CheckBox cb = (CheckBox) findViewById(R.id.GravityCheck);
			cb.setChecked(true);
		}
		if (sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
			CheckBox cb = (CheckBox) findViewById(R.id.GyroscopeCheck);
			cb.setChecked(true);
		}
		if (sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null){
			CheckBox cb = (CheckBox) findViewById(R.id.LinearAccelerationCheck);
			cb.setChecked(true);
		}
		if (sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
			CheckBox cb = (CheckBox) findViewById(R.id.MagneticFieldCheck);
			cb.setChecked(true);
		}
		if (sm.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null){
			CheckBox cb = (CheckBox) findViewById(R.id.OrientationCheck);
			cb.setChecked(true);
		}
		if (sm.getDefaultSensor(Sensor.TYPE_PRESSURE) != null){
			CheckBox cb = (CheckBox) findViewById(R.id.PressureCheck);
			cb.setChecked(true);
		}
		if (sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null){
			CheckBox cb = (CheckBox) findViewById(R.id.RotationVectorCheck);
			cb.setChecked(true);
		}
		
		
	}
	
	
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		
		checkSensorAvailability();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_test, menu);
		return true;
	}

}
