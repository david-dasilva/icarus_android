package fr.unice.miage.icarus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;

import android.app.Activity;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class AsyncHttpPostTask extends AsyncTask<Void, Void, String> {
	
	private Activity activity;
	private String server;
	private HashMap<String, ContentBody> params;
	
	private static final String MESSAGE_OK = "1";
		
	public AsyncHttpPostTask(Activity activity, final String server, HashMap<String, ContentBody> params){
		this.activity = activity;
		this.server = server;
		this.params = params;
	}

	@Override
	protected String doInBackground(Void... arg0) {

		String statusCode = "";
		String messageRetour = "";
		
		Log.d("Icarus", "AsyncTask : doInBackground");
		//HttpClient httpClient = new DefaultHttpClient();
		AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Icarus");
		HttpPost httpPost = new HttpPost(this.server);
		
		MultipartEntity entity = new MultipartEntity();
		
		/*
		 * Récuperation des parametres
		 */
		Iterator<Entry<String, ContentBody>> it = params.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, ContentBody> pairs = (Map.Entry<String, ContentBody>)it.next();
			entity.addPart(pairs.getKey(), pairs.getValue());
		}
		
		httpPost.setEntity(entity);
		
		try{
			/*
			 * Envoi + réponse
			 */
			Log.d("Icarus", "Sending...");
			HttpResponse reponse = httpClient.execute(httpPost);
		
			BufferedReader rd = new BufferedReader(new InputStreamReader(reponse.getEntity().getContent()));
			final StringBuilder out = new StringBuilder();
			String line;
			
			// Code retour HTTP. OK si page valide. Ne garantie pas que le php a bien géré le fichier
			statusCode = reponse.getStatusLine().getReasonPhrase();
			
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
			messageRetour = out.toString();
			
			
			
			
			
		} catch (ClientProtocolException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}

		if (httpClient != null)
			httpClient.close();
		
		if (messageRetour.equals(MESSAGE_OK) && statusCode.equals("OK")){
			return UploadStatus.SUCCESS.toString();
		} else if (!statusCode.equals("OK")){
			return UploadStatus.SERVER_ERROR.toString();
		} else {
			return UploadStatus.FAILED.toString();
		}
	}


	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		//TODO : check la réponse et fermer l'activité si c'est OK
		if(result.equals(UploadStatus.SUCCESS.toString())){
			Toast toast = Toast.makeText(activity, "Enregistrement envoyé", Toast.LENGTH_LONG);
			toast.show();
			
			// Retour au Home
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activity.startActivity(intent);
			activity.finish();
			
			
		} else {
			Toast toast = Toast.makeText(activity, "Erreur de communication avec le serveur. code :"+result, Toast.LENGTH_LONG);
			toast.show();
		}
		
		//this.activity.finish();
	}
	

}
