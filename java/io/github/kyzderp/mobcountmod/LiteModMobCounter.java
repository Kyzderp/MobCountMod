package io.github.kyzderp.mobcountmod;

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
import com.mumfrey.liteloader.OutboundChatFilter;
import com.mumfrey.liteloader.OutboundChatListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.core.LiteLoaderEventBroker.ReturnValue;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;

/**
 * Simple LiteMod that displays entity counts for certain mobs within a specified radius
 *
 * @author Kyzeragon
 */
@ExposableOptions(strategy = ConfigStrategy.Versioned, filename="mobcountermod.json")
public class LiteModMobCounter implements Tickable, OutboundChatFilter
{
	private static final boolean staff = false;
	private static final boolean useOptions = false;
	private static final boolean rebel = false;
	private static KeyBinding counterKeyBinding;
	private static KeyBinding hostileKeyBinding;
	private static KeyBinding optionsKeyBinding;

	private MobCounterConfigScreen configScreen = new MobCounterConfigScreen();
	private MobCounter counter = new MobCounter(staff);
	private String[] toMessage;
	private boolean notifyFac;
	private String sound;

	private int counterVisible = 0; // 0 - not visible, 1 - compact, 2 - expanded
	private int hostileVisible = 0;
	private int playSoundCount = 0; // counts up so sound plays once per sec
	private int sendMsgCount = 0; // counts up so message sends every 5 minutes

	private String[] passives = {"Chickens: ", "Pigs: ", "Sheep: ", "Cows: ", "Horses: ", "Mooshrooms: ", "Rabbits: ", "Wolves: "};
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
	public String getVersion() { return "1.2.0"; }

