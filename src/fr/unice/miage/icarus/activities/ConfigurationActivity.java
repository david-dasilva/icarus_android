package fr.unice.miage.icarus.activities;

import fr.unice.miage.icarus.FlightSettings;
import fr.unice.miage.icarus.R;
import fr.unice.miage.icarus.R.id;
import fr.unice.miage.icarus.R.layout;
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
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ConfigurationActivity extends Activity {

	private SensorManager sm;
	private LocationManager locationManager;
	
	private FlightSettings flightSettings;
	private float			updateIntervalInSeconds = 1.0f;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
			flightSettings.setUserid(extras.getInt("userid"));
		}
		
		
		// Acquire a reference to the system SensorManager
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// If GPS is not enabled, throw an alert dialog
		if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			buildAlertMessageNoGps();
		}
		
		// Listener pour le bouton "demarrer enregistrement"
		final Button button = (Button)findViewById(R.id.buttonStartRecording);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				storeSettings();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocListener); // GPS
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterLocationListener();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterLocationListener();
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
	
	private void unregisterLocationListener(){
		locationManager.removeUpdates(myLocListener);
	}
	
	private void enableRecordingButton(){
		Button button = (Button)findViewById(R.id.buttonStartRecording);
		button.setEnabled(true);
	}
	
	
	

	
	
	
	private LocationListener myLocListener = new LocationListener() {
		
		@Override
		public void onLocationChanged(Location location) {
			// On attend d'avoir une position renseignée par le GPS
			// une fois la position obtenue, on active le bouton
			enableRecordingButton();
			// On se désabonne du GPS
			unregisterLocationListener();
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


