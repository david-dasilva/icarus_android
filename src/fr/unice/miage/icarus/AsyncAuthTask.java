package fr.unice.miage.icarus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import fr.unice.miage.icarus.activities.ConfigurationActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class AsyncAuthTask extends AsyncTask<String, Void, String> {

	private String login;
	private String password;
	private String id;
	private Activity activity;
	private String server;
	
	public AsyncAuthTask(Activity activity, String server){
		this.activity = activity;
		this.server = server;
	}
	
	@Override
	protected String doInBackground(String... params) {
		
		String statusCode = "";
		
		Log.d("Icarus", "AsyncTask : doInBackground");
		
		login = params[0];
		password = params[1];
		
		AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Icarus");
		HttpPost httpPost = new HttpPost(this.server);
		try{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("identifiant",login));
			nameValuePairs.add(new BasicNameValuePair("mot_de_passe", password));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			// Execution de la requete
			HttpResponse response = httpClient.execute(httpPost);
			
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			final StringBuilder out = new StringBuilder();
			String line;
			
			// Code retour HTTP. OK si page valide.
			statusCode = response.getStatusLine().getReasonPhrase();
			
			try{
				while ((line = rd.readLine()) != null){
					out.append(line);
				}
			} catch (Exception e){
				
			}
			
			try{
				rd.close();
			} catch (IOException e){
				e.printStackTrace();
			}
			
			Log.d("Icarus", "SERVER_RESPONSE : "+out.toString());
			id = out.toString();
			
			
		} catch(ClientProtocolException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		
		
		if (httpClient != null)
			httpClient.close();
		
		if (!statusCode.equals("OK")){
			return null;
		}
		
		
		return id;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		if (result != null){
			if (!id.equals("0")){
				
				
				Toast toast = Toast.makeText(activity, "Authentification OK", Toast.LENGTH_SHORT);
				toast.show();
				
				// Appel de l'activité de configuration
				Intent configurationIntent = new Intent(activity, ConfigurationActivity.class);
				configurationIntent.putExtra("userid", id);
				
				activity.startActivity(configurationIntent);
				activity.finish();
				
				
			} else {
				Toast toast = Toast.makeText(activity, "Identifiants incorrects", Toast.LENGTH_LONG);
				toast.show();
			}
		} else {
			Toast toast = Toast.makeText(activity, "Erreur communication serveur", Toast.LENGTH_LONG);
			toast.show();
		}
		
	}
	
	
	

}