	@Override
	public void init(File configPath)
	{
		this.notifyFac = false;
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
			this.notifyFac = true;
			this.hostileVisible = 1;
			this.counter.setXP5(true);
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
	@SuppressWarnings("unused")
	@Override
	public boolean onSendChatMessage(String message)
	{
		String[] tokens = message.split(" ");
		if (tokens[0].equalsIgnoreCase("/counter"))
		{
			if (tokens.length < 2)
			{
				this.logMessage("§2" + this.getName() + " §8[§2v" + this.getVersion() + "§8] §aby Kyzeragon", false);
				this.logMessage("Type §2/counter help §afor commands.", false);
				return false;
			}
			if (tokens[1].equalsIgnoreCase("message") || tokens[1].equalsIgnoreCase("m")
					|| tokens[1].equalsIgnoreCase("msg"))
			{
				if (tokens.length < 3)
				{
					if (this.toMessage == null)
					{
						this.logMessage("Not currently notifying any players.", true);
						return false;
					}
					String toSend = "Currently notifying: ";
					for (String name : this.toMessage)
						toSend += name + " ";
					this.logMessage(toSend, true);
				}
				else if (tokens.length > 3)
					this.logError("Too many args! Usage: /counter msg clear OR /counter msg [player1[,player2]]]");
				else if (tokens[2].equalsIgnoreCase("clear"))
				{
					this.toMessage = null;
					this.logMessage("Not currently notifying any players.", true);
				}
				else
				{
					this.toMessage = tokens[2].split(",");
					String toSend = tokens[2].replaceAll(",", " ");
					this.logMessage("Now notifying: " + toSend, true);
				}
			}
			else if (tokens[1].equalsIgnoreCase("sound"))
			{
				if (tokens.length < 3)
					this.logMessage("Current hostile sound: " + this.sound, true);
				else if (tokens.length > 3)
					this.logError("Too many args! Usage: /counter sound <sound file>");
				else
				{
					this.sound = tokens[2];
					this.logMessage("Now using " + this.sound + " as notification sound.", true);
				}
			}
			else if (tokens[1].matches("fac|faction"))
			{
				if (tokens.length > 2)
				{
					if (tokens[2].equalsIgnoreCase("on"))
					{
						this.notifyFac = true;
						this.logMessage("Now notifying in faction chat when over 150 mobs.", true);
					}
					else if (tokens[2].equalsIgnoreCase("off"))
					{
						this.notifyFac = false;
						this.logMessage("Not notifying in faction chat.", true);
					}
					else
						this.logError("Usage: /counter fac <on|off>");
				}
				else if (this.notifyFac)
					this.logMessage("Currently notifying faction chat.", true);
				else
					this.logMessage("Currently not notifying faction chat.", true);
			}
			else if (tokens[1].equalsIgnoreCase("xp5") && this.staff)
			{
				if (tokens.length > 2)
				{
					if (tokens[2].equalsIgnoreCase("on"))
					{
						this.counter.setXP5(true);
						this.logMessage("Now counting only mobs at ShockerzXP5 kill points... mostly.", true);
					}
					else if (tokens[2].equalsIgnoreCase("off"))
					{
						this.counter.setXP5(false);
						this.logMessage("Using normal mob counter radius.", true);
					}
				}
				this.logError("Usage: /sd xp5 <on|off>");
			}
			else if (tokens[1].equalsIgnoreCase("help"))
			{
				String[] commands = {"msg [player1[,player2]] §7- §aSet notified players",
						"msg clear §7- §aClear the list of notfied players",
						"fac|faction <on|off> §7- §aToggle notification in faction chat.",
						"sound [sound file] §7- §aSet the notification sound.",
				"help §7- §aThis help message. Hurrdurr."};
				this.logMessage("§2" + this.getName() + " §8[§2v" + this.getVersion() + "§8] §acommands", false);
				for (String command : commands)
					this.logMessage("/counter " + command, false);
			}
			else {
				this.logMessage(this.getName() + " [v" + this.getVersion() + "]", false);
				this.logMessage("Type /counter help for commands.", false);
			}
		}
		return true;
	}

	///// PRIVATE METHODS /////
	/**
	 * Plays the sound "note.bass" if there are 150+ hostile mobs in the hostile mob count radius
	 */
	private void hostileLimit()
	{
		int totalCount = 0;
		for (int i = 0; i < 8; i++)
			totalCount += this.counter.countEntity(i + 8);
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
				if (this.notifyFac)
					Minecraft.getMinecraft().thePlayer.sendChatMessage("/ch qm f Automated Message: "
							+ totalCount + " mobz. Kill pl0x.");
				if (this.toMessage != null && this.toMessage.length > 0)
				{
					for (String player : this.toMessage)
						Minecraft.getMinecraft().thePlayer.sendChatMessage("/m " + player 
								+ " Automated Message: " + totalCount + " mobz. Kill pl0x.");
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
		FontRenderer fontRender = Minecraft.getMinecraft().fontRendererObj;
		this.counter.updateBB();
		fontRender.drawStringWithShadow("Radius: " + this.counter.getRadius(), 0, 0, 0xFFAA00);
		if (staff)
		{
			fontRender.drawStringWithShadow("Players: " + this.counter.countEntity(16), 60, 0, 0xFFFFFF);
			int color = 0xFFFFFF;
			int count = this.counter.countEntity(18);
			if (count > 16) color = 0xAA0000;
			fontRender.drawStringWithShadow("Snowmen: " + count, 0, 50, color);
		}
		String toDisplay;

		int color = 0xFFFFFF;
		for (int i = 0; i < 4; i++)
		{
			int count = this.counter.countEntity(i);
			toDisplay = "" + count;
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
		FontRenderer fontRender = Minecraft.getMinecraft().fontRendererObj;
		for (int i = 4; i < 8; i++)
		{
			int color = 0xFFFFFF;
			int count = this.counter.countEntity(i);
			String toDisplay = "" + count;
			int x = 70;

			if (count > 16) color = 0xAA0000;
			fontRender.drawStringWithShadow(this.passives[i] + toDisplay, x, i * 10 - 30, color);
			color = 0xFFFFFF;
		}
		
		if (this.staff)
		{
			int color = 0xFFFFFF;
			int count = this.counter.countEntity(17);
			if (count > 16) color = 0xAA0000;
			fontRender.drawStringWithShadow("Golems: " + count, 70, 50, color);
		}
	}

	/**
	 * Display hostile counter
	 */
	private void displayHostile()
	{
		FontRenderer fontRender = Minecraft.getMinecraft().fontRendererObj;
		int offset = 0;
		if (this.counterVisible > 0)
			offset = 50;
		if (this.staff)
			offset += 10;

		if (this.counter.getXP5())
		{
			fontRender.drawStringWithShadow("ShockerzXP5", 0, offset, 0xFFAA00);
		}
		else
		{
			this.counter.updateHostileBB();
			fontRender.drawStringWithShadow("Radius: " + this.counter.getHRadius(), 0, offset, 0xFFAA00);
		}

		int totalCount = 0;
		for (int i = 0; i < 4; i++)
		{
			int count = this.counter.countEntity(i + 8);
			totalCount += count;
			fontRender.drawStringWithShadow(this.hostiles[i] + count, 0, i * 10 + 10 + offset, 0xFFFFFF);
		}
		for (int i = 4; i < 8; i++) 
		{
			int count = this.counter.countEntity(i + 8);
			totalCount += count;
			if (this.hostileVisible > 1)
				fontRender.drawStringWithShadow(this.hostiles[i] + count, 90, i * 10 - 30 + offset, 0xFFFFFF);
		}
		int color = 0xFFFFFF;
		if (totalCount > 149) // if 150+ mobs, display in red.
			color = 0xAA0000;
		else
			this.playSoundCount = 100;
		
		if (this.counter.getXP5())
			fontRender.drawStringWithShadow("Total: " + totalCount, 70, offset, color);
		else
			fontRender.drawStringWithShadow("Total: " + totalCount, 60, offset, color);
	}

	/**
	 * Logs the message to the user
	 * @param message The message to log
	 */
	public static void logMessage(String message, boolean usePrefix)
	{
		if (usePrefix)
			message = "§8[§2MobCounter§8] §a" + message;
		ChatComponentText displayMessage = new ChatComponentText(message);
		displayMessage.setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}

	/**
	 * Logs the error message to the user
	 * @param message The error message to log
	 */
	public static void logError(String message)
	{
		ChatComponentText displayMessage = new ChatComponentText("§8[§4!§8] §c" + message + " §8[§4!§8]");
		displayMessage.setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.RED));
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}
}
