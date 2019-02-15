package me.ipodtouch0218.pancakepartner.handlers;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.commands.CmdStar;
import me.ipodtouch0218.pancakepartner.commands.CmdStar.StarredMessageInfo;
import me.ipodtouch0218.pancakepartner.config.GuildSettings;
import me.ipodtouch0218.pancakepartner.utils.MessageInfoContainer;
import net.dv8tion.jda.core.entities.ChannelType;
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
	

	private static final String STAR_REACTION = "\u2B50";
	private static final String REMOVE_REACTION = new String(Character.toChars(0x1F6AB));
	
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
		if (e.getReactionEmote().getName().equals(STAR_REACTION)) { //star emote added
			if (e.getChannel().getType() != ChannelType.TEXT) { return; }
			
			StarredMessageInfo starinfo = CmdStar.getStarredMessageInfo();
			GuildSettings guildSettings = BotMain.getGuildSettings(e.getGuild());
			TextChannel starChannel = BotMain.getJDA().getTextChannelById(guildSettings.getStarChannelID());
			
			if (starinfo.isMessageIgnored(e.getMessageIdLong())) {
				return;
			}
			
			if (starinfo.isMessageStarred(e.getMessageIdLong())) {
				e.getChannel().getMessageById(e.getMessageId()).queue(m -> {
					CmdStar.editStarredMessage(m);
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
	
	@Override
	public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent e) {
		if (e.getUser().equals(e.getJDA().getSelfUser())) { return; }
		if (e.getReactionEmote().getName().equals(REMOVE_REACTION)) {
			e.getChannel().getMessageById(e.getMessageIdLong()).queue(m -> {
				StarredMessageInfo info = CmdStar.getStarredMessageInfo();
				if (!info.isNotificationMessage(e.getMessageIdLong())) {
					return;
				}
				MessageInfoContainer starredMsg = info.getMessageFromNotification(e.getMessageIdLong());
				MessageInfoContainer toDelete = info.getStarredMessages().get(starredMsg.getMessageId());
				toDelete.getMessage(BotMain.getJDA()).queue(toDeleteMessage -> {
					toDeleteMessage.delete().queue();
				});
				info.getStarredMessages().remove(starredMsg.getMessageId());
				info.getNotificationMessages().remove(e.getMessageIdLong());
				info.getIgnoredMessages().add(starredMsg.getMessageId());
				CmdStar.saveStarredMessages();
				
				m.editMessage(":star: **Star Message Notification:** Successfully removed your message with ID `" 
						+ starredMsg.getMessageId() + "` as a starred message.").queue();
			});
		}
	}
}
