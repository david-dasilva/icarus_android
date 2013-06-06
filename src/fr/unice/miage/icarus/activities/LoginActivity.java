package fr.unice.miage.icarus.activities;

import android.app.Activity;
import android.os.Bundle;
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
	
	private static final String WEBSERVICE_URL = "http://projetdannee.pardailhan.org/CodeIgniter/index.php/WebService/authentification";
	private AsyncAuthTask asyncTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		
		
		
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
