package com.girafi.impstorage.block;

import com.girafi.impstorage.block.tile.TileController;
import com.girafi.impstorage.client.screen.ControllerScreen;
import com.girafi.impstorage.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockController extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BlockController() {
        super(Block.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).requiresCorrectToolForDrops().strength(2.0F, 2.0F).sound(SoundType.METAL).pushReaction(PushReaction.BLOCK).isRedstoneConductor(ModBlocks::always));

        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public void onBlockPlacedBy(Level level, BlockPos pos, BlockState state, EntityLivingBase placer, ItemStack stack) {
        Direction facing = placer.getHorizontalFacing();
        level.setBlock(pos, state.setValue(FACING, facing), 2);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileController) {
            ((TileController) blockEntity).initialize(facing);
        }
    }

    @Override
    public boolean onBlockActivated(Level level, BlockPos pos, BlockState state, EntityPlayer playerIn, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileController) {
                Minecraft.getInstance().setScreen(new ControllerScreen((TileController) blockEntity)); //TODO Test
            }
        }
        return true;
    }

    @Override
    public void breakBlock(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                ((TileController) blockEntity).onBlockBreak();
            }
        }
        super.breakBlock(level, pos, state);
    }

    @Override
    public boolean hasAnalogOutputSignal(@Nonnull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileController) {
            return ((TileController) blockEntity).getRedstoneLevel();
        } else {
            return 0;
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
    @Nullable
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new TileController(pos, state);
    }
}
