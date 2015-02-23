package com.kyzeragon.mobcountmod;

import java.io.File;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import org.lwjgl.input.Keyboard;

import com.mumfrey.liteloader.ChatFilter;
import com.mumfrey.liteloader.OutboundChatListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;

/**
 * Simple LiteMod that displays entity counts for certain mobs within a specified radius
 *
 * @author Kyzeragon
 */
@ExposableOptions(strategy = ConfigStrategy.Versioned, filename="mobcountermod.json")
public class LiteModMobCounter implements Tickable, ChatFilter, OutboundChatListener
{
	private static final boolean staff = true;
	private static final boolean useOptions = false;
	private static final boolean rebel = false;
	private static KeyBinding counterKeyBinding;
	private static KeyBinding hostileKeyBinding;
	private static KeyBinding optionsKeyBinding;

	private boolean showChildCounts;
	private boolean sentCmd;
	private MobCounterConfigScreen configScreen = new MobCounterConfigScreen();
	private MobCounter counter = new MobCounter(staff);
	private String[] toMessage;
	private String sound;

	private int counterVisible = 0; // 0 - not visible, 1 - compact, 2 - expanded
	private int hostileVisible = 1;
	private int playSoundCount = 0; // counts up so sound plays once per sec
	private int sendMsgCount = 0; // counts up so message sends every 5 minutes

	private String[] passives = {"Chickens: ", "Pigs: ", "Sheep: ", "Cows: ", "Horses: ", "Mooshrooms: ", "Ocelots: ", "Wolves: "};
	private String[] hostiles = {"Zombies: ", "CaveSpiders: ", "Skeletons: ", "Spiders: ", 
			"Creepers: ", "Witches: ", "Pigmen: ", "Slimes: "};


	public LiteModMobCounter() {}

	@Override
	public String getName()
	{
		String toReturn = "Mob Counter";
		if (this.rebel)
			toReturn += " - RebelAngel";
		else if (this.staff)
			toReturn += " - Staff";
		return toReturn;
	}

	@Override
	public String getVersion() { return "1.0.3"; }

	@Override
	public void init(File configPath)
	{
		this.sentCmd = false;
		this.showChildCounts = false;
		this.sound = "note.bass";

		counterKeyBinding = new KeyBinding("key.counter.toggle", Keyboard.KEY_P, "key.categories.litemods");
		hostileKeyBinding = new KeyBinding("key.counter.togglehostile", Keyboard.KEY_O, "key.categories.litemods");
		if (this.useOptions)
			optionsKeyBinding = new KeyBinding("key.counter.options", Keyboard.KEY_SEMICOLON, "key.categories.litemods");

		LiteLoader.getInput().registerKeyBinding(counterKeyBinding);
		LiteLoader.getInput().registerKeyBinding(hostileKeyBinding);
		if (this.useOptions)
			LiteLoader.getInput().registerKeyBinding(optionsKeyBinding);

		if (this.rebel) { // EEEEEEEEEEEEEEEbel
			//			this.hostileVisible = 1;
		}
	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {}

	@SuppressWarnings("unused")
	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock)
	{
		if (inGame && minecraft.currentScreen != null && minecraft.currentScreen.equals(configScreen)) {
			configScreen.updateScreen();
		}

		if (inGame) {
			this.hostileLimit();
		}

		if (inGame && minecraft.currentScreen == null && Minecraft.isGuiEnabled())
		{
			if (this.useOptions && LiteModMobCounter.optionsKeyBinding.isPressed())
			{
				minecraft.displayGuiScreen(this.configScreen);
			}

			///// Passives /////
			this.passiveKey();

			if (this.counterVisible > 0) // compact
			{
				this.displayPassiveCompact();
				if (this.counterVisible > 1) // expanded
				{
					this.displayPassiveExpanded();
				}
			}

			///// Hostiles /////
			this.hostileKey();

			if (this.hostileVisible > 0) // compact
			{
				this.displayHostile();
			}
		}
	}

