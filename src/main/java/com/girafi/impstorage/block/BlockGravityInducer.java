package com.girafi.impstorage.block;

import com.girafi.impstorage.lib.ModTab;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import javax.annotation.Nonnull;

public class BlockGravityInducer extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BlockGravityInducer() {
        super(Material.IRON);

        setHardness(2F);
        setResistance(2F);

        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public void onBlockPlacedBy(Level level, BlockPos pos, BlockState state, EntityLivingBase placer, ItemStack stack) {
        level.setBlockState(pos, state.withProperty(FACING, Direction.getDirectionFromEntityLiving(pos, placer)), 2);
    }

    @Override
    public void onBlockAdded(Level level, BlockPos pos, BlockState state) {
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
}
