package com.girafi.impstorage.block;

import com.girafi.impstorage.init.ModBlocks;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;

import javax.annotation.Nullable;

public class BlockPhantom extends Block {
    public static final EnumProperty<EnumType> TYPE = EnumProperty.create("type", EnumType.class);

    public BlockPhantom() {
        super(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.8F).noLootTable().noOcclusion().isValidSpawn(ModBlocks::never).noParticlesOnBreak().pushReaction(PushReaction.BLOCK));

        this.registerDefaultState(this.getStateDefinition().any().setValue(TYPE, EnumType.BLOCK));
    }

    //TODO Hide bounding-box unless holding item


    @Nullable
    @Override
    protected RayTraceResult rayTrace(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB boundingBox) {
        return super.rayTrace(pos, start, end, boundingBox);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TYPE);
    }

    public static enum EnumType implements StringRepresentable {
        BLOCK("block", 0),
        COLUMN("column", 1);

        private static final BlockPhantom.EnumType[] META_LOOKUP = new BlockPhantom.EnumType[values().length];

        private final String name;
        private final int metadata;
        private EnumType(String name, int metadata) {
            this.name = name;
            this.metadata = metadata;
        }

        @Override
        public String getName() {
            return this.name;
        }

        public int getMetadata() {
            return this.metadata;
        }

        public static BlockPhantom.EnumType fromMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
            return META_LOOKUP[meta];
        }

        static {
            for (BlockPhantom.EnumType type : values()) {
                META_LOOKUP[type.getMetadata()] = type;
            }
        }
    }
}
