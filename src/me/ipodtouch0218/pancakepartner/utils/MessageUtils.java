package me.ipodtouch0218.pancakepartner.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ipodtouch0218.pancakepartner.BotMain;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public class MessageUtils {

	private MessageUtils() {} 	//stops instance creation, static methods only.
	
	
	
	//--Mention Utils (@ping)--//
	private static final Pattern mentionPattern = Pattern.compile("<@\\d+>");
	
	//returns true only if whole string is a mention.
	public static boolean isMention(String str) {
		return mentionPattern.matcher(str.trim()).matches();
	}
	
	//returns true if found at least one mention
	public static boolean containsMention(String str) {
		return mentionPattern.matcher(str).find();
	}
	
	//returns user if the given string is a mention.
	public static User getMentionedUser(String str) {
		if (BotMain.getJdaInstance() == null) {
			//not yet connected to dsicord, we can't get any users.
			return null;
		}
		if (!isMention(str)) {
			//given string is not a mention, abort! jump ship!
			return null;
		}
		
		String userId = str.substring(2, str.length()-1);
		User mentionedUser = BotMain.getJdaInstance().getUserById(userId);
		
		return mentionedUser;
	}
	
	//returns all users mentioned within the string, in order.
	public static User[] getMentionedUsers(String str) {
		if (BotMain.getJdaInstance() == null) {
			//not yet connected to discord, we can't get any users
			return null;
		}
		
		Matcher regexMatcher = mentionPattern.matcher(str);
		ArrayList<User> matchedUsers = new ArrayList<>();
		while (regexMatcher.find()) {
			String mention = str.substring(regexMatcher.start(), regexMatcher.end());
			User mentionedUser = getMentionedUser(mention);
			matchedUsers.add(mentionedUser);
		}
		
		if (!matchedUsers.isEmpty()) {
			return matchedUsers.toArray(new User[]{});
		}
		return null;
	}
	
	//--Format Utils--//
	public static String nameAndDiscrim(User user) {
		return user.getName() + "#" + user.getDiscriminator();
	}
	
	public static String getMessageURL(Message m) {
		long guildId = m.getGuild().getIdLong();
		long channelId = m.getChannel().getIdLong();
		long messageId = m.getIdLong();
		
		return "https://discordapp.com/channels/" + guildId + "/" + channelId + "/" + messageId;
	}
}
