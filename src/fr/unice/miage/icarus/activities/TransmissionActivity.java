package fr.unice.miage.icarus.activities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import fr.unice.miage.icarus.AsyncHttpPostTask;
import fr.unice.miage.icarus.FlightSettings;
import fr.unice.miage.icarus.R;
import fr.unice.miage.icarus.R.layout;
import fr.unice.miage.icarus.R.menu;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.v4.os.ParcelableCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class TransmissionActivity extends Activity {

	private FlightSettings flightSettings;
	private static final String WEBSERVICE_URL = "http://projetdannee.pardailhan.org/CodeIgniter/index.php/WebService/verificationAjoutVol";
	private static final String SERVER_URL = "http://projetdannee.pardailhan.org";
	//private static final String FROM_EMAIL = "contact@daviddasilva.net";
	
	
	private boolean	isPrivate = false;
	private boolean	isVisible = true;
	private String		password = "";
	
	private AsyncHttpPostTask asyncTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_transmission);
		
		
		/*
		 *  récupération des settings
		 */
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			flightSettings  = (FlightSettings)extras.get("settings");
		}
		
		
		/*
		 * Enregistrement listener bouton envoyer par e-mail
		 */
		final Button buttonSendMail = (Button)findViewById(R.id.buttonSendEmail);
		buttonSendMail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendRecordByEmail();
				buttonSendMail.setEnabled(false);
			}
		});
		
		/*
		 * Enregistrement listener bouton envoyer serveur
		 */
		final Button buttonSendToServer = (Button)findViewById(R.id.buttonSendRecordToServer);
		buttonSendToServer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendRecordToServer();
				//quitApplication();
			}
		});
		
		/*
		 * Enregistrement listener bouton envoyer serveur
		 */
		final Button buttonQuit = (Button)findViewById(R.id.buttonQuit);
		buttonQuit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quitApplication();
			}
		});
		
		/*
		 * Enregistrement listener sur slider Confidentialité
		 */
		final Switch switchPrivacy = (Switch)findViewById(R.id.switchPrivacy);
		switchPrivacy.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				isPrivate = isChecked;
				
				// Si vol privé, on active le mot de passe
				if(isPrivate){
					// Label
					final TextView passwordLabel = (TextView)findViewById(R.id.textViewPassword);
					passwordLabel.setEnabled(true);
					// Champ texte
					final EditText passwordField = (EditText)findViewById(R.id.editTextRecordPassword);
					passwordField.setEnabled(true);
					/*
					 * Listener qui désactive le bouton envoyer si le password est vide
					 */
					buttonSendToServer.setEnabled(false);
					
					passwordField.addTextChangedListener(new TextWatcher() {
						@Override
						public void afterTextChanged(Editable s) {
							if(s.length() > 0){
								buttonSendToServer.setEnabled(true);
								password = passwordField.getText().toString();
							}
						}
						@Override
						public void beforeTextChanged(CharSequence s,
								int start, int count, int after) {
						}
						@Override
						public void onTextChanged(CharSequence s, int start,
								int before, int count) {
						}
					});
				}
			}
		});
		
		// Remplissage de l'url du fichier
		TextView textViewUrl = (TextView)findViewById(R.id.textViewRecordPath);
		textViewUrl.setText(flightSettings.getLogFile().getPath());
	}

	
	
	private void sendRecordByEmail(){
		
		String bodyText = "Bonjour,\n";
		bodyText 		+= "Vous trouverez en pièce jointe l'enregistrement du vol ";
		bodyText 		+= flightSettings.getFlightName()+".\n";
		bodyText 		+= "Vous pouvez l'uploader à cette adresse : "+SERVER_URL;
		
		final EditText mailField = (EditText)findViewById(R.id.editTextEmail);
		String destination = mailField.getText().toString();
		/*
		 * Creation de l'intent pour le mail 
		 */
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("*/*");
		i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(flightSettings.getLogFile()));
		i.putExtra(Intent.EXTRA_EMAIL, new String[] {
				destination
		});
		i.putExtra(Intent.EXTRA_SUBJECT, "[Icarus] Enregistrement "+flightSettings.getFlightName());
		i.putExtra(Intent.EXTRA_TEXT, bodyText);
		
		
		startActivity(createEmailChooserIntent(i, destination, "Envoyer par mail..."));
	}
	
	/**
	 * Crée un chooser pour choisir l'application mail a utiliser.
	 * @param source
	 * @param destination
	 * @param chooserTitle
	 * @return
	 */
	private Intent createEmailChooserIntent(Intent source, String destination, CharSequence chooserTitle){
		
		Stack<Intent> intents = new Stack<Intent>();
		Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", destination, null));
		List<ResolveInfo> activities = getPackageManager().queryIntentActivities(i, 0);
		
		
		for(ResolveInfo ri : activities){
			Intent target = new Intent(source);
			target.setPackage(ri.activityInfo.packageName);
			intents.add(target);
		}
		
		if(!intents.isEmpty()){
			Intent chooserIntent = Intent.createChooser(intents.remove(0), chooserTitle);
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[intents.size()]));
			
			return chooserIntent;
		} else {
			return Intent.createChooser(source, chooserTitle);
		}
	}
	
	
	/**
	 * Envoie le fichier + quelques parametres au webservice de Dorian
	 * @source StackOverflow
	 */
	private void sendRecordToServer(){
		
		Log.d("Icarus","Preparation a l'envoi au serveur");
		
		String privacy;
		String visibility;
		
		if (isPrivate)
			privacy = "1";
		else
			privacy = "0";
		
		if(isVisible)
			visibility = "1";
		else
			visibility = "0";

		HashMap<String, ContentBody> params = new HashMap<String, ContentBody>();
		
		try {
			params.put("nom_vol", new StringBody(flightSettings.getFlightName()));
			params.put("prive", new StringBody(privacy));
			if(isPrivate)
				params.put("mot_de_passe", new StringBody(password));
			params.put("visible", new StringBody(visibility));
			params.put("id_utilisateur", new StringBody(flightSettings.getUserid()));
			params.put("fichier_vol", new FileBody(flightSettings.getLogFile()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		asyncTask = new AsyncHttpPostTask(this, WEBSERVICE_URL, params);
		asyncTask.execute();
		
	}
	
	/**
	 * Ferme l'activité
	 */
	private void quitApplication(){

			// Retour au Home
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
	}
	

}
