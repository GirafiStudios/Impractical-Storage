package com.girafi.impstorage.block.blockentity;

import com.girafi.impstorage.block.ControllerInterfaceBlock;
import com.girafi.impstorage.init.ModBlockEntities;
import com.girafi.impstorage.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ControllerInterfaceBlockEntity extends BlockEntityCore {
    public BlockPos selectedController;

    public ControllerInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTROLLER_INTERFACE.get(), pos, state);
    }

    public void registerController(ControllerBlockEntity tile) {
        if (selectedController == null) {
            selectedController = tile.getBlockPos();
            System.out.println("Set controller from interface");
            setState(ControllerInterfaceBlock.InterfaceState.ACTIVE);
        } else {
            if (selectedController != tile.getBlockPos()) {
                System.out.println("clear controller from interface");
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
        if (selectedController == null || selectedController == BlockPos.ZERO) return null;

        System.out.println("beep");

        BlockState state = this.level.getBlockState(getBlockPos());

        if (state.getBlock() instanceof ControllerInterfaceBlock && state.getValue(ControllerInterfaceBlock.STATE) == ControllerInterfaceBlock.InterfaceState.ERROR)
            return null;

        BlockEntity blockEntity = this.level.getBlockEntity(selectedController);
        if (!(blockEntity instanceof ControllerBlockEntity)) return null;

        System.out.println("getController");

        return (ControllerBlockEntity) blockEntity;
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        System.out.println("write");
        super.saveAdditional(tag);
        if (selectedController != null) {
            System.out.println("write: " + selectedController);
            tag.putLong("selected", selectedController.asLong());
        }
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        System.out.println("Read");
        super.load(tag);
        if (tag.contains("selected")) {
            selectedController = BlockPos.of(tag.getLong("selected"));
        }
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (!this.remove && this.getController() != null && cap == ForgeCapabilities.ITEM_HANDLER) {
            return this.getController().itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (this.getController() != null) {
            this.getController().itemHandler.invalidate();
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        if (this.getController() != null) {
            this.getController().itemHandler = LazyOptional.of(this.getController()::createItemHandler);
        }
    }
}