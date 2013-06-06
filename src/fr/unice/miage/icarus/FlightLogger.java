/**
 * 
 */
package fr.unice.miage.icarus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

/**
 * @author David Da Silva
 *
 */
public class FlightLogger extends FlightDataBuffer {
	
	public File logFile = null;
	private FileOutputStream fOut = null;
	private OutputStreamWriter osw = null;
	
	private boolean usePressure = false;
	
	String _author;
	String _updateFreq; 
	String _aircraft; 
	String _depart; 
	String _device; 
	String _misc;
	
	public FlightLogger(Context ctx, 
			String flightName, 
			String author, 
			String updateFreq, 
			String aircraft, 
			String depart, 
			String device, 
			String misc,
			boolean usePressure){
		
		super(ctx, flightName);
		this._author = author;
		this._updateFreq = updateFreq;
		this._aircraft = aircraft;
		this._depart = depart;
		this._device = device;
		this._misc = misc;
		this.usePressure = usePressure;
		
		Log.d("Icarus", "FlightLogger Created");
		
		
		/*
		 * Creation et ouverture du fichier
		 */
		try{
			logFile = new File(context.getExternalFilesDir(null), _flight+".xml" );
			Log.d("Icarus", "File : "+logFile.toString());
			Toast toast = Toast.makeText(context, "Fichier crée : "+logFile.getPath(), Toast.LENGTH_LONG);
			toast.show();
			fOut = new FileOutputStream(logFile, true);
			osw = new OutputStreamWriter(fOut);
		}
		catch (Exception e) {
			Toast toast = Toast.makeText(context, "Erreur creation du fichier", Toast.LENGTH_LONG);
			toast.show();
			Log.e("Icarus", "erreur a la creation du fichier");
			e.printStackTrace();
		}
		
		
		/*
		 * Ecriture de l'entete du fichier
		 */
		ecrireEnteteFichier();
	}

	/**
	 * Ferme le fichier correctement
	 */
	public File close(){
		try {
			// Dernière ligne du fichier
			osw.write("\t</mesures>\n</vol>");
			osw.flush();
			// Fermeture des streams
			osw.close();
			fOut.close();
		} catch (IOException e) {
			Toast toast = Toast.makeText(context, "Erreur fermeture du fichier", Toast.LENGTH_LONG);
			toast.show();
			Log.e("Icarus", "erreur a la fermeture du fichier");
			e.printStackTrace();
		}
		
		return logFile;
		
	}
	
	/**
	 * Creer l'entete du fichier, toute la partie "<description></description>"
	 */
	private void ecrireEnteteFichier(){
		String ligne = "";
		ligne += "<vol id="+_flight+">\n";
		ligne += "\t<description>\n";
		ligne += "\t\t<auteur>"+_author+"</auteur>\n";
		ligne += "\t\t<tempsRecuperation>"+_updateFreq+"</tempsRecuperation>\n";
		ligne += "\t\t<typeAvion>"+_aircraft+"</typeAvion>\n";
		ligne += "\t\t<depart>"+_depart+"</depart>\n";
		ligne += "\t\t<capteurs>"+_device+"</capteurs>\n";
		ligne += "\t\t<remarques>"+_misc+"</remarques>\n";
		ligne += "\t</description>\n";
		ligne += "\t<mesures>\n";
		
		
		try{
			osw.write(ligne);
			osw.flush();
		}
		catch (Exception e) {
			Toast toast = Toast.makeText(context, "Erreur ecriture entete dans le fichier", Toast.LENGTH_LONG);
			toast.show();
			Log.e("Icarus", "erreur a l'ecriture de l'entete dans le fichier");
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Methode appellée pour ecrire une ligne sur le fichier
	 */
	public void flush(){

		// On vérifie que la ligne est bien complète
		if(!isReady()) return;

		Log.d("Icarus", "Flushing data in "+_flight+".txt");
		
		/*
		 * Construction de la ligne
		 */
		String ligne = "\t\t<mesure time="+_timestamp.toString()+">\n";
		
		/*
		 * Position
		 */
		String sLat = Double.toString(_location.getLatitude());
		String sLng = Double.toString(_location.getLongitude());
		String sAlt;
		if (usePressure){
			sAlt = Double.toString(_altitude);
		} else{
			sAlt = Double.toString(_location.getAltitude());
		}
		String sBearing = Double.toString(_location.getBearing());
		ligne += "\t\t\t<position>\n";
		ligne += "\t\t\t\t<lat>"+sLat+"</lat>\n";
		ligne += "\t\t\t\t<lng>"+sLng+"</lng>\n";
		ligne += "\t\t\t\t<alt>"+sAlt+"</alt>\n";
		ligne += "\t\t\t\t<bearing>"+sBearing+"</bearing>\n";
		/*
		 * Orientation		
		 */
		String sAzimuth = Float.toString(_orix);
		String sPitch = Float.toString(_oriy);
		String sRoll = Float.toString(_oriz);
		ligne += "\t\t\t\t<azimuth>"+sAzimuth+"</azimuth>\n";
		ligne += "\t\t\t\t<pitch>"+sPitch+"</pitch>\n";
		ligne += "\t\t\t\t<roll>"+sRoll+"</roll>\n";
		
		ligne += "\t\t\t</position>\n";

				
				
		ligne += "\t\t</mesure>\n";
		
		try{
			osw.write(ligne);
			osw.flush();
		}
		catch (Exception e) {
			Toast toast = Toast.makeText(context, "Erreur ecriture dans le fichier", Toast.LENGTH_LONG);
			toast.show();
			Log.e("Icarus", "erreur a l'ecriture dans le fichier");
			e.printStackTrace();
		}
		
		reset();
		
	}
	
}
