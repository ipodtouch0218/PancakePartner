package me.ipodtouch0218.pancakepartner;

public class BotSettings {

	//---Variables---//
	//General bot settings
	private String token = "n/a";
	private String botPlayingMessage = "Use ;help for help~";
	
	//Command-specfic Settings
	private String starChannelName = "pins";
	private int starRequiredStars = 3;
	
	//---Getters---//
	public String getToken() { return token; }
	public String getBotPlayingMessage() { return botPlayingMessage; }
	public String getCmdStarChannelName() { return starChannelName; }
	public int getCmdStarRequiredStars() { return starRequiredStars; }
	
	//---Setters---//
	public void setBotPlayingMessage(String value) { botPlayingMessage = value; }
	public void setCmdStarChannelName(String value) { starChannelName = value; }
	public void setCmdStarRequiredStars(int value) { starRequiredStars = value; }
}
