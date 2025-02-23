package com.girafi.impstorage.block;

import com.girafi.impstorage.block.blockentity.ItemBlockEntity;
import com.girafi.impstorage.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemBlockBlock extends BaseEntityBlock {

    public ItemBlockBlock() {
        super(Block.Properties.of().strength(-1.0F, 3600000.8F).noLootTable().noOcclusion().isValidSpawn(ModBlocks::never).pushReaction(PushReaction.BLOCK));
    }

    @Override
    @Nonnull
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new ItemBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity livingEntity, @Nonnull ItemStack stack) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ItemBlockEntity) {
                ((ItemBlockEntity) blockEntity).updateItemBlock(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Block block, @Nonnull BlockPos neighBorPos, boolean b) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ItemBlockEntity) {
            ((ItemBlockEntity) blockEntity).updateItemBlock(ItemStack.EMPTY);
        }
    }

    @Override
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ItemBlockEntity) {
                ((ItemBlockEntity) blockEntity).getDrop();
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ItemBlockEntity) {
            return ((ItemBlockEntity) blockEntity).stack;
        }

        return super.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    @Nonnull
    public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (!level.isClientSide) {
            if (player.isCrouching()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof ItemBlockEntity) {
                    ItemStack drop = ((ItemBlockEntity) blockEntity).getDrop();
                    if (!drop.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), drop);
                    }
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}