package me.ipodtouch0218.pancakepartner.commands;

import java.util.ArrayList;
import java.util.Arrays;

import me.ipodtouch0218.sjbotcore.command.BotCommand;
import me.ipodtouch0218.sjbotcore.command.FlagSet;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CmdTextFormat extends BotCommand {

	private static final TextFormatter[] formats = {TextFormatter.ZALGO, TextFormatter.SMALLTEXT};
	
	public CmdTextFormat() {
		super("textformat", true, true);
		setAliases("format", "text");
		setHelpInfo("Changes the appearence of the inputted text.", "textformat <type> <text>");
	}

	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, FlagSet flags) {
		MessageChannel channel = msg.getChannel();
		if (args.size() <= 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must choose a text format to use.").queue();
			return;
		}
		String formatStr = args.get(0);
		TextFormatter format = null;
		for (TextFormatter formatter : formats) {
			if (formatter.toString().equalsIgnoreCase(formatStr)) {
				format = formatter;
				break;
			}
		}
		if (format == null) {
			String possibleformats = Arrays.toString(formats);
			possibleformats = possibleformats.substring(1, possibleformats.length()-2);
			channel.sendMessage(String.format(":pancakes: **Invalid Arguments:** '%s' is not a valid formatter type. Possible formats: `%s`", formatStr, possibleformats)).queue();
			return;
		}
		if (args.size() <= 1) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** You must specify text to input by formatted.").queue();
			return;
		}
		String text = "";
		for (int i = 1; i < args.size(); i++) {
			text += args.get(i) + " ";
		}
		text = text.trim();
		
		String output = format.formatText(text);
		channel.sendMessage(output).queue();
	}
	
	
	
	private static abstract class TextFormatter {
		
		static final TextFormatter ZALGO = new TextFormatter("zalgo") {
			private final int[] upperchars = {768,769,770,771,772,773,774,775,776,777,778,779,780,781,
					782,783,784,785,786,787,788,789,794,795,829,830,831,832,833,834,835,836,838,842,843,844,
					848,849,850,855,856,859,861,862,864,865};
			private final int[] belowchars = {790,791,792,793,796,797,798,799,800,801,802,803,804,805,
					806,807,808,809,810,811,812,813,814,815,816,817,818,819,825,826,827,828,837,839,840,841,
					845,846,851,852,853,854,857,858,860,863,866};
			private final int[] centerchars = {820,821,822,823,824};
			
			public String formatText(String in) {
				double intensity = Math.min(3, (int) (2000/in.length())-1);
				
			    StringBuilder formatted = new StringBuilder();
			    for (int i = 0; i <= in.length(); i++) {
			    	char origChar = (i == in.length()) ? '\u0000' : in.charAt(i);
			    	if (((i % (1d/intensity) <= intensity) || intensity >= 1) && !Character.isWhitespace(origChar)) {
			    		formatted.append(Character.toChars(centerchars[(int) (Math.random()*centerchars.length)]));
			        	for (int j = 1; j < intensity; j++) {
			        		int[] array = (Math.random() < 0.5 ? upperchars : belowchars);
			        		int newchar = array[(int) (array.length*Math.random())];
			        		formatted.append(Character.toChars(newchar));
			        	}
			        }
			    	if (origChar != '\u0000') {
			    		formatted.append(origChar);
			    	}
			    }
			    return formatted.toString().trim();
			}
		};
		static final TextFormatter SMALLTEXT = new TextFormatter("smallcaps") {
			private final int[] chars = {7424,665,7428,7429,7431,1171,610,668,618,7434,7435,671,7437,628,7439,7448,491,640,115,7451,7452,7456,7457,120,655,7458};
			public String formatText(String in) {
				StringBuilder modifier = new StringBuilder(in);
				for (int i = 0; i < modifier.length(); i++) {
					char charAtPos = modifier.charAt(i);
					
					if (charAtPos >= 97 && charAtPos <= 122)
						modifier.setCharAt(i, (char) chars[charAtPos-97]);
				}
				return modifier.toString().trim();
			}
		};

		private String name;
		TextFormatter(String name) {
			this.name = name;
		}
		@Override
		public String toString() { return name; }
		public abstract String formatText(String in);
	}
}
