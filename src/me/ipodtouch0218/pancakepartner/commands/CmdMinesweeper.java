package me.ipodtouch0218.pancakepartner.commands;

import java.util.ArrayList;
import java.util.Random;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CmdMinesweeper extends BotCommand {

	private static final Random rand = new Random();
	private static final char[] monospace_chars = {'\u3000','\uFF11','\uFF12','\uFF13','\uFF14','\uFF15','\uFF16','\uFF17','\uFF18','\uFF19'};
	
	public CmdMinesweeper() {
		super("minesweeper", true, true);
		setHelpInfo("Creates a playable game of minesweeper! Use the \"-mobile\" flag within the command for easier viewing on mobile. (X's are mines, max 13x13)", "minesweeper [[width] [height]] [mines]");
		setAliases("mine", "mines");
	}

	@Override
	public void execute(Message msg, String alias, ArrayList<String> args, ArrayList<String> flags) {
		MessageChannel channel = msg.getChannel();
		int boardwidth = 8;
		int boardheight = 8;
		int minecount = 10;
		
		if (args.size() == 1) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** Both a height and a width are required.").queue();
			return;
		}
		if (args.size() >= 2) {
			//board size
			try {
				boardwidth = Integer.parseInt(args.get(0));
				boardheight = Integer.parseInt(args.get(1));
			} catch (NumberFormatException e) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid height and width! (not a number?)").queue();
				return;
			}
			minecount = (int) (boardwidth*boardheight*0.15625);
		} 
		if (args.size() > 2) {
			try {
				minecount = Integer.parseInt(args.get(2));
			} catch (NumberFormatException e) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid mine count! (not a number?)").queue();
				return;
			}
		}
		
		if (boardwidth > 13 || boardheight > 13 || boardwidth <= 0 || boardheight <= 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid height and width! (minsize=1, maxsize=13x13)").queue();
			return;
		}
		
		String info = "*Settings: " + boardwidth + "x" + boardheight + ", " + minecount + " mines.*";
		if (minecount > boardwidth*boardheight) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid mine count! (more mines than board size)").queue();
			return;
		} else if (minecount < 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid mine count! (cannot have negative mines!?)").queue();
			return;
		} else if (minecount == boardwidth*boardheight) {
			info = "*Only mines? How do you win!?*";
		} else if (minecount == 0) {
			info = "*No mines. What fun.*";
		}
		
		
		String output = generateMinesweeperBoard(boardwidth, boardheight, minecount, flags.contains("-mobile"));
		channel.sendMessage(":pancakes: **PancakeGames: Minesweeper** " + info + "\n" + output).queue();
	}
	
	private String generateMinesweeperBoard(int width, int height, int mines, boolean space) {
		char[][] board = new char[width][height];
		while (mines > 0) {
			int newx = rand.nextInt(width);
			int newy = rand.nextInt(height);
			if (board[newx][newy] != 'X') {
				board[newx][newy] = 'X';
				mines--;
				
				for (int deltax = -1; deltax <= 1; deltax++) {
					for (int deltay = -1; deltay <= 1; deltay++) {
						if (deltax == 0 && deltay == 0) { continue; }
						int neighborx = newx+deltax;
						if (neighborx < 0 || neighborx >= width) { continue; }
						int neighbory = newy+deltay;
						if (neighbory < 0 || neighbory >= height) { continue; }
						if (board[neighborx][neighbory] == 'X') { continue; }
						
						board[neighborx][neighbory] += 1;
					}
				}
			}
		}
		
		StringBuilder output = new StringBuilder();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				output.append("||");
				char value = board[x][y];
				if (value == 'X') {
					output.append('\uFF38');
				} else {
					output.append(monospace_chars[board[x][y]]);
				}
				output.append("||" + (space ? " " : ""));
			}
			output.append("\n");
		}
		return output.toString();
	}
}
