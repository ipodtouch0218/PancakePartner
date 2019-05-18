package me.ipodtouch0218.pancakepartner.commands.info;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.utils.MessageUtils;
import me.ipodtouch0218.pancakepartner.utils.MiscUtils;
import me.ipodtouch0218.sjbotcore.command.BotCommand;
import me.ipodtouch0218.sjbotcore.command.FlagSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;

public class CmdBotInfo extends BotCommand {

	private static final String inviteLink = "https://discordapp.com/oauth2/authorize?client_id=532561823988318219&scope=bot";
	private static final String githubLink = "https://github.com/ipodtouch0218/PancakePartner/";
	
	public CmdBotInfo() {
		super("botinfo", true, true);
		setHelpInfo("Provides information about the current bot processes", "info");
		setAliases("stats", "info", "uptime");
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
		embed.setColor(1752220);
		embed.addField(":chart_with_upwards_trend:  Statistics", String.format("Users: %d | Guilds: %d", manager.getUsers().size(), manager.getGuilds().size()), true);
		embed.addField(":small_blue_diamond: Shard Info", String.format("Shard: %d out of %d shard(s)", currentJDA.getShardInfo().getShardId()+1, currentJDA.getShardInfo().getShardTotal()), true);
		embed.addField(":pencil: Memory Usage", String.format("%dMB / %dMB", ramUsed, ramMax), true);
		embed.addField(":stopwatch: Ping", String.format("Shard: %dms | Avg: %.0fms", currentJDA.getGatewayPing(), manager.getAverageGatewayPing()), true);
		{
			long elapsed = System.currentTimeMillis() - BotMain.getStartupTime();
			String elapsedStr = MiscUtils.timeElapsed(TimeUnit.MILLISECONDS, elapsed, TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS);
			embed.addField(":timer: Uptime", elapsedStr, true);
		}
		embed.addField(":link: Links", String.format("[Source Code (Github)](%s)\n[Invite Link](%s)", githubLink, inviteLink), true);
		
		embed.setFooter("Requested By: " + MessageUtils.nameAndDiscrim(requester), requester.getAvatarUrl());
		embed.setTimestamp(Instant.now());
		
		channel.sendMessage(embed.build()).queue();
	}

}
