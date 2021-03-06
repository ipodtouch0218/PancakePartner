package me.ipodtouch0218.pancakepartner.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ipodtouch0218.sjbotcore.SJBotCore;
import me.ipodtouch0218.sjbotcore.util.MessageContainer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class MessageUtils {

	private MessageUtils() {} //stops instance creation, static methods only.
	
	//--Mention Utils (@ping)--//
	private static final Pattern mentionUserPattern = Pattern.compile("<@\\d+>");
	private static final Pattern mentionChannelPattern = Pattern.compile("<#\\d+>");
	
	//--Mentioned Users--//
	//returns true only if whole string is a mention.
	public static boolean isUserMention(String str) {
		return mentionUserPattern.matcher(str.trim()).matches();
	}
	
	//returns true if found at least one mention
	public static boolean containsUserMention(String str) {
		return mentionUserPattern.matcher(str).find();
	}
	
	//returns user if the given string is a mention.
	public static User getMentionedUser(String str, SJBotCore core) {
		if (!isUserMention(str)) {
			//given string is not a mention, abort! jump ship!
			return null;
		}
		
		String userId = str.substring(2, str.length()-1);
		User mentionedUser = core.getShardManager().getUserById(userId);
		
		return mentionedUser;
	}
	
	//returns all users mentioned within the string, in order.
	public static User[] getMentionedUsers(String str, SJBotCore core) {
		if (!containsUserMention(str)) {
			//given string is not a mention
			return null;
		}
		
		Matcher regexMatcher = mentionUserPattern.matcher(str);
		ArrayList<User> matchedUsers = new ArrayList<>();
		while (regexMatcher.find()) {
			String mention = str.substring(regexMatcher.start(), regexMatcher.end());
			User mentionedUser = getMentionedUser(mention, core);
			matchedUsers.add(mentionedUser);
		}
		
		if (!matchedUsers.isEmpty()) {
			return matchedUsers.toArray(new User[]{});
		}
		return null;
	}
	
	//--Mentioned Channel--//
	public static boolean isChannelMention(String str) {
		return mentionChannelPattern.matcher(str.trim()).matches();
	}
	
	public static TextChannel getMentionedChannel(String str, SJBotCore core) {
		if (!isChannelMention(str)) {
			//given string is not a mention
			return null;
		}
		
		String channelId = str.substring(2, str.length()-1);
		TextChannel channel = core.getShardManager().getTextChannelById(channelId);
		
		return channel;
	}
	
	//--Format Utils--//
	public static String nameAndDiscrim(User user) {
		return user.getName() + "#" + user.getDiscriminator();
	}
	
	public static String getMessageURL(Message m) {
		String guildId = "@me";
		if (m.getGuild() != null) {
			guildId = m.getGuild().getId();
		}
		long channelId = m.getChannel().getIdLong();
		long messageId = m.getIdLong();
		
		return "https://discordapp.com/channels/" + guildId + "/" + channelId + "/" + messageId;
	}

	public static String asChannelMention(MessageChannel channel) {
		return "<#" + channel.getId() + ">";
	}
	
	//--stuff--//
	public static MessageContainer parseMessageURL(String url) {
		Matcher m = MiscUtils.PATTERN_MESSAGE_LINK.matcher(url);
		m.find();
		long guildid = -1;
		if (!m.group("guild").equalsIgnoreCase("@me")) {
			guildid = Long.parseLong(m.group("guild"));
		}
		long channelid = Long.parseLong(m.group("channel"));
		long messageid = Long.parseLong(m.group("messageid"));
		return new MessageContainer(guildid, channelid, messageid);
	}
}
