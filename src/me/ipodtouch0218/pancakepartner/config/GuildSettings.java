package me.ipodtouch0218.pancakepartner.config;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

import me.ipodtouch0218.pancakepartner.BotMain;

public class GuildSettings {

	@JsonIgnore
	private static final File GUILD_FOLDER = new File("guilds/");
	
	////////////////
	//--SETTINGS--//
	
	private String commandPrefix = ";"; //Command prefix to be used in place of the default prefix
	private HashSet<Long> botAdmins = new HashSet<>(); //List of administrators which have permissions to all commands

	private long starChannelID = -1; //channel where starred messages should be pasted.
	private int starRequiredStars = 3; //required amount of stars before the message gets pinned to the star channel.
	
	private long pollChannelID = -1; //channel where polls are pasted.
	
	//--Getters--//
	public String getCommandPrefix() { return commandPrefix; }
	public long getStarChannelID() { return starChannelID; }
	public int getStarRequiredStars() { return starRequiredStars; }
	public HashSet<Long> getBotAdmins() { return botAdmins; }
	public long getPollChannelID() { return pollChannelID; }
	public boolean isBotAdmin(long id) { return botAdmins.contains(id); }
	
	//--Setters--//
	public void setCommandPrefix(String prefix) { commandPrefix = prefix; }
	public void setStarChannelID(long id) { starChannelID = id; }
	public void setStarRequiredStars(int stars) { starRequiredStars = stars; }
	public void setPollChannelID(long id) { pollChannelID = id; }
	
	//--Misc--//
	public boolean addBotAdmin(long id) { return botAdmins.add(id); }
	public boolean removeBotAdmin(long id) { return botAdmins.remove(id); }
	
	//////////////////////////
	//--SAVING AND LOADING--//
	public void save(long id) {
		File settingsFile = new File("guilds/" + id + ".yml");
		try {
			GUILD_FOLDER.mkdirs();
			BotMain.yamlMapper.writeValue(settingsFile, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static GuildSettings load(long id) {
		File settingsFile = new File("guilds/" + id + ".yml");
		
		try {
			return BotMain.yamlMapper.readValue(settingsFile, GuildSettings.class);
		} catch (IOException e) {
			e.printStackTrace();
			//file doesnt exist or mangled file? oh well, new settings.
			GuildSettings newSettings = new GuildSettings();
			newSettings.save(id);
			return newSettings;
		}
	}
}
