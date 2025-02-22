package com.girafi.impstorage.block;

import com.girafi.impstorage.lib.ModInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class CrateBlock extends Block {
    private final int blockStorage;
    private final int itemStorage;

    public CrateBlock(int itemStorage) {
        this(0, itemStorage);
    }

    public CrateBlock(int blockStorage, int itemStorage) {
        super(Block.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 2.0F).sound(SoundType.WOOD));
        this.blockStorage = blockStorage;
        this.itemStorage = itemStorage;
    }

    public int getBlockStorage() {
        return this.blockStorage;
    }

    public int getItemStorage() {
        return this.itemStorage;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter getter, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, getter, tooltip, tooltipFlag);

        //TODO Test
        if (this.getBlockStorage() > 0) tooltip.add(Component.translatable(ModInfo.ID + ".tooltip.capacity.block").append(":").append(String.valueOf(this.getBlockStorage())));
        if (this.getItemStorage() > 0) tooltip.add(Component.translatable(ModInfo.ID + ".tooltip.capacity.item").append(":").append(String.valueOf(this.getItemStorage())));
    }
}