	/**
	 * Client side commands
	 */
	@Override
	public void onSendChatMessage(C01PacketChatMessage packet, String message)
	{
		String[] tokens = message.split(" ");
		if (tokens[0].equalsIgnoreCase("/counter") || tokens[0].equalsIgnoreCase("/count"))
		{
			this.sentCmd = true;
			if (tokens.length > 1)
			{
				if (tokens[1].equalsIgnoreCase("message") || tokens[1].equalsIgnoreCase("m")
						|| tokens[1].equalsIgnoreCase("msg"))
				{
					if (tokens.length < 3)
					{
						String toSend = "Currently notifying: ";
						for (String name : this.toMessage)
							toSend += name + " ";
						this.logMessage(toSend);
					}
					else if (tokens.length > 3)
						this.logError("Too many args! Usage: /counter msg [player1[,player2]]]");
					else
					{
						this.toMessage = tokens[2].split(",");
						String toSend = tokens[2].replaceAll(",", " ");
						this.logMessage("Now notifying: " + toSend);
					}
				}
				else if (tokens[1].equalsIgnoreCase("sound"))
				{
					if (tokens.length < 3)
						this.logMessage("Current hostile sound: " + this.sound);
					else if (tokens.length > 3)
						this.logError("Too many args! Usage: /counter sound <sound file>");
					else
					{
						this.sound = tokens[2];
						this.logMessage("Now using " + this.sound + " as notification sound.");
					}
				}
				else if (tokens[1].equalsIgnoreCase("help"))
				{
					String[] commands = {"msg [player1[,player2]] - Set notified players",
							"sound [sound file] - Set the notification sound.",
							"help - This help message. Hurrdurr."};
					this.logMessage(this.getName() + " [v" + this.getVersion() + "] commands");
					for (String command : commands)
						this.logMessage("/counter " + command);
				}
				else {
					this.logMessage(this.getName() + " [v" + this.getVersion() + "]");
					this.logMessage("Type /counter help for commands.");
				}
			}
			else {
				this.logMessage(this.getName() + " [v" + this.getVersion() + "]");
				this.logMessage("Type /counter help for commands.");
			}
		}
	}

	/**
	 * Stops the Unknown command error from the server from displaying
	 */
	@Override
	public boolean onChat(S02PacketChat chatPacket, IChatComponent chat,
			String message) {
		if (message.matches(".*nknown.*ommand.*") && this.sentCmd)
		{
			this.sentCmd = false;
			return false;
		}
		return true;
	}

	///// PRIVATE METHODS /////
	/**
	 * Plays the sound "note.bass" if there are 150+ hostile mobs in the hostile mob count radius
	 */
	private void hostileLimit()
	{
//		System.out.println("playSoundCount: " + this.playSoundCount + " sendMsgCount: " + this.sendMsgCount);
		int totalCount = 0;
		for (int i = 0; i < 8; i++)
			totalCount += this.counter.countEntity(i + 8, true);
		if (this.playSoundCount != 0)
			this.playSoundCount++;
		if (this.sendMsgCount != 0)
			this.sendMsgCount++;

		if (totalCount > 149)
		{
			if (this.playSoundCount == 0)
			{
				Minecraft.getMinecraft().thePlayer.playSound(this.sound, 1.0F, 1.0F);
				this.playSoundCount++;
			}
			if (this.playSoundCount > 100)
				this.playSoundCount = -1;

			if (this.sendMsgCount == 0)
			{
				if (this.rebel)
					Minecraft.getMinecraft().thePlayer.sendChatMessage("/ch qm f Automated Message: "
							+ totalCount + " mobz. Kill pl0x.");
				if (this.toMessage != null && this.toMessage.length > 0)
				{
					for (String player : this.toMessage)
						Minecraft.getMinecraft().thePlayer.sendChatMessage("/m " + player 
								+ " Automated Message: Kill mobz pl0x.");
				}
				this.sendMsgCount++;
			}
			if (this.sendMsgCount > 10000)
				this.sendMsgCount = -1;
		}
	}

