package me.ipodtouch0218.pancakepartner.handlers;

import java.util.HashMap;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.commands.custom.CmdStar;
import me.ipodtouch0218.pancakepartner.commands.custom.CmdStar.StarredMessageInfo;
import me.ipodtouch0218.pancakepartner.config.GuildSettings;
import me.ipodtouch0218.pancakepartner.utils.MessageInfoContainer;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
	
	private static final String STAR_REACTION = "\u2B50";
	private static final String REMOVE_REACTION = new String(Character.toChars(0x1F6AB));
	
	private static HashMap<Long, ReactionHandler> reactionHandlers = new HashMap<>();
	
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
	public void onGenericMessageReaction(GenericMessageReactionEvent e) {
		if (e.getUser().getIdLong() == BotMain.getJDA().getSelfUser().getIdLong()) { return; }
		
		if (e.getChannelType() == ChannelType.TEXT) {	
			//reaction was added within a guild, check for stars.
			boolean handled = handleStarredReaction(e);
			if (handled) {
				//reaction was considered to be a star, return
				return;
			}
		}
		
		long messageId = e.getMessageIdLong();
		if (reactionHandlers.containsKey(messageId)) {
			//this message has a reactionhandler for it. time to check.
			ReactionHandler handler = reactionHandlers.get(messageId);
			handleReaction(handler, e);
		}
	}
	
	private void handleReaction(ReactionHandler handler, GenericMessageReactionEvent e) {
		boolean isOwner = false;
		if (handler.getOwnerId() > -1) {
			isOwner = (e.getUser().getIdLong() == handler.getOwnerId());
		}
		handler.handleReaction(e, isOwner);
	}
	
	private boolean handleStarredReaction(GenericMessageReactionEvent e) {
		if (e.getGuild() == null) { return false; }
		if (e.getChannel().getType() != ChannelType.TEXT) { return false; }
		if (!e.getReactionEmote().getName().equals(STAR_REACTION)) { return false; }
		
		StarredMessageInfo starinfo = CmdStar.getStarredMessageInfo();
		long messageId = e.getMessageIdLong();
		
		if (starinfo.isMessageIgnored(messageId)) { return false; }
		
		GuildSettings guildSettings = BotMain.getGuildSettings(e.getGuild());
		TextChannel starChannel = BotMain.getJDA().getTextChannelById(guildSettings.getStarChannelID());
		
		if (starinfo.isMessageStarred(messageId)) {
			e.getChannel().getMessageById(messageId).queue(m -> {
				CmdStar.editStarredMessage(m);
			});
		} else {
			e.getReaction().getUsers().queue(u -> {
				int count = u.size() + 1;
				if (count > guildSettings.getStarRequiredStars()) {
					e.getChannel().getMessageById(messageId).queue(m -> {
						CmdStar.sendStarredMessage(m, starChannel);
					});
				}
			});
		}
		return true;
	}
	
	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent e) {
		StarredMessageInfo info = CmdStar.getStarredMessageInfo();
		if (info.getStarredMessages().containsKey(e.getMessageIdLong())) { 
			//only if the starred message embed posted in the channel was deleted
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
	
	//---//
	public static void addReactionHandler(long messageid, ReactionHandler handler) {
		reactionHandlers.put(messageid, handler);
	}
}
