package io.github.kyzderp.mobcountmod;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;

public class MobCounter {

	private int radius;
	private boolean xp5;
	private AxisAlignedBB boundingBox;
	private int hRadius; //radius for hostiles
	private AxisAlignedBB hostileBB;

	public MobCounter(boolean isStaff)
	{
		this.xp5 = false;
		this.radius = 16;
		if (isStaff)
			this.hRadius = 25;
		else
			this.hRadius = 16;

		this.boundingBox = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		this.hostileBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
	}

	public int countEntity(int num)
	{
		Minecraft minecraft = Minecraft.getMinecraft();
		switch (num)
		{
		case 0: return minecraft.world.getEntitiesWithinAABB(EntityChicken.class, boundingBox).size();
		case 1: return minecraft.world.getEntitiesWithinAABB(EntityPig.class, boundingBox).size();
		case 2: return minecraft.world.getEntitiesWithinAABB(EntitySheep.class, boundingBox).size();
		case 3: return minecraft.world.getEntitiesWithinAABB(EntityCow.class, boundingBox).size() - this.countEntity(5);
		case 4: return minecraft.world.getEntitiesWithinAABB(EntityHorse.class, boundingBox).size();
		case 5: return minecraft.world.getEntitiesWithinAABB(EntityMooshroom.class, boundingBox).size();
		case 6: return minecraft.world.getEntitiesWithinAABB(EntityRabbit.class, boundingBox).size();
		case 7: return minecraft.world.getEntitiesWithinAABB(EntityWolf.class, boundingBox).size();

		case 8: return (minecraft.world.getEntitiesWithinAABB(EntityZombie.class, hostileBB).size() - this.countEntity(14));
		case 9: return minecraft.world.getEntitiesWithinAABB(EntityCaveSpider.class, hostileBB).size();
		case 10: return minecraft.world.getEntitiesWithinAABB(EntitySkeleton.class, hostileBB).size();
		case 11: return (minecraft.world.getEntitiesWithinAABB(EntitySpider.class, hostileBB).size() - this.countEntity(9));
		case 12: return minecraft.world.getEntitiesWithinAABB(EntityCreeper.class, hostileBB).size();
		case 13: return minecraft.world.getEntitiesWithinAABB(EntityWitch.class, hostileBB).size();
		case 14: return minecraft.world.getEntitiesWithinAABB(EntityPigZombie.class, hostileBB).size();
		case 15: return minecraft.world.getEntitiesWithinAABB(EntitySlime.class, hostileBB).size();
		case 16: return minecraft.world.getEntitiesWithinAABB(EntityGuardian.class, hostileBB).size();

		case 17: return minecraft.world.getEntitiesWithinAABB(EntityIronGolem.class, boundingBox).size();
		case 18: return minecraft.world.getEntitiesWithinAABB(EntitySnowman.class, boundingBox).size();
		case 20: return minecraft.world.getEntitiesWithinAABB(EntityPlayer.class, boundingBox).size() - 1;

		}
		return 0;
	}

	public void updateBB()
	{
		EntityPlayer player = Minecraft.getMinecraft().player;
		this.boundingBox = new AxisAlignedBB(player.posX - this.radius, player.posY - this.radius, player.posZ - this.radius, 
				player.posX + this.radius, player.posY + this.radius, player.posZ + this.radius);
	}

	public void updateHostileBB()
	{
		if (!this.xp5)
		{
			EntityPlayer player = Minecraft.getMinecraft().player;
			this.hostileBB = new AxisAlignedBB(player.posX - this.hRadius, player.posY - this.hRadius, player.posZ - this.hRadius, 
					player.posX + this.hRadius, player.posY + this.hRadius, player.posZ + this.hRadius);
		}
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

	public boolean getXP5() { return this.xp5; }

	public void setXP5(boolean isOn) 
	{ 
		this.xp5 = isOn;
		if (isOn)
			this.setXP5bounding();
	}

	private void setXP5bounding()
	{
		this.hostileBB = new AxisAlignedBB(5229, 5, -4700, 5250, 34, -4692);
	}
}
