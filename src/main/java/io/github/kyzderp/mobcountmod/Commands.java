package io.github.kyzderp.mobcountmod;

public class Commands 
{
	private LiteModMobCounter main;
	
	public Commands(LiteModMobCounter main)
	{
		this.main = main;
	}
	
	/**
	 * Handles StaffDerps commands
	 * @param message
	 */
	public void handleCommand(String message)
	{
		String[] tokens = message.split(" ");
		
		if (tokens.length < 2)
		{
			LiteModMobCounter.logMessage("§2" + this.main.getName() + " §8[§2v" + this.main.getVersion() + "§8] §aby Kyzeragon", false);
			LiteModMobCounter.logMessage("Type §2/counter help §afor commands.", false);
		}
		else if (tokens[1].equalsIgnoreCase("message") || tokens[1].equalsIgnoreCase("m")
				|| tokens[1].equalsIgnoreCase("msg"))
		{
			messageCommand(tokens);
		}
		else if (tokens[1].equalsIgnoreCase("sound"))
		{
			soundCommand(tokens);
		}
		else if (tokens[1].matches("fac|faction"))
		{
			notifyFacCommand(tokens);
		}
		else if (tokens[1].equalsIgnoreCase("xp5") && LiteModMobCounter.isStaff())
		{
			xp5Command(tokens);
		}
		else if (tokens[1].equalsIgnoreCase("help"))
		{
			helpCommand();
		}
		else 
		{
			LiteModMobCounter.logMessage(this.main.getName() + " [v" + this.main.getVersion() + "]", false);
			LiteModMobCounter.logMessage("Type /counter help for commands.", false);
		}
	} // handleCommand

	
	////////////////////////////////////////////////////////////////////////////
	//////////////////////////     COMMANDS      ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Usage/help output
	 */
	private void helpCommand() 
	{
		String[] commands = {"msg [player1[,player2]] §7- §aSet notified players",
				"msg clear §7- §aClear the list of notfied players",
				"fac|faction <on|off> §7- §aToggle notification in faction chat.",
				"sound [sound file] §7- §aSet the notification sound.",
		"help §7- §athis.main help message. Hurrdurr."};
		LiteModMobCounter.logMessage("§2" + this.main.getName() + " §8[§2v" + this.main.getVersion() + "§8] §acommands", false);
		for (String command : commands)
			LiteModMobCounter.logMessage("/counter " + command, false);
	} // helpCommand

	/**
	 * Handles the xp5 command for Ebelrawrgon
	 * @param tokens
	 */
	private void xp5Command(String[] tokens) 
	{
		if (tokens.length > 2)
		{
			if (tokens[2].equalsIgnoreCase("on"))
			{
				this.main.getCounter().setXP5(true);
				LiteModMobCounter.logMessage("Now counting only mobs at ShockerzXP5 kill points... mostly.", true);
				return;
			}
			else if (tokens[2].equalsIgnoreCase("off"))
			{
				this.main.getCounter().setXP5(false);
				LiteModMobCounter.logMessage("Using normal mob counter radius.", true);
				return;
			}
		}
		LiteModMobCounter.logError("Usage: /counter xp5 <on|off>");
	} // xp5Command

	/**
	 * Handles the faction notifications command
	 * @param tokens
	 */
	private void notifyFacCommand(String[] tokens) {
		if (tokens.length > 2)
		{
			if (tokens[2].equalsIgnoreCase("on"))
			{
				this.main.setNotifyFac(true);
				LiteModMobCounter.logMessage("Now notifying in faction chat when over 150 mobs.", true);
			}
			else if (tokens[2].equalsIgnoreCase("off"))
			{
				this.main.setNotifyFac(false);
				LiteModMobCounter.logMessage("Not notifying in faction chat.", true);
			}
			else
				LiteModMobCounter.logError("Usage: /counter fac <on|off>");
		}
		else if (this.main.isNotifyFac())
			LiteModMobCounter.logMessage("Currently notifying faction chat.", true);
		else
			LiteModMobCounter.logMessage("Currently not notifying faction chat.", true);
	} // notifyFacCommand

	/**
	 * Handles the command that deals with notification sound
	 * @param tokens
	 */
	private void soundCommand(String[] tokens) {
		if (tokens.length < 3)
			LiteModMobCounter.logMessage("Current hostile sound: " + this.main.getSound(), true);
		else if (tokens.length > 3)
			LiteModMobCounter.logError("Too many args! Usage: /counter sound <sound file>");
		else
		{
			this.main.setSound(tokens[2]);
			LiteModMobCounter.logMessage("Now using " + this.main.getSound() + " as notification sound.", true);
		}
	} // soundCommand

	/**
	 * Handles the auto messager command
	 * @param tokens
	 */
	private void messageCommand(String[] tokens) 
	{
		if (tokens.length < 3)
		{
			if (this.main.getToMessage() == null)
			{
				LiteModMobCounter.logMessage("Not currently notifying any players.", true);
				return;
			}
			String toSend = "Currently notifying: ";
			for (String name : this.main.getToMessage())
				toSend += name + " ";
			LiteModMobCounter.logMessage(toSend, true);
		}
		else if (tokens.length > 3)
			LiteModMobCounter.logError("Too many args! Usage: /counter msg clear OR /counter msg [player1[,player2]]]");
		else if (tokens[2].equalsIgnoreCase("clear"))
		{
			this.main.setToMessage(null);
			LiteModMobCounter.logMessage("Not currently notifying any players.", true);
		}
		else
		{
			this.main.setToMessage(tokens[2].split(","));
			String toSend = tokens[2].replaceAll(",", " ");
			LiteModMobCounter.logMessage("Now notifying: " + toSend, true);
		}
	} // automessageCommand
}
