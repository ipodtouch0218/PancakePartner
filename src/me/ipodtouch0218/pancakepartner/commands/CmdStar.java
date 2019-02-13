package me.ipodtouch0218.pancakepartner.commands;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.config.GuildSettings;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
import me.ipodtouch0218.pancakepartner.utils.MiscUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.TextChannel;

public class CmdStar extends BotCommand {
	
	//--Variables & Constructor--//
	private static final File saveFile = new File("starredmsgs.yml");
	private static StarredMessageInfo info;
	
	/* TODO:
	 *   - Support multiple guilds
	 *   - Get all info from a Guild Settings object
	 *   - Save all info to Guild-Specific files instead of one starredmsgs.txt
	 */
	public CmdStar() {
		super("star", true, false, Permission.MESSAGE_MANAGE);
		setHelpInfo("Forcefully pins a message to the starred channel for later viewing. Turn on developer mode to be able to copy message IDs for this command's parameters.", "star <message id|url>");
		loadStarredMessages();
	}

	//--//
	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, ArrayList<String> flags) {
		MessageChannel channel = msg.getChannel();
		GuildSettings guildSettings = BotMain.getGuildSettings(msg.getGuild());
		TextChannel starChannel = BotMain.getJDA().getTextChannelById(guildSettings.getStarChannelID());
		
		if (args.size() <= 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must specify a message's ID or a message link.").queue();
			return;
		} 
		
		Matcher match = MiscUtils.PATTERN_MESSAGE_LINK.matcher(args.get(0));
		if (match.matches()) {
			
			Guild guild = BotMain.getJDA().getGuildById(match.group("guild"));
			if (guild == null) {
				channel.sendMessage(":pancakes: **Invalid Argument:** I cannot access this message: Not in the same guild as the message.").queue();
				return;
			}
			TextChannel starMsgChannel = guild.getTextChannelById(match.group("channel"));
			if (starMsgChannel == null) {
				channel.sendMessage(":pancakes: **Invalid Argument:** I cannot access this message: Cannot access/view channel with the message.").queue();
				return;
			}
			
			starMsgChannel.getMessageById(match.group("messageid")).queue(m -> {
				if (info.starredMessages.containsKey(m.getIdLong())) {
					channel.sendMessage(":pancakes: **Invalid Argument:** That message is already starred and in <#" + starChannel.getId() + ">.").queue();
					return;
				} 
				if (info.ignoredMessages.contains(m.getIdLong())) {
					channel.sendMessage(":pancakes: **Invalid Argument:** That message cannot be pinned, as it has been removed by the message owner.").queue();
					return;
				}
				
				m.addReaction("\u2B50").complete();
				channel.sendMessage(":pancakes: Successfully forcefully pinned the message from `" + MessageUtils.nameAndDiscrim(m.getAuthor()) + "` into <#" + starChannel.getId() + ">.").queue();
				sendStarredMessage(m, starChannel);
			}, th -> {
				//if errors
				channel.sendMessage(":pancakes: **Invalid Argument:** I cannot access this message: There is no message at the specified link, or I can't access that message.").queue();
			});
			return;
		}
		
		channel.getMessageById(args.get(0)).queue(m -> {
			if (info.starredMessages.containsKey(m.getIdLong())) {
				channel.sendMessage(":pancakes: **Invalid Argument:** That message is already starred and in <#" + starChannel.getId() + ">.").queue();
				return;
			}
			if (info.ignoredMessages.contains(m.getIdLong())) {
				channel.sendMessage(":pancakes: **Invalid Argument:** That message cannot be pinned, as it has been removed by the message owner.").queue();
				return;
			}
			
			m.addReaction("\u2B50").complete();
			channel.sendMessage(":pancakes: Successfully forcefully pinned the message from `" + MessageUtils.nameAndDiscrim(m.getAuthor()) + "` into <#" + starChannel.getId() + ">.").queue();
			sendStarredMessage(m, starChannel);
		}, th -> {
			//if errors
			channel.sendMessage(":pancakes: **Invalid Argument:** There is no message with the ID `" + args.get(0) 
					+ "` in this text channel. Try using a link instead of the id?").queue();
		});
		return;
	}
	
	//--Sending starred message to channel--//
	public static void sendStarredMessage(Message msg, TextChannel channel) {
		MessageEmbed embed = buildStarredMessageEmbed(msg);
		
		channel.sendMessage(embed).queue(m -> {
			info.starredMessages.put(msg.getIdLong(), m.getIdLong());
			saveStarredMessages();
		});
		sendNotificationMessage(msg);
	}
	
	public static void editStarredMessage(Message sourceMsg, TextChannel channel) {
		MessageEmbed embed = buildStarredMessageEmbed(sourceMsg);
		channel.getMessageById(
				info.
				starredMessages.get(
						sourceMsg
						.getIdLong())).queue(editmsg -> {
			editmsg.editMessage(embed).queue();
			saveStarredMessages();
		});
	}
	
	private static MessageEmbed buildStarredMessageEmbed(Message m) {
		int starReactions = 1;
		for (MessageReaction r : m.getReactions()) {
			if (r.getReactionEmote().getName().equals("\u2B50")) {
				starReactions = r.getCount();
				break;
			}
		}
	
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("Starred Message - :star: " + starReactions);
		embed.setColor(Color.ORANGE);
		embed.setDescription(m.getContentDisplay());
		embed.setFooter(MessageUtils.nameAndDiscrim(m.getAuthor()), m.getAuthor().getAvatarUrl());
		embed.addField("\u200E", "[Direct Link](" + MessageUtils.getMessageURL(m) + ")", false);
		embed.setTimestamp(m.getCreationTime());
		if (!m.getAttachments().isEmpty()) {
			for (Attachment attachment : m.getAttachments()) {
				if (attachment.isImage()) {
					if (m.getContentDisplay().equals("") || m.getContentDisplay() == null) {
						embed.setImage(attachment.getUrl());
					} else {
						embed.setThumbnail(attachment.getUrl());
					}
					break;
				}
			}
		}
		
		return embed.build();
	}
	
	//--Sending notifications to user--//
	private static void sendNotificationMessage(Message msg) {
		msg.getAuthor().openPrivateChannel().queue(ch -> {
			ch.sendMessage(":star: **Starred Message Notification:** Your starred message (ID: " + msg.getId() + ") has been starred within \"" + msg.getGuild().getName() 
					+ "\". ```" + msg.getContentDisplay() + "``` Message Link: " + MessageUtils.getMessageURL(msg) + "\nIf you do not want this message to be starred," 
					+ "click the :no_entry_sign: reaction under this mesasge to remove it.")
			.queue(m -> {
				m.addReaction(new String(Character.toChars(0x1F6AB))).queue();
			});
		});
	}
	
	//--Loading and Saving-//
	public static void saveStarredMessages() {
		try {
			BotMain.yamlMapper.writeValue(saveFile, info);
		} catch (IOException e) {
			System.out.println("Unable to save starred messages to file!");
			e.printStackTrace();
		}
	}
	
	private static void loadStarredMessages() {
		if (saveFile.exists()) {
			try {
				info = BotMain.yamlMapper.readValue(saveFile, StarredMessageInfo.class);
			} catch (IOException e) {
				//TRY LOADING OLD-FORMAT SAVED FILE
				e.printStackTrace();
				info = new StarredMessageInfo();
			}
		} else {
			info = new StarredMessageInfo();
		}
	}
	
	public static StarredMessageInfo getStarredMessageInfo() { return info; }
	
	//--Info Class--//
	public static class StarredMessageInfo {
		private HashMap<Long, Long> starredMessages = new HashMap<>();
		private ArrayList<Long> ignoredMessages = new ArrayList<>();
		
		public HashMap<Long, Long> getStarredMessages() { return starredMessages; }
		public ArrayList<Long> getIgnoredMessages() { return ignoredMessages; }
		
		public boolean isMessageIgnored(long id) { return ignoredMessages.contains(id); }
		public boolean isMessageStarred(long id) { return starredMessages.containsKey(id); }
	}
	
}
