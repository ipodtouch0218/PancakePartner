package me.ipodtouch0218.pancakepartner.commands;

public class CommandFlag {

	private String flagTag; //Tag of the flag, ex. -<tag> in the command itself.
	private String[] parameters; //Arguments after the flag that were consumed as parameters.
	
	public CommandFlag(String tag, String[] parameters) {
		this.flagTag = tag;
		this.parameters = parameters;
	}
	
	//--Getters--//
	public String[] getParameters() { return parameters; }
	public String getTag() { return flagTag; }
}
