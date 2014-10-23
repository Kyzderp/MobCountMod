package com.kyzeragon.mobcountmod;

import java.io.File;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.item.EntityTNTPrimed;

import org.lwjgl.input.Keyboard;

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
public class LiteModMobCounter implements Tickable
{
	private boolean staff = false;
	private boolean showChildCounts = true;
	
	private MobCounterConfigScreen configScreen = new MobCounterConfigScreen();
	private MobCounter counter = new MobCounter();
	private static KeyBinding counterKeyBinding;
	private static KeyBinding hostileKeyBinding;
	private static KeyBinding optionsKeyBinding;


	private int counterVisible = 1; // 0 - not visible, 1 - compact, 2 - expanded
	private int hostileVisible = 0;

	public LiteModMobCounter() {}

	@Override
	public String getName()
	{
		return "Mob Counter";
	}

	@Override
	public String getVersion()
	{
		return "1.0.0";
	}

	@Override
	public void init(File configPath)
	{
		counterKeyBinding = new KeyBinding("key.counter.toggle", Keyboard.KEY_P, "key.categories.litemods");
		hostileKeyBinding = new KeyBinding("key.counter.togglehostile", Keyboard.KEY_O, "key.categories.litemods");
		optionsKeyBinding = new KeyBinding("key.counter.options", Keyboard.KEY_SEMICOLON, "key.categories.litemods");
		
		LiteLoader.getInput().registerKeyBinding(counterKeyBinding);
		LiteLoader.getInput().registerKeyBinding(hostileKeyBinding);
	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {}

	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock)
	{
		if (inGame && minecraft.currentScreen != null && minecraft.currentScreen.equals(configScreen))
		{
			configScreen.updateScreen();
		}
		if (inGame && minecraft.currentScreen == null && Minecraft.isGuiEnabled())
		{
			if (LiteModMobCounter.optionsKeyBinding.isPressed())
			{
				minecraft.displayGuiScreen(this.configScreen);
			}
			FontRenderer fontRender = minecraft.fontRenderer;
			///// Passives /////
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

			if (this.counterVisible > 0) // compact
			{
				this.counter.updateBB();
				fontRender.drawStringWithShadow("Radius: " + this.counter.getRadius(), 0, 0, 0xFFAA00);
				if (staff) 
					fontRender.drawStringWithShadow("Players: " + this.counter.countEntity(16, true), 60, 0, 0xFFFFFF);
				String toDisplay;

				String[] mobs = {"Chickens: ", "Pigs: ", "Sheep: ", "Cows: ", "Horses: ", "Mooshrooms: ", "Ocelots: ", "Wolves: "};
				int color = 0xFFFFFF;
				for (int i = 0; i < 4; i++)
				{
					int count = this.counter.countEntity(i, true) + this.counter.countEntity(i, false);
					toDisplay = "" + count;
					if (this.showChildCounts)
						toDisplay = this.counter.countEntity(i, false) + "/" + this.counter.countEntity(i, true);
					if (count > 16) color = 0xAA0000;
					fontRender.drawStringWithShadow(mobs[i] + toDisplay, 0, i * 10 + 10, color);
					color = 0xFFFFFF;
				}
				if (this.counterVisible > 1) // expanded
				{
					for (int i = 4; i < 8; i++)
					{
						int count = this.counter.countEntity(i, true) + this.counter.countEntity(i, false);
						toDisplay = "" + count;
						int x = 70;
						if (this.showChildCounts)
						{
							toDisplay = this.counter.countEntity(i, false) + "/" + this.counter.countEntity(i, true);
							x = 80;
						}
						if (count > 16) color = 0xAA0000;
						fontRender.drawStringWithShadow(mobs[i] + toDisplay, x, i * 10 - 30, color);
						color = 0xFFFFFF;
					}
				}
			}

			///// Hostiles /////
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

			if (this.hostileVisible > 0) // compact
			{
				int offset = 0;
				if (this.counterVisible > 0)
					offset = 50;

				this.counter.updateHostileBB();
				fontRender.drawStringWithShadow("Radius: " + this.counter.getHRadius(), 0, offset, 0xFFAA00);
				String[] mobs = {"Zombies: ", "CaveSpiders: ", "Skeletons: ", "Spiders: ", 
						"Creepers: ", "Witches: ", "Pigmen: ", "Slimes: "};
				int totalCount = 0;
				for (int i = 0; i < 4; i++)
				{
					int count = this.counter.countEntity(i + 8, true);
					totalCount += count;
					fontRender.drawStringWithShadow(mobs[i] + count, 0, i * 10 + 10 + offset, 0xFFFFFF);
				}
				for (int i = 4; i < 8; i++) 
				{
					int count = this.counter.countEntity(i + 8, true);
					totalCount += count;
					if (this.hostileVisible > 1)
						fontRender.drawStringWithShadow(mobs[i] + count, 90, i * 10 - 30 + offset, 0xFFFFFF);
				}
				int color = 0xFFFFFF;
				if (totalCount > 64)
					color = 0xAA0000;
				fontRender.drawStringWithShadow("Total: " + totalCount, 60, offset, color);
			}

		}
	}
}
