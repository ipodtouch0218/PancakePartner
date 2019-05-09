package me.ipodtouch0218.pancakepartner.config;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonIgnore;

import me.ipodtouch0218.sjbotcore.files.YamlConfig;

public class GuildSettings extends YamlConfig {

	@JsonIgnore
	private static final File GUILD_FOLDER = new File("guilds/");
	
	////////////////
	//--SETTINGS--//
	
	public String commandPrefix = ";"; //Command prefix to be used in place of the default prefix

	public long starChannelID = -1; //channel where starred messages should be pasted.
	public int starRequiredStars = 3; //required amount of stars before the message gets pinned to the star channel.
	
	//--Getters--//


	//--SAVING AND LOADING--//
	/**
	 * Saves this GuildSettings instance to a file as specified by the guild's ID.
	 * @param id - The ID of the guild these settings represent.
	 */
	public void save(long id) {
		File settingsFile = new File("guilds/" + id + ".yml");
		GUILD_FOLDER.mkdirs();
		super.saveConfig(settingsFile);
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
		
		return YamlConfig.loadConfig(settingsFile, GuildSettings.class);
	}
}