	/**
	 * Adjust passive counter's settings according to key input
	 */
	private void passiveKey()
	{
		if (LiteModMobCounter.counterKeyBinding.isPressed())
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_UP))
				this.counter.increaseRadius(staff);
			else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
				this.counter.decreaseRadius();
			else
			{
				this.counterVisible++;
				if (this.counterVisible > 2)
					this.counterVisible = 0;
			}
		}
	}

	/**
	 * Adjust hostile counter's settings according to key input
	 */
	private void hostileKey()
	{
		if (LiteModMobCounter.hostileKeyBinding.isPressed())
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_UP))
				this.counter.increaseHRadius(staff);
			else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
				this.counter.decreaseHRadius();
			else
			{
				this.hostileVisible++;
				if (this.hostileVisible > 2)
					this.hostileVisible = 0;
			}
		}
	}

	/**
	 * Display compact passive
	 */
	private void displayPassiveCompact()
	{
		FontRenderer fontRender = Minecraft.getMinecraft().fontRenderer;
		this.counter.updateBB();
		fontRender.drawStringWithShadow("Radius: " + this.counter.getRadius(), 0, 0, 0xFFAA00);
		if (staff) 
			fontRender.drawStringWithShadow("Players: " + this.counter.countEntity(16, true), 60, 0, 0xFFFFFF);
		String toDisplay;

		int color = 0xFFFFFF;
		for (int i = 0; i < 4; i++)
		{
			int count = this.counter.countEntity(i, true) + this.counter.countEntity(i, false);
			toDisplay = "" + count;
			if (this.showChildCounts)
				toDisplay = this.counter.countEntity(i, false) + "/" + this.counter.countEntity(i, true);
			if (count > 16) color = 0xAA0000;
			fontRender.drawStringWithShadow(this.passives[i] + toDisplay, 0, i * 10 + 10, color);
			color = 0xFFFFFF;
		}
	}

	/**
	 * Display expanded passive
	 */
	private void displayPassiveExpanded()
	{
		for (int i = 4; i < 8; i++)
		{
			FontRenderer fontRender = Minecraft.getMinecraft().fontRenderer;
			int color = 0xFFFFFF;
			int count = this.counter.countEntity(i, true) + this.counter.countEntity(i, false);
			String toDisplay = "" + count;
			int x = 70;
			if (this.showChildCounts)
			{
				toDisplay = this.counter.countEntity(i, false) + "/" + this.counter.countEntity(i, true);
				x = 80;
			}
			if (count > 16) color = 0xAA0000;
			fontRender.drawStringWithShadow(this.passives[i] + toDisplay, x, i * 10 - 30, color);
			color = 0xFFFFFF;
		}
	}

	/**
	 * Display hostile counter
	 */
	private void displayHostile()
	{
		FontRenderer fontRender = Minecraft.getMinecraft().fontRenderer;
		int offset = 0;
		if (this.counterVisible > 0)
			offset = 50;

		this.counter.updateHostileBB();
		fontRender.drawStringWithShadow("Radius: " + this.counter.getHRadius(), 0, offset, 0xFFAA00);

		int totalCount = 0;
		for (int i = 0; i < 4; i++)
		{
			int count = this.counter.countEntity(i + 8, true);
			totalCount += count;
			fontRender.drawStringWithShadow(this.hostiles[i] + count, 0, i * 10 + 10 + offset, 0xFFFFFF);
		}
		for (int i = 4; i < 8; i++) 
		{
			int count = this.counter.countEntity(i + 8, true);
			totalCount += count;
			if (this.hostileVisible > 1)
				fontRender.drawStringWithShadow(this.hostiles[i] + count, 90, i * 10 - 30 + offset, 0xFFFFFF);
		}
		int color = 0xFFFFFF;
		if (totalCount > 149) // if 150+ mobs, display in red.
		{
			color = 0xAA0000;
		}
		else
			this.playSoundCount = 100;
		fontRender.drawStringWithShadow("Total: " + totalCount, 60, offset, color);
	}

	/**
	 * Logs the message to the user
	 * @param message The message to log
	 */
	private void logMessage(String message)
	{
		ChatComponentText displayMessage = new ChatComponentText(message);
		displayMessage.setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.AQUA));
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}

	/**
	 * Logs the error message to the user
	 * @param message The error message to log
	 */
	private void logError(String message)
	{
		ChatComponentText displayMessage = new ChatComponentText(message);
		displayMessage.setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.RED));
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}
}
