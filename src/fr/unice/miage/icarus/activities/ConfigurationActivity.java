package fr.unice.miage.icarus.activities;

import fr.unice.miage.icarus.FlightSettings;
import fr.unice.miage.icarus.R;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
	private float altitudeP0;
	private boolean altitudeP0Set = false;
	
	private boolean calibrationModePressure = false;
	
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
		final Button boutonCalibrerAltitude = (Button)findViewById(R.id.buttonCalibrerAltitude);
		boutonCalibrerAltitude.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				buildAlertCalibrerAltitude();
			}
		});
		
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
		
		
		//////////////////////////////////////////
		//TODO REMOVE THIS
		//enableRecordingButton();
		//////////////////////////////////////////
		
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
					SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		/*
		 * Pression
		 */
		if(sm.getDefaultSensor(Sensor.TYPE_PRESSURE) != null){
			// Nous avons un capteur de pression!
			flightSettings.setUsePressure(true);
			Log.d("Icarus", "Utilisation du capteur de pression");
			
			sm.registerListener(myPreSensorListener,
					sm.getDefaultSensor(Sensor.TYPE_PRESSURE),
					SensorManager.SENSOR_DELAY_NORMAL);

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
		
		/*
		 * Pression
		 */
		if(sm.getDefaultSensor(Sensor.TYPE_PRESSURE) != null){
			sm.unregisterListener(myPreSensorListener);
		}
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
	
	
	private void buildAlertCalibrerAltitude(){
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.altitude_calibration, (ViewGroup)findViewById(R.layout.activity_configuration));
		
		final EditText altitudeField	= (EditText)layout.findViewById(R.id.editTextAltitude);
		final TextView altitudeHint		= (TextView)layout.findViewById(R.id.textViewUnitHint);
		final RadioGroup rGroup			= (RadioGroup)layout.findViewById(R.id.radioGroupModeCalibration);
		final RadioButton	rPression	= (RadioButton)layout.findViewById(R.id.radioPression);
		
		
		if (!flightSettings.usePressure()){
			rPression.setEnabled(false);
		}
		
		
		
		rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// récuperation du bouton qui a subit un changement
				RadioButton checkedRadioButton = (RadioButton)rGroup.findViewById(checkedId);
				if(checkedRadioButton.isChecked()){
					
					// bouton "utiliser l'altitude
					if (checkedId == R.id.radioAltitude){
						// Changement du texte
						altitudeHint.setText(getString(R.string.hintAltitude));
						altitudeField.setText(String.valueOf(flightSettings.getCorrectionAltitude()));
						setCorrectionModePressure(false);
					}
					// bouton "utiliser la pression
					if (checkedId == R.id.radioPression){
						// Changement du texte
						altitudeHint.setText(getString(R.string.hintPressure));
						altitudeField.setText(String.valueOf(flightSettings.getQNH()));
						setCorrectionModePressure(true);
					}
				}
			}
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
										.setView(layout)
										.setTitle("Calibration de l'Altimètre")
										.setNegativeButton("Annuler", null)
										.setPositiveButton("Calibrer", new OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												String altitudeFieldValue = altitudeField.getText().toString();
												if(altitudeField.length() > 0){
													Log.d("Icarus","valeur du champ altitude : "+altitudeFieldValue);
													
													float altitude = 0.0f;
													try {
														altitude = Float.parseFloat(altitudeFieldValue);
													} catch (NumberFormatException e){
														
													}
													setCorrectionAltitude(altitude);
												}
												
											}
										})
										.setNeutralButton("Re-initialiser", new OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												setCorrectionModePressure(true);
												setCorrectionAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE);
												
											}
										});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();

		// Déplacer ici listener sur les elements contenus dans le dialog?
		
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
	
	private void setCorrectionModePressure(boolean isPressure){
		this.calibrationModePressure = isPressure;
		if (isPressure){
			Log.d("Icarus", "Mode de calibration de l'altitude : Pression");
			// RAZ de la correction par l'altitude
			//flightSettings.setAltitudeReelleInitiale(0.0f);
		}
		else{
			Log.d("Icarus", "Mode de calibration de l'altitude : Altitude");
			// RAZ de la correction par la pression
			flightSettings.setQNH(SensorManager.PRESSURE_STANDARD_ATMOSPHERE);
		}
	}
	
	
	
	private void setCorrectionAltitude(float value){
		// Detection du mode de calibrage
		if (this.calibrationModePressure){
			// On utilise la pression, la valeur est donc le QNH
			flightSettings.setQNH(value);
			Log.d("Icarus", "QNH réel :"+String.format("%.2f", value));
			//RAZ de la correction d'altitude en metres
			flightSettings.setCorrectionAltitude(0.0f);
		}	
		else {
			// On utilise l'altitude en metres, la valeur est donc l'altitude réelle
			//flightSettings.setAltitudeReelleInitiale(value);
			Log.d("Icarus", "Altitude réelle :"+String.format("%.2f", value));
			calcDeltaAltitude(value);
		}
		
		
		
	}
	
	private void setAltitudeP0(float altitudeP0){
		
		Log.d("Icarus", "Altitude renseignée : "+String.format("%.2f", altitudeP0));
		this.altitudeP0 = altitudeP0;
		this.altitudeP0Set= true;
		
		/*
		 * Liberation du listener de pression car il ne sert qu'a ça.
		 */
		if(sm.getDefaultSensor(Sensor.TYPE_PRESSURE) != null){
			sm.unregisterListener(myPreSensorListener);
		}
		
		//calcDeltaAltitude(altitudeP0);
	}
	
	
	/**
	 * Calcule la correction a appliquer a l'altitude
	 * @param altitudeT0 l'altitude au moment du calcul
	 */
	private void calcDeltaAltitude(float altitude){
		float altitudeCapteur = this.altitudeP0;
		float altitudeReelle = altitude;
		float deltaAltitude = altitudeReelle - altitudeCapteur;
		Log.d("Icarus", "altitudeReelle("+String.format("%.2f", altitudeReelle)+") - altitudeCapteur("+String.format("%.2f", altitudeCapteur)+") = deltatAltitude = "+String.format("%.2f", deltaAltitude));
		Log.d("Icarus", "Delta Altitude : "+String.format("%.2f", deltaAltitude));
		
		flightSettings.setCorrectionAltitude(deltaAltitude);
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
			
			// Si l'altitude n'a pas encore été renseignée et
			// que l'on n'utilise pas l'altitude via la pression
			if(!altitudeP0Set && !flightSettings.usePressure()){
				
				float altitude = (float) location.getAltitude();
				Log.d("Icarus","GPS fourni l'altitude a T0 : "+String.format("%.2f", altitude));
				setAltitudeP0(altitude);
			}
		}
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		
		@Override
		public void onProviderEnabled(String provider) {}
		
		@Override
		public void onProviderDisabled(String provider) {}
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
			
			// Si l'altitude initiale n'a pas encore été renseignée :
			if(!altitudeP0Set){
				Log.d("Icarus","Capteur de pression fourni l'altitude a T0 : "+String.format("%.2f", altitude));
				setAltitudeP0(altitude);
			}
			
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
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


