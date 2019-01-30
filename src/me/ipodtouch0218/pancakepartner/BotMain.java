package me.ipodtouch0218.pancakepartner;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import me.ipodtouch0218.pancakepartner.commands.CmdHelp;
import me.ipodtouch0218.pancakepartner.commands.CmdPing;
import me.ipodtouch0218.pancakepartner.commands.CmdStar;
import me.ipodtouch0218.pancakepartner.handlers.CommandHandler;
import me.ipodtouch0218.pancakepartner.handlers.MessageListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;

public class BotMain {

	///STATIC STUFF
	//--Main Method--//
	
	public static void main(String[] args) {
		new BotMain();
	}
	
	///INSTANCE STUFF
	//--Variables & Constructor--//
	private final File settingsFile = new File("settings.yml");
	private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
	
	private static JDA jdaInstance;					//instance for the jda, the bot itself.
	private static MessageListener messageListener;	//message listener, passes messages to the command handler 
	private static CommandHandler commandHandler;	//command handler, performs commands passed by the message listener
	private static BotSettings settings;			//instance of settings used to initiate the bot and for misc command settings.
	
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
				.setToken(settings.getToken())				//set the bot token for login
				.setAudioEnabled(false)						//not a music bot, ignore audio
				.addEventListener(messageListener)			//initializes messagelistener to the bot
				.setGame(Game.playing(settings.getBotPlayingMessage()))	//bot playing message, showing people usage.
				.buildBlocking();							//finalizes and builds the bot
		} catch (Exception e) {
			System.err.println("Unable to start the bot!");	//error! shoot
			e.printStackTrace();							//print error to the console output
		}
	}
	
	private void registerCommands() {
		new CmdPing().register(commandHandler);
		new CmdHelp().register(commandHandler);
		
		//TODO: rewrite star command to handle multiple guilds.
		new CmdStar(jdaInstance.getGuilds().get(0).getTextChannelsByName(settings.getCmdStarChannelName(), true).get(0)).register(commandHandler);
	}
	
	//--Misc Stuff--//
	private void loadSettings() {
		if (!settingsFile.exists()) {
			settings = new BotSettings();
			saveSettings();
			//save default values to the file.
		}
		try {
			settings = yamlMapper.readValue(settingsFile, BotSettings.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveSettings() {
		try {
			yamlMapper.writeValue(settingsFile, settings);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//--Getters--//
	public static JDA getJdaInstance() { return jdaInstance; }
	public static MessageListener getMessageListener() { return messageListener; }
	public static CommandHandler getCommandHandler() { return commandHandler; }
	public static BotSettings getBotSettings() { return settings; }
}
