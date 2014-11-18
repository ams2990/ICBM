package icbm.content.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import icbm.Reference;
import icbm.content.prefab.item.ItemICBMBase;

public class ItemPoisonPowder extends ItemICBMBase
{
    public ItemPoisonPowder()
    {
        super("poisonPowder");
    }

    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        super.registerIcons(iconRegister);
        
        // Icon for base item.
        this.itemIcon = iconRegister.registerIcon(Reference.PREFIX + "poisonPowder");
    }
}