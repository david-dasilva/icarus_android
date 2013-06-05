package fr.unice.miage.icarus.activities;

import fr.unice.miage.icarus.FlightSettings;
import fr.unice.miage.icarus.R;
import fr.unice.miage.icarus.R.id;
import fr.unice.miage.icarus.R.layout;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class ConfigurationActivity extends Activity {

	private SensorManager sm;
	private LocationManager locationManager;
	
	private FlightSettings flightSettings;
	private float			updateIntervalInSeconds = 1.0f;
	
	private float currentPitch;
	private float currentRoll;
	private float currentAzimuth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_configuration);
		
		
		SeekBar seekBar = (SeekBar)findViewById(R.id.seekBarUpdateInterval);
		seekBar.setOnSeekBarChangeListener(mySeekBarListener);
		

		
		// Création de l'objet qui contiendra les parametres du vol
		flightSettings = new FlightSettings();
		/*
		 * Récupération de l'id de l'utilisateur
		 */
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			flightSettings.setUserid(extras.getString("userid"));
		}
		
		
		// Acquire a reference to the system SensorManager
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// If GPS is not enabled, throw an alert dialog
		if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			buildAlertMessageNoGps();
		}
		
		/*
		 * Listener bouton Calibrer Azimuth
		 */
		final Button boutonCalibrerAzimuth = (Button)findViewById(R.id.buttonCalibrerAzimuth);
		boutonCalibrerAzimuth.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				buildAlertCalibrerAzimuth();
			}
		});
		
		/*
		 * Listener bouton RAZ pitch et roll
		 */
		final Button boutonRazPitchRoll = (Button)findViewById(R.id.buttonResetPitchRoll);
		boutonRazPitchRoll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				buildAlertMessageRazPitchRoll();
			}
		});
		
		/*
		 * Listener bouton Calibrer Altitude
		 */
		
		
		/*
		 *  Listener pour le bouton "demarrer enregistrement"
		 */
		final Button button = (Button)findViewById(R.id.buttonStartRecording);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				storeSettings();
			}
		});
		
		
		//TODO Remove this
		enableRecordingButton();
		/*
		 * 
		 * 
		 * DEBUG
		 * 
		 */
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
	}
	
	public void unregisterListeners(){		
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
	
	
	
	
	
	
	private void storeSettings(){
		
		final EditText flightNameField = (EditText) findViewById(R.id.editTextFlightName);
		final EditText piloteField = (EditText) findViewById(R.id.editTextPilote);
		final EditText avionField = (EditText) findViewById(R.id.editTextAvion);
		final EditText departField = (EditText) findViewById(R.id.editTextDepart);
		final EditText notesField = (EditText) findViewById(R.id.editTextRemarques);
		
		
		flightSettings.setFlightName(flightNameField.getText().toString());
		flightSettings.setPilot(piloteField.getText().toString());
		flightSettings.setAircraft(avionField.getText().toString());
		flightSettings.setDepart(departField.getText().toString());
		flightSettings.setNotes(notesField.getText().toString());
		
		flightSettings.setDevice(android.os.Build.MODEL);
		flightSettings.setUpdateIntervalInSeconds(updateIntervalInSeconds);
		
		//TODO : store calibration settings.
		
		// Appel de l'activité d'enregistrement
		Intent enregistrementIntent = new Intent(this, EnregistrementActivity.class);
		enregistrementIntent.putExtra("settings", flightSettings);
		
		startActivity(enregistrementIntent);
		finish();
	}
	
	
	
	
	
	
	
	private void resetPitchRoll(){
		
		float correctionPitch = currentPitch * -1.0f;
		float correctionRoll = currentRoll * -1.0f;
		flightSettings.setCorrectionPitch(correctionPitch);
		flightSettings.setCorrectionRoll(correctionRoll);
		
		
		
		Log.d("Icarus","Pitch initial : "+flightSettings.getCorrectionPitch()+" Roll initial :"+flightSettings.getCorrectionRoll());
		
		Toast toast = Toast.makeText(this, "Inclinaison enregistrée", Toast.LENGTH_SHORT);
		toast.show();
		
	}
	
	
	private void correctAzimuth(float vraicap){
		float correctionAzimuth = currentAzimuth - vraicap;

		// Si le cap calculé est superieur au cap réel la correction doit être négative!
		if(vraicap < currentAzimuth)
			correctionAzimuth = correctionAzimuth *-1.0f;
		
		flightSettings.setCorrectionAzimuth(correctionAzimuth);
		Log.d("Icarus","correction azimuth : "+String.valueOf(correctionAzimuth));
	}
	
	
	
	
	
	/**
	 * Affiche une AlertBox pour remetre à 0 le pitch et le roll
	 */
	private void buildAlertMessageRazPitchRoll(){
		
		new AlertDialog.Builder(this)
		.setTitle("Raz Pitch et Roll")
		.setMessage("Posez le téléphone sur son socle et appuyez sur le bouton pour enregister l'inclinaison initiale.")
		.setNeutralButton("Remise à Zéro", onRazPitchRoll)
		.show();
	}
	
	
	private void buildAlertCalibrerAzimuth(){
		
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.azimuth_calibration, (ViewGroup)findViewById(R.layout.activity_configuration));
		final EditText azimuthField = (EditText)layout.findViewById(R.id.editTextAzimuth);
		final TextView azimuthView	= (TextView)layout.findViewById(R.id.textViewCurrentAzimuth);
		azimuthView.setText(String.valueOf(currentAzimuth));
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
										.setView(layout)
										.setTitle("Calibrer la boussole")
										.setMessage("Indiquer le cap réel en degrées (Nord = 0, Sud = 180)")
										.setNegativeButton("Annuler", null)
										.setPositiveButton("Corriger", new OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												String correctAzimuth = azimuthField.getText().toString();
												Log.d("Icarus","valeur du champ azimuth : "+correctAzimuth);
												correctAzimuth(Float.parseFloat(correctAzimuth));
											}
										});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
		
		final SeekBar sb = (SeekBar)layout.findViewById(R.id.seekBarAzimuth);
		sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				azimuthField.setText(String.valueOf(progress));
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});
		
	}
	
	
	/**
	 * Affiche un message d'alerte si le GPS n'est pas activé et renvoie vers
	 * les parametres pour le faire.
	 * @source StackOverflow
	 */
	private void buildAlertMessageNoGps(){
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Votre GPS semble désactivé, voulez-vous l'activer?")
			.setCancelable(false)
			.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			})
			.setNegativeButton("Non", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
		final AlertDialog alert = builder.create();
		alert.show();
	}
	
	
	private void updateIntervalValue(int interval){
		
		Log.d("Icarus", "maj interval, value : "+ interval);
		
		float updateInterval = (float) interval;
		
		// Le minimum de la SeekBar est 0, je la transforme en 0.5 secondes
		if(updateInterval == 0) updateInterval = 0.5f;
		
		/*
		 * Mise a jour du libellé
		 */
		TextView seekBarValue = (TextView)findViewById(R.id.textViewUpdateIntervalValue);
		seekBarValue.setText(updateInterval+" s");
		
		updateIntervalInSeconds = updateInterval;
	}
	
	
	private void enableRecordingButton(){
		Button button = (Button)findViewById(R.id.buttonStartRecording);
		button.setEnabled(true);
	}
	
	/*
	 * 
	 * Listeners
	 * 
	 * 
	 */
	
	
	/**
	 * Custom SensorEventListener for the Orientation Sensor
	 */
	private SensorEventListener myOriSensorListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			currentAzimuth	= event.values[0];
			currentPitch	= event.values[1];
			currentRoll		= event.values[2];
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
	
	

	/**
	 * Custom onClickListener pour le Dialog RAZ Pitch&Roll
	 */
	private DialogInterface.OnClickListener onRazPitchRoll = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			resetPitchRoll();
		}
	};
	

	
	private LocationListener myLocListener = new LocationListener() {
		
		@Override
		public void onLocationChanged(Location location) {
			// On attend d'avoir une position renseignée par le GPS
			// une fois la position obtenue, on active le bouton
			enableRecordingButton();
		}
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		
		@Override
		public void onProviderEnabled(String provider) {}
		
		@Override
		public void onProviderDisabled(String provider) {}
	};
	
	
	/**
	 * Listener qui permet de récuperer la valeur de la seekBar et de l'afficher
	 */
	private OnSeekBarChangeListener mySeekBarListener = new OnSeekBarChangeListener() {
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			Log.d("Icarus", "seekBar changing!");
			updateIntervalValue(progress);
		}
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}
	};

}


