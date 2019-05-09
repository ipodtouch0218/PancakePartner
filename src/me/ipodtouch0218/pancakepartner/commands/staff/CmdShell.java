package me.ipodtouch0218.pancakepartner.commands.staff;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import jdk.jshell.JShell;
import me.ipodtouch0218.sjbotcore.command.BotCommand;
import me.ipodtouch0218.sjbotcore.command.FlagSet;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CmdShell extends BotCommand {
	
	private static final long ACCOUNT_ID = 233360087979130882L;
	
	private JShell shell;
	
	public CmdShell() {
		super("shell", true, true);
		setHelpInfo("Runs Java code through JShell", "shell [code]");
		initShell();
	}
	
	private void initShell() {
		shell = JShell.create();
		
		//TODO: default imports
	}
	

	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, FlagSet flags) {
		MessageChannel channel = msg.getChannel();
		if (msg.getAuthor().getIdLong() != ACCOUNT_ID) {
			channel.sendMessage(":pancakes: **Permissions Error:** Only the bot host can run this command, for security reasons.").queue();
			return;
		}
		if (args.isEmpty()) {
			//TODO send state of shell
			
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must enter code to run!").queue();
			return;
		}
		
		String command = Arrays.stream(msg.getContentRaw().split(" ")).skip(1).collect(Collectors.joining(" "));
		runCommand(msg, command);
	}

	private void runCommand(Message msg, String command) {
		//TODO: figure out how to send message instance to the shell
		
		shell.eval(command).stream().forEach(s -> {
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("JShell Input");
			embed.setColor(Color.CYAN);
			embed.addField("Input", "```java\n" + command + "```", false);
			if (s.exception() != null) {
				StringWriter writer = new StringWriter();
				s.exception().printStackTrace(new PrintWriter(writer));
				embed.addField("Exception", "```" + writer.toString() + "```", false);
			} else {
				embed.addField("Return Value", "```java\n" + s.value() + "```", false);
			}
			if (shell.diagnostics(s.snippet()).count() > 0) {
				String output = shell.diagnostics(s.snippet()).map(d -> d.getMessage(Locale.ENGLISH)).collect(Collectors.joining("\n"));
				embed.addField("Diagnostics", "```" + output + "```", false);
			}
			
			msg.getChannel().sendMessage(embed.build()).queue();
		});	
	}

}
