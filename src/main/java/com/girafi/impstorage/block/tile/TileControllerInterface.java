package com.girafi.impstorage.block.tile;

import com.girafi.impstorage.block.BlockControllerInterface;
import com.girafi.impstorage.init.ModBlockEntities;
import com.girafi.impstorage.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class TileControllerInterface extends TileCore {
    public BlockPos selectedController;

    public TileControllerInterface(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTROLLER_INTERFACE.get(), pos, state);
    }

    public void registerController(TileController tile) {
        if (selectedController == null) {
            selectedController = tile.getBlockPos();
            setState(BlockControllerInterface.InterfaceState.ACTIVE);
        } else {
            if (selectedController != tile.getBlockPos()) {
                setState(BlockControllerInterface.InterfaceState.ERROR);
            }
        }
    }

    private void setState(BlockControllerInterface.InterfaceState state) {
        level.setBlock(getBlockPos(), ModBlocks.CONTROLLER_INTERFACE.get().defaultBlockState().setValue(BlockControllerInterface.STATE, state), 2);
    }

    private TileController getController() {
        if (selectedController == null || selectedController == BlockPos.ZERO)
            return null;

        if (level.getBlockState(getBlockPos()).getValue(BlockControllerInterface.STATE) == BlockControllerInterface.InterfaceState.ERROR)
            return null;

        BlockEntity blockEntity = level.getBlockEntity(selectedController);
        if (blockEntity == null || !(blockEntity instanceof TileController))
            return null;

        return (TileController) blockEntity;
    }

    @Override
    public void writeToDisk(CompoundTag compound) {
        if (selectedController != null)
            compound.putLong("selected", selectedController.toLong());
    }

    @Override
    public void readFromDisk(CompoundTag compound) {
        if (compound.contains("selected"))
            selectedController = BlockPos.fromLong(compound.getLong("selected"));
        else
            selectedController = null;
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