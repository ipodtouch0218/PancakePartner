package me.ipodtouch0218.pancakepartner.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
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
	
	private static final Comparator<TimeUnit> timeCompare = new Comparator<TimeUnit>() {
		public int compare(TimeUnit o1, TimeUnit o2) {
			return o2.compareTo(o1);
		}
	};
	
	public static String timeElapsed(TimeUnit differenceUnit, long difference, TimeUnit... displayUnits) {
		if (displayUnits.length <= 0) { return ""; }
		Arrays.sort(displayUnits, timeCompare);
		String output = "";
		boolean blank = true;
		for (int i = 0; i < displayUnits.length; i++) {
			TimeUnit unit = displayUnits[i];
			long amount = unit.convert(difference, differenceUnit);
			if (amount > 0 || (i >= (displayUnits.length-1) && blank)) {
				//TODO: enum name instead of prefix array
//				output += (amount + " " + (unit.toString().substring(0, unit.toString().length()-(amount == 1 ? 1 : 0))) + " ");
				output += (amount + timeUnitToPrefix(unit) + " ");
				blank = false;
			}
			difference -= differenceUnit.convert(amount, unit);
		}
		return output.trim().toLowerCase();
	}
	private static final String[] unitPrefixes = {"ns","\u03BCs","ms","s","m","h","d"};
	private static String timeUnitToPrefix(TimeUnit unit) {
		return unitPrefixes[unit.ordinal()];
	}
	
}
