package me.ipodtouch0218.pancakepartner.commands;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
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

	private static Pattern linkPattern = Pattern.compile("(https?:\\/\\/)?(www\\.)?discordapp\\.com\\/channels\\/(?<guild>\\d+)\\/(?<channel>\\d+)\\/(?<messageid>\\d+)");
	
	//--Variables & Constructor--//
	private static File saveFile = new File("starredmsgs.txt");
	public static HashMap<Long, Long> starredMessages = new HashMap<>();
	public MessageChannel starChannel;
	
	/* TODO:
	 * - Support multiple guilds
	 *   - Get all info from a Guild Settings object
	 *   - Save all info to Guild-Specific files instead of one starredmsgs.txt
	 */
	public CmdStar(MessageChannel channel) {
		super("star", true, false, Permission.MESSAGE_MANAGE);
		starChannel = channel;
		setHelpInfo("Forcefully pins a message to #" + channel.getName() + " for later viewing. Turn on developer mode to be able to copy message IDs for this command's parameters.", "star <message id|message url>");
		loadStarredMessages();
	}

	//--//
	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, ArrayList<String> flags) {
		MessageChannel channel = msg.getChannel();
		
		if (args.size() <= 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must specify a message's ID or a message link.").queue();
			return;
		} 
		
		Matcher match = linkPattern.matcher(args.get(0));
		if (match.matches()) {
			
			Guild guild = BotMain.getJdaInstance().getGuildById(match.group("guild"));
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
				if (starredMessages.containsKey(m.getIdLong())) {
					channel.sendMessage(":pancakes: **Invalid Argument:** That message is already starred and in <#" + starChannel.getId() + ">.").queue();
					return;
				}
				
				m.addReaction("\u2B50").complete();
				channel.sendMessage(":pancakes: Successfully forcefully pinned the message from `" + MessageUtils.nameAndDiscrim(m.getAuthor()) + "` into <#" + starChannel.getId() + ">.").queue();
				sendStarredMessage(m);
			}, th -> {
				//if errors
				channel.sendMessage(":pancakes: **Invalid Argument:** I cannot access this message: There is no message at the specified link, or I can't access that message.").queue();
			});
			return;
		}
		
		channel.getMessageById(args.get(0)).queue(m -> {
			if (starredMessages.containsKey(m.getIdLong())) {
				channel.sendMessage(":pancakes: **Invalid Argument:** That message is already starred and in <#" + starChannel.getId() + ">.").queue();
				return;
			}
			
			m.addReaction("\u2B50").complete();
			channel.sendMessage(":pancakes: Successfully forcefully pinned the message from `" + MessageUtils.nameAndDiscrim(m.getAuthor()) + "` into <#" + starChannel.getId() + ">.").queue();
			sendStarredMessage(m);
		}, th -> {
			//if errors
			channel.sendMessage(":pancakes: **Invalid Argument:** There is no message with the ID `" + args.get(0) 
					+ "` in this text channel. Try using a link instead of the id?").queue();
		});
		return;
	}
	
	//--Sending starred message to channel--//
	public void sendStarredMessage(Message msg) {
		MessageEmbed embed = buildStarredMessageEmbed(msg);
		
		starChannel.sendMessage(embed).queue(m -> {
			starredMessages.put(msg.getIdLong(), m.getIdLong());
			saveStarredMessages();
		});
	}
	
	public void editStarredMessage(Message sourceMsg) {
		MessageEmbed embed = buildStarredMessageEmbed(sourceMsg);
		starChannel.getMessageById(starredMessages.get(sourceMsg.getIdLong())).queue(editmsg -> {
			editmsg.editMessage(embed).queue();
		});
	}
	
	private MessageEmbed buildStarredMessageEmbed(Message m) {
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
					embed.setThumbnail(attachment.getUrl());
					break;
				}
			}
		}
		
		return embed.build();
	}
	
	//--Loading and Saving-//
	private void saveStarredMessages() {
		try {
			if (!saveFile.exists()) { saveFile.createNewFile(); }
		    PrintWriter pw = new PrintWriter(new FileOutputStream(saveFile));
		    for (Entry<Long,Long> msgIds : starredMessages.entrySet()) {
		        pw.println(msgIds.getKey() +","+ msgIds.getValue());
		    }
		    pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadStarredMessages() {
		if (!saveFile.exists()) { return; }
		try {
			for (String id : Files.readAllLines(saveFile.toPath())) {
				try {
					String[] split = id.split(",");
					starredMessages.put(Long.parseLong(split[0]), Long.parseLong(split[1]));
				} catch (Exception e) {}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
