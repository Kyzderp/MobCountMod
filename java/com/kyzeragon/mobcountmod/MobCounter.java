package com.kyzeragon.mobcountmod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class MobCounter {

	private int radius;
	private AxisAlignedBB boundingBox;
	private int hRadius; //radius for hostiles
	private AxisAlignedBB hostileBB;

	public MobCounter(boolean isStaff)
	{
		this.radius = 16;
		if (isStaff)
			this.hRadius = 25;
		else
			this.hRadius = 16;
		
		this.boundingBox = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
		this.hostileBB = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
	}

	public int countEntity(int num, boolean adult)
	{
		Minecraft minecraft = Minecraft.getMinecraft();
		List result = new ArrayList();
		switch (num)
		{
		case 0: result = minecraft.theWorld.getEntitiesWithinAABB(EntityChicken.class, boundingBox); break;
		case 1: result = minecraft.theWorld.getEntitiesWithinAABB(EntityPig.class, boundingBox); break;
		case 2: result = minecraft.theWorld.getEntitiesWithinAABB(EntitySheep.class, boundingBox); break;
		case 3: result = minecraft.theWorld.getEntitiesWithinAABB(EntityCow.class, boundingBox); break;
		case 4: result = minecraft.theWorld.getEntitiesWithinAABB(EntityHorse.class, boundingBox); break;
		case 5: result = minecraft.theWorld.getEntitiesWithinAABB(EntityMooshroom.class, boundingBox); break;
		case 6: result = minecraft.theWorld.getEntitiesWithinAABB(EntityOcelot.class, boundingBox); break;
		case 7: result = minecraft.theWorld.getEntitiesWithinAABB(EntityWolf.class, boundingBox); break;

		case 8: return (minecraft.theWorld.getEntitiesWithinAABB(EntityZombie.class, hostileBB).size() - this.countEntity(14, true));
		case 9: return minecraft.theWorld.getEntitiesWithinAABB(EntityCaveSpider.class, hostileBB).size();
		case 10: return minecraft.theWorld.getEntitiesWithinAABB(EntitySkeleton.class, hostileBB).size();
		case 11: return (minecraft.theWorld.getEntitiesWithinAABB(EntitySpider.class, hostileBB).size() - this.countEntity(9, true));
		case 12: return minecraft.theWorld.getEntitiesWithinAABB(EntityCreeper.class, hostileBB).size();
		case 13: return minecraft.theWorld.getEntitiesWithinAABB(EntityWitch.class, hostileBB).size();
		case 14: return minecraft.theWorld.getEntitiesWithinAABB(EntityPigZombie.class, hostileBB).size();
		case 15: return minecraft.theWorld.getEntitiesWithinAABB(EntitySlime.class, hostileBB).size();
		
		case 16: return minecraft.theWorld.getEntitiesWithinAABB(EntityPlayer.class, boundingBox).size() - 1;
		}
		if (adult)
		{
			return result.size() - this.countEntity(num, false);
		} else {
			int count = 0;
			for (Object mob: result)
			{
				if (((EntityAgeable)mob).isChild())
					count++;
			}
			return count;
		}
	}
	
	public void updateBB()
	{
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		this.boundingBox.setBounds(player.posX - this.radius, player.posY - this.radius, player.posZ - this.radius, 
				player.posX + this.radius, player.posY + this.radius, player.posZ + this.radius);
	}
	
	public void updateHostileBB()
	{
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		this.hostileBB.setBounds(player.posX - this.hRadius, player.posY - this.hRadius, player.posZ - this.hRadius, 
				player.posX + this.hRadius, player.posY + this.hRadius, player.posZ + this.hRadius);
	}
	
	public int getRadius() 
	{
		return this.radius;
	}

	public void increaseRadius(boolean staff)
	{
		if (staff)
		{
			if (this.radius < 100)
				this.radius++;
		} else {
			if (this.radius < 16)
				this.radius++;
		}
	}

	public void decreaseRadius()
	{
		if (this.radius > 0)
			this.radius--;
	}
	
	public int getHRadius()
	{
		return this.hRadius;
	}
	
	public void increaseHRadius(boolean staff)
	{
		if (staff)
		{
			if (this.hRadius < 100)
				this.hRadius++;
		} else {
			if (this.hRadius < 16)
				this.hRadius++;
		}
	}
	
	public void decreaseHRadius()
	{
		if (this.hRadius > 0)
			this.hRadius--;
	}

}
