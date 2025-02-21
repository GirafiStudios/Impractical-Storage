package me.dmillerw.impstorage.block.item;

import me.dmillerw.impstorage.block.BlockCrate;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author dmillerw
 */
public class ItemBlockCrate extends ItemBlock {

    public ItemBlockCrate(Block block) {
        super(block);

        setHasSubtypes(true);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
        BlockCrate.EnumType type = BlockCrate.EnumType.fromMetadata(stack.getMetadata());
        if (type.getBlockStorage() > 0) tooltip.add(I18n.translateToLocal("tooltip.capacity.block") + ": " + type.getBlockStorage());
        if (type.getItemStorage() > 0) tooltip.add(I18n.translateToLocal("tooltip.capacity.item") + ": " + type.getItemStorage());
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName() + "." + BlockCrate.EnumType.fromMetadata(stack.getMetadata()).getName();
    }
}
