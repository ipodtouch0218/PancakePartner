package me.ipodtouch0218.pancakepartner.commands;

import me.ipodtouch0218.pancakepartner.BotMain;
import net.dv8tion.jda.core.entities.Message;

public class CmdPing extends BotCommand {

	public CmdPing() {
		super("ping", true, true);
		setHelpInfo("Pong! Replies with the time it takes for the bot to connect to Discord.", "ping");
		setAliases("pong");
	}
	
	//--//
	@Override
	public void execute(Message msg, String alias, String[] arguments) {
		String reply = "Pong~!";
		if (alias.equalsIgnoreCase("pong")) {
			reply = "... Ping?";
		}
		
		msg.getChannel().sendMessage(":pancakes: **" + reply + "** `" + BotMain.getJdaInstance().getPing() + "ms`").queue();
		return;
	}

}