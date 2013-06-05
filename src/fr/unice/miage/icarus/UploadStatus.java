package fr.unice.miage.icarus;

public enum UploadStatus {

	SUCCESS("SUCCESS"),
	FAILED("FAILED"),
	SERVER_ERROR("SERVER_ERROR");
	
	private final String text;
	
	private UploadStatus(final String text){
		this.text = text;
	}
	
	@Override
	public String toString(){
		return text;
	}
}
