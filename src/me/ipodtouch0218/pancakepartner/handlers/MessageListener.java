package me.ipodtouch0218.pancakepartner.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.commands.CmdStar;
import me.ipodtouch0218.pancakepartner.commands.CmdStar.StarredMessageInfo;
import me.ipodtouch0218.pancakepartner.config.GuildSettings;
import me.ipodtouch0218.pancakepartner.utils.MiscUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
	
	//---Event Handling---//
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		Message msg = e.getMessage();
		User author = e.getAuthor();
		
		if (e.getAuthor().getIdLong() == BotMain.getJDA().getSelfUser().getIdLong()) { return; }
		if (CommandHandler.isCommand(msg)) {
			//the message is a command, send to the commandhandler
			BotMain.getCommandHandler().executeCommand(msg, author); //result is if the command was successful.
		}
	}
	
	
	//---Starred Message System---//
	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e) {
		handleStarredReaction(e);
	}
	
	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent e) {
		handleStarredReaction(e);
	}
	
	private void handleStarredReaction(GenericGuildMessageReactionEvent e) {
		if (e.getReactionEmote().getName().equals("\u2B50")) { //star emote added
			StarredMessageInfo starinfo = CmdStar.getStarredMessageInfo();
			GuildSettings guildSettings = BotMain.getGuildSettings(e.getGuild());
			TextChannel starChannel = BotMain.getJDA().getTextChannelById(guildSettings.getStarChannelID());
			
			if (starinfo.isMessageIgnored(e.getMessageIdLong())) {
				return;
			}
			
			if (starinfo.isMessageStarred(e.getMessageIdLong())) {
				e.getChannel().getMessageById(e.getMessageId()).queue(m -> {
					CmdStar.editStarredMessage(m, starChannel);
				});
			} else {
				int count = 1;
				try {
					count = e.getReaction().getUsers().complete().size();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				if (count >= guildSettings.getStarRequiredStars()) {
					e.getChannel().getMessageById(e.getMessageId()).queue(m -> {
						CmdStar.sendStarredMessage(m, starChannel);
					});
				}
			}
		}
	}
	
	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent e) {
		StarredMessageInfo info = CmdStar.getStarredMessageInfo();
		if (info.getStarredMessages().containsKey(e.getMessageIdLong())) { //only if the starred message embed posted in the channel was deleted
			info.getStarredMessages().remove(e.getMessageIdLong());
			CmdStar.saveStarredMessages();
		}
	}
	
	private static final Pattern idPattern = Pattern.compile("\\d+");
//	private static final Pattern 
	@Override
	public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent e) {
		if (e.getUser().equals(e.getJDA().getSelfUser())) { return; }
		if (e.getReactionEmote().getName().equals(new String(Character.toChars(0x1F6AB)))) {
			e.getChannel().getMessageById(e.getMessageIdLong()).queue(m -> {
				if (!m.getContentDisplay().startsWith(":star: **Starred Message Notification:** Your starred message (ID:")) { return; }
				
				Matcher linkMatcher = MiscUtils.PATTERN_MESSAGE_LINK.matcher(m.getContentDisplay());
				linkMatcher.find();
				if (!linkMatcher.matches()) {
					//malformed message
					return;
				}
				Guild guild = BotMain.getJDA().getGuildById(linkMatcher.group("guild"));
				GuildSettings guildSettings = BotMain.getGuildSettings(guild);
				TextChannel starChannel = BotMain.getJDA().getTextChannelById(guildSettings.getStarChannelID());
				
				Matcher matcher = idPattern.matcher(m.getContentDisplay());
				matcher.find();
				long id = Long.parseLong(matcher.group());
				if (CmdStar.getStarredMessageInfo().isMessageIgnored(id)) {
					return;
				}
				
				long toDeleteID = CmdStar.getStarredMessageInfo().getStarredMessages().get(id);
				starChannel.deleteMessageById(toDeleteID).queue();
				CmdStar.getStarredMessageInfo().getStarredMessages().remove(id);
				CmdStar.getStarredMessageInfo().getIgnoredMessages().add(id);
				CmdStar.saveStarredMessages();
				
				m.editMessage(":star: **Star Message Notification:** Successfully removed your message with ID \"" + id + "\" as a starred message.").queue();
			});
		}
	}
}
