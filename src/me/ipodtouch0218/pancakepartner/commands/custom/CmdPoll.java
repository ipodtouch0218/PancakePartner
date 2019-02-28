package me.ipodtouch0218.pancakepartner.commands.custom;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.commands.BotCommand;
import me.ipodtouch0218.pancakepartner.commands.CommandFlag;
import me.ipodtouch0218.pancakepartner.config.GuildSettings;
import me.ipodtouch0218.pancakepartner.handlers.MessageListener;
import me.ipodtouch0218.pancakepartner.handlers.ReactionHandler;
import me.ipodtouch0218.pancakepartner.utils.MessageInfoContainer;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;

public class CmdPoll extends BotCommand {

	private static final File POLL_SAVE_FILE = new File("polls.yml");
	private ArrayList<PollInfo> polls = new ArrayList<>();
	
	public CmdPoll() {
		super("poll", true, false, Permission.ADMINISTRATOR);
		setHelpInfo("Creates a poll using reactions for people to answer.", "poll \"<message>\" <emoji1> [emoji2] [emoji3...]");
		
		registerFlag("duration", 1);
		registerFlag("votes", 1);
		registerFlag("title", 1);
		registerFlag("channel", 1);
		registerFlag("clearresults", 0);
		
		loadPolls();
		
		//TODO: update polls timers and close them when appropriate
		new Thread(() -> {
			while (true) {
				for (PollInfo poll : polls) {
					editPollMessage(poll);
				}
				try {
					Thread.sleep(10*1000);
				} catch (InterruptedException e) {}
			}
		}).start();
	}

	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, HashMap<String,CommandFlag> flags) {
		MessageChannel channel = msg.getChannel();
		
		if (args.size() <= 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must specify a message to send! (Use quotes around the message!!)").queue();
			return;
		}
		if (args.size() < 2) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must specify at least one voting option.").queue();
			return;
		}
		
		String message = args.get(0);
		String[] options = new String[args.size()-1];
		for (int i = 1; i < args.size(); i++) {
			options[i-1] = args.get(i);
		}
		
		String title = "New Poll";
		int allowedvotes = 1;
		long expireMillis = -1;
		boolean clearresults = flags.containsKey("clearresults");
		if (flags.containsKey("title")) {
			title = flags.get("title").getParameters()[0];
		}
		if (flags.containsKey("votes")) {
			try {
				allowedvotes = Integer.parseInt(flags.get("votes").getParameters()[0]);
			} catch (NumberFormatException e) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Parameter for 'votes' flag was not a number.").queue();
				return;
			}
			if (allowedvotes <= 0 || allowedvotes > args.size()) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid vote amount (either too little < 1, or too big > possible options).").queue();
				return;
			}
		}
		if (flags.containsKey("duration")) {
			try {
				int minutes = Integer.parseInt(flags.get("duration").getParameters()[0]);
				expireMillis = System.currentTimeMillis() + (minutes * 60 * 1000);
			} catch (NumberFormatException e) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Parameter for 'duration' flag was not a number (NOTE: duration is in MINUTES).").queue();
				return;
			}
		}

		GuildSettings settings = BotMain.getGuildSettings(msg.getGuild());
		MessageChannel postChannel = BotMain.getJDA().getTextChannelById(settings.getPollChannelID());
		if (flags.containsKey("channel")) {
			postChannel = MessageUtils.getMentionedChannel(flags.get("channel").getParameters()[0]);
			if (postChannel == null) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Parameter for 'votes' flag was not a channel mention or an invalid channel.").queue();
				return;
			}
		}
		if (postChannel == null) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** Channel for polls is unset! Use `settings poll channel <#channel>` to set it!").queue();
			return;
		}
		
		createPollMessage(postChannel, msg.getAuthor(), title, message, expireMillis, allowedvotes, clearresults, options);
	}
	
	//--//
	public void createPollMessage(MessageChannel channel, User creator, String title, String message, long expireMillis, int allowedvotes, boolean clearresults, String... options) {
		channel.sendMessage(buildMessage(creator, title, message, expireMillis, allowedvotes, clearresults, null)).queue(m -> {
			PollInfo info = new PollInfo(m, creator, title, message, expireMillis, allowedvotes, clearresults);
			polls.add(info);
			savePolls();
			MessageListener.addReactionHandler(m.getIdLong(), new PollReactionHandler(info));
			ReactionHandler.setReactions(m, options);
		});
	}
	
	public void editPollMessage(PollInfo info) {
		User creator = BotMain.getJDA().getUserById(info.getCreatorId());
		
		info.getMessageInfo().getMessage(BotMain.getJDA()).queue(m -> {
			m.editMessage(buildMessage(creator, info)).queue();
		});
	}
	
	private MessageEmbed buildMessage(User creator, String title, String message, long expireTimeMillis, int allowedvotes, boolean clearresults, PollInfo info) {
		EmbedBuilder embed = new EmbedBuilder();
		
		embed.setColor(15074559);
		boolean closed = false;
		String expiresIn = "";
		if (expireTimeMillis != -1) {
			if (expireTimeMillis < System.currentTimeMillis()) {
				expiresIn = " - Poll Closed!";
				closed = true;
			} else {
				int s = (int) (expireTimeMillis - System.currentTimeMillis())/(1000);
				int m = s/60;
				int h = m/60;
				int d = h/24;
				s%=60;
				m%=60;
				h%=24;
				expiresIn = " - Expires in " + (d>0? d+"D " : "") + (h>0? h+"H " : "") + m + "M " + s + "S";
			}
		}
		embed.setTitle(title + expiresIn);
		if (closed) {
			Message m = info.getMessageInfo().getMessage(BotMain.getJDA()).complete();
			String results = "";
			for (MessageReaction r : m.getReactions()) {
				results += r.getReactionEmote().getName() + " - " + (r.getCount()-1) + "\n";
			}
			embed.addField("FINAL RESULTS:", results.trim(), false);
					
			polls.remove(info);
			MessageListener.removeReactionHandler(info.getMessageInfo().getMessageId());
			if (clearresults) {
				m.clearReactions().queue();	
			}
		}
		embed.setDescription(message);
		embed.setFooter("Created by: " + MessageUtils.nameAndDiscrim(creator), creator.getAvatarUrl());
		if (info == null) {
			embed.setTimestamp(Instant.now());
		} else {
			embed.setTimestamp(Instant.ofEpochMilli(info.getCreatedDate()));
		}
		
		return embed.build();
	}
	private MessageEmbed buildMessage(User creator, PollInfo info) {
		return buildMessage(creator, info.title, info.message, info.expireTimeMillis, info.allowedVotes, info.clearResults, info);
	}
	
	//--//
	public void savePolls() {
		try {
			BotMain.yamlMapper.writeValue(POLL_SAVE_FILE, polls);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to WRITE the poll file... malformed list or IOException??");
		}
	}
	public void loadPolls() {
		if (POLL_SAVE_FILE.exists()) {
			try {
				polls = new ArrayList<>(Arrays.asList(BotMain.yamlMapper.readValue(POLL_SAVE_FILE, PollInfo[].class)));
				for (PollInfo poll : polls) {
					MessageListener.addReactionHandler(poll.getMessageInfo().getMessageId(), new PollReactionHandler(poll));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Unable to READ the poll file... malformed list or IOException??");
			}
		}
	}
	
	//--//
	public static class PollInfo {
		
		private String title = "";
		private String message = "";
		private long creatorId = -1;
		private long expireTimeMillis = -1;
		private int allowedVotes = 1;
		private MessageInfoContainer messageInfo = null;
		private boolean clearResults = false;
		private long createdDate = -1;
		
		public PollInfo() {}
		public PollInfo(Message poll, User creator, String title, String message, long expireTime, int votes, boolean clearresults) {
			messageInfo = new MessageInfoContainer(poll);
			this.title = title;
			expireTimeMillis = expireTime;
			allowedVotes = votes;
			this.message = message;
			this.clearResults = clearresults;
			this.creatorId = creator.getIdLong();
			createdDate = System.currentTimeMillis();
		}
		
		public String getMessage() { return message; }
		public long getCreatorId() { return creatorId; }
		public long getExpireTimeMillis() { return expireTimeMillis; }
		public String getTitle() { return title; }
		public int getAllowedVotes() { return allowedVotes; }
		public boolean willClearResults() { return clearResults; }
		public MessageInfoContainer getMessageInfo() { return messageInfo; }
		public long getCreatedDate() { return createdDate; }
	}
	public static class PollReactionHandler extends ReactionHandler {

		private PollInfo info;
		public PollReactionHandler(PollInfo info) {
			this.info = info;
		}
		public void handleReaction(GenericMessageReactionEvent e, boolean add, boolean isOwner) {
			if (!add) { return; }
			info.messageInfo.getMessage(e.getJDA()).queue(m -> {
				for (MessageReaction r : m.getReactions()) {
					if (r.getReactionEmote().equals(e.getReactionEmote())) { continue; }
					r.removeReaction(e.getUser()).queue();
				}
			});
		}
	}
}
