package me.ipodtouch0218.pancakepartner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import me.ipodtouch0218.pancakepartner.commands.custom.*;
import me.ipodtouch0218.pancakepartner.config.BotSettings;
import me.ipodtouch0218.pancakepartner.config.GuildSettings;
import me.ipodtouch0218.pancakepartner.handlers.CommandHandler;
import me.ipodtouch0218.pancakepartner.handlers.MessageListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;

public class BotMain {

	/* TODO:
	 * Display possible flags in help command.
	 * Clean up utils (in general)
	 * 
	 * Give javadocs to some more classes:
	 * - MessageListener
	 * - ReactionHandler
	 * - BotSettings
	 * - Finish BotMain
	 * - Finish CommandHandler
	 */
	
	//PROGRAM START
	
	public static void main(String[] args) {
		new BotMain();
	}
	
	///INSTANCE STUFF
	//--Variables & Constructor--//
	private final File configFile = new File("config.yml");
	public static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
	
	private static JDA jdaInstance;	//instance for the jda, the bot itself.
	private static MessageListener messageListener;	//message listener, passes messages to the command handler 
	private static CommandHandler commandHandler; //command handler, performs commands passed by the message listener
	private static BotSettings botConfig; //instance of settings used to initiate the bot.
	private static HashMap<Long, GuildSettings> guildSettings = new HashMap<>(); //settings used per guild.
	
	public BotMain() {
		loadSettings();
		buildBot();
		registerCommands();
	}
	
	//--Startup Methods--//
	private void buildBot() {
		messageListener = new MessageListener();
		commandHandler = new CommandHandler();
		
		//create the bot through jda
		try {
			jdaInstance = new JDABuilder(AccountType.BOT) //bot account, not user account
				.setToken(botConfig.getToken()) //set the bot token for login
				.addEventListener(messageListener) //initializes messagelistener to the bot
				.setGame(Game.playing(botConfig.getBotPlayingMessage())) //bot playing message, showing people usage.
				.build(); //finalizes and builds the bot
		} catch (Exception e) {
			System.err.println("Unable to start the bot!"); //error! program terminates from here.
			e.printStackTrace(); //print error to the console output
		}
	}
	
	private void registerCommands() {
		new CmdSettings().register(commandHandler);
		new CmdPing().register(commandHandler);
		new CmdHelp().register(commandHandler);
		new CmdMinesweeper().register(commandHandler);
		new CmdStar().register(commandHandler);
		new CmdRoll().register(commandHandler);
		new CmdPoll().register(commandHandler);
	}
	
	//--Configuration--//
	public void loadSettings() {
		if (!configFile.exists()) {
			botConfig = new BotSettings();
			saveSettings();
		}
		try {
			botConfig = yamlMapper.readValue(configFile, BotSettings.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the current {@link BotSettings} to the config.yml file.
	 */
	public void saveSettings() {
		try {
			yamlMapper.writeValue(configFile, botConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//--Getters--//
	/**
	 * Returns the JDA instance the bot is currently running on. Can be null before running {@link BotMain#buildBot()}
	 * @return {@link JDA} instance of the bot.
	 */
	public static JDA getJDA() { return jdaInstance; }
	/**
	 * Returns a MessageListener instance which the bot uses for event handling.
	 * @return {@link MessageListener} the bot is using.
	 */
	public static MessageListener getMessageListener() { return messageListener; }
	/**
	 * Returns the a CommandHandler instance that the current {@link MessageHandler} uses. 
	 * @return Current {@link CommandHandler} instance.
	 */
	public static CommandHandler getCommandHandler() { return commandHandler; }
	/**
	 * Returns settings for the whole bot to use. Contains things like the default Command Prefix, bot token, {@link Game} playing
	 * message, and more miscellaneous settings. Saved to config.yml.
	 * @return {@link BotSettings} instance for the whole bot.
	 */
	public static BotSettings getBotSettings() { return botConfig; }
	/**
	 * Returns guild-specific settings which Bot Administrators can set using {@link CmdSettings}
	 * Functionally the same to {@link BotMain#getGuildSettings(long)} without having to call {@link Guild#getIdLong()}
	 * @param guild - {@link Guild} instance to retrieve settings for.
	 * @return {@link GuildSettings} instance for the given guild.
	 * @see BotMain#getGuildSettings(long)
	 */
	public static GuildSettings getGuildSettings(Guild guild) { return getGuildSettings(guild.getIdLong()); }
	/**
	 * Returns guild-specfic settings which Bot Administrators can set using {@link CmdSettings}
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
