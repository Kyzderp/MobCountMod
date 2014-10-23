package com.kyzeragon.mobcountmod;

/**
 * Configuration screen for Mob Counter mod
 * 
 * @author Kyzeragon
 */

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;

public class MobCounterConfigScreen extends GuiScreen {

	public MobCounterConfigScreen()
	{
		super();
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		this.drawDefaultBackground();
		this.buttonList.add(new GuiButton(0, this.width/2 + 10, this.height/4 - 10, 100, 20, "Compact"));
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) //no idea what the params are for :D
	{
		this.drawDefaultBackground();
		this.drawCenteredString(fontRendererObj, "Mob Counter Options", this.width/2, this.height/2 - 90, 0xFFFFFF);
		this.drawCenteredString(fontRendererObj, "Passive", this.width/2 - 102, this.height/2 - 60, 0xFFFFFF);
		super.drawScreen(par1, par2, par3);
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
	{
		switch (button.id)
		{
		case 0: break;
		}
		this.updateScreen();
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
	}
}
