package me.ipodtouch0218.pancakepartner;

public class BotSettings {

	//---Settings---//
	//General bot settings
	private String token = "n/a"; //Bot token used for logging into discord without using email/password
	private String botPlayingMessage = "Use ;help for help~"; //Bot game message, used as a short message displayed in the user profile
	
	//Command Settings
	private boolean deleteIssuedCommand = false; //If a command issued by a user should be deleted automatically
	
	//---Getters---//
	public String getToken() { return token; }
	public String getBotPlayingMessage() { return botPlayingMessage; }
	public boolean getDeleteIssuedCommand() { return deleteIssuedCommand; }
	
	//---Setters---//
	public void setBotPlayingMessage(String value) { botPlayingMessage = value; }
	public void setDeleteIssuedCommand(boolean value) { deleteIssuedCommand = value; }
}
