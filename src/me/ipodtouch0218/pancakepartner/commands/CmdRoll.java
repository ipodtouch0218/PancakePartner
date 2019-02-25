package me.ipodtouch0218.pancakepartner.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import me.ipodtouch0218.pancakepartner.utils.MiscUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CmdRoll extends BotCommand {

	private static final Random RAND = new Random();
	
	public CmdRoll() {
		super("roll", true, true);
		setHelpInfo("Rolls a virtual dice or chooses an option from a list. Flags: -noduplicates", "roll <#|[item1,item2]> [# of rolls]");
		setAliases("random");
	}

	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, ArrayList<String> flags) {
		MessageChannel channel = msg.getChannel();
		if (args.size() <= 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must specify either a list or dice to roll!").queue();
			return;
		}
		
		int rollCount = 1;
		boolean allowDuplicates = !flags.contains("-noduplicates");
		
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
		
		if (rollParams.matches("d?\\d+")) {
			//first arg matches d#
			String cleansedParams = rollParams.replaceAll("[^\\d]", "");
			//remove letter(s) from the beginning
			int sides = Integer.parseInt(cleansedParams);
			
			if (sides <= 0) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** How do you even roll a die with 0 sides?").queue();
				return;
			}
			
			if (allowDuplicates) {
				for (int i = 0 ; i < rollCount; i++) {
					if (results.length() > 1900) {
						error = "*(Warning: Ran out of space to place results. " + (rollCount - rollsTaken) + " roll(s) were not performed.)*";
						break;
					}
					if (!results.equals("")) { results += ","; }
					rollsTaken++;
					results += (RAND.nextInt(sides)+1);
				}
			} else {
				ArrayList<Integer> randElements = new ArrayList<>();
				for (int range : MiscUtils.createArrayRange(0, sides)) {
					randElements.add(range);
				}
				for (int i = 0; i < rollCount; i++) {
					if (randElements.isEmpty()) {
						error = "*(Warning: Ran out of possible results. " + (rollCount - rollsTaken) + " roll(s) were not performed.)*"; 
						break;
					} else if (results.length() > 1900) {
						error = "*(Warning: Ran out of space to place results. " + (rollCount - rollsTaken) + " roll(s) were not performed.)*";
						break;
					}
					if (!results.equals("")) { results += ","; }
					int randIndex = RAND.nextInt(randElements.size());
					rollsTaken++;
					results += (randElements.get(randIndex)+1);
					
					randElements.remove(randIndex);
				}
			}
			
		} else if (rollParams.matches("\\[(.+,?)*\\]")) {
			//first arg matches [1,2]
			String cleansedParams = rollParams.substring(1, rollParams.length()-1);
			//remove brackets from beginning and end
			String[] splitParams = cleansedParams.split(",");
			ArrayList<String> randElements = new ArrayList<>(Arrays.asList(splitParams));
			//split elements by commas.
			//ready to roll
			for (int i = 0; i < rollCount; i++) {
				if (randElements.isEmpty()) {
					error = "*(Warning: Ran out of possible results. " + (rollCount - rollsTaken) + " roll(s) were not performed.)*"; 
					break;
				} else if (results.length() > 1900) {
					error = "*(Warning: Ran out of space to place results. " + (rollCount - rollsTaken) + " roll(s) were not performed.)*";
					break;
				}
				if (!results.equals("")) { results += ","; }
				int randIndex = RAND.nextInt(randElements.size());
				rollsTaken++;
				results += (randElements.get(randIndex));
				
				if (!allowDuplicates) {
					randElements.remove(randIndex);
				}
			}
		} else {
			channel.sendMessage(":pancakes: **Invalid Arguments:** Parameters don't match the proper format of either `d#` or `[1,2,3]`").queue();
			return;
		}
		
		channel.sendMessage(":pancakes: **Roll:** Rolling a die with parameters `" + rollParams + "` " + rollCount + " time" + (rollCount > 1 ? "s" : "") 
				+ (allowDuplicates ? " with duplicates allowed" : " without duplicates") + "...\n```RESULTS: " + results + "```" + error).queue();
	}
}
