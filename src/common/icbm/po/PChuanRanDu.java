package icbm.po;

import icbm.ICBM;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.DamageSource;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPig;
import net.minecraft.src.EntityPigZombie;
import net.minecraft.src.EntityVillager;
import net.minecraft.src.EntityZombie;
import universalelectricity.prefab.potion.CustomPotion;

public class PChuanRanDu extends CustomPotion 
{
    public static final PChuanRanDu INSTANCE = new PChuanRanDu(22, false, 5149489, "Virus");
	
	public PChuanRanDu(int id, boolean isBadEffect, int color, String name)
	{
		super(id, isBadEffect, color, name);
		this.setIconIndex(6, 0);
	}

	@Override
	public void performEffect(EntityLiving par1EntityLiving, int amplifier)
    {
		if(!(par1EntityLiving instanceof EntityZombie) && !(par1EntityLiving instanceof EntityPigZombie))
    	{
        	par1EntityLiving.attackEntityFrom(DamageSource.magic, 1);
    	}
    	
    	//Poison things around it
    	int r = 13;
    	AxisAlignedBB entitySurroundings = AxisAlignedBB.getBoundingBox(par1EntityLiving.posX-r, par1EntityLiving.posY-r, par1EntityLiving.posZ-r, par1EntityLiving.posX+r, par1EntityLiving.posY+r, par1EntityLiving.posZ+r);
    	EntityLiving nearestEntity = (EntityLiving)par1EntityLiving.worldObj.findNearestEntityWithinAABB(EntityLiving.class, entitySurroundings, par1EntityLiving);
    	
    	if(nearestEntity != null)
    	{
    		if(nearestEntity instanceof EntityPig)
            {
        		EntityPigZombie var2 = new EntityPigZombie(nearestEntity.worldObj);
                var2.setLocationAndAngles(nearestEntity.posX, nearestEntity.posY, nearestEntity.posZ, nearestEntity.rotationYaw, nearestEntity.rotationPitch);
                nearestEntity.worldObj.spawnEntityInWorld(var2);
                nearestEntity.setDead();
            }
        	else if(nearestEntity instanceof EntityVillager)
            {
        		EntityZombie var2 = new EntityZombie(nearestEntity.worldObj);
                var2.setLocationAndAngles(nearestEntity.posX, nearestEntity.posY, nearestEntity.posZ, nearestEntity.rotationYaw, nearestEntity.rotationPitch);
                nearestEntity.worldObj.spawnEntityInWorld(var2);
                nearestEntity.setDead();
            }
    		
	        ICBM.DU_CHUAN_RAN.poisonEntity(nearestEntity);
    	}
    }
	
	@Override
	public boolean isReady(int duration, int amplifier)
    {
		if(duration % (20 * 2) == 0)
		{
			return true;
		}
		
		return false;
    }
}
