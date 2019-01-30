package me.ipodtouch0218.pancakepartner.commands;

import me.ipodtouch0218.pancakepartner.handlers.CommandHandler;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

public abstract class BotCommand {

	//--Variables & Constructor--//
	private String name;			//Command name. Used to run the command itself, [prefix][name]
	private Permission permission;	//Permission the user needs to use this command.
	
	private String usage;			//Command usage, <> = required parameters, [] = optional parameters 
	private String description;		//Command description, used in outputting the help page.
	
	private boolean useInGuilds;	//Command can be used within Guilds
	private boolean useInDMs;		//Command can be used within DMs
	
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
	public abstract void execute(Message msg, String[] args);
	
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
	
	//--Getters--//
	public String getName() { return name; }
	public String getUsage() { return usage; }
	public String getDescription() { return description; }
	public Permission getPermission() { return permission; }
	
	public boolean canExecute(Message msg) {
		switch (msg.getChannelType()) {
		case PRIVATE:
		case GROUP: { return useInDMs; }
		
		case TEXT: { return useInGuilds; }
		default: { return false; }
		}
		
	}
}
