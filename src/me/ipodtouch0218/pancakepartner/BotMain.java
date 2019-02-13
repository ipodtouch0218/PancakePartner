package me.ipodtouch0218.pancakepartner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import me.ipodtouch0218.pancakepartner.commands.*;
import me.ipodtouch0218.pancakepartner.handlers.CommandHandler;
import me.ipodtouch0218.pancakepartner.handlers.MessageListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;

public class BotMain {

	/* ----TODO----
	 * > Create a way to display flags in the help command
	 * > Guild independence (star command, settings)
	 */
	
	
	///STATIC STUFF
	//--Main Method--//
	
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
	@SuppressWarnings("deprecation")
	private void buildBot() {
		messageListener = new MessageListener();
		commandHandler = new CommandHandler();
		
		//create the bot through jda
		try {
			jdaInstance = new JDABuilder(AccountType.BOT)	//bot account, not user account
				.setToken(botConfig.getToken())			//set the bot token for login
				.setAudioEnabled(false)						//not a music bot, ignore audio
				.addEventListener(messageListener)			//initializes messagelistener to the bot
				.setGame(Game.playing(botConfig.getBotPlayingMessage()))	//bot playing message, showing people usage.
				.buildBlocking();							//finalizes and builds the bot
		} catch (Exception e) {
			System.err.println("Unable to start the bot!");	//error! shoot
			e.printStackTrace();							//print error to the console output
		}
	}
	
	private void registerCommands() {
		new CmdSettings().register(commandHandler);
		new CmdPing().register(commandHandler);
		new CmdHelp().register(commandHandler);
		new CmdMinesweeper().register(commandHandler);
		new CmdStar().register(commandHandler);
	}
	
	//--Misc Stuff--//
	private void loadSettings() {
		if (!configFile.exists()) {
			botConfig = new BotSettings();
			saveSettings();
			//save default values to the file.
		}
		try {
			botConfig = yamlMapper.readValue(configFile, BotSettings.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveSettings() {
		try {
			yamlMapper.writeValue(configFile, botConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//--Getters--//
	public static JDA getJDA() { return jdaInstance; }
	public static MessageListener getMessageListener() { return messageListener; }
	public static CommandHandler getCommandHandler() { return commandHandler; }
	public static BotSettings getBotSettings() { return botConfig; }
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
	public static GuildSettings getGuildSettings(Guild guild) { return getGuildSettings(guild.getIdLong()); }
}
