package me.ipodtouch0218.pancakepartner.commands;

import java.util.ArrayList;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.config.GuildSettings;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class CmdSettings extends BotCommand {
	
	public CmdSettings() {
		super("settings", true, false, Permission.MANAGE_SERVER);
		setHelpInfo("Modifies guild-specific settings for the bot", "settings <field> <params>");
	}

	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, ArrayList<String> flags) {
		MessageChannel channel = msg.getChannel();
		GuildSettings settings = BotMain.getGuildSettings(msg.getGuild());
		
		if (args.size() <= 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments** You must specify a setting to view or modify!").queue();
			return;
		}
		switch (args.get(0)) {
		case "admin": {
			if (args.size() >= 2) {
				if (args.size() < 3) {
					channel.sendMessage(":pancakes: **Invalid Arguments** You must specify a user to add or remove").queue();
				}
				switch (args.get(1)) {
				case "add": {
					if (MessageUtils.isUserMention(args.get(2))) {
						User user = MessageUtils.getMentionedUser(args.get(2));
						if (settings.isBotAdmin(user.getIdLong())) {
							channel.sendMessage(":pancakes: This user is already a bot admin!").queue();
							return;
						}
						settings.addBotAdmin(user.getIdLong());
						channel.sendMessage(":pancakes: Added " + MessageUtils.nameAndDiscrim(user) + " as a bot admin!").queue();
						settings.save(msg.getGuild().getIdLong());
					} else {
						channel.sendMessage(":pancakes: **Invalid Arguments** You must mention a user as the next parameter!").queue();
					}
					return;
				}
				case "remove": {
					if (MessageUtils.isUserMention(args.get(2))) {
						User user = MessageUtils.getMentionedUser(args.get(2));
						if (!settings.isBotAdmin(user.getIdLong())) {
							channel.sendMessage(":pancakes: This user not already a bot admin!").queue();
							return;
						}
						settings.removeBotAdmin(user.getIdLong());
						channel.sendMessage(":pancakes: " + MessageUtils.nameAndDiscrim(user) + " is no longer a bot admin!").queue();
						settings.save(msg.getGuild().getIdLong());
					} else {
						channel.sendMessage(":pancakes: **Invalid Arguments** You must mention a user as the next parameter!").queue();
					}
					return;
				}
				default: {
					channel.sendMessage(":pancakes: **Invalid Arguments** Unknown function `" + args.get(1) + "`, try 'add' or 'remove'.").queue();
					return;
				}
				}
			}
			String list = "";
			for (long id : settings.getBotAdmins()) {
				list += (MessageUtils.nameAndDiscrim(BotMain.getJDA().getUserById(id)) + "\n");
			}
		
			channel.sendMessage(":pancakes: List of all bot-admins in this guild: ```" + list + "```To add or remove users, use 'add' and 'remove' as command parameters.").queue();
			return;
		}
		case "prefix": {
			if (args.size() > 1) {
				StringBuilder newPrefix = new StringBuilder();
				for (int i = 1; i < args.size(); i++) {
					newPrefix.append(args.get(i)).append(" ");
				}
				String p = newPrefix.toString().trim();
				if (p.equals("")) {
					channel.sendMessage(":pancakes: Cannot set the prefix to be blank!").queue();
					return;
				}
				settings.setCommandPrefix(p);
				channel.sendMessage(":pancakes: Set `" + p + "` to be the new command prefix!").queue();
				settings.save(msg.getGuild().getIdLong());
				return;
			}
			channel.sendMessage(":pancakes: `" + settings.getCommandPrefix() + "` is the current command prefix.").queue();
			return;
		}
		case "star": {
			if (args.size() >= 2) {
				if (args.size() < 3) {
					channel.sendMessage(":pancakes: **Invalid Arguments** You must specify a user to add or remove").queue();
				}
				switch (args.get(1)) {
				case "channel": {
					if (MessageUtils.isChannelMention(args.get(2))) {
						TextChannel newchannel = MessageUtils.getMentionedChannel(args.get(2));
						
						channel.sendMessage(":pancakes: Set " + MessageUtils.asChannelMention(newchannel) + " to be the starred message channel!").queue();
						settings.setStarChannelID(newchannel.getIdLong());
						settings.save(msg.getGuild().getIdLong());
					} else {
						channel.sendMessage(":pancakes: **Invalid Arguments** You must mention a text channel as the next parameter!").queue();
					}
					return;
				}
				case "required": {
					try {
						int required = Integer.parseInt(args.get(2));
						settings.setStarRequiredStars(required);
						channel.sendMessage(":pancakes: Messages now require " + required + " star reactions before they will be starred. (NOTE: This will not remove existing starred messages.").queue();
						settings.save(msg.getGuild().getIdLong());
					} catch (Exception e) {
						channel.sendMessage(":pancakes: **Invalid Arguments:** `" + args.get(2) + "` is not a valid number!").queue();
					}
					return;
				}
				default: {
					channel.sendMessage(":pancakes: **Invalid Arguments** Unknown function `" + args.get(1) + "`, try 'add' or 'remove'.").queue();
					return;
				}
				}
			}
			String list = "";
			for (long id : settings.getBotAdmins()) {
				list += (MessageUtils.nameAndDiscrim(BotMain.getJDA().getUserById(id)) + "\n");
			}
		
			channel.sendMessage(":pancakes: List of all bot-admins in this guild: ```" + list + "```To add or remove users, use 'add' and 'remove' as command parameters.").queue();
			return;
		}
		}
	}
	

}
