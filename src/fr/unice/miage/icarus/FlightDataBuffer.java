package fr.unice.miage.icarus;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;

/**
 * Cette classe représente toutes les données que l'on possède
 * sur la position et l'inclinaison de l'appareil à un instant T.
 *  
 * Toutes ces données doivent être renseignées dans la classe
 * avant d'être transmises au serveur et/ou enregistrées dans un fichier. 
 * @author David Da Silva
 *
 */
public abstract class FlightDataBuffer {

	protected Context 	context;
	
	protected String	_flight;
	protected Long		_timestamp;
	
	protected Location	_location;
	protected boolean	_locationReady;
	
	
	protected float	_orix;
	protected float	_oriy;
	protected float	_oriz;
	protected boolean	_orientationReady;
	
	
	
	public FlightDataBuffer(Context context, String flightName){
		
		this.context = context;
		
		this._flight = flightName;
		this._flight.replaceAll(" ", "_");
		
		this._timestamp = System.currentTimeMillis();
		
		this._locationReady		= false;
		this._orientationReady	= false;
	}
	
	public void setLocation(Location loc){
		if(loc == null) return;
		
		this._location = loc;
		this._locationReady = true;
		this._timestamp = System.currentTimeMillis();
		
		if(isReady())
			flush();
	}
	
	public void setOrientation(float x, float y, float z){
		this._orix = x;
		this._oriy = y;
		this._oriz = z;
		this._timestamp = System.currentTimeMillis();
		this._orientationReady = true;

		if(isReady())
			flush();
	}

	public void reset(){
		this._timestamp = System.currentTimeMillis();
		this._locationReady		= false;
		this._orientationReady	= false;
	}
	
	
	
	public boolean isReady(){
		if ( !_flight.isEmpty() 
				&& _timestamp != 0.0f
				&& _locationReady 
				&& _orientationReady){
			return true;
		}
		return false;
	}
	
	
	public abstract void flush();
	

	
}
