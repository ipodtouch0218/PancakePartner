package me.ipodtouch0218.pancakepartner.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import me.ipodtouch0218.pancakepartner.utils.MiscUtils;
import me.ipodtouch0218.sjbotcore.command.BotCommand;
import me.ipodtouch0218.sjbotcore.command.FlagSet;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CmdRoll extends BotCommand {

	private static final Random RAND = new Random();
	
	public CmdRoll() {
		super("roll", true, true);
		setHelpInfo("Rolls a virtual dice or chooses an option from a list. Flags: -noduplicates", "roll <#|[item1,item2]> [# of rolls]");
		setAliases("random");
		
		registerFlag("noduplicates", 0);
	}

	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, FlagSet flags) {
		MessageChannel channel = msg.getChannel();
		if (args.size() <= 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must specify either a list or dice to roll!").queue();
			return;
		}
		
		int rollCount = 1;
		boolean allowDuplicates = !flags.containsFlag("noduplicates");
		
		if (args.size() >= 2) {
			try {
				rollCount = Integer.parseInt(args.get(1));
			} catch (Exception e) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** " + args.get(1) + " is not a valid number of roll attempts.").queue();
				return;
			}
			if (rollCount <= 0) {
				//0 or negative roll count
				channel.sendMessage(":pancakes: **Invalid Arguments:** I cannot roll a die " + rollCount + " times... it doesn't make sense!").queue();
				return;
			}
		}
		
		String rollParams = args.get(0);
		
		String results = "";
		String error = "";
		int rollsTaken = 0;
		
		ArrayList<Object> randElements = new ArrayList<>();
		
		if (rollParams.matches("d?\\d+")) {
			String cleansedParams = rollParams.replaceAll("[^\\d]", "");
			int sides = 0;
			try {
				sides = Integer.parseInt(cleansedParams);
			} catch (Exception e) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Unable to parse parameters. (Integer Overflow)").queue();
				return;
			}
			if (sides <= 0) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Too few sides. (Lower Bound is 1)").queue();
				return;
			}
			
			if (allowDuplicates) {
				
				for (int i = 0; i < rollCount; i++) {
					int nextElement = RAND.nextInt(sides);
					if ((results + nextElement).length() > 1750) {
						error = "*(Warning: Ran out of space to place results. " + (rollCount - rollsTaken) + " roll(s) were not performed.)*";
						break;
					}
					if (!results.equals("")) { results += ","; }
					results += nextElement;
					rollsTaken++;
				}
				
			} else {
				for (int numb : MiscUtils.createArrayRange(1,sides+1)) {
					randElements.add(numb);
				}
			}
		} else if (rollParams.matches("\\[(.+,?)*\\]")) {
			String cleansedParams = rollParams.substring(1, rollParams.length()-1);
			String[] splitParams = cleansedParams.split(",");
			
			randElements.addAll(Arrays.asList(splitParams));
		} else {
			channel.sendMessage(":pancakes: **Invalid Arguments:** Parameters don't match the proper format of either `d#` or `[1,2,3]`").queue();
			return;
		}
		
		if (!randElements.isEmpty()) {
			for (int i = 0; i < rollCount; i++) {
				if (randElements.isEmpty()) {
					error = "*(Warning: Ran out of possible results. " + (rollCount - rollsTaken) + " roll(s) were not performed.)*"; 
					break;
				}
				int randIndex = RAND.nextInt(randElements.size());
				Object nextElement = randElements.get(randIndex);
				
				if ((results + nextElement).length() > 1750) {
					error = "*(Warning: Ran out of space to place results. " + (rollCount - rollsTaken) + " roll(s) were not performed.)*";
					break;
				}
				if (!results.equals("")) { results += ","; }
				rollsTaken++;
				results += (nextElement);
				
				if (!allowDuplicates) {
					randElements.remove(randIndex);
				}
			}
		}
		
		channel.sendMessage(":pancakes: **Roll:** Rolling a die with parameters `" + rollParams + "` " + rollCount + " time" + (rollCount > 1 ? "s" : "") 
				+ (allowDuplicates ? " with duplicates allowed" : " without duplicates") + "...\n```RESULTS: " + results + "```" + error).queue();
	}
}
