package com.girafi.impstorage.block;

import com.girafi.impstorage.init.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PhantomBlock extends Block {
    public static final EnumProperty<EnumType> TYPE = EnumProperty.create("type", EnumType.class);

    public PhantomBlock() {
        super(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.8F).noLootTable().noCollission().noOcclusion().isValidSpawn(ModBlocks::never).noParticlesOnBreak().pushReaction(PushReaction.BLOCK));

        this.registerDefaultState(this.getStateDefinition().any().setValue(TYPE, EnumType.BLOCK));
    }

    //TODO Hide bounding-box unless holding item

    @Override
    @Nonnull
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter blockGetter, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, blockGetter, tooltip, tooltipFlag);
        tooltip.add(Component.translatable("tooltip.phantom.type." + TYPE.getName())); //TODO Test
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> states) {
        states.add(TYPE);
    }

    public enum EnumType implements StringRepresentable {
        BLOCK("block"),
        COLUMN("column");

        private final String name;

        private EnumType(String name) {
            this.name = name;
        }

        @Override
        @Nonnull
        public String getSerializedName() {
            return this.name;
        }
    }
}