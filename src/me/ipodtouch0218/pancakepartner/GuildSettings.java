package me.ipodtouch0218.pancakepartner;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GuildSettings {

	//--STATIC--//
	@JsonIgnore
	private static final File GUILD_FOLDER = new File("guilds/");
	
	/////////////////////
	
	//--SETTINGS--//
	private String commandPrefix = ";";
	private HashSet<Long> botAdmins = new HashSet<>();
	//-Star Command-//
	private long starChannelID = -1;
	private int starRequiredStars = 3;
	
	////////////////////
	
	//--Getters--//
	public String getCommandPrefix() { return commandPrefix; }
	public long getStarChannelID() { return starChannelID; }
	public int getStarRequiredStars() { return starRequiredStars; }
	public HashSet<Long> getBotAdmins() { return botAdmins; }
	public boolean isBotAdmin(long id) { return botAdmins.contains(id); }
	
	//--Setters--//
	public void setCommandPrefix(String prefix) { commandPrefix = prefix; }
	public void setStarChannelID(long id) { starChannelID = id; }
	public void setStarRequiredStars(int stars) { starRequiredStars = stars; }
	
	//--Misc--//
	public boolean addBotAdmin(long id) { return botAdmins.add(id); }
	public boolean removeBotAdmin(long id) { return botAdmins.remove(id); }
	
	///////////////////
	
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
