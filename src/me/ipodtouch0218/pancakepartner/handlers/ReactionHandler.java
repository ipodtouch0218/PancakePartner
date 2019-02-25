package me.ipodtouch0218.pancakepartner.handlers;

import me.ipodtouch0218.pancakepartner.BotMain;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;

public abstract class ReactionHandler {

	protected long ownerId = -1; //only the owner can trigger the reactions, -1 if null.
	
	public ReactionHandler() {}
	
	public abstract void handleReaction(GenericMessageReactionEvent e, boolean isOwner);
	
	public void setOwnerId(long value) { ownerId = value; }
	public long getOwnerId() { return ownerId; }
	
	
	//---//
	public void clearOwnerReactions(Message m) {
		if (ownerId <= -1) { return; }
		User owner = BotMain.getJDA().getUserById(ownerId);
		for (MessageReaction r : m.getReactions()) {
			r.removeReaction(owner).queue();
		}
	}
	//---//
	public static void clearAllReactions(Message m) {
		if (m.getChannel().getType() != ChannelType.TEXT) { return; }
		m.clearReactions().queue();
	}
	public static void setReactions(Message m, String... options) {
		if (m.getChannel().getType() != ChannelType.TEXT) { return; }
		if (!m.getReactions().isEmpty()) { 
			m.clearReactions().complete();
		}
		for (String option : options) {
			m.addReaction(option).queue();
		}
	}
	public static void setReactions(MessageChannel channel, long idLong, String... options) {
		channel.getMessageById(idLong).queue(m -> {
			setReactions(m, options);
		});
	}
	
}
