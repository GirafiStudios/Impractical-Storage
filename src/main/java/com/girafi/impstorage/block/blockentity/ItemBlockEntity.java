package com.girafi.impstorage.block.blockentity;

import com.girafi.impstorage.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ItemBlockEntity extends BlockEntityCore {
    public static boolean DROPS = true;
    public ItemStack stack = ItemStack.EMPTY;
    private BlockPos controllerPos;

    public ItemBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_BLOCK.get(), pos, state);
    }

    @Override
    public void writeToDisk(CompoundTag compound) {
        super.writeToDisk(compound);

        if (controllerPos != null) {
            compound.putLong("controller", controllerPos.asLong());
        }

        CompoundTag tag = new CompoundTag();
        compound.put("stack", stack.save(tag));
    }

    @Override
    public void readFromDisk(CompoundTag compound) {
        super.readFromDisk(compound);

        if (compound.contains("controller")) {
            controllerPos = BlockPos.of(compound.getLong("controller"));
        } else {
            controllerPos = null;
        }

        if (compound.contains("stack")) {
            stack = ItemStack.of(compound.getCompound("stack"));
        } else {
            stack = ItemStack.EMPTY;
        }
    }

    public void setController(ControllerBlockEntity controller) {
        this.controllerPos = controller.getBlockPos();
    }

    private ControllerBlockEntity getController() {
        if (controllerPos == null || controllerPos.equals(BlockPos.ZERO) || level == null) {
            return null;
        }
        return (ControllerBlockEntity) level.getBlockEntity(controllerPos);
    }

    public void updateItemBlock(ItemStack force) {
        ControllerBlockEntity controller = getController();
        if (controller != null) {
            this.stack = force.isEmpty() ? controller.getStackForPosition(getBlockPos()) : force;
            this.markDirtyAndNotify();
        }
    }

    public ItemStack getDrop() {
        if (!DROPS) return null;

        ControllerBlockEntity controller = getController();
        if (controller != null) {
            int slot = controller.getSlotForPosition(getBlockPos());
            if (slot == -1)
                return ItemStack.EMPTY;

            ItemStack drop = controller.getStackInSlot(slot).copy();

            controller.setInventorySlotContents(slot, ItemStack.EMPTY, false, true, false);

            return drop;
        } else {
            return stack;
        }
    }
}
