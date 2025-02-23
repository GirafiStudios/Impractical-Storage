package com.girafi.impstorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class GravityInducerBlock extends DirectionalBlock {

    public GravityInducerBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).requiresCorrectToolForDrops().strength(2.0F, 2.0F).sound(SoundType.METAL));

        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity livingEntity, @Nonnull ItemStack stack) {
        super.setPlacedBy(level, pos, state, livingEntity, stack);
        dropBlocks(level, pos, state);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighbor, boolean update) {
        dropBlocks(level, pos, state);
    }

    private void dropBlocks(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide()) {
            pos = pos.relative(state.getValue(FACING));
            if (level.getBlockState(pos.below()).isAir()) {
                BlockState s = level.getBlockState(pos);
                if (!level.getBlockState(pos).isAir()) {
                    FallingBlockEntity.fall(level, pos, s);
                }
            }
        }
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
        return state.setValue(FACING, direction.rotate(state.getValue(FACING)));
    }

    @Override
    @Nonnull
    public BlockState mirror(@Nonnull BlockState state, @Nonnull Mirror mirror) {
        return state.setValue(FACING, mirror.rotation().rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> states) {
        states.add(FACING);
    }
}