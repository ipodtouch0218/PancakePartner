package me.ipodtouch0218.pancakepartner.commands.info;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
import me.ipodtouch0218.pancakepartner.utils.MiscUtils;
import me.ipodtouch0218.sjbotcore.command.BotCommand;
import me.ipodtouch0218.sjbotcore.command.FlagInfo;
import me.ipodtouch0218.sjbotcore.command.FlagSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class CmdHelp extends BotCommand {

	//--Variables & Constructor--//
	private static final int cmdsPerPage = 8;
	
	public CmdHelp() {
		super("help", true, true);
		setHelpInfo("Provides either a list of commands or command-specific usage info.", "help [#|command]");
		
		registerFlag("dm", 0, "Sends help info to your private DM instead of the current channel.");
	}

	//--//
	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, FlagSet flags) {
		MessageChannel channel = msg.getChannel();
		boolean dm = flags.containsFlag("dm");
		User sender = msg.getAuthor();
		if (dm) {
			try {
				channel = sender.openPrivateChannel().complete();
			} catch (Exception e) {
				channel.sendMessage(":pancakes: **Error:** Unable to open a DM with you! Maybe you have DM's closed to server members?").queue();
				return;
			}
		}
		
		int pageNumber = 0;
		if (args.size() >= 1) {
			if (MiscUtils.isInteger(args.get(0))) {
				pageNumber = Integer.parseInt(args.get(0));
			} else {
				Optional<BotCommand> command = BotMain.getBotCore().getCommandHandler().getCommandByName(args.get(0));
				if (!command.isPresent()) {
					channel.sendMessage(":pancakes: **Invalid Argument:** `" + args.get(0) + "` is not a valid command.").queue();
					return;
				}
				outputCommandPage(msg.getGuild(), channel, command.get(), sender);
				if (dm) {
					msg.getChannel().sendMessage(":pancakes: Sent you a DM containing help info!").queue();
				}
				return;
			}
		}
		outputPagedCommandList(channel, pageNumber, sender, null);
	}
	
	private static void outputPagedCommandList(MessageChannel channel, int pagenumber, User sender, Message override) {

		BotCommand[] allCmds = BotMain.getBotCore().getCommandHandler().getAllCommands().toArray(new BotCommand[]{});
		int maxpages = (allCmds.length-1)/cmdsPerPage;
		if (pagenumber > maxpages) { 
			pagenumber = maxpages;
		}
		
		EmbedBuilder page = new EmbedBuilder();
		page.setTitle(":pancakes: **Command List:** `(Page " + (pagenumber+1) + "/" + (maxpages+1) + ")`");
		page.setColor(Color.GREEN);
		for (int i = 0; i < cmdsPerPage; i++) {
			if (i + (pagenumber * cmdsPerPage) >= allCmds.length) { break; }
			BotCommand nextCmd = allCmds[i + (pagenumber * cmdsPerPage)];

			String title = nextCmd.getName();
			page.addField(title, nextCmd.getDescription(), false);
		}
		page.setFooter("Requested by " + MessageUtils.nameAndDiscrim(sender), sender.getAvatarUrl()).setTimestamp(Instant.now());
		
		channel.sendMessage(page.build()).queue();
	}
	
	private static void outputCommandPage(Guild guild, MessageChannel channel, BotCommand cmd, User sender) {
		String cmdPrefix = BotMain.getBotCore().getBotSettings().defaultCommandPrefix;
		if (guild != null) {
			cmdPrefix = BotMain.getGuildSettings(guild).commandPrefix;
		}
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(":pancakes: **Command Help:** `" + cmd.getName() + "`");
		embed.setColor(1752220);
		embed.setDescription("*Usage: " + cmdPrefix + cmd.getUsage() + "*");
		embed.addField("Description", cmd.getDescription(), false);
		if (!cmd.getFlags().isEmpty()) {
			String flagList = "";
			for (FlagInfo flags : cmd.getFlags()) {
				flagList += ("**`-" + flags.getUsage() + "`** \u21D2 " + flags.getDescription() + "\n");
			}
			embed.addField("Flags", flagList.substring(0, flagList.length()-1), false);
		}
		embed.addField("Permission", (cmd.getPermission() == null ? "None" : cmd.getPermission().name()), true);
		
		String aliases = cmd.getAliases().map(Arrays::toString).orElse("None");
		embed.addField("Aliases", aliases, true);
		
		embed.setFooter("Requested by " + MessageUtils.nameAndDiscrim(sender), sender.getAvatarUrl()).setTimestamp(Instant.now());
		
		channel.sendMessage(embed.build()).queue();
	}
}