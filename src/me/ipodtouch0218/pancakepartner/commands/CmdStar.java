package me.ipodtouch0218.pancakepartner.commands;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageReaction;

public class CmdStar extends BotCommand {

	private static Pattern linkPattern = Pattern.compile("(https?:\\/\\/)?discordapp\\.com\\/channels\\/(?<guild>\\d+)\\/(?<channel>\\d+)\\/(?<messageid>\\d+)");
	private static File saveFile = new File("starredmsgs.txt");
	
	public static HashMap<Long, Long> starredMessages = new HashMap<>();
	
	public MessageChannel starChannel;
	
	public CmdStar(MessageChannel channel) {
		super("star", true, false, Permission.MESSAGE_MANAGE);
		starChannel = channel;
		setHelpInfo("Forcefully pins a message to #" + channel.getName() + " for later viewing.", "star <message id|message url>");
		loadStarredMessages();
	}

	@Override
	public void execute(Message msg, String[] args) {
		MessageChannel channel = msg.getChannel();
		
		if (args.length <= 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must specify a message's ID.").queue();
			return;
		} 
		
		Matcher match = linkPattern.matcher(args[0]);
		if (match.matches()) {
			BotMain.getJdaInstance().getGuildById(match.group("guild")).getTextChannelById(match.group("channel")).getMessageById(match.group("messageid")).queue(m -> {
				if (m == null) {
					channel.sendMessage(":pancakes: **Invalid Argument:** There is no message at the specified link, or I can't access that message.").queue();
					return;
				}
				if (starredMessages.containsKey(m.getIdLong())) {
					channel.sendMessage(":pancakes: **Invalid Argument:** That message is already starred and in <#" + starChannel.getId() + ">.").queue();
					return;
				}
				
				m.addReaction("\u2B50").complete();
				sendStarredMessage(m);
			});
			return;
		}
		
		channel.getMessageById(args[0]).queue(m -> {
			if (m == null) {	//no message with that ID in the channel
				channel.sendMessage(":pancakes: **Invalid Argument:** There is no message with the ID `" + args[0] 
						+ "` in this text channel. (This command must be ran in the same channel as the message.)").queue();
				return;
			}
			if (starredMessages.containsKey(m.getIdLong())) {
				channel.sendMessage(":pancakes: **Invalid Argument:** That message is already starred and in <#" + starChannel.getId() + ">.").queue();
				return;
			}
			
			m.addReaction("\u2B50").complete();
			sendStarredMessage(m);
		});
		return;
	}
	
	public void sendStarredMessage(Message msg) {
		MessageEmbed embed = buildEmbed(msg);
		
		starChannel.sendMessage(embed).queue(m -> {
			starredMessages.put(msg.getIdLong(), m.getIdLong());
			saveStarredMessages();
		});
	}
	
	public void editStarredMessage(long id) {
		starChannel.getMessageById(id).queue(m -> {
			MessageEmbed embed = buildEmbed(m);
			
			starChannel.getMessageById(starredMessages.get(m.getIdLong())).queue(editmsg -> {
				editmsg.editMessage(embed).queue();
			});
		});
	}
	
	private MessageEmbed buildEmbed(Message m) {
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
		embed.setFooter("-" + MessageUtils.nameAndDiscrim(m.getAuthor()), m.getAuthor().getAvatarUrl());
		embed.setTimestamp(m.getCreationTime());
		if (!m.getAttachments().isEmpty()) {
			for (Attachment attachment : m.getAttachments()) {
				if (attachment.isImage()) {
					embed.setImage(attachment.getUrl());
					break;
				}
			}
		}
		
		return embed.build();
	}
	
	//---//
	
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
