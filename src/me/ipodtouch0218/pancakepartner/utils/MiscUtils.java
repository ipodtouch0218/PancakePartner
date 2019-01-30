package me.ipodtouch0218.pancakepartner.utils;

public class MiscUtils {

	private MiscUtils() {} //disable instances
	
	
	public static String[] arrayRemoveAndShrink(String[] original, int element){
	    String[] n = new String[original.length - 1];
	    System.arraycopy(original, 0, n, 0, element );
	    System.arraycopy(original, element+1, n, element, original.length - element-1);
	    return n;
	}

	public static boolean isInteger(String string) {
		return string.matches("[-+]?\\d+");
	}
	
	
	//---Levenshtein Distance---//
	public static int calcLevenshteinDistance(String x, String y) {
		//uhhh dont' ask i just copy-pasted
	    int[][] dp = new int[x.length() + 1][y.length() + 1];
	 
	    for (int i = 0; i <= x.length(); i++) {
	        for (int j = 0; j <= y.length(); j++) {
	            if (i == 0) {
	                dp[i][j] = j;
	            }
	            else if (j == 0) {
	                dp[i][j] = i;
	            }
	            else {
	                dp[i][j] = Math.min(dp[i - 1][j - 1] 
	                + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), 
	                  Math.min(dp[i - 1][j] + 1, 
	                  dp[i][j - 1] + 1));
	            }
	        }
	    }
	 
	    return dp[x.length()][y.length()];
	}
    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }
}
