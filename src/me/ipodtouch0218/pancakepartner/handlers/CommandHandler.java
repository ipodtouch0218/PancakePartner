package me.ipodtouch0218.pancakepartner.handlers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.commands.BotCommand;
import me.ipodtouch0218.pancakepartner.utils.MiscUtils;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class CommandHandler {

	///INSTANCE STUFFS
	//--Variables & Constructor--//
	private ArrayList<BotCommand> commands		//list of all registered commands
				= new ArrayList<>();
	
	//--Command Execution--//
	public void executeCommand(Message msg, User sender) {
		if (!isCommand(msg)) { return; }	//not a command, but somehow got passed as one? huh.
		MessageChannel channel = msg.getChannel();
		
		String prefixRegex = Matcher.quoteReplacement(BotMain.getBotSettings().getCommandPrefix());
		String strippedMessage = msg.getContentDisplay().replaceFirst(prefixRegex, "");	//removed the command prefix from the message
		String[] splitMsg = strippedMessage.split(" ");	
		
		BotCommand command = getCommandByName(splitMsg[0]); 			//first argument is the command itself.
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
		String[] args = MiscUtils.arrayRemoveAndShrink(splitMsg, 0);	//dont pass the command as an argument 
		
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
			
			if (BotMain.getBotSettings().getDeleteIssuedCommand()) {
				msg.delete().queue();
			}
		} catch (Exception e) {
			//some error occured? output error message to discord
			StringWriter stacktrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stacktrace));
			channel.sendMessage(":pancakes: **Command Error Caught:** " + e.getMessage() + " ```" + stacktrace.toString() + "```").queue();
		}
	}
	
	//--Misc Functions--//
	public static boolean isCommand(Message msg) {
		return msg.getContentRaw().startsWith(BotMain.getBotSettings().getCommandPrefix());
	}
	
	private BotCommand closestCommand(String input) {

		BotCommand closest = null;
		float closestDistance = 1;
		for (BotCommand other : commands) {
			int distance = MiscUtils.calcLevenshteinDistance(input, other.getName());
			float smartDistance = (float) distance / (float) other.getName().length();
			if (smartDistance < closestDistance) {
				closestDistance = smartDistance;
				closest = other;
			}
		}
		
		if (closestDistance <= 0.5) { //at least half the characters have to match for a suggestion
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
	public ArrayList<BotCommand> getAllCommands() { return commands; }
	public BotCommand getCommandByName(String name) {
		for (BotCommand cmds : commands) {
			if (cmds.getName().equalsIgnoreCase(name)) { return cmds; }
		}
		return null;
	}

}
