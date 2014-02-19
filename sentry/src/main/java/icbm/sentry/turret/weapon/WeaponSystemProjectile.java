package icbm.sentry.turret.weapon;

import icbm.api.sentry.IAmmunition;
import icbm.sentry.ICBMSentry;
import icbm.sentry.turret.Sentry;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import universalelectricity.api.vector.Vector3;

public class WeaponSystemProjectile extends WeaponSystem
{
    public WeaponSystemProjectile(Sentry sentry)
    {
        super(sentry);
    }

    @Override
    public void fire (Vector3 target)
    {
        Vector3 barrel = new Vector3();
        barrel.add(this.sentry.getAimOffset());
        barrel.add(this.sentry.getCenterOffset());
        barrel.rotate(this.sentry.host.yaw(), this.sentry.host.pitch());
        barrel.add(new Vector3(sentry.host.x(), sentry.host.y(), sentry.host.z()));

        ICBMSentry.proxy.renderBeam(this.sentry.world(), barrel, target, 1F, 1F, 1F, 10);
        //TODO Damage target

    }


    public boolean isAmmo(ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof IAmmunition;
    }

    /** Used to consume ammo or check if ammo can be consumed
     * 
     * @param count - number of items to consume
     * @param doConsume - true items will be consumed
     * @return true if all rounds were consumed */
    public boolean consumeAmmo(int count, boolean doConsume)
    {
        if (count > 0 && sentry.getHost() instanceof IInventory)
        {
            //TODO add a way to restrict this to a set range of slots
            IInventory inv = ((IInventory) sentry.host);
            //0-4 are upgrade slots for the sentry, 5-8 are ammo slots
            int consumeCount = 0;
            for (int slot = 5; slot < inv.getSizeInventory(); slot++)
            {
                ItemStack stack = inv.getStackInSlot(slot);
                if (isAmmo(stack))
                {
                    if (stack.stackSize >= count)
                    {
                        if (doConsume)
                            stack.stackSize -= count;
                        return true;
                    }
                    else
                    {

                    }
                }
            }
        }
        return false;
    }
}
