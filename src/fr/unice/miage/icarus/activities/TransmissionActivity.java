package fr.unice.miage.icarus.activities;

import fr.unice.miage.icarus.R;
import fr.unice.miage.icarus.R.layout;
import fr.unice.miage.icarus.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class TransmissionActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transmission);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.transmission, menu);
		return true;
	}

}
