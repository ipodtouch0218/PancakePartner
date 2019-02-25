package me.ipodtouch0218.pancakepartner.commands;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import me.ipodtouch0218.pancakepartner.BotMain;
//import me.ipodtouch0218.pancakepartner.handlers.MessageListener;
import me.ipodtouch0218.pancakepartner.handlers.ReactionHandler;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
import me.ipodtouch0218.pancakepartner.utils.MiscUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;

public class CmdHelp extends BotCommand {

	//--Variables & Constructor--//
	private static final int cmdsPerPage = 3;
	
	public CmdHelp() {
		super("help", true, true);
		setHelpInfo("Provides either a list of commands or command-specific usage info.", "help [#|command]");
	}

	//--//
	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, ArrayList<String> flags) {
		MessageChannel channel = msg.getChannel();
		boolean dm = flags.contains("-dm");
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
				//this is a page number, show the next set of commands.
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

		ArrayList<BotCommand> allCmds = BotMain.getCommandHandler().getAllCommands();
		int maxpages = ((allCmds.size()-1)/cmdsPerPage);
		if (pagenumber > maxpages) { 
			pagenumber = maxpages;
		}
//		int finalpagenumber = pagenumber;
		
		EmbedBuilder page = new EmbedBuilder();
		page.setTitle(":pancakes: **Command List:** `(Page " + (pagenumber+1) + "/" + (maxpages+1) + ")`");
		page.setColor(Color.GREEN);
		for (int i = 0; i < cmdsPerPage; i++) {
			if (i + (pagenumber * cmdsPerPage) >= allCmds.size()) { break; }
			BotCommand nextCmd = allCmds.get(i + (pagenumber * cmdsPerPage));

			String title = nextCmd.getName();
			page.addField(title, nextCmd.getDescription(), true);
		}
		page.setFooter("Requested by " + MessageUtils.nameAndDiscrim(sender), sender.getAvatarUrl()).setTimestamp(Instant.now());
		
//		if (override != null) {
//			override.editMessage(page.build()).queue();
//			return;
//		}
		channel.sendMessage(page.build()).queue(m -> {
//			HelpPageHandler newHandler = new HelpPageHandler(finalpagenumber, maxpages);
//			newHandler.setOwnerId(sender.getIdLong());
//			MessageListener.addReactionHandler(m.getIdLong(), newHandler);
//			ReactionHandler.setReactions(m, "\u25C0", "\u25B6");
		});
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
	
	//----other----//
	public static class HelpPageHandler extends ReactionHandler {
		
		private int page;
		private int maxPage;
		public HelpPageHandler(int startingPage, int maxPage) {
			this.page = startingPage;
			this.maxPage = maxPage;
		}
		
		public void handleReaction(GenericMessageReactionEvent e, boolean isOwner) {
			if (!isOwner) { return; }
			switch (e.getReactionEmote().getName()) {
			case "\u25C0": 
				page -= 1;
				break;
			case "\u25B6": 
				page += 1;
				break;
			}
			
			page = Math.max(0, Math.min(page, maxPage));
			
			e.getChannel().getMessageById(e.getMessageIdLong()).queue(m -> {
				outputPagedCommandList(e.getChannel(), page, e.getUser(), m);
				clearOwnerReactions(m);
			});
		}
		
	}
}
