package me.ipodtouch0218.pancakepartner.commands.info;

import java.util.ArrayList;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.sjbotcore.command.BotCommand;
import me.ipodtouch0218.sjbotcore.command.FlagSet;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.sharding.ShardManager;

public class CmdPing extends BotCommand {

	public CmdPing() {
		super("ping", true, true);
		setHelpInfo("Pong! Replies with the time it takes for the bot to connect to Discord.", "ping");
		setAliases("pong");
	}
	
	//--//
	@Override
	public void execute(Message msg, String alias, ArrayList<String> arguments, FlagSet flags) {
		String reply = "Pong~!";
		switch (alias.toLowerCase()) {
		case "pong": { reply = "... Ping?"; break; }
		}
		ShardManager m = BotMain.getBotCore().getShardManager();
		
		String message = String.format(":pancakes: **%s** \\|\\| Shard: `%dms` | Average: `%.0fms`",
				reply, msg.getJDA().getGatewayPing(), m.getAverageGatewayPing());

		msg.getChannel().sendMessage(message).queue();
		
	}

}
