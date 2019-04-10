package me.ipodtouch0218.pancakepartner;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import javax.security.auth.login.LoginException;

import me.ipodtouch0218.pancakepartner.commands.*;
import me.ipodtouch0218.pancakepartner.config.GuildSettings;
import me.ipodtouch0218.sjbotcore.SJBotCore;
import me.ipodtouch0218.sjbotcore.command.BotCommand;
import me.ipodtouch0218.sjbotcore.files.BotSettings;
import me.ipodtouch0218.sjbotcore.files.YamlConfig;
import net.dv8tion.jda.core.entities.Guild;

public class BotMain {

	/* TODO:
	 * Clean up utils (in general)
	 * Fix deprications with permissions system. (SJBotCore todo.)
	 * 
	 * Give javadocs to some more classes:
	 * - BotSettings
	 * - Finish BotMain
	 */
	
	//PROGRAM START
	
	public static void main(String[] args) {
		new BotMain();
	}
	
	///INSTANCE STUFF
	//--Variables & Constructor--//
	private final File configFile = new File("config.yml");
	
	private static SJBotCore botCore;
	private static HashMap<Long, GuildSettings> guildSettings = new HashMap<>(); //settings used per guild.
	
	public BotMain() {
		buildBot();
		registerCommands();
	}
	
	//--Startup Methods--//
	private void buildBot() {
		BotSettings settings = YamlConfig.loadConfig(configFile, BotSettings.class);
		botCore = new SJBotCore(settings);
		try {
			botCore.startBot();
			botCore.getShardManager().addEventListener(new CustomListener());
		} catch (IllegalArgumentException | LoginException e) {
			e.printStackTrace();
		}
	}
	
	private void registerCommands() {
		BotCommand[] commands = {new CmdSettings(), new CmdPing(), new CmdHelp(),
				new CmdMinesweeper(), new CmdStar(), new CmdRoll(), new CmdPoll(),
				new CmdBotInfo(), new CmdPurge(), new CmdTextFormat()};
		Arrays.stream(commands).forEach(cmd -> botCore.registerCommand(cmd));
	}
	
	//--Configuration--//
	/**
	 * Saves the current {@link BotSettings} to the config.yml file.
	 */
	public void saveSettings() {
		botCore.getBotSettings().saveConfig(configFile);
	}

	//--Getters--//
	public static SJBotCore getBotCore() { return botCore; }
	/**
	 * Returns guild-specific settings which Bot Administrators can set using {@link CmdSettings}
	 * Functionally the same to {@link BotMain#getGuildSettings(long)} without having to call {@link Guild#getIdLong()}
	 * @param guild - {@link Guild} instance to retrieve settings for.
	 * @return {@link GuildSettings} instance for the given guild.
	 * @see BotMain#getGuildSettings(long)
	 */
	public static GuildSettings getGuildSettings(Guild guild) { return getGuildSettings(guild.getIdLong()); }
	/**
	 * Returns guild-specific settings which Bot Administrators can set using {@link CmdSettings}
	 * @param id - Guild ID to retrieve settings for.
	 * @return {@link Guildsettings} instance for the given guild.
	 */
	public static GuildSettings getGuildSettings(long id) {
		if (guildSettings.containsKey(id)) {
			//settings already loaded, return
			return guildSettings.get(id);
		}
		//load guild settings or create a new one.
		GuildSettings loaded = GuildSettings.load(id);
		guildSettings.put(id, loaded);
		
		return loaded;
	}

}
