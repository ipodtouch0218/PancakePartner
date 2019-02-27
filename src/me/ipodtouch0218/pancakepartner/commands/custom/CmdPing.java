package me.ipodtouch0218.pancakepartner.commands.custom;

import java.util.ArrayList;
import java.util.HashMap;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.commands.BotCommand;
import me.ipodtouch0218.pancakepartner.commands.CommandFlag;
import net.dv8tion.jda.core.entities.Message;

public class CmdPing extends BotCommand {

	public CmdPing() {
		super("ping", true, true);
		setHelpInfo("Pong! Replies with the time it takes for the bot to connect to Discord.", "ping");
		setAliases("pong");
	}
	
	//--//
	@Override
	public void execute(Message msg, String alias, ArrayList<String> arguments, HashMap<String,CommandFlag> flags) {
		String reply = "Pong~!";
		if (alias.equalsIgnoreCase("pong")) {
			reply = "... Ping?";
		}
		
		msg.getChannel().sendMessage(":pancakes: **" + reply + "** `" + BotMain.getJDA().getPing() + "ms`").queue();
		return;
	}

}
