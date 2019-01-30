package me.ipodtouch0218.pancakepartner.handlers;

import me.ipodtouch0218.pancakepartner.BotMain;
import me.ipodtouch0218.pancakepartner.commands.CmdStar;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {
	
	//---Event Handling---//
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		Message msg = e.getMessage();
		User author = e.getAuthor();
		
		if (e.getAuthor().getIdLong() == BotMain.getJdaInstance().getSelfUser().getIdLong()) { return; }
		if (CommandHandler.isCommand(msg)) {
			//the message is a command, send to the commandhandler
			BotMain.getCommandHandler().executeCommand(msg, author); //result is if the command was successful.
		}
	}
	
	
	//---Starred Message System---//
	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent e) {
		handleStarredReaction(e);
	}
	
	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent e) {
		handleStarredReaction(e);
	}
	
	private void handleStarredReaction(GenericGuildMessageReactionEvent e) {
		if (e.getReactionEmote().getName().equals("\u2B50")) { //star emote added
			CmdStar starCmd = (CmdStar) BotMain.getCommandHandler().getCommandByName("star");
			
			if (CmdStar.starredMessages.containsKey(e.getMessageIdLong())) {
				starCmd.editStarredMessage(e.getMessageIdLong());
			} else {
				int count = 1;
				try {
					count = e.getReaction().getUsers().complete().size();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				if (count >= BotMain.getBotSettings().getCmdStarRequiredStars()) {
					e.getChannel().getMessageById(e.getMessageId()).queue(m -> {
						starCmd.sendStarredMessage(m);
					});
				}
			}
		}
	}
	
	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent e) {
		if (CmdStar.starredMessages.containsValue(e.getMessageIdLong())) { //only if the starred message embed posted in the channel was deleted
			CmdStar.starredMessages.values().remove(e.getMessageIdLong());
		}
	}
}
