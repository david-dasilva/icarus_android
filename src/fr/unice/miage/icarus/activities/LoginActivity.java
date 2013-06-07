package fr.unice.miage.icarus.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import fr.unice.miage.icarus.AsyncAuthTask;
import fr.unice.miage.icarus.R;

public class LoginActivity extends Activity {
	
	private static final String WEBSERVICE_URL = "http://projetdannee.pardailhan.org/index.php/WebService/authentification";
	private AsyncAuthTask asyncTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		
		Log.d("Icarus", "Lancement application Icarus");
		
		/*
		 * Bouton envoyer
		 */
		final Button button = (Button)findViewById(R.id.buttonLogin);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				obtenirId();
			}
		});
		
		/*
		 * Bouton "pas de compte"
		 */
		final Button buttonNoAccount = (Button)findViewById(R.id.buttonNoAccount);
		buttonNoAccount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				noAccount();
			}
		});
		
		/*
		 * Champ password
		 * Listener pour la touche <Entrée>
		 */
		final EditText passwordField =(EditText)findViewById(R.id.editTextPassword);
		
		passwordField.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_NULL 
						&& event.getAction() == KeyEvent.ACTION_DOWN){
					obtenirId();
				}
				return true;
			}
		});
	}
	
	/**
	 * L'application peut être utilisée sans compte. L'id vaut alors -1
	 */
	private void noAccount(){
		Log.d("Icarus","Utilisation sans compte");
		// Appel de l'activité de configuration
		Intent configurationIntent = new Intent(this, ConfigurationActivity.class);
		configurationIntent.putExtra("userid", "-1");
		
		startActivity(configurationIntent);
		finish();
	}
	
	private void obtenirId(){
		
		/*
		 * Champ identifiant	
		 */
		final EditText loginField = (EditText)findViewById(R.id.editTextLogin);
		String login = loginField.getText().toString();
		
		/*
		 * Champ password
		 */
		final EditText passwordField =(EditText)findViewById(R.id.editTextPassword);
		String password = passwordField.getText().toString();
		
		
		/*
		 * Création d'une tache qui va récupérer la valeur
		 */
		asyncTask = new AsyncAuthTask(this, WEBSERVICE_URL);
		
		//ArrayList listParams = new ArrayList<String>(2);
		//listParams.
		/*
		 * Lancement de la tâche. Lors de la complétion elle lancera l'activité suivante.
		 */
		asyncTask.execute(login, password);
		
		
	}
	
}
