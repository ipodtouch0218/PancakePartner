package me.ipodtouch0218.pancakepartner.utils;

import java.util.regex.Pattern;

public class MiscUtils {

	private MiscUtils() {} //disable instances
	
	public static final Pattern PATTERN_MESSAGE_LINK = Pattern.compile("(https?:\\/\\/)?(www\\.)?discordapp\\.com\\/channels\\/(?<guild>\\d+)\\/(?<channel>\\d+)\\/(?<messageid>\\d+)");
	
	public static String[] arrayRemoveAndShrink(String[] original, int element){
	    String[] n = new String[original.length - 1];
	    System.arraycopy(original, 0, n, 0, element );
	    System.arraycopy(original, element+1, n, element, original.length - element-1);
	    return n;
	}

	public static boolean isInteger(String string) {
		return string.matches("[-+]?\\d+");
	}
	
	public static int[] createArrayRange(int start, int end) {
		int[] result = new int[end-start];
		for (int i = start; i < end; i++) {
			result[i-start] = i;
		}
		return result;
	}
}
