package me.dmillerw.impstorage.block.item;

import me.dmillerw.impstorage.block.BlockPhantom;
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
public class ItemBlockPhantom extends ItemBlock {

    public ItemBlockPhantom(Block block) {
        super(block);

        setHasSubtypes(true);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
        BlockPhantom.EnumType type = BlockPhantom.EnumType.fromMetadata(stack.getMetadata());
        tooltip.add(I18n.translateToLocal("tooltip.phantom.type." + type.getName()));
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName() + "." + BlockPhantom.EnumType.fromMetadata(stack.getMetadata()).getName();
    }
}
