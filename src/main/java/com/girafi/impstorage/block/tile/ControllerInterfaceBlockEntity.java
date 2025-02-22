package com.girafi.impstorage.block.tile;

import com.girafi.impstorage.block.ControllerInterfaceBlock;
import com.girafi.impstorage.init.ModBlockEntities;
import com.girafi.impstorage.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class ControllerInterfaceBlockEntity extends BlockEntityCore {
    public BlockPos selectedController;

    public ControllerInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTROLLER_INTERFACE.get(), pos, state);
    }

    public void registerController(ControllerBlockEntity tile) {
        if (selectedController == null) {
            selectedController = tile.getBlockPos();
            setState(ControllerInterfaceBlock.InterfaceState.ACTIVE);
        } else {
            if (selectedController != tile.getBlockPos()) {
                setState(ControllerInterfaceBlock.InterfaceState.ERROR);
            }
        }
    }

    private void setState(ControllerInterfaceBlock.InterfaceState state) {
        if (this.level != null) {
            level.setBlock(getBlockPos(), ModBlocks.CONTROLLER_INTERFACE.get().defaultBlockState().setValue(ControllerInterfaceBlock.STATE, state), 2);
        }
    }

    private ControllerBlockEntity getController() {
        if (selectedController == null || selectedController == BlockPos.ZERO || this.level == null) return null;

        if (this.level.getBlockState(getBlockPos()).getValue(ControllerInterfaceBlock.STATE) == ControllerInterfaceBlock.InterfaceState.ERROR)
            return null;

        BlockEntity blockEntity = this.level.getBlockEntity(selectedController);
        if (!(blockEntity instanceof ControllerBlockEntity)) return null;

        return (ControllerBlockEntity) blockEntity;
    }

    @Override
    public void writeToDisk(CompoundTag compound) {
        if (selectedController != null) {
            compound.putLong("selected", selectedController.asLong());
        }
    }

    @Override
    public void readFromDisk(CompoundTag compound) {
        if (compound.contains("selected")) {
            selectedController = BlockPos.of(compound.getLong("selected"));
        } else {
            selectedController = null;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && getController() != null;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) getController().itemHandler;
        }
        return super.getCapability(capability, facing);
    }
}