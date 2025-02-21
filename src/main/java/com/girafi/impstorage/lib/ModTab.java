package com.girafi.impstorage.lib;

import com.girafi.impstorage.init.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

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
