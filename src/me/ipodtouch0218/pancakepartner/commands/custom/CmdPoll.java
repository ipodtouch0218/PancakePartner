package me.ipodtouch0218.pancakepartner.commands.custom;

import java.util.ArrayList;

import me.ipodtouch0218.pancakepartner.commands.BotCommand;
import me.ipodtouch0218.pancakepartner.commands.CommandFlag;
import net.dv8tion.jda.core.entities.Message;

public class CmdPoll extends BotCommand {

	public CmdPoll() {
		super("poll", true, false);
		setHelpInfo("", "poll <");
	}

	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, ArrayList<CommandFlag> flags) {
		
	}
	
}
