package me.ipodtouch0218.pancakepartner.listeners;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.commands.staff.CmdStar;
import me.ipodtouch0218.pancakepartner.commands.staff.CmdStar.StarredMessageInfo;
import me.ipodtouch0218.pancakepartner.config.GuildSettings;
import me.ipodtouch0218.sjbotcore.util.MessageContainer;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CustomListener extends ListenerAdapter {

	private static final String STAR_REACTION = "\u2B50";
	private static final String REMOVE_REACTION = "\u26D4";
	
	@Override
	public void onGenericMessageReaction(GenericMessageReactionEvent e) {
		if (e.getUser().isBot()) { return; }
		
		if (e.getChannelType() == ChannelType.TEXT) {	
			//reaction was added within a guild, check for stars.
			boolean handled = handleStarredReaction(e);
			if (handled) {
				//reaction was considered to be a star, return
				return;
			}
		}
	}
	
	private boolean handleStarredReaction(GenericMessageReactionEvent e) {
		if (e.getGuild() == null) { return false; }
		if (e.getChannel().getType() != ChannelType.TEXT) { return false; }
		if (!e.getReactionEmote().getName().equals(STAR_REACTION)) { return false; }
		
		StarredMessageInfo starinfo = CmdStar.getStarredMessageInfo();
		long messageId = e.getMessageIdLong();
		
		if (starinfo.isMessageIgnored(messageId)) { return false; }
		
		GuildSettings guildSettings = BotMain.getGuildSettings(e.getGuild());
		TextChannel starChannel = BotMain.getBotCore().getShardManager().getTextChannelById(guildSettings.starChannelID);
		
		if (starinfo.isMessageStarred(messageId)) {
			e.getChannel().retrieveMessageById(messageId).queue(m -> {
				CmdStar.editStarredMessage(m);
			});
		} else {
			e.getReaction().retrieveUsers().queue(u -> {
				int count = u.size() + 1;
				if (count > guildSettings.starRequiredStars) {
					e.getChannel().retrieveMessageById(messageId).queue(m -> {
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
			e.getChannel().retrieveMessageById(e.getMessageIdLong()).queue(m -> {
				StarredMessageInfo info = CmdStar.getStarredMessageInfo();
				if (!info.isNotificationMessage(e.getMessageIdLong())) {
					return;
				}
				MessageContainer starredMsg = info.getMessageFromNotification(e.getMessageIdLong());
				MessageContainer toDelete = info.getStarredMessages().get(starredMsg.getMessageId());
				toDelete.getMessage(BotMain.getBotCore().getShardManager()).queue(toDeleteMessage -> {
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
