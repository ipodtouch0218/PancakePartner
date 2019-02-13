package me.ipodtouch0218.pancakepartner.config;

public class BotSettings {

	//---Settings---//
	private String token = "<PASTE TOKEN HERE>"; //Bot token used for logging into discord without using email/password
	private String botPlayingMessage = "Use ;help for help~"; //Bot game message, used as a short message displayed in the user profile

	private String defaultCommandPrefix = ";"; //Command prefix used in dm's or servers without a custom cmd prefix.
	private boolean deleteIssuedCommand = false; //If a command issued by a user should be deleted automatically
	
	//---Getters---//
	public String getToken() { return token; }
	public String getBotPlayingMessage() { return botPlayingMessage; }
	public String getDefaultCommandPrefix() { return defaultCommandPrefix; }
	public boolean getDeleteIssuedCommand() { return deleteIssuedCommand; }
	
	//---Setters---//
	public void setBotPlayingMessage(String value) { botPlayingMessage = value; }
	public void setDeleteIssuedCommand(boolean value) { deleteIssuedCommand = value; }
}
