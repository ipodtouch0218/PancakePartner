package me.ipodtouch0218.pancakepartner.commands.custom;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.commands.BotCommand;
import me.ipodtouch0218.pancakepartner.commands.CommandFlag;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
import me.ipodtouch0218.pancakepartner.utils.MiscUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class CmdHelp extends BotCommand {

	//--Variables & Constructor--//
	private static final int cmdsPerPage = 6;
	
	public CmdHelp() {
		super("help", true, true);
		setHelpInfo("Provides either a list of commands or command-specific usage info.", "help [#|command]");
		registerFlag("dm", 0);
	}

	//--//
	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, HashMap<String,CommandFlag> flags) {
		MessageChannel channel = msg.getChannel();
		boolean dm = flags.containsKey("dm");
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
				BotCommand command = BotMain.getCommandHandler().getCommandByName(args.get(0));
				if (command == null) {
					channel.sendMessage(":pancakes: **Invalid Argument:** `" + args.get(0) + "` is not a valid command.").queue();
					return;
				}
				outputCommandPage(msg.getGuild(), channel, command, sender);
				if (dm) {
					msg.getChannel().sendMessage(":pancakes: Sent you a DM containing help info!").queue();
				}
				return;
			}
		}
		outputPagedCommandList(channel, pageNumber, sender, null);
	}
	
	private static void outputPagedCommandList(MessageChannel channel, int pagenumber, User sender, Message override) {

		BotCommand[] allCmds = BotMain.getCommandHandler().getAllCommands().toArray(new BotCommand[]{});
		int maxpages = ((allCmds.length-1)/cmdsPerPage);
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
			page.addField(title, nextCmd.getDescription(), true);
		}
		page.setFooter("Requested by " + MessageUtils.nameAndDiscrim(sender), sender.getAvatarUrl()).setTimestamp(Instant.now());
		
		channel.sendMessage(page.build()).queue();
	}
	
	private static void outputCommandPage(Guild guild, MessageChannel channel, BotCommand cmd, User sender) {
		String cmdPrefix = BotMain.getBotSettings().getDefaultCommandPrefix();
		if (guild != null) {
			cmdPrefix = BotMain.getGuildSettings(guild).getCommandPrefix();
		}
		
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(":pancakes: **Command Help:** `" + cmd.getName() + "`");
		embed.setColor(Color.GREEN);
		embed.setDescription("*Usage: " + cmdPrefix + cmd.getUsage() + "*");
		embed.addField("Description", cmd.getDescription(), false);
		embed.addField("Required Permission", (cmd.getPermission() == null ? "None" : cmd.getPermission().name()), false);
		
		if (cmd.getAliases() != null) {
			embed.addField("Aliases", Arrays.toString(cmd.getAliases()), false);
		}
		embed.setFooter("Requested by " + MessageUtils.nameAndDiscrim(sender), sender.getAvatarUrl()).setTimestamp(Instant.now());
		
		channel.sendMessage(embed.build()).queue();
	}
}