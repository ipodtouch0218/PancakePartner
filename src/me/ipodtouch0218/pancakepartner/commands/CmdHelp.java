package me.ipodtouch0218.pancakepartner.commands;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
import me.ipodtouch0218.pancakepartner.utils.MiscUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class CmdHelp extends BotCommand {

	//--Variables & Constructor--//
	private static final int cmdsPerPage = 8;
	
	public CmdHelp() {
		super("help", true, true);
		setHelpInfo("Provides either a list of commands or command-specific usage info.", "help [#|command]");
	}

	//--//
	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, ArrayList<String> flags) {
		MessageChannel channel = msg.getChannel();
		User sender = msg.getAuthor();
		
		if (args.size() <= 0) { //no parameters set, default to first help page.
			outputPagedCommandList(msg.getGuild(), channel, 0, sender);
			return;
		}
		if (args.size() >= 1) { //one parameter set, either a page # or a command
			if (MiscUtils.isInteger(args.get(0))) {
				//this is a page number, show the next set of commands.
				int pageNumber = Integer.parseInt(args.get(0));
				outputPagedCommandList(msg.getGuild(), channel, pageNumber, sender);
				return;
			}
			//not a page number, it's a subpage.
			
			BotCommand command = BotMain.getCommandHandler().getCommandByName(args.get(0));
			if (command == null) {
				channel.sendMessage(":pancakes: **Invalid Argument:** `" + args.get(0) + "` is not a valid command.").queue();
				return;
			}
			
			outputCommandPage(msg.getGuild(), channel, command, sender);
		}
		return;
	}
	
	private void outputPagedCommandList(Guild guild, MessageChannel channel, int pagenumber, User sender) {
		ArrayList<BotCommand> allCmds = BotMain.getCommandHandler().getAllCommands();
		int maxpages = ((allCmds.size()-1)/cmdsPerPage);
		if (pagenumber > maxpages) { 
			pagenumber = maxpages;
		}
		
		EmbedBuilder page = new EmbedBuilder();
		page.setTitle(":pancakes: **Command List:** `(Page " + (pagenumber+1) + "/" + (maxpages+1) + ")`");
		page.setColor(Color.GREEN);
		for (int i = 0; i < cmdsPerPage; i++) {
			if (i + (pagenumber * cmdsPerPage) >= allCmds.size()) { break; }
			BotCommand nextCmd = allCmds.get(i + (pagenumber * cmdsPerPage));

			String title = nextCmd.getName();
			page.addField(BotMain.getGuildSettings(guild).getCommandPrefix() + title, nextCmd.getDescription(), false);
		}
		page.setFooter("Requested by " + MessageUtils.nameAndDiscrim(sender), sender.getAvatarUrl()).setTimestamp(Instant.now());
		
		channel.sendMessage(page.build()).queue();
	}
	
	private void outputCommandPage(Guild guild, MessageChannel channel, BotCommand cmd, User sender) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(":pancakes: **Command Help:** `" + cmd.getName() + "`");
		embed.setColor(Color.GREEN);
		embed.setDescription("*Usage: " + BotMain.getGuildSettings(guild).getCommandPrefix() + cmd.getUsage() + "*");
		embed.addField("Description", cmd.getDescription(), false);
		embed.addField("Required Permission", (cmd.getPermission() == null ? "None" : cmd.getPermission().name()), false);
		
		if (cmd.getAliases() != null) {
			embed.addField("Aliases", Arrays.toString(cmd.getAliases()), false);
		}
		embed.setFooter("Requested by " + MessageUtils.nameAndDiscrim(sender), sender.getAvatarUrl()).setTimestamp(Instant.now());
		
		channel.sendMessage(embed.build()).queue();
	}
}
