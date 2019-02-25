package me.ipodtouch0218.pancakepartner.commands;

import java.util.ArrayList;

import net.dv8tion.jda.core.entities.Message;

public class CmdPoll extends BotCommand {

	public CmdPoll() {
		super("poll", true, false);
		setHelpInfo("", "poll <");
	}

	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, ArrayList<String> flags) {
		
	}
	
}
