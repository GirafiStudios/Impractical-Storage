package com.girafi.impstorage.item;

import com.girafi.impstorage.block.blockentity.ControllerBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ControllerItemHandler extends ItemStackHandler {
    private final ControllerBlockEntity controller;
    private static final int MAX_BLOCK_STACK_SIZE = 1;
    private static final int MAX_ITEM_STACK_SIZE = 16;

    public ControllerItemHandler(ControllerBlockEntity controller) {
        this.controller = controller;
    }

    @Override
    public int getSlots() {
        return this.controller.totalSize;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.controller.getStackInSlot(slot);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        this.controller.setChanged();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (ControllerBlockEntity.INVENTORY_BLOCK || stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack stackInSlot = this.controller.getStackInSlot(slot);

        int m;
        if (!stackInSlot.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
                return stack;

            m = Math.min(stack.getMaxStackSize(), getMaxStackSize(stackInSlot)) - stackInSlot.getCount();

            if (stack.getCount() <= m) {
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    copy.grow(stackInSlot.getCount());
                    this.controller.setInventorySlotContents(slot, copy, false, true, true);
                }
                return ItemStack.EMPTY;
            } else {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    ItemStack copy = stack.split(m);
                    copy.grow(stackInSlot.getCount());
                    this.controller.setInventorySlotContents(slot, copy, false, true, true);
                    return stack;
                } else {
                    stack.shrink(m);
                    return stack;
                }
            }
        } else {
            m = Math.min(stack.getMaxStackSize(), getMaxStackSize(stack));
            if (m < stack.getCount()) {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate) {
                    this.controller.setInventorySlotContents(slot, stack.split(m), false, true, true);
                } else {
                    stack.shrink(m);
                }
                return stack;
            } else {
                if (!simulate) {
                    this.controller.setInventorySlotContents(slot, stack, false, true, true);
                }
                return ItemStack.EMPTY;
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (ControllerBlockEntity.INVENTORY_BLOCK || amount == 0) return ItemStack.EMPTY;

        ItemStack stackInSlot = this.controller.getStackInSlot(slot);
        if (stackInSlot.isEmpty()) return ItemStack.EMPTY;

        if (simulate) {
            if (stackInSlot.getCount() < amount) {
                return stackInSlot.copy();
            } else {
                ItemStack copy = stackInSlot.copy();
                copy.setCount(amount);
                return copy;
            }
        } else {
            int m = Math.min(stackInSlot.getCount(), amount);
            ItemStack old = this.controller.getStackInSlot(slot);
            ItemStack decr = old.split(m);

            this.controller.setInventorySlotContents(slot, old, true, true, true);

            return decr;
        }
    }

    public static int getMaxStackSize(ItemStack itemStack) {
        return itemStack.getItem() instanceof BlockItem ? MAX_BLOCK_STACK_SIZE : MAX_ITEM_STACK_SIZE;
    }
}