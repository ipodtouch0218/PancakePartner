package me.ipodtouch0218.pancakepartner.commands.staff;

import java.util.ArrayList;

import me.ipodtouch0218.sjbotcore.command.BotCommand;
import me.ipodtouch0218.sjbotcore.command.FlagSet;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

public class CmdLockdown extends BotCommand {

	public CmdLockdown() {
		super("lockdown", true, false, Permission.MANAGE_SERVER);
		setHelpInfo("Enables slowmode for all text channels to help combat raids", "lockdown");
	}

	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, FlagSet flags) {
		msg.getGuild().getTextChannels().forEach(tc -> tc.getManager().setSlowmode(120).queue());
		msg.getChannel().sendMessage(":pancakes: Enabled slowmode for all channels!").queue();
	}

	
	
	
}
