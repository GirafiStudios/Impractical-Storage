package com.girafi.impstorage.block;

import com.girafi.impstorage.block.tile.TileControllerInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class BlockControllerInterface extends BaseEntityBlock {
    public static final EnumProperty<InterfaceState> STATE = EnumProperty.create("state", InterfaceState.class);

    public BlockControllerInterface() {
        super(Block.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).requiresCorrectToolForDrops().strength(2.0F, 2.0F).sound(SoundType.METAL).pushReaction(PushReaction.BLOCK));

        registerDefaultState(this.getStateDefinition().any().setValue(STATE, InterfaceState.INACTIVE));
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new TileControllerInterface(pos, state);
    }

    public static enum InterfaceState implements StringRepresentable {
        INACTIVE("inactive"),
        ACTIVE("active"),
        ERROR("error");

        private final String name;
        private InterfaceState(String name) {
            this.name = name;
        }

        @Override
        @Nonnull
        public String getSerializedName() {
            return name;
        }
    }
}