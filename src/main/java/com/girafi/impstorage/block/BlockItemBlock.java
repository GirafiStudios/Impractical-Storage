package com.girafi.impstorage.block;

import com.girafi.impstorage.block.property.UnlistedItemStack;
import com.girafi.impstorage.block.tile.TileItemBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockItemBlock extends BaseEntityBlock {
    public static final UnlistedItemStack ITEM = new UnlistedItemStack("item");//TODO. NOT POSSIBLE ANYMORE, AS ITEMSTACK IS NOT COMPARABLE. Probably just store as NBT, don't need to be a state.

    public BlockItemBlock() {
        super(Material.ROCK);

        setBlockUnbreakable();
        setResistance(100F);
    }


    @Override
    public void setPlacedBy(Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity livingEntity, @Nonnull ItemStack stack) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null && blockEntity instanceof TileItemBlock) {
                ((TileItemBlock) blockEntity).updateItemBlock(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Block block, @Nonnull BlockPos neighBorPos, boolean b) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null && blockEntity instanceof TileItemBlock) {
            ((TileItemBlock) blockEntity).updateItemBlock(ItemStack.EMPTY);
        }
    }

    @Override
    public void breakBlock(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null && blockEntity instanceof TileItemBlock) {
                ((TileItemBlock) blockEntity).getDrop();
            }
        }
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, Level world, BlockPos pos, EntityPlayer player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null && blockEntity instanceof TileItemBlock) {
            return ((TileItemBlock) blockEntity).item;
        }

        return super.getPickBlock(state, target, world, pos, player);
    }

    @Override
    public boolean onBlockActivated(Level level, BlockPos pos, BlockState state, Player playerIn, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!level.isClientSide) {
            if (playerIn.isCrouching()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof TileItemBlock) {
                    ItemStack drop = ((TileItemBlock) blockEntity).getDrop();
                    if (!drop.isEmpty()) {
                        InventoryHelper.spawnItemStack(level, pos.getX(), pos.getY(), pos.getZ(), drop);
                    }
                    level.setBlockToAir(pos);
                }
                return true;
            } else {
                return false;
            }
        } else {
            return playerIn.isCrouching();
        }
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return true;
    }

    @Override
    public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new TileItemBlock(pos, state);
    }
}