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
	/**
	 * Returns the Command Prefix, a value used by a {@link CommandHandler} to detect
	 * if a command has been executed within the message. Command Prefixes are
	 * checked for at the beginning of the message.
	 * @return The Command Prefix.
	 */
	public String getCommandPrefix() { return commandPrefix; }
	public long getStarChannelID() { return starChannelID; }
	public int getStarRequiredStars() { return starRequiredStars; }
	/**
	 * Returns a list of ID's of Bot Administrators. Bot Administrators are users which ignore
	 * the permissions check when executing commands and can execute any command.
	 * @return HashSet of user ID's of Bot Administrators.
	 */
	public HashSet<Long> getBotAdmins() { return botAdmins; }
	public long getPollChannelID() { return pollChannelID; }
	/**
	 * Returns if the specified user ID is a current Bot Administrator within this guild.
	 * @param id - User ID to check if they are a Bot Administrator.
	 * @return If the given user ID is a Bot Administrator
	 */
	public boolean isBotAdmin(long id) { return botAdmins.contains(id); }
	
	//--Setters--//
	public void setCommandPrefix(String prefix) { commandPrefix = prefix; }
	public void setStarChannelID(long id) { starChannelID = id; }
	public void setStarRequiredStars(int stars) { starRequiredStars = stars; }
	public void setPollChannelID(long id) { pollChannelID = id; }
	
	//--Misc--//
	/**
	 * Adds a user via ID to the list of Bot Administrators for this guild. Bot Administrators
	 * can override any permission checks and execute any command within this guild.
	 * @param id - The ID of the user to make into a Bot Administrator.
	 * @return If the user was successfully added as a Bot Administrator.
	 */
	public boolean addBotAdmin(long id) { return botAdmins.add(id); }
	/**
	 * Removes a user via ID from the list of Bot Administrators for this guild.
	 * @param id - ID of the user to remove Bot Administrator status.
	 * @return If the user was removed from their role as a Bot Administrator.
	 */
	public boolean removeBotAdmin(long id) { return botAdmins.remove(id); }

	//--SAVING AND LOADING--//
	/**
	 * Saves this GuildSettings instance to a file as specified by the guild's ID.
	 * @param id - The ID of the guild these settings represent.
	 */
	public void save(long id) {
		File settingsFile = new File("guilds/" + id + ".yml");
		try {
			GUILD_FOLDER.mkdirs();
			BotMain.yamlMapper.writeValue(settingsFile, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads a GuildSettings instance from file, according to the ID of the guild. If the file
	 * is invalid or missing, this menthod automatically makes a new file containing default settings
	 * and overwrites any existing file with the same guild ID.
	 * @param id - The ID of the guild to load from file.
	 * @return {@link GuildSettings} instance loaded from file.
	 */
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
