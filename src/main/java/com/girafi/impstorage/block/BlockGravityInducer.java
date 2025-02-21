package com.girafi.impstorage.block;

import com.girafi.impstorage.lib.ModInfo;
import com.girafi.impstorage.lib.ModTab;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.Direction;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class BlockGravityInducer extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BlockGravityInducer() {
        super(Material.IRON);

        setHardness(2F);
        setResistance(2F);

        setCreativeTab(ModTab.TAB);

        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)), 2);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        dropBlocks(worldIn, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        dropBlocks(worldIn, pos, state);
    }

    private void dropBlocks(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            pos = pos.offset(state.getValue(FACING));
            if (worldIn.isAirBlock(pos.down())) {
                IBlockState s = worldIn.getBlockState(pos);
                if (!worldIn.isAirBlock(pos)) {
                    EntityFallingBlock entity = new EntityFallingBlock(worldIn, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, s);
                    worldIn.spawnEntity(entity);
                }
            }
        }
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING});
    }
}
