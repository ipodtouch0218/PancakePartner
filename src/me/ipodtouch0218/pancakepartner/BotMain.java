package me.ipodtouch0218.pancakepartner;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import javax.security.auth.login.LoginException;

import me.ipodtouch0218.pancakepartner.commands.CmdPoll;
import me.ipodtouch0218.pancakepartner.commands.CmdRoll;
import me.ipodtouch0218.pancakepartner.commands.CmdTextFormat;
import me.ipodtouch0218.pancakepartner.commands.games.CmdMinesweeper;
import me.ipodtouch0218.pancakepartner.commands.info.CmdBotInfo;
import me.ipodtouch0218.pancakepartner.commands.info.CmdHelp;
import me.ipodtouch0218.pancakepartner.commands.info.CmdPing;
import me.ipodtouch0218.pancakepartner.commands.staff.CmdLockdown;
import me.ipodtouch0218.pancakepartner.commands.staff.CmdPurge;
import me.ipodtouch0218.pancakepartner.commands.staff.CmdSettings;
import me.ipodtouch0218.pancakepartner.commands.staff.CmdShell;
import me.ipodtouch0218.pancakepartner.commands.staff.CmdStar;
import me.ipodtouch0218.pancakepartner.config.GuildSettings;
import me.ipodtouch0218.pancakepartner.listeners.CustomListener;
import me.ipodtouch0218.sjbotcore.SJBotCore;
import me.ipodtouch0218.sjbotcore.command.BotCommand;
import me.ipodtouch0218.sjbotcore.files.BotSettings;
import me.ipodtouch0218.sjbotcore.files.YamlConfig;
import net.dv8tion.jda.api.entities.Guild;

public class BotMain  {

	/* TODO:
	 * Clean up utils, move some methods to SJBotCore.
	 * Remove static methods and only use instance ones.
	 * Potentionally some even logging to a channel.
	 * Console input parser?
	 */
	public static void main(String[] args) {
		new BotMain();
	}
	
	//------------------------------//
	
	//--Variables & Constructor--//
	private static SJBotCore botCore;
	private static final File configFile = new File("config.yml");
	private static long startupTime;
	private static HashMap<Long, GuildSettings> guildSettings = new HashMap<>(); //settings used per guild, cached.
	
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
		startupTime = System.currentTimeMillis();
	}
	
	private void registerCommands() {
		BotCommand[] commands = {new CmdSettings(), new CmdPing(), new CmdHelp(),
				new CmdMinesweeper(), new CmdStar(), new CmdRoll(), new CmdPoll(),
				new CmdBotInfo(), new CmdPurge(), new CmdTextFormat(), new CmdShell(),
				new CmdLockdown()};
		
		Arrays.stream(commands).forEach(botCore::registerCommand);
	}
	
	//--Configuration--//
	/**
	 * Saves the current {@link BotSettings} to the config.yml file.
	 */
	public void saveSettings() {
		botCore.getBotSettings().saveConfig(configFile);
	}

	//--Getters--//
	public static long getStartupTime() { return startupTime; }
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
