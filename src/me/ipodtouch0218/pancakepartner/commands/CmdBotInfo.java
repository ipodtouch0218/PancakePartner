package me.ipodtouch0218.pancakepartner.commands;

import java.time.Instant;
import java.util.ArrayList;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
import me.ipodtouch0218.sjbotcore.command.BotCommand;
import me.ipodtouch0218.sjbotcore.command.FlagSet;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class CmdBotInfo extends BotCommand {

	public CmdBotInfo() {
		super("botinfo", true, true);
		setHelpInfo("Provides information about the current bot processes", "info");
		setAliases("stats");
	}

	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, FlagSet flags) {
		outputInfo(msg.getAuthor(), msg.getChannel(), msg.getJDA());
	}
	
	private void outputInfo(User requester, MessageChannel channel, JDA currentJDA) {
		ShardManager manager = BotMain.getBotCore().getShardManager();
		EmbedBuilder embed = new EmbedBuilder();
		
		long ramMax = Runtime.getRuntime().totalMemory() / (1024 * 1024);
		long ramUsed = ramMax - (Runtime.getRuntime().freeMemory() / (1024 * 1024));
		embed.setTitle(":pancakes: PancakePartner Info");
		embed.setColor(6881196);
		embed.addField(":chart_with_upwards_trend:  Statistics", String.format("Users: %d | Guilds: %d", manager.getUsers().size(), manager.getGuilds().size()), true);
		embed.addField(":small_blue_diamond: Shard Info", String.format("Current Shard: %d | %d total shard(s)", currentJDA.getShardInfo().getShardId()+1, currentJDA.getShardInfo().getShardTotal()), true);
		embed.addField(":pencil: Memory Usage", String.format("%dMB / %dMB", ramUsed, ramMax), true);
		embed.addField(":stopwatch: Ping", String.format("Shard: %dms | Average: %.0fms", currentJDA.getPing(), manager.getAveragePing()), true);
		embed.setFooter("Requested By: " + MessageUtils.nameAndDiscrim(requester), requester.getAvatarUrl());
		embed.setTimestamp(Instant.now());
		
		channel.sendMessage(embed.build()).queue();
	}

}
