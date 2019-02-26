package me.ipodtouch0218.pancakepartner.commands;

import java.util.ArrayList;
import java.util.HashMap;

import me.ipodtouch0218.pancakepartner.handlers.CommandHandler;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

public abstract class BotCommand {

	//--Variables & Constructor--//
	private String name; //Command name. Used to run the command itself, [prefix][name]
	private String[] aliases; //The command can also be ran through these names. Command names take priority over aliases (if there's overlap)
	private Permission permission; //Permission the user needs to use this command.
	
	private String usage; //Command usage, <> = required parameters, [] = optional parameters 
	private String description; //Command description, used in outputting the help page.
	
	private HashMap<String,Integer> registeredFlags = new HashMap<>(); //List of all flags. Flags must be registered before they will be parsed as flags.
	
	private boolean useInGuilds; //Command can be used within Guilds
	private boolean useInDMs; //Command can be used within DMs
	
	public BotCommand(String name, boolean guilds, boolean dms) {
		this(name, guilds, dms, null);
	}
	public BotCommand(String name, boolean guilds, boolean dms, Permission perm) {
		this.name = name;
		this.useInGuilds = guilds;
		this.useInDMs = dms;
		this.permission = perm;
		
		this.usage = name;
		this.description = "No Info Provided.";
	}
	
	//--//
	public abstract void execute(Message msg, String alias, ArrayList<String> args, ArrayList<CommandFlag> flags);
	
	public void register(CommandHandler cmdHandler) {
		cmdHandler.registerCommand(this);
	}
	
	//--Setters--//
	public void setPermission(Permission perm) {
		this.permission = perm;
	}
	public void setHelpInfo(String description, String usage) {
		this.usage = usage;
		this.description = description; 
	}
	public void setAliases(String... alises) {
		this.aliases = alises;
	}
	public void registerFlag(String tag, int parameters) {
		registeredFlags.put(tag,parameters);
	}
	
	//--Getters--//
	public String getName() { return name; }
	public String[] getAliases() { return aliases; }
	public String getUsage() { return usage; }
	public String getDescription() { return description; }
	public boolean isFlagRegistered(String tag) { return registeredFlags.containsKey(tag); }
	public Permission getPermission() { return permission; }
	public HashMap<String,Integer> getFlags() { return registeredFlags; }
	
	public boolean canExecute(Message msg) {
		switch (msg.getChannelType()) {
		case PRIVATE:
		case GROUP: { return useInDMs; }
		
		case TEXT: { return useInGuilds; }
		default: { return false; }
		}
	}
	
	//--Misc?--//
	//TODO: find something better than this...
	public static boolean containsFlag(String tag, ArrayList<CommandFlag> flags) {
		for (CommandFlag flag : flags) {
			if (flag.getTag().equalsIgnoreCase(tag)) { return true; }
		}
		return false;
	}
}
