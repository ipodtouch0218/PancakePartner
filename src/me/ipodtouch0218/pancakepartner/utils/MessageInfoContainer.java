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
		messageId = msg.getIdLong();
		channelId = msg.getChannel().getIdLong();
		if (msg.getGuild() != null) {
			guildId = msg.getGuild().getIdLong();
		}
	}
	public MessageInfoContainer(long guildId, long channelId, long messageId) {
		this.messageId = messageId;
		this.channelId = channelId;
		this.guildId = guildId;
	}
	
	public long getMessageId() { return messageId; }
	public long getChannelId() { return channelId; }
	public long getGuildId() { return guildId; }
	public Guild getGuild(JDA jda) {
		if (guildId <= -1) { return null; }
		return jda.getGuildById(guildId);
	}
	public MessageChannel getChannel(JDA jda) {
		if (guildId <= -1) {
			return jda.getPrivateChannelById(channelId);
		} 
		return jda.getTextChannelById(channelId);
	}
	public RestAction<Message> getMessage(JDA jda) {
		return getChannel(jda).getMessageById(messageId);
	}
	
	@JsonIgnore
	public String getDirectLink() {
		return "https://discordapp.com/channels/" + (guildId <= -1 ? "@me" : guildId) + "/" + channelId + "/" + messageId;
	}
	@JsonIgnore
	public boolean isInGuild() { return guildId >= 0; }
	
}