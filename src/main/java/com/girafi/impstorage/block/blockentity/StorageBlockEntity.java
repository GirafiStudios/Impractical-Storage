package com.girafi.impstorage.block.blockentity;

import com.girafi.impstorage.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class StorageBlockEntity extends BlockEntityCore {
    public static boolean DROPS = true;
    public ItemStack stack = ItemStack.EMPTY;
    private BlockPos controllerPos = BlockPos.ZERO;

    public StorageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STORAGE.get(), pos, state);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);

        compound.putLong("controller", this.controllerPos.asLong());
        CompoundTag tag = new CompoundTag();
        compound.put("stack", this.stack.save(tag));
    }

    @Override
    public void load(@Nonnull CompoundTag compound) {
        super.load(compound);

        this.controllerPos = BlockPos.of(compound.getLong("controller"));


        if (compound.contains("stack", 10)) {
            stack = ItemStack.of(compound.getCompound("stack"));
        }
    }

    public void setController(ControllerBlockEntity controller) {
        this.controllerPos = controller.getBlockPos();
    }

    private ControllerBlockEntity getController() {
        if (controllerPos == BlockPos.ZERO || this.level == null) {
            return null;
        }
        return (ControllerBlockEntity) this.level.getBlockEntity(controllerPos);
    }

    public void updateItemBlock(ItemStack force) {
        ControllerBlockEntity controller = getController();
        if (controller != null) {
            this.stack = force.isEmpty() ? controller.getStackForPosition(getBlockPos()) : force;
            this.setChanged();
        }
    }

    public ItemStack getDrop() {
        if (!DROPS) return ItemStack.EMPTY;

        ControllerBlockEntity controller = getController();
        if (controller != null) {
            int slot = controller.getSlotForPosition(getBlockPos());
            if (slot == -1) return ItemStack.EMPTY;

            ItemStack drop = controller.getStackInSlot(slot).copy();

            controller.setInventorySlotContents(slot, ItemStack.EMPTY, false, true, false);

            return drop;
        } else {
            return stack;
        }
    }
}
