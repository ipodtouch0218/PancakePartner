package me.ipodtouch0218.pancakepartner.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.requests.RestAction;

public class MessageInfoContainer {
	
	private long messageId;
	private long channelId;
	private long guildId = -1; //can be blank in dm's
	
	public MessageInfoContainer() {}
	public MessageInfoContainer(Message msg) {
		setMessage(msg);
	}
	public MessageInfoContainer(long guildId, long channelId, long messageId) {
		this.messageId = messageId;
		this.channelId = channelId;
		this.guildId = guildId;
	}
	
	/**
	 * Overwrites the existing message contained within this container with a new Message.
	 * @param msg - Discord {@link Message} instance.
	 */
	public void setMessage(Message msg) {
		messageId = msg.getIdLong();
		channelId = msg.getChannel().getIdLong();
		if (msg.getGuild() != null) {
			guildId = msg.getGuild().getIdLong();
		}
	}
	
	public long getMessageId() { return messageId; }
	public long getChannelId() { return channelId; }
	public long getGuildId() { return guildId; }
	/**
	 * Returns the Guild this message is contained within. If there is no guild, but rather the message
	 * is present within a DM or Group PM, this method will return null.
	 * @param jda - JDA Bot instance
	 * @return Possibly-null Guild instance.
	 */
	public Guild getGuild(JDA jda) {
		if (guildId <= -1) { return null; }
		return jda.getGuildById(guildId);
	}
	/**
	 * Returns the Channel this message is contained within. If the bot does not have access to the given channel,
	 * this method will return null. Works with Guilds, DMs and Group PMs.
	 * @param jda - JDA Bot instance
	 * @return Possibly-null Channel instance.
	 */
	public MessageChannel getChannel(JDA jda) {
		if (guildId <= -1) {
			return jda.getPrivateChannelById(channelId);
		} 
		return jda.getTextChannelById(channelId);
	}
	/**
	 * Returns a {@link RestAction} to retrieve the message within this container.
	 * @param jda - JDA Bot instance.
	 * @return RestAction to retrieve the message.
	 */
	public RestAction<Message> getMessage(JDA jda) {
		return getChannel(jda).getMessageById(messageId);
	}
	
	/**
	 * Returns the direct URL to the message specified by this container. Discord Message URLs follow this format:
	 * {@link https://discordapp.com/channels/guildId/channelId/messageId/}
	 * @return String URL to the message.
	 */
	@JsonIgnore
	public String getDirectLink() {
		return "https://discordapp.com/channels/" + (guildId <= -1 ? "@me" : guildId) + "/" + channelId + "/" + messageId;
	}
	/**
	 * Returns if the message specified by this container is in a guild text channel.
	 * @return If this message is in a guild.
	 */
	@JsonIgnore
	public boolean isInGuild() { return guildId >= 0; }
	
}