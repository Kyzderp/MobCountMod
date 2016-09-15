package io.github.kyzderp.mobcountmod;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import org.lwjgl.input.Keyboard;

import com.mumfrey.liteloader.OutboundChatFilter;
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
public class LiteModMobCounter implements Tickable, OutboundChatFilter
{
	private static final boolean staff = false;
	private static final boolean rebel = false;
	private static KeyBinding counterKeyBinding;
	private static KeyBinding hostileKeyBinding;

	private final Commands commands = new Commands(this);
	private final MobCounter counter = new MobCounter(isStaff());
	private String[] toMessage;
	private boolean notifyFac;
	private String soundString;

	private int counterVisible = 0; // 0 - not visible, 1 - compact, 2 - expanded
	private int hostileVisible = 0;
	private int playSoundCount = 0; // counts up so sound plays once per sec
	private int sendMsgCount = 0; // counts up so message sends every 5 minutes

	private String[] passives = {"Chickens: ", "Pigs: ", "Sheep: ", "Cows: ", "Horses: ", "Mooshrooms: ", "Rabbits: ", "Wolves: "};
	private String[] hostiles = {"Zombies: ", "CaveSpiders: ", "Skeletons: ", "Spiders: ", 
			"Creepers: ", "Witches: ", "Pigmen: ", "Slimes: ", "Guardians: "};


	public LiteModMobCounter() {}

	@Override
	public String getName()
	{
		String toReturn = "Mob Counter";
		if (LiteModMobCounter.rebel)
			toReturn += " - RebelAngel";
		else if (LiteModMobCounter.isStaff())
			toReturn += " - Staff";
		return toReturn;
	}

	@Override
	public String getVersion() { return "1.3.1"; }

