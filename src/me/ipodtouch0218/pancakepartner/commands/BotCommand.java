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
	/**
	 * Method that gets called when a message comes from a user that both 
	 * starts with the command prefix + {@link BotCommand#name} OR any {@link BotCommand#aliases}
	 * & The sender has the correct {@link BotCommand#permission} to use this command.
	 * 
	 * @param msg - Discord message instance.
	 * @param alias - Alias used to execute the command.
	 * @param args - List of arguments separated by spaces (Excluding flags and their parameters).
	 * @param flags - List of flags (including flag parameters) which were parsed out of the command.
	 */
	public abstract void execute(Message msg, String alias, ArrayList<String> args, HashMap<String,CommandFlag> flags);
	
	/**
	 * Registers this command's instance to a {@link CommandHandler}.
	 * @param cmdHandler - CommandHandler instance to register to.
	 */
	public void register(CommandHandler cmdHandler) {
		cmdHandler.registerCommand(this);
	}
	
	//--Setters--//
	/**
	 * Sets the {@link Permission} a sender of a command must have to execute this command
	 * @param permission - New {@link Permission} to require.
	 */
	public void setPermission(Permission permission) {
		this.permission = permission;
	}
	/**
	 * Populates the description and usage. Natively used by {@link CmdHelp}
	 * @param description - Description of the command.
	 * @param usage - Usage of the command including possible arguments.
	 */
	public void setHelpInfo(String description, String usage) {
		this.usage = usage;
		this.description = description; 
	}
	/**
	 * Sets aliases that the command can use in substitution of the {@link BotCommand#name}. The name of the command
	 * has higher priority than an alias.
	 * @param alises - An array of aliases to use.
	 */
	public void setAliases(String... alises) {
		this.aliases = alises;
	}
	/**
	 * Adds a {@link CommandFlag} to this command. A flag must be registered in order to be parsed
	 * properly by the {@link CommandHandler}.
	 * 
	 * @param tag - Name of the flag to add.
	 * @param parameters - Required number of parameters for this flag.
	 */
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
}
