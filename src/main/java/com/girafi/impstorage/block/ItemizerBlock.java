package com.girafi.impstorage.block;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ItemizerBlock extends DirectionalBlock {

    public ItemizerBlock() {
        super(Block.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).requiresCorrectToolForDrops().strength(2.0F, 2.0F).sound(SoundType.METAL).pushReaction(PushReaction.BLOCK));

        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }
    @Nullable
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void onPlace(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state2, boolean p_60570_) {
        itemizeBlocks(level, pos, state);
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Block block, @Nonnull BlockPos neighbor, boolean update) {
        itemizeBlocks(level, pos, state);
    }

    private void itemizeBlocks(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide()) {
            pos = pos.relative(state.getValue(FACING)); //TODO Test
            BlockState s = level.getBlockState(pos);
            if (!state.isAir()) {
                ItemStack stack = new ItemStack(s.getBlock());

                ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
                entity.setDeltaMovement(0, 0, 0);
                level.addFreshEntity(entity);

                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
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