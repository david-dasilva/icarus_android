package fr.unice.miage.icarus.activities;



import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
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
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import fr.unice.miage.icarus.FlightLogger;
import fr.unice.miage.icarus.FlightSettings;
import fr.unice.miage.icarus.R;

public class EnregistrementActivity extends Activity{

	private SensorManager sm;
	private LocationManager locationManager;
	
	private FlightLogger logger;
	private FlightSettings flightSettings;
	
	
    // angular speeds from gyro
    private float[] gyro = new float[3];
 
    // rotation matrix from gyro data
    private float[] gyroMatrix = new float[9];
 
    // orientation angles from gyro matrix
    private float[] gyroOrientation = new float[3];
 
    // magnetic field vector
    private float[] magnet = new float[3];
 
    // accelerometer vector
    private float[] accel = new float[3];
 
    // orientation angles from accel and magnet
    private float[] accMagOrientation = new float[3];
 
    // final orientation angles from sensor fusion
    private float[] fusedOrientation = new float[3];
 
    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];
    
    public static final float EPSILON = 0.000000001f;
    private static final float NS2S = 1.0f / 1000000000.0f;
	private float timestamp;
	private boolean initState = true;
    
	public static final int TIME_CONSTANT = 30;
	public static final float FILTER_COEFFICIENT = 0.98f;
	private Timer fuseTimer = new Timer();
	public Handler mHandler;
	
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_enregistrement);
		Log.d("Icarus", "EnregistrementActivity started");
		
		
		
        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;
 
        // initialise gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;
		
		
		
		
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
		
		/*
		 * Création du Logger
		 */
		if (logger == null){ // FIX: évite de créer plusieurs fois le logger.
			logger = new FlightLogger(this,
					flightSettings.getFlightName(),
					flightSettings.getPilot(),
					String.valueOf(flightSettings.getUpdateIntervalInSeconds()),
					flightSettings.getAircraft(),
					flightSettings.getDepart(),
					flightSettings.getDevice(),
					flightSettings.getNotes(),
					flightSettings.usePressure());
		}
		
		
		// wait for one second until gyroscope and magnetometer/accelerometer
        // data is initialised then scedule the complementary filter task
        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),1000, TIME_CONSTANT);
		
        mHandler = new Handler();
		
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
		 * Accelerometre
		 */
		if(sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
			sm.registerListener(fusedSensorListener,
					sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_FASTEST);
		}
		
		/*
		 * Gyroscope
		 */
		if(sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
			sm.registerListener(fusedSensorListener,
					sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
					SensorManager.SENSOR_DELAY_FASTEST);
		}
		
		/*
		 * Magnetic field
		 */
		if(sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
			sm.registerListener(fusedSensorListener,
					sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
					SensorManager.SENSOR_DELAY_FASTEST);
		}
		
		
		/*
		 * Orientation
		 */
//		if (sm.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null){	
//			sm.registerListener(
//					myOriSensorListener, 
//					sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), 
//					SensorManager.SENSOR_DELAY_NORMAL);
//		}
		
		/*
		 * Position
		 */
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, flightSettings.getUpdateIntervalInMillis(), 0, myLocListener); // GPS
//		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, updateFrequency, 0, myLocListener); // NETWORK
		
		/*
		 * Pression
		 */
		if(sm.getDefaultSensor(Sensor.TYPE_PRESSURE) != null){
			sm.registerListener(myPreSensorListener,
					sm.getDefaultSensor(Sensor.TYPE_PRESSURE),
					SensorManager.SENSOR_DELAY_NORMAL);
			
		}
		
	}
	
	public void unregisterListeners(){
		
		/*
		 * Fused Sensor
		 */
		sm.unregisterListener(fusedSensorListener);
		
		/*
		 * Orientation
		 */
		//sm.unregisterListener(myOriSensorListener);
		
		/*
		 * Position
		 */
		locationManager.removeUpdates(myLocListener);
		
		/*
		 * Pression
		 */
		sm.unregisterListener(myPreSensorListener);
	}
	
	
	
	private void stopRecording() {
		unregisterListeners();
		flightSettings.setLogFile(logger.close());
		
		// Appel de l'activité d'enregistrement
		Intent transmissionIntent = new Intent(this, TransmissionActivity.class);
		transmissionIntent.putExtra("settings", flightSettings);
		
		startActivity(transmissionIntent);
		finish();
	}
	
	

	
	/**
	 * Affiche les valeurs des capteurs d'orientation sur l'écran de test
	 * @param x
	 * @param y
	 * @param z
	 */
	private void updateOriValues(){

		float azimuth	= (float) (fusedOrientation[0] * 180/Math.PI);
		float pitch		= (float) (fusedOrientation[1] * 180/Math.PI);
		float roll		= (float) (fusedOrientation[2] * 180/Math.PI);
		
		
		
		float correctedAzimuth	= azimuth + flightSettings.getCorrectionAzimuth();
		if (correctedAzimuth > 360.0f)
			correctedAzimuth -= 360.0f;
		if (correctedAzimuth < 0.0f)
			correctedAzimuth += 360.0f;
		
		
		float correctedPitch	= pitch + flightSettings.getCorrectionPitch();
		if(correctedPitch > 180.0f)
			correctedPitch -= 360.0f;
		if(correctedPitch < -180.0f)
			correctedPitch += 360.0f;
		float correctedRoll		= roll + flightSettings.getCorrectionRoll();
		if(correctedRoll > 90.0f)
			correctedRoll -=180.0f;
		if(correctedRoll < -90.0f)
			correctedRoll +=180.0f;
		
    	TextView oriX = (TextView) findViewById(R.id.textViewAzimuth);
		oriX.setText(String.format("%.2f",correctedAzimuth));
		
		TextView oriY = (TextView) findViewById(R.id.textViewPitch);
		oriY.setText(String.format("%.2f",correctedPitch));
		
		TextView oriZ = (TextView) findViewById(R.id.textViewRoll);
		oriZ.setText(String.format("%.2f",correctedRoll));
		
		logger.setOrientation(correctedAzimuth, correctedPitch, correctedRoll);
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
		
		logger.setLocation(loc);
	}
	
	
	private void updateAltitude(float height){
		
		float altitudeCorrigée = height + flightSettings.getCorrectionAltitude();
		
		if(flightSettings.usePressure()){
			Log.d("Icarus", "Altitude via pression :"+String.format("%.2f", height));
			Log.d("Icarus", "Altitude via pression après correction :"+String.format("%.2f", altitudeCorrigée));
		}
		else {
			Log.d("Icarus", "Altitude via gps :"+String.format("%.2f", height));
			Log.d("Icarus", "Altitude via gps après correction :"+String.format("%.2f", altitudeCorrigée));
		} 
		
		TextView alt = (TextView) findViewById(R.id.textViewAltitude);
		alt.setText( String.format("%.2f", altitudeCorrigée));
		
		logger.setAltitude(altitudeCorrigée);
	}
	
	
	
	/*
	 * Fused Sensor functions
	 */
	
	/**
	 * Calculate orientation angles from accelerometer and magnetometer output
	 */
	public void calculateAccMagOrientation(){
		if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
			SensorManager.getOrientation(rotationMatrix, accMagOrientation);
		}
	}
	
	
	/**
	 * Calculates a rotation vector from the gyroscope angular speed values
	 * @source : Android references
	 */
	public void getRotationVectorFromGyro(float[] gyroValues,
			float[] deltaRotationVector,
			float timeFactor)
	{
		float[] normValues = new float[3];
		
		// calculate the angular speed of the sample
		float omegaMagnitude = 
				(float)Math.sqrt(gyroValues[0] * gyroValues[0] +
								gyroValues[1] * gyroValues[1] +
								gyroValues[2] * gyroValues[2]);
		
		// Normalize the rotation vector if it's big enough to get the axis
		if (omegaMagnitude > EPSILON) {
			normValues[0] = gyroValues[0] / omegaMagnitude;
			normValues[1] = gyroValues[1] / omegaMagnitude;
			normValues[2] = gyroValues[2] / omegaMagnitude;
		}
		
		
		/*
		 * Integrate around this axis with the angular speed by the timestep
		 *  in order to get a delta rotation from this sample over the timestep
		 *  We will convert this axis-angle representation of the delta rotation
		 *  into a quaternion before turning it into the rotation matrix.
		 */
		float thetaOverTwo		= omegaMagnitude * timeFactor;
		float sinThetaOverTwo	= (float)Math.sin(thetaOverTwo);
		float cosThetaOverTwo	= (float)Math.cos(thetaOverTwo);
		deltaRotationVector[0]	= sinThetaOverTwo * normValues[0];
		deltaRotationVector[1]	= sinThetaOverTwo * normValues[1];
		deltaRotationVector[2]	= sinThetaOverTwo * normValues[2];
		deltaRotationVector[3]	= cosThetaOverTwo;
	}
	
	
	/**
	 * This function performs the integration of the gyroscope data.
	 * It writes the gyroscope based orientation into gyroOrientation
	 */
	public void gyroFunction(SensorEvent event){
		
		// don't start until first accelerometer/magnetometer orientation has been acquired
		if(accMagOrientation == null)
			return;
		
		// initialisation of the gyroscope based rotation matrix
		if(initState){
			float[] initMatrix = new float[9];
			initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
			float[] test = new float[3];
			SensorManager.getOrientation(initMatrix, test);
			gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
			initState = false;
		}
		
		/*
		 * copy the new gyro values into the gyro array
		 * convert the raw gyro data into a rotation vector
		 */
		float[] deltaVector = new float[4];
		if(timestamp != 0){
			final float dT = (event.timestamp - timestamp) * NS2S;
			System.arraycopy(event.values, 0, gyro, 0, 3);
			getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
		}
		
		// measurement done, save current time for next interval
		timestamp = event.timestamp;
		
		// convert rotation vector into rotation matrix
		float[] deltaMatrix = new float[9];
		SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);
		
		
		// apply the new rotation interval on the gyroscope based rotation matrix
		gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);
		
		// get the gyroscope based orientation from the rotation matrix
		SensorManager.getOrientation(gyroMatrix, gyroOrientation);
	}
	
	
	private float[] getRotationMatrixFromOrientation(float[] o){
		float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];
     
        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);
     
        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;
     
        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;
     
        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;
     
        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
	}
	
	private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];
     
        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];
     
        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];
     
        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];
     
        return result;
    }
	
	
	/*
	 * 
	 * Time class
	 * 
	 * 
	 */
	
	class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
            
            /*
             * Fix for 179° <--> -179° transition problem:
             * Check whether one of the two orientation angles (gyro or accMag) is negative while the other one is positive.
             * If so, add 360° (2 * math.PI) to the negative value, perform the sensor fusion, and remove the 360° from the result
             * if it is greater than 180°. This stabilizes the output in positive-to-negative-transition cases.
             */
            
            // azimuth
            if (gyroOrientation[0] < -0.5 * Math.PI && accMagOrientation[0] > 0.0) {
            	fusedOrientation[0] = (float) (FILTER_COEFFICIENT * (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[0]);
        		fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[0] < -0.5 * Math.PI && gyroOrientation[0] > 0.0) {
            	fusedOrientation[0] = (float) (FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * (accMagOrientation[0] + 2.0 * Math.PI));
            	fusedOrientation[0] -= (fusedOrientation[0] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
            	fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
            }
            
            // pitch
            if (gyroOrientation[1] < -0.5 * Math.PI && accMagOrientation[1] > 0.0) {
            	fusedOrientation[1] = (float) (FILTER_COEFFICIENT * (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[1]);
        		fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[1] < -0.5 * Math.PI && gyroOrientation[1] > 0.0) {
            	fusedOrientation[1] = (float) (FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * (accMagOrientation[1] + 2.0 * Math.PI));
            	fusedOrientation[1] -= (fusedOrientation[1] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
            	fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
            }
            
            // roll
            if (gyroOrientation[2] < -0.5 * Math.PI && accMagOrientation[2] > 0.0) {
            	fusedOrientation[2] = (float) (FILTER_COEFFICIENT * (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[2]);
        		fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[2] < -0.5 * Math.PI && gyroOrientation[2] > 0.0) {
            	fusedOrientation[2] = (float) (FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * (accMagOrientation[2] + 2.0 * Math.PI));
            	fusedOrientation[2] -= (fusedOrientation[2] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
            	fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
            }
     
            // overwrite gyro matrix and orientation with fused orientation
            // to comensate gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
            
            
            // update sensor output in GUI
            mHandler.post(updateOreintationDisplayTask);
        }
    }

    
    private Runnable updateOreintationDisplayTask = new Runnable() {
		public void run() {
			updateOriValues();
			//updateOreintationDisplay();
		}
	};
	
	
	
	/*
	 * 
	 * Listeners Classes
	 * 
	 */
	
	/**
	 * Custom listener for the "fused sensor" : Accelerometer + Gyro + magnetic field
	 */
	private SensorEventListener fusedSensorListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			
			switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				// copy new accelerometer data into accel array anc calculate orientation
				System.arraycopy(event.values, 0, accel, 0, 3);
				calculateAccMagOrientation();
				break;
				
			case Sensor.TYPE_GYROSCOPE:
				//process gyro data
				gyroFunction(event);
				break;
				
			case Sensor.TYPE_MAGNETIC_FIELD:
				System.arraycopy(event.values, 0, magnet, 0, 3);
				break;
			}
			
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
	
	
	/**
	 * Custom SensorEventListener for the Orientation Sensor
	 */
//	private SensorEventListener myOriSensorListener = new SensorEventListener() {
//		
//		@Override
//		public void onSensorChanged(SensorEvent event) {
//			updateOriValues(event.values[0], event.values[1], event.values[2]);
//		}
//		
//		@Override
//		public void onAccuracyChanged(Sensor sensor, int accuracy) {
//		}
//	};

	/**
	 * Custom LocationListener
	 */
	private LocationListener myLocListener = new LocationListener() {
		
		@Override
		public void onLocationChanged(Location location) {
			updatePosValues(location);
			/*
			 * Si on n'utilise pas le capteur de pression, l'altitude est donnée par le GPS
			 */
			if(!flightSettings.usePressure()){
				updateAltitude((float)location.getAltitude());
			}
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
	
	
	/**
	 * Custom Sensor listener for Pressure
	 */
	private SensorEventListener myPreSensorListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {

			float pressure_value	= 0.0f;
			float altitude			= 0.0f;
			// récupération de la valeur de pression actuelle
			pressure_value = event.values[0];
			
			/*
			 * Calcule la hauteur via la différence entre 2 pressions P0 et P1
			 * P0 est le QNH. Par defaut c'est la pression atmosphérique moyenne 1013.25hPa
			 * sauf si l'utilisateur l'a corrigé.
			 * P1 est la pression actuelle
			 */
			altitude = SensorManager.getAltitude(flightSettings.getQNH(), pressure_value);
			
			// Si on utilise le capteur de pression (normalement s'il est dispo, on l'utilise)
			if(flightSettings.usePressure())
				updateAltitude(altitude);
			
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
}
