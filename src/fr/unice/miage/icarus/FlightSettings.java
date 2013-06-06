package fr.unice.miage.icarus;

import java.io.File;
import java.io.Serializable;

import android.hardware.SensorManager;
import android.location.LocationManager;

/**
 * Cette classe contient toute la configuration du vol
 * @author David Da Silva
 *
 */
public class FlightSettings implements Serializable{
	
	// userid retourné par le serveur
	private String userid;
	/*
	 * User inputs
	 */
	private String flightName;
	private float 	updateIntervalInSeconds;
	private String	pilot;
	private String	aircraft;
	private String depart;
	private String device;
	private String notes;
	
	private boolean usePressure;
	
	/*
	 * Données de calibrage
	 */
	private float	altitudeReelleInitiale;
	private float	correctionAltitude;
	private float	qnh;
	private float	correctionPitch;
	private float	correctionRoll;
	private float	correctionAzimuth;
	
	private File logFile;
	
	
	public FlightSettings(){
		userid = "1";
		flightName ="EmptyFlightName";
		updateIntervalInSeconds = 1.0f;
		pilot ="EmptyPilot";
		aircraft ="EmptyAircraft";
		depart ="EmptyDepart";
		device ="EmptyDevice";
		notes ="EmptyNotes";
		
		usePressure = false;
		
		altitudeReelleInitiale = 0.0f;
		correctionAltitude = 0.0f;    
		qnh = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;     
		correctionPitch = 0.0f;   
		correctionRoll = 0.0f;    
		correctionAzimuth = 0.0f;
		
		logFile = null;
	}
	
	
	
	/**
	 * @return the userid
	 */
	public String getUserid() {
		return userid;
	}
	/**
	 * @param userid the userid to set
	 */
	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	/**
	 * @return the flightName
	 */
	public String getFlightName() {
		return flightName;
	}
	/**
	 * @param flightName the flightName to set
	 */
	public void setFlightName(String flightName) {
		this.flightName = flightName;
	}
	
	/**
	 * @return the updateInterval in seconds
	 */
	public float getUpdateIntervalInSeconds() {
		return updateIntervalInSeconds;
	}
	/**
	 * @param updateIntervalInSeconds2 the updateIntervalInSeconds to set
	 */
	public void setUpdateIntervalInSeconds(float updateIntervalInSeconds2) {
		this.updateIntervalInSeconds = updateIntervalInSeconds2;
	}
	/**
	 * @return the updateInterval in milliseconds
	 */
	public int getUpdateIntervalInMillis(){
		return (int)updateIntervalInSeconds*1000;
	}
	
	/**
	 * @return the pilot
	 */
	public String getPilot() {
		return pilot;
	}
	/**
	 * @param pilot the pilot to set
	 */
	public void setPilot(String pilot) {
		this.pilot = pilot;
	}
	
	/**
	 * @return the aircraft
	 */
	public String getAircraft() {
		return aircraft;
	}
	/**
	 * @param aircraft the aircraft to set
	 */
	public void setAircraft(String aircraft) {
		this.aircraft = aircraft;
	}
	
	/**
	 * @return the depart
	 */
	public String getDepart() {
		return depart;
	}
	/**
	 * @param depart the depart to set
	 */
	public void setDepart(String depart) {
		this.depart = depart;
	}
	
	/**
	 * @return the device
	 */
	public String getDevice() {
		return device;
	}
	/**
	 * @param device the device to set
	 */
	public void setDevice(String device) {
		this.device = device;
	}
	
	/**
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}
	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * 
	 * @return l'altitude initiale réelle spécifiée par l'utilisateur
	 */
	public float getAltitudeReelleInitiale(){
		return this.altitudeReelleInitiale;
	}
	
	/**
	 * @param alt the altitude to set
	 */
	public void setAltitudeReelleInitiale(float alt){
		this.altitudeReelleInitiale = alt;
	}

	/**
	 * @return the correctionAltitude
	 */
	public float getCorrectionAltitude() {
		return correctionAltitude;
	}



	/**
	 * @param correctionAltitude the correctionAltitude to set
	 */
	public void setCorrectionAltitude(float correctionAltitude) {
		this.correctionAltitude = correctionAltitude;
	}



	/**
	 * @return the correctionQNH
	 */
	public float getQNH() {
		return qnh;
	}



	/**
	 * @param correctionQNH the correctionQNH to set
	 */
	public void setQNH(float correctionQNH) {
		this.qnh = correctionQNH;
	}



	/**
	 * @return the correctionPitch
	 */
	public float getCorrectionPitch() {
		return correctionPitch;
	}



	/**
	 * @param correctionPitch the correctionPitch to set
	 */
	public void setCorrectionPitch(float correctionPitch) {
		this.correctionPitch = correctionPitch;
	}



	/**
	 * @return the correctionRoll
	 */
	public float getCorrectionRoll() {
		return correctionRoll;
	}



	/**
	 * @param correctionRoll the correctionRoll to set
	 */
	public void setCorrectionRoll(float correctionRoll) {
		this.correctionRoll = correctionRoll;
	}



	/**
	 * @return the correctionAzimuth
	 */
	public float getCorrectionAzimuth() {
		return correctionAzimuth;
	}



	/**
	 * @param correctionAzimuth the correctionAzimuth to set
	 */
	public void setCorrectionAzimuth(float correctionAzimuth) {
		this.correctionAzimuth = correctionAzimuth;
	}



	/**
	 * @return the logFile
	 */
	public File getLogFile() {
		return logFile;
	}



	/**
	 * @param logFile the logFile to set
	 */
	public void setLogFile(File logFile) {
		this.logFile = logFile;
	}



	public boolean usePressure() {
		return usePressure;
	}



	public void setUsePressure(boolean usePressure) {
		this.usePressure = usePressure;
	}
	
	
	
	
	
	
	
	
	
}
