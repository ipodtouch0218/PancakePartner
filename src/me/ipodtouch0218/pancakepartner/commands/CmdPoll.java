package me.ipodtouch0218.pancakepartner.commands;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.config.GuildSettings;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
import me.ipodtouch0218.sjbotcore.SJBotCore;
import me.ipodtouch0218.sjbotcore.command.BotCommand;
import me.ipodtouch0218.sjbotcore.command.FlagSet;
import me.ipodtouch0218.sjbotcore.files.YamlConfig;
import me.ipodtouch0218.sjbotcore.handler.ReactionHandler;
import me.ipodtouch0218.sjbotcore.util.MessageContainer;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.requests.RestAction;

public class CmdPoll extends BotCommand {

	private static final File POLL_SAVE_FILE = new File("polls.yml");
	private ArrayList<PollInfo> polls = new ArrayList<>();
	
	public CmdPoll() {
		super("poll", true, false, Permission.ADMINISTRATOR);
		setHelpInfo("Creates a poll using reactions for people to answer.", "poll \"<message>\" <emoji1> [emoji2] [emoji3...]");
		
		registerFlag("duration", 1);
		registerFlag("title", 1);
		registerFlag("channel", 1);
		registerFlag("clearresults", 0);
		
		loadPolls();
		
		new Thread(() -> {
			while (true) {
				if (BotMain.getBotCore() != null) {
					for (PollInfo poll : polls) {
						editPollMessage(poll, BotMain.getBotCore());
					}
				}
				try {
					Thread.sleep(10*1000);
				} catch (InterruptedException e) {}
			}
		}).start();
	}

	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, FlagSet flags) {
		MessageChannel channel = msg.getChannel();
		
		if (args.size() <= 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must specify a title and message to send! (Use quotes around the message!!)").queue();
			return;
		}
		if (args.size() < 3) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must specify at least one voting option.").queue();
			return;
		}
		
		String title = args.get(0);
		String message = args.get(1);
		String[] options = new String[args.size()-1];
		for (int i = 2; i < args.size(); i++) {
			options[i-2] = args.get(i);
		}
		
		long expireMillis = -1;
		boolean clearresults = flags.containsFlag("clearresults");
		if (flags.containsFlag("duration")) {
			try {
				int minutes = Integer.parseInt(flags.getFlag("duration").get().getParameters()[0]);
				expireMillis = System.currentTimeMillis() + (minutes * 60 * 1000);
			} catch (NumberFormatException e) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Parameter for 'duration' flag was not a number (NOTE: duration is in MINUTES).").queue();
				return;
			}
		}

		GuildSettings settings = BotMain.getGuildSettings(msg.getGuild());
		MessageChannel postChannel = BotMain.getBotCore().getShardManager().getTextChannelById(settings.getPollChannelID());
		if (flags.containsFlag("channel")) {
			postChannel = MessageUtils.getMentionedChannel(flags.getFlag("channel").get().getParameters()[0], BotMain.getBotCore());
			if (postChannel == null) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Parameter for 'channel' flag was not a channel mention or an invalid channel.").queue();
				return;
			}
		}
		if (postChannel == null) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** Channel for polls is unset! Use `settings poll channel <#channel>` to set it!").queue();
			return;
		}
		
		createPollMessage(postChannel, msg.getAuthor(), title, message, expireMillis, clearresults, options);
	}
	
	//--//
	public void createPollMessage(MessageChannel channel, User creator, String title, String message, long expireMillis, boolean clearresults, String... options) {
		channel.sendMessage(buildMessage(creator, title, message, expireMillis, clearresults, null)).queue(m -> {
			PollInfo info = new PollInfo(m, creator, title, message, expireMillis, clearresults);
			polls.add(info);
			savePolls();
			BotMain.getBotCore().getCommandHandler().addReactionHandler(m.getIdLong(), new PollReactionHandler(info));
			ReactionHandler.setReactions(m, options);
		});
	}
	
	public void editPollMessage(PollInfo info, SJBotCore core) {
		User creator = core.getShardManager().getUserById(info.getCreatorId());
		
		RestAction<Message> promise = info.getMessageInfo().getMessage(core.getShardManager());
		if (promise == null) { return; }
		promise.queue(m -> {
			m.editMessage(buildMessage(creator, info)).queue();
		}, e -> {
			//unable to get the message that the poll is in
			polls.remove(info);
			savePolls();
		});
	}
	
	private MessageEmbed buildMessage(User creator, String title, String message, long expireTimeMillis, boolean clearresults, PollInfo info) {
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
			Message m = info.getMessageInfo().getMessage(BotMain.getBotCore().getShardManager()).complete();
			String results = "";
			for (MessageReaction r : m.getReactions()) {
				results += r.getReactionEmote().getName() + " - " + (r.getCount()-1) + "\n";
			}
			embed.addField("FINAL RESULTS:", results.trim(), false);
					
			polls.remove(info);
			savePolls();
			BotMain.getBotCore().getCommandHandler().removeReactionHandler(info.getMessageInfo().getMessageId());
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
		return buildMessage(creator, info.title, info.message, info.expireTimeMillis, info.clearResults, info);
	}
	
	//--//
	public void savePolls() {
		try {
			YamlConfig.mapper.writeValue(POLL_SAVE_FILE, polls);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to WRITE the poll file... malformed list or IOException??");
		}
	}
	public void loadPolls() {
		if (POLL_SAVE_FILE.exists()) {
			try {
				polls = new ArrayList<>(Arrays.asList(YamlConfig.mapper.readValue(POLL_SAVE_FILE, PollInfo[].class)));
				for (PollInfo poll : polls) {
					BotMain.getBotCore().getCommandHandler().addReactionHandler(poll.getMessageInfo().getMessageId(), new PollReactionHandler(poll));
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
		private MessageContainer messageInfo = null;
		private boolean clearResults = false;
		private long createdDate = -1;
		
		public PollInfo() {}
		public PollInfo(Message poll, User creator, String title, String message, long expireTime, boolean clearresults) {
			messageInfo = new MessageContainer(poll);
			this.title = title;
			expireTimeMillis = expireTime;
			this.message = message;
			this.clearResults = clearresults;
			this.creatorId = creator.getIdLong();
			createdDate = System.currentTimeMillis();
		}
		
		public String getMessage() { return message; }
		public long getCreatorId() { return creatorId; }
		public long getExpireTimeMillis() { return expireTimeMillis; }
		public String getTitle() { return title; }
		public boolean willClearResults() { return clearResults; }
		public MessageContainer getMessageInfo() { return messageInfo; }
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