	@Override
	public void init(File configPath)
	{
		this.setNotifyFac(false);
		this.setSound("minecraft:block.note.bass");

		counterKeyBinding = new KeyBinding("key.counter.toggle", Keyboard.KEY_P, "key.categories.litemods");
		hostileKeyBinding = new KeyBinding("key.counter.togglehostile", Keyboard.KEY_O, "key.categories.litemods");

		LiteLoader.getInput().registerKeyBinding(counterKeyBinding);
		LiteLoader.getInput().registerKeyBinding(hostileKeyBinding);

		if (LiteModMobCounter.rebel) { // EEEEEEEEEEEEEEEbel
			this.setNotifyFac(true);
			this.hostileVisible = 1;
			this.getCounter().setXP5(true);
		}
	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {}

	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock)
	{
		if (inGame) {
			this.hostileLimit();
		}

		if (inGame && minecraft.currentScreen == null && Minecraft.isGuiEnabled())
		{
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
	public boolean onSendChatMessage(String message)
	{
		while (message.matches(".*  .*"))
			message = message.replaceAll("  ", " ");
		
		if (message.split(" ")[0].equalsIgnoreCase("/counter"))
		{
			this.commands.handleCommand(message);
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
		int totalCount = 0;
		for (int i = 0; i < 8; i++)
			totalCount += this.getCounter().countEntity(i + 8);
		if (this.playSoundCount != 0)
			this.playSoundCount++;
		if (this.sendMsgCount != 0)
			this.sendMsgCount++;

		if (totalCount > 149)
		{
			if (this.playSoundCount == 0)
			{
				SoundEvent soundEvent = SoundEvent.REGISTRY.getObject(new ResourceLocation(this.soundString));
				if (soundEvent != null)
				{
					ISound sound = PositionedSoundRecord.getMasterRecord(soundEvent, 1.0F);
	                Minecraft.getMinecraft().getSoundHandler().playSound(sound);
				}
				this.playSoundCount++;
			}
			if (this.playSoundCount > 100)
				this.playSoundCount = -1;

			if (this.sendMsgCount == 0)
			{
				if (this.isNotifyFac())
					Minecraft.getMinecraft().thePlayer.sendChatMessage("/ch qm f Automated Message: "
							+ totalCount + " mobz. Kill pl0x.");
				if (this.getToMessage() != null && this.getToMessage().length > 0)
				{
					for (String player : this.getToMessage())
						Minecraft.getMinecraft().thePlayer.sendChatMessage("/m " + player 
								+ " Automated Message: " + totalCount + " mobz. Kill pl0x.");
				}
				this.sendMsgCount++;
			}
			if (this.sendMsgCount > 10000)
				this.sendMsgCount = -1;
		}
	} // hostileLimit

	/**
	 * Adjust passive counter's settings according to key input
	 */
	private void passiveKey()
	{
		if (LiteModMobCounter.counterKeyBinding.isPressed())
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_UP))
				this.getCounter().increaseRadius(isStaff());
			else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
				this.getCounter().decreaseRadius();
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
				this.getCounter().increaseHRadius(isStaff());
			else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
				this.getCounter().decreaseHRadius();
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
		this.getCounter().updateBB();
		fontRender.drawStringWithShadow("Radius: " + this.getCounter().getRadius(), 5, 5, 0xFFAA00);
		if (isStaff())
		{
			fontRender.drawStringWithShadow("Players: " + this.getCounter().countEntity(20), 65, 5, 0xFFFFFF);
			int color = 0xFFFFFF;
			int count = this.getCounter().countEntity(18);
			if (count > 16) color = 0xAA0000;
			fontRender.drawStringWithShadow("Snowmen: " + count, 5, 55, color);
		}
		String toDisplay;

		int color = 0xFFFFFF;
		for (int i = 0; i < 4; i++)
		{
			int count = this.getCounter().countEntity(i);
			toDisplay = "" + count;
			if (count > 16) color = 0xAA0000;
			fontRender.drawStringWithShadow(this.passives[i] + toDisplay, 5, i * 10 + 15, color);
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
			int count = this.getCounter().countEntity(i);
			String toDisplay = "" + count;

			if (count > 16) color = 0xAA0000;
			fontRender.drawStringWithShadow(this.passives[i] + toDisplay, 75, i * 10 - 25, color);
			color = 0xFFFFFF;
		}
		
		if (LiteModMobCounter.isStaff())
		{
			int color = 0xFFFFFF;
			int count = this.getCounter().countEntity(17);
			if (count > 16) color = 0xAA0000;
			fontRender.drawStringWithShadow("Golems: " + count, 75, 55, color);
		}
	}

	/**
	 * Display hostile counter
	 */
	private void displayHostile()
	{
		FontRenderer fontRender = Minecraft.getMinecraft().fontRendererObj;
		int offset = 5;
		if (this.counterVisible > 0)
			offset += 50;
		if (LiteModMobCounter.isStaff())
			offset += 10;

		if (this.getCounter().getXP5())
		{
			fontRender.drawStringWithShadow("ShockerzXP5", 5, offset, 0xFFAA00);
		}
		else
		{
			this.getCounter().updateHostileBB();
			fontRender.drawStringWithShadow("Radius: " + this.getCounter().getHRadius(), 5, offset, 0xFFAA00);
		}

		int totalCount = 0;
		for (int i = 0; i < 5; i++)
		{
			int count = this.getCounter().countEntity(i + 8);
			totalCount += count;
			fontRender.drawStringWithShadow(this.hostiles[i] + count, 5, i * 10 + 10 + offset, 0xFFFFFF);
		}
		for (int i = 5; i < 9; i++) 
		{
			int count = this.getCounter().countEntity(i + 8);
			totalCount += count;
			if (this.hostileVisible > 1)
				fontRender.drawStringWithShadow(this.hostiles[i] + count, 95, i * 10 - 40 + offset, 0xFFFFFF);
		}
		int color = 0xFFFFFF;
		if (totalCount > 149) // if 150+ mobs, display in red.
			color = 0xAA0000;
		else
			this.playSoundCount = 100;
		
		if (this.getCounter().getXP5())
			fontRender.drawStringWithShadow("Total: " + totalCount, 75, offset, color);
		else
			fontRender.drawStringWithShadow("Total: " + totalCount, 65, offset, color);
	}

	
	/** Getters and Setters */
	public String[] getToMessage() { return toMessage; }
	public void setToMessage(String[] toMessage) { this.toMessage = toMessage; }
	public String getSound() { return soundString; }
	public void setSound(String sound) { this.soundString = sound; }
	public boolean isNotifyFac() { return notifyFac; }
	public void setNotifyFac(boolean notifyFac) { this.notifyFac = notifyFac; }
	public static boolean isStaff() { return staff; }
	public MobCounter getCounter() { return counter; }

	/**
	 * Logs the message to the user
	 * @param message The message to log
	 */
	public static void logMessage(String message, boolean usePrefix)
	{
		if (usePrefix)
			message = "\u00A78[\u00A72MobCounter\u00A78] \u00A7a" + message;
		TextComponentString displayMessage = new TextComponentString(message);
		displayMessage.setStyle((new Style()).setColor(TextFormatting.GREEN));
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}

	/**
	 * Logs the error message to the user
	 * @param message The error message to log
	 */
	public static void logError(String message)
	{
		TextComponentString displayMessage = new TextComponentString("\u00A78[\u00A74!\u00A78] \u00A7c" + message + " \u00A78[\u00A74!\u00A78]");
		displayMessage.setStyle((new Style()).setColor(TextFormatting.RED));
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(displayMessage);
	}
}
