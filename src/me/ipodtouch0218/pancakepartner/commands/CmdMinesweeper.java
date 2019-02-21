package me.ipodtouch0218.pancakepartner.commands;

import java.util.ArrayList;
import java.util.Random;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.utils.tuple.Pair;

public class CmdMinesweeper extends BotCommand {

	private static final Random rand = new Random();
	private static final char[] monospace_chars = {'\u3000','\uFF11','\uFF12','\uFF13','\uFF14','\uFF15','\uFF16','\uFF17','\uFF18','\uFF19'};
	
	public CmdMinesweeper() {
		super("minesweeper", true, true);
		setHelpInfo("Creates a playable game of minesweeper! FLAGS: \"-mobile\" and \"hint\". (X's are mines, max 13x13)", "minesweeper [[width] [height]] [mines]");
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
			//board size, 1st and 2nd argument
			try {
				boardwidth = Integer.parseInt(args.get(0));
				boardheight = Integer.parseInt(args.get(1));
				//set board width and height from the command
			} catch (NumberFormatException e) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid height and width! (not a number?)").queue();
				return;
			}
			minecount = (int) (boardwidth*boardheight*0.15625);
		} 
		if (args.size() > 2) {
			//mine count, 3rd argument
			try {
				minecount = Integer.parseInt(args.get(2));
				//set mine count
			} catch (NumberFormatException e) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid mine count! (not a number?)").queue();
				return;
			}
		}
		
		if (boardwidth > 13 || boardheight > 13 || boardwidth <= 0 || boardheight <= 0) {
			//board too big or less than 1
			channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid height and width! (minsize=1, maxsize=13x13)").queue();
			return;
		}
		
		String info = "*Settings: " + boardwidth + "x" + boardheight + ", " + minecount + " mines.*";
		if (minecount > boardwidth*boardheight) {
			//more mines than available spaces
			channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid mine count! (more mines than board size)").queue();
			return;
		} else if (minecount < 0) {
			//negative amount of mines
			channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid mine count! (cannot have negative mines!?)").queue();
			return;
		} else if (minecount == boardwidth*boardheight) {
			//mines = available spaces
			info = "*Only mines? How do you win!?*";
		} else if (minecount == 0) {
			//no mines
			info = "*No mines. What fun.*";
		}
		
		boolean outputHint = flags.contains("-hint");
		
		char[][] board = generateMinesweeperBoard(boardwidth, boardheight, minecount, outputHint);
		String hint = "";
		if (outputHint) {
			//
			if (hintX == -1 || hintY == -1) {
				hint = " HINT: There are no blank tiles! (good luck)";
			} else {
				hint = " HINT: (" + (hintX+1) + ", " + (hintY+1) + ") is blank!";
			}
		}
		channel.sendMessage(":pancakes: **PancakeGames: Minesweeper** " + info + hint + "\n" + outputBoard(board, flags.contains("-mobile"), flags.contains("-coords"))).queue();
	}
	
	private int hintX, hintY;
	
	private char[][] generateMinesweeperBoard(int width, int height, int mines, boolean setHint) {
		
		ArrayList<Pair<Integer,Integer>> blankTiles = new ArrayList<>();
		if (setHint) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					blankTiles.add(Pair.of(x,y));
				}
			}
		}
		
		char[][] board = new char[width][height]; 
		while (mines > 0) {
			int newx = rand.nextInt(width);
			int newy = rand.nextInt(height);
			if (board[newx][newy] != 'X') {
				//blank location for a mine
				board[newx][newy] = 'X';
				mines--;
				
				for (int deltax = -1; deltax <= 1; deltax++) {
					for (int deltay = -1; deltay <= 1; deltay++) {
						int neighborx = newx+deltax;
						int neighbory = newy+deltay;
						
						blankTiles.remove(Pair.of(neighborx, neighbory));
						
						if (deltax == 0 && deltay == 0) { continue; }
						if (neighborx < 0 || neighborx >= width) { continue; }
						if (neighbory < 0 || neighbory >= height) { continue; }
						if (board[neighborx][neighbory] == 'X') { continue; }
						
						board[neighborx][neighbory] += 1; //set all adjacent spaces +1
					}
				}
			}
		}
		
		if (setHint) {
			if (blankTiles.isEmpty()) {
				hintX = -1;
				hintY = -1;
			} else {
				Pair<Integer,Integer> rand = blankTiles.get((int) (Math.random()*blankTiles.size()));
				hintX = rand.getLeft();
				hintY = rand.getRight();
			}
		}
		return board;
	}
	
	private String outputBoard(char[][] board, boolean space, boolean grid) {
		StringBuilder output = new StringBuilder();
		if (grid) {
			output.append("    ");
			for (int x = 0; x < board.length; x++) {
				output.append(Character.toChars(9351 + (x+1)));
				if (space) {
					output.append(" ");
				} else {
					output.append("\u2009");
				}
			}
			output.append("\n");
		}
		for (int y = 0; y < board[0].length; y++) {
			if (grid) {
				output.append(Character.toChars(9351 + (y+1)));
			}
			for (int x = 0; x < board.length; x++) {
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
