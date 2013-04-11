/**
 * 
 */
package fr.unice.miage.icarus;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.location.Location;

/**
 * @author David Da Silva
 *
 */
public class Logger {

	private float[] acceleration = new float[3];
	private float[] orientation = new float[3];
	private Location position;
	
	private Context context;
	
	private FileOutputStream fOut = null;
	private OutputStreamWriter osw = null;
	
	public Logger(Context ctx){
		this.context = ctx;
	}
	
	
	public void setAcceleration(float[] acc){
		
	}
	
	/**
	 * Methode appellée pour ecrire une ligne sur le fichier
	 */
	private void flush(){

		/*
		 * Construction de la ligne
		 */
		Long timestamp = System.currentTimeMillis()/1000;
		String ligne = "<record time="+timestamp.toString()+">\n";
		
		/*
		 * Position
		 */
		String sLat = Double.toString(position.getLatitude());
		String sLng = Double.toString(position.getLongitude());
		String sAlt = Double.toString(position.getAltitude());
		String sBearing = Double.toString(position.getBearing());
		ligne += "\t<position>\n";
		ligne += "\t\t<lat>"+sLat+"</lat>";
		ligne += "\t\t<lng>"+sLng+"</lng>";
		ligne += "\t\t<alt>"+sAlt+"</alt>";
		ligne += "\t\t<bearing>"+sBearing+"</bearing>";
		ligne += "\t</position>";
		/*
		 * Acceleration
		 */
		String sAccelerationX = Float.toString(acceleration[0]);
		String sAccelerationY = Float.toString(acceleration[1]);
		String sAccelerationZ = Float.toString(acceleration[2]);
		ligne += "\t<acceleration x="+sAccelerationX+" y="+sAccelerationY+" z="+sAccelerationX+">\n";
		
		/*
		 * Orientation		
		 */
		String sAzimuth = Float.toString(orientation[0]);
		String sPitch = Float.toString(orientation[1]);
		String sRoll = Float.toString(orientation[2]);
		ligne += "\t<azimuth>"+sAzimuth+"</azimuth>\n";
		ligne += "\t<pitch>"+sPitch+"</pitch>\n";
		ligne += "\t<roll>"+sRoll+"</roll>\n";
				
				
		ligne += "</record>";
		
		try{
			fOut = context.openFileOutput("enregistreurvol.txt", Context.MODE_APPEND);
			osw = new OutputStreamWriter(fOut);
			osw.write(ligne);
			osw.flush();
			osw.close();
			fOut.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
