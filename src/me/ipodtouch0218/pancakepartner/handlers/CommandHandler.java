package me.ipodtouch0218.pancakepartner.handlers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;

import me.ipodtouch0218.pancakepartner.commands.BotCommand;
import me.ipodtouch0218.pancakepartner.utils.MiscUtils;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class CommandHandler {

	///INSTANCE STUFFS
	//--Variables & Constructor--//
	
	private static String cmdPrefix = ";";		//prefix to specify a command
	private boolean deleteCommand = false;		//if the sender's message should be removed
	private ArrayList<BotCommand> commands		//list of all registered commands
				= new ArrayList<>();
	
	//--Command Execution--//
	public void executeCommand(Message msg, User sender) {
		if (!isCommand(msg)) { return; }	//not a command, but somehow got passed as one? huh.
		
		MessageChannel channel = msg.getChannel();
		String[] splitMsg = msg.getContentDisplay().replaceFirst(Matcher.quoteReplacement(cmdPrefix), "").split(" ");
		BotCommand command = getCommandByName(splitMsg[0]);
		if (command == null) {	
			//invalid command, send error and return.
			BotCommand closest = closestCommand(splitMsg[0]);
			channel.sendMessage(":pancakes: **Unknown Command:** `" + splitMsg[0] + "`." + (closest!=null ? " Did you mean to type `" + closest.getName() + "`?" : "")).queue();
			return;
		}
		if (!command.canExecute(msg)) {
			//command cannot be ran through this channel
			channel.sendMessage(":pancakes: **Error:** You cannot run this command in a " + (msg.getChannelType() == ChannelType.TEXT ? "Guild" : "DM") + "!").queue();
			return;
		}
		String[] args = MiscUtils.arrayRemoveAndShrink(splitMsg, 0);
		
		if (msg.getChannelType() == ChannelType.TEXT) {	
			//guild text channel, can check for permissions
			if (command.getPermission() != null && !msg.getMember().hasPermission(command.getPermission())) {
				//sender doesn't have permission, deny usage.
				channel.sendMessage(":pancakes: **Error:** You must have the `" + command.getPermission().name() + "` permission to use this command!").queue();
				return;
			}
		}
		
		try {
			command.execute(msg, args);
			
			if (deleteCommand) {
				msg.delete().queue();
			}
		} catch (Exception e) {
			//some error occured? output error message.
			StringWriter stacktrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stacktrace));
			channel.sendMessage(":pancakes: **Command Error Caught:** " + e.getMessage() + " ```" + stacktrace.toString() + "```").queue();
		}
	}
	
	//--Misc Functions--//
	
	public static boolean isCommand(Message msg) {
		return msg.getContentRaw().startsWith(cmdPrefix);
	}
	
	private BotCommand closestCommand(String input) {
		int cutoff = 6;
		
		BotCommand closest = null;
		int closestDistance = -1;
		for (BotCommand other : commands) {
			int distance = MiscUtils.calcLevenshteinDistance(input, other.getName());
			if (closestDistance == -1 || distance < closestDistance) {
				closestDistance = distance;
				closest = other;
			}
		}
		
		if (closestDistance <= cutoff) {
			return closest;
		}
		return null;
	}
	
	
	//--Command Management--//
	public boolean registerCommand(BotCommand newCmd) {
		return commands.add(newCmd);
	}
	public boolean unregisterCommand(BotCommand newCmd) {
		return commands.remove(newCmd);
	}
	
	//--Getters--//
	public String getCommandPrefix() { return cmdPrefix; }
	public ArrayList<BotCommand> getAllCommands() { return commands; }
	public BotCommand getCommandByName(String name) {
		for (BotCommand cmds : commands) {
			if (cmds.getName().equalsIgnoreCase(name)) { return cmds; }
		}
		return null;
	}

}
