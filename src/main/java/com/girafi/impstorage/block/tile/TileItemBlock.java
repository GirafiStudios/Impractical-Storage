package com.girafi.impstorage.block.tile;

import com.girafi.impstorage.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileItemBlock extends TileCore {
    public static boolean DROPS = true;
    public ItemStack item = ItemStack.EMPTY;
    private BlockPos controllerPos;

    public TileItemBlock(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_BLOCK.get(), pos, state);
    }

    @Override
    public void writeToDisk(CompoundTag compound) {
        super.writeToDisk(compound);

        if (controllerPos != null) {
            compound.putLong("controller", controllerPos.asLong());
        }

        CompoundTag tag = new CompoundTag();
        compound.put("item", item.save(tag));
    }

    @Override
    public void readFromDisk(CompoundTag compound) {
        super.readFromDisk(compound);

        if (compound.contains("controller")) {
            controllerPos = BlockPos.fromLong(compound.getLong("controller"));
        } else {
            controllerPos = null;
        }

        if (compound.contains("item")) {
            item = ItemStack.of(compound.getCompound("item"));
        } else {
            item = ItemStack.EMPTY;
        }
    }

    public void setController(TileController controller) {
        this.controllerPos = controller.getBlockPos();
    }

    private TileController getController() {
        if (controllerPos == null || controllerPos.equals(BlockPos.ZERO)) {
            return null;
        }
        return (TileController) level.getBlockEntity(controllerPos);
    }

    public void updateItemBlock(ItemStack force) {
        TileController controller = getController();
        if (controller != null) {
            this.item = force.isEmpty() ? controller.getStackForPosition(getBlockPos()) : force;
            this.markDirtyAndNotify();
        }
    }

    public ItemStack getDrop() {
        if (!DROPS) return null;

        TileController controller = getController();
        if (controller != null) {
            int slot = controller.getSlotForPosition(getBlockPos());
            if (slot == -1)
                return ItemStack.EMPTY;

            ItemStack drop = controller.getStackInSlot(slot).copy();

            controller.setInventorySlotContents(slot, ItemStack.EMPTY, false, true, false);

            return drop;
        } else {
            return item;
        }
    }
}
