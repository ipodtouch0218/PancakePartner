package me.ipodtouch0218.pancakepartner.commands;

import java.util.HashSet;
import java.util.Random;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public class CmdMinesweeper extends BotCommand {

	private static final Random rand = new Random();
	
	public CmdMinesweeper() {
		super("minesweeper", true, true);
		setHelpInfo("Creates a playable game of minesweeper! (X's are mines, max 15x15)", "minesweeper [[width] [height]] [mines]");
		setAliases("mines");
	}

	@Override
	public void execute(Message msg, String alias, String[] args) {
		MessageChannel channel = msg.getChannel();
		int boardwidth = 8;
		int boardheight = 8;
		int minecount = 10;
		
		if (args.length == 1) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** Both a height and a width are required.").queue();
			return;
		}
		if (args.length >= 2) {
			//board size
			try {
				boardwidth = Integer.parseInt(args[0]);
				boardheight = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid height and width! (not a number?)").queue();
				return;
			}
		} 
		if (args.length > 2) {
			try {
				minecount = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid mine count! (not a number?)").queue();
				return;
			}
		}
		
		if (boardwidth > 15 || boardheight > 15 || boardwidth <= 0 || boardheight <= 0) {
			channel.sendMessage(":pancakes: **Invalid Arguments:** Invalid height and width! (minsize=1, maxsize=1)").queue();
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
		
		
		String output = generateMinesweeperBoard(boardwidth, boardheight, minecount);
		channel.sendMessage(":pancakes: **PancakeGames: Minesweeper** " + info + "\n" + output).queue();
	}
	
	private String generateMinesweeperBoard(int width, int height, int mines) {
		HashSet<Integer[]> minePositions = new HashSet<>();
		char[][] board = new char[width][height];
		while (mines > 0) {
			int newx = rand.nextInt(width);
			int newy = rand.nextInt(height);
			if (board[newx][newy] == 0) {
				board[newx][newy] = 'X';
				minePositions.add(new Integer[]{newx,newy});
				mines--;
			}
		}
		
		for (Integer[] position : minePositions) {
			int x = position[0];
			int y = position[1];
			
			for (int deltax = -1; deltax <= 1; deltax++) {
				for (int deltay = -1; deltay <= 1; deltay++) {
					if (deltax == 0 && deltay == 0) { continue; }
					int neighborx = x+deltax;
					if (neighborx < 0 || neighborx >= width) { continue; }
					int neighbory = y+deltay;
					if (neighbory < 0 || neighbory >= height) { continue; }
					if (board[neighborx][neighbory] == 'X') { continue; }
					
					board[neighborx][neighbory] += 1;
				}
			}
		}
		
		StringBuilder output = new StringBuilder();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				output.append("||`");
				char value = board[x][y];
				if (value == 'X') {
					output.append(value);
				} else {
					output.append((char) (board[x][y]+'0'));
				}
				output.append("`|| ");
			}
			output.append("\n");
		}
		return output.toString();
	}
}
