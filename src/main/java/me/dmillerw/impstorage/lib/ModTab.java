package me.dmillerw.impstorage.lib;

import me.dmillerw.impstorage.block.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

/**
 * @author dmillerw
 */
public class ModTab extends CreativeTabs {

    public static final ModTab TAB = new ModTab();

    public ModTab() {
        super(ModInfo.ID);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(ModBlocks.crate);
    }
}
