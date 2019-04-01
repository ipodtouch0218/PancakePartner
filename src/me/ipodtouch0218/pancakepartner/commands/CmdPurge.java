package me.ipodtouch0218.pancakepartner.commands;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
import me.ipodtouch0218.sjbotcore.command.BotCommand;
import me.ipodtouch0218.sjbotcore.command.CommandFlag;
import me.ipodtouch0218.sjbotcore.command.FlagSet;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

public class CmdPurge extends BotCommand {

	private static final String regexHelp = "https://www.regexbuddy.com/regex.html";
	
	public CmdPurge() {
		super("purge", true, false, Permission.MESSAGE_MANAGE);
		setHelpInfo("Deletes previous commands matching specific criteria.", "purge <amount>");
		
		registerFlag("user", 1, "Deletes messages from a specific user.");
		registerFlag("regex", 1, String.format("Deletes messages matching a [regex](%s)", regexHelp));
		registerFlag("contains", 1, "Deletes messages containing the specified text.");
		registerFlag("channel", 1, "Purges messages from another channel.");
	}

	@Override
	public void execute(Message msg, String label, ArrayList<String> args, FlagSet flags) {
		TextChannel channel = msg.getTextChannel();
		if (args.size() <= 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must specify the amount of messages to purge!").queue();
			return;
		}
		int messages = 0;
		try {
			messages = Integer.parseInt(args.get(0));
		} catch (Exception e) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** `" + args.get(0) + "` is not considered a valid number of messages!").queue();
			return;
		}
		if (messages <= 0 || messages > 500) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** `" + args.get(0) + "` is not considered a valid number of messages! (1-500)").queue();
			return;
		}
		MessageChannel purgeChannel = channel;
		User user = null;
		String contains = null;
		Pattern regex = null;
		
		if (flags.containsFlag("channel")) {
			CommandFlag channelflag = flags.getFlag("channel").get();
			String channelString = channelflag.getParameters()[0];
			purgeChannel = MessageUtils.getMentionedChannel(channelString, BotMain.getBotCore());
			if (purgeChannel == null) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Parameter for 'channel' flag was not a channel mention or an invalid channel.").queue();
				return;
			}
		}
		if (flags.containsFlag("user")) {
			CommandFlag userflag = flags.getFlag("user").get();
			String userString = userflag.getParameters()[0];
			user = MessageUtils.getMentionedUser(userString, BotMain.getBotCore());
			if (user == null) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Parameter for 'user' flag was not a user mention or a user I can't see!").queue();
				return;
			}
		}
		if (flags.containsFlag("contains")) {
			CommandFlag containsflag = flags.getFlag("contains").get();
			contains = containsflag.getParameters()[0];
		}
		if (flags.containsFlag("regex")) {
			CommandFlag regexflag = flags.getFlag("regex").get();
			String regexstring = Pattern.quote(regexflag.getParameters()[0]);
			try {
				regex = Pattern.compile(regexstring);
			} catch (PatternSyntaxException e) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Parameter for 'regex' flag was a malformed RegEx input!").queue();
				return;
			}
		}
		
		PurgeStatus status = new PurgeStatus();
		final int finalMessages = messages;
		channel.sendMessage(String.format("<a:panload:557011383611555861> **Purge:** Starting purge on %d messages with criteria ...", finalMessages)).queue(m -> {
			
			while (!status.checkIfComplete()) {
				try { Thread.sleep(800); } catch (InterruptedException e) {}
			}
			//Status is done, update the message.
			m.editMessage(String.format(":pancakes: **Purge:** Purged ~%d messages out of %d requested.", status.failedPurged+status.successfullyPurged, finalMessages)).queue();
		});
		
		OffsetDateTime twoWeeksAgo = OffsetDateTime.ofInstant(Instant.now().minus(14, ChronoUnit.DAYS), Calendar.getInstance().getTimeZone().toZoneId());

		long lastmsg = msg.getIdLong();
		for (int j = 0; j < ((messages-1)/100)+1; j++) {
			int toPoll = Math.min(messages-(j*100), 100);
			MessageHistory history = purgeChannel.getHistoryBefore(lastmsg, toPoll).complete();
			ArrayList<Message> messagesToDelete = new ArrayList<>();
			for (Message msgToPurge : history.getRetrievedHistory()) {
				String content = msgToPurge.getContentRaw();
				if (user != null && msgToPurge.getAuthor().getIdLong() != user.getIdLong()) { continue; }
				if (contains != null && !content.contains(contains)) { continue; }
				if (regex != null && !regex.matcher(content).matches()) { continue; }
						
				status.attemptedPurged++;
				if (msgToPurge.getCreationTime().isBefore(twoWeeksAgo)) {
					msgToPurge.delete().queue(s -> {
						status.successfullyPurged++;
					}, err -> {
						status.failedPurged++;
					});
				} else {
					messagesToDelete.add(msgToPurge);
				}
				
				lastmsg = msgToPurge.getIdLong();
			}

			if (messagesToDelete.size() == 1) {
				messagesToDelete.get(0).delete().queue(s -> {
					status.successfullyPurged++;
				}, err -> {
					status.failedPurged++;
				});
			} else if (messagesToDelete.size() > 1) {
				channel.deleteMessages(messagesToDelete).queue(s -> {
					status.successfullyPurged+=messagesToDelete.size();
				}, err -> {
					status.failedPurged+=messagesToDelete.size();
					err.printStackTrace();
				});
			}
		}
		status.readyForCheck = true;
	}
	
	private class PurgeStatus {
		int successfullyPurged = 0;
		int failedPurged = 0;
		int attemptedPurged = 0;
		boolean readyForCheck;
		public boolean checkIfComplete() {
			if (!readyForCheck) { return false; }
			return ((successfullyPurged + failedPurged) >= attemptedPurged);
		}
	}
}
