package me.ipodtouch0218.pancakepartner.commands;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.config.GuildSettings;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
import me.ipodtouch0218.pancakepartner.utils.MiscUtils;
import me.ipodtouch0218.sjbotcore.command.BotCommand;
import me.ipodtouch0218.sjbotcore.command.FlagSet;
import me.ipodtouch0218.sjbotcore.files.YamlConfig;
import me.ipodtouch0218.sjbotcore.util.MessageContainer;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.Message.Attachment;

public class CmdStar extends BotCommand {
	
	//--Variables & Constructor--//
	private static final File saveFile = new File("starredmsgs.yml");
	private static StarredMessageInfo info;

	public CmdStar() {
		super("star", true, false, Permission.MESSAGE_MANAGE);
		setHelpInfo("Forcefully pins a message to the starred channel for later viewing. Turn on developer mode to be able to copy message IDs for this command's parameters.", "star <message id|url>");
		loadStarredMessages();
	}

	//--//
	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, FlagSet flags) {
		MessageChannel channel = msg.getChannel();
		GuildSettings guildSettings = BotMain.getGuildSettings(msg.getGuild());
		
		if (guildSettings.getStarChannelID() == -1) {
			channel.sendMessage(":pancakes: **Error:** The star channel is not set! Use 'settings star channel #<channel>' to set the channel for this guild!").queue();
			return;
		}
		TextChannel starChannel = BotMain.getBotCore().getShardManager().getTextChannelById(guildSettings.getStarChannelID());
		
		if (args.size() <= 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must specify a message's ID or a message link.").queue();
			return;
		} 
		
		Matcher match = MiscUtils.PATTERN_MESSAGE_LINK.matcher(args.get(0));
		if (match.matches()) {
			long id = Long.parseLong(match.group("guild"));
			if (id != msg.getGuild().getIdLong()) {
				channel.sendMessage(":pancakes: **Invalid Argument:** Cannot star a message from another guild! Try running the command in *that* guild instead.").queue();
				return;
			}
			Guild guild = msg.getJDA().getGuildById(id);
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
	public static void sendStarredMessage(Message originalMsg, TextChannel channel) {
		MessageEmbed embed = buildStarredMessageEmbed(originalMsg);
		
		channel.sendMessage(embed).queue(m -> {
			info.starredMessages.put(originalMsg.getIdLong(), new MessageContainer(m));
			saveStarredMessages();
		});
		sendNotificationMessage(originalMsg);
	}
	
	public static void editStarredMessage(Message sourceMsg) {
		MessageEmbed embed = buildStarredMessageEmbed(sourceMsg);
		
		info.starredMessages.get(sourceMsg.getIdLong()).getMessage(BotMain.getBotCore().getShardManager()).queue(editmsg -> {
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
			boolean attached = false;
			for (MessageEmbed embeds : m.getEmbeds()) {
				if (embeds.getImage() != null) {
					embed.setImage(embeds.getImage().getUrl());
					attached = true;
					break;
				}
			}
			if (!attached) {
				for (Attachment attachment : m.getAttachments()) {
					//TODO: fix video embeds
					if (attachment.isImage() || attachment.getUrl().matches(".*\\.(mov|mp4)$")) {
							embed.setImage(attachment.getUrl());
						break;
					}
				}
			}
		}
		
		return embed.build();
	}
	
	//--Sending notifications to user--//
	private static void sendNotificationMessage(Message msg) {
		User author = msg.getAuthor();
		try {
			msg.getAuthor().openPrivateChannel().queue(ch -> {
				ch.sendMessage(String.format(":star: **Starred Message Notification:** Your starred message (ID: %d) has been starred within %s. \nMessage Link: %s",
						msg.getIdLong(), msg.getGuild().getName(), MessageUtils.getMessageURL(msg)))
				.queue(m -> {
					m.addReaction("\u26D4").queue();
					info.notificationMessages.put(m.getIdLong(), new MessageContainer(msg));
					saveStarredMessages();
				}, e -> {
					
				});
			}, e -> {
				msg.getChannel().sendMessage(":star: **Starred Message Notification:** <@" + author.getId() + ">, your message at " + MessageUtils.getMessageURL(msg) 
						+ " was starred, however I can't open a dm with you, so I couldn't send you a notification there.").queue();
			});
		} catch (Exception e) {
			msg.getChannel().sendMessage(":star: **Starred Message Notification:** <@" + author.getId() + ">, your message at " + MessageUtils.getMessageURL(msg) 
			+ " was starred, however I can't open a dm with you, so I couldn't send you a notification there.").queue();
		}
	}
	
	//--Loading and Saving-//
	public static void saveStarredMessages() {
		info.saveConfig(saveFile);
	}
	
	private static void loadStarredMessages() {
		info = YamlConfig.loadConfig(saveFile, StarredMessageInfo.class);
	}
	
	public static StarredMessageInfo getStarredMessageInfo() { return info; }
	
	//--Info Class--//
	public static class StarredMessageInfo extends YamlConfig {
		private HashMap<Long, MessageContainer> starredMessages = new HashMap<>();
		private ArrayList<Long> ignoredMessages = new ArrayList<>();
		private HashMap<Long, MessageContainer> notificationMessages = new HashMap<>();
		
		public HashMap<Long, MessageContainer> getStarredMessages() { return starredMessages; }
		public ArrayList<Long> getIgnoredMessages() { return ignoredMessages; }
		public HashMap<Long, MessageContainer> getNotificationMessages() { return notificationMessages; }
		
		public boolean isMessageIgnored(long id) { return ignoredMessages.contains(id); }
		public boolean isMessageStarred(long id) { return starredMessages.containsKey(id); }
		public MessageContainer getMessageFromNotification(long id) { return notificationMessages.get(id); }
		public boolean isNotificationMessage(long id) { return notificationMessages.containsKey(id); }
	}
}
