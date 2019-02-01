package me.ipodtouch0218.pancakepartner;

public class BotSettings {

	//---Settings---//
	//General bot settings
	private String token = "n/a"; //Bot token used for logging into discord without using email/password
	private String botPlayingMessage = "Use ;help for help~"; //Bot game message, used as a short message displayed in the user profile
	
	//Command Settings
	private boolean deleteIssuedCommand = false; //If a command issued by a user should be deleted automatically
	private String commandPrefix = ";"; //Prefix a message must start with to be considered to be a command.
	//Command-specfic Settings
	private String starChannelName = "pins"; //Channel name where starred messages should get reposted.
	private int starRequiredStars = 3; //Number of required star reactions before messages get reposted to the star channel.
	
	//---Getters---//
	public String getToken() { return token; }
	public String getBotPlayingMessage() { return botPlayingMessage; }
	public boolean getDeleteIssuedCommand() { return deleteIssuedCommand; }
	public String getCommandPrefix() { return commandPrefix; }
	public String getCmdStarChannelName() { return starChannelName; }
	public int getCmdStarRequiredStars() { return starRequiredStars; }
	
	//---Setters---//
	public void setBotPlayingMessage(String value) { botPlayingMessage = value; }
	public void setDeleteIssuedCommand(boolean value) { deleteIssuedCommand = value; }
	public void setCommandPrefix(String value) { commandPrefix = value; }
	public void setCmdStarChannelName(String value) { starChannelName = value; }
	public void setCmdStarRequiredStars(int value) { starRequiredStars = value; }
}
