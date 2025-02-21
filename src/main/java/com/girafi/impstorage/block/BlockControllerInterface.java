package com.girafi.impstorage.block;

import com.girafi.impstorage.block.tile.TileControllerInterface;
import com.girafi.impstorage.lib.ModInfo;
import com.girafi.impstorage.lib.ModTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class BlockControllerInterface extends Block implements ITileEntityProvider {
    public static final EnumProperty<InterfaceState> STATE = EnumProperty.create("state", InterfaceState.class);

    public BlockControllerInterface() {
        super(Material.ANVIL);

        setDefaultState(blockState.getBaseState().withProperty(STATE, InterfaceState.INACTIVE));

        setHardness(2F);
        setResistance(2F);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { STATE });
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileControllerInterface();
    }

    public static enum InterfaceState implements StringSerializable {
        INACTIVE("inactive"),
        ACTIVE("active"),
        ERROR("error");

        private final String name;
        private InterfaceState(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
