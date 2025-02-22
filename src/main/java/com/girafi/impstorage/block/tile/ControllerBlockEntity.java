package com.girafi.impstorage.block.tile;

import com.girafi.impstorage.block.ControllerBlock;
import com.girafi.impstorage.block.PhantomBlock;
import com.girafi.impstorage.core.BlockOverrides;
import com.girafi.impstorage.init.ModBlockEntities;
import com.girafi.impstorage.init.ModBlocks;
import com.girafi.impstorage.lib.ImpracticalConfig;
import com.girafi.impstorage.lib.data.SortingType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Random;

public class ControllerBlockEntity extends BlockEntityCore {
    private static final int NUM_X_BITS = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;

    private static final int Y_SHIFT = NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;

    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

    private static final int MAX_BLOCK_STACK_SIZE = 1;
    private static final int MAX_ITEM_STACK_SIZE = 16;

    public ControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTROLLER.get(), pos, state);
    }

    public static long getLongFromPosition(int x, int y, int z) {
        return ((long) x & X_MASK) << X_SHIFT | ((long) y & Y_MASK) << Y_SHIFT | ((long) z & Z_MASK) << 0;
    }

    private static int getMaxStackSize(ItemStack itemStack) {
        return itemStack.getItem() instanceof BlockItem ? MAX_BLOCK_STACK_SIZE : MAX_ITEM_STACK_SIZE;
    }

    public static boolean INVENTORY_BLOCK = false;

    public static class ItemHandler implements IItemHandler {
        private final ControllerBlockEntity controller;

        private ItemHandler(ControllerBlockEntity controller) {
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

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (INVENTORY_BLOCK || stack.isEmpty()) return ItemStack.EMPTY;

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
                        this.controller.markDirtyAndNotify();
                    }

                    return ItemStack.EMPTY;
                } else {
                    // copy the stack to not modify the original one
                    stack = stack.copy();
                    if (!simulate) {
                        ItemStack copy = stack.split(m);
                        copy.grow(stackInSlot.getCount());
                        this.controller.setInventorySlotContents(slot, copy, false, true, true);
                        this.controller.markDirtyAndNotify();
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
                        this.controller.markDirtyAndNotify();
                    } else {
                        stack.shrink(m);
                    }
                    return stack;
                } else {
                    if (!simulate) {
                        this.controller.setInventorySlotContents(slot, stack, false, true, true);
                        this.controller.markDirtyAndNotify();
                    }
                    return ItemStack.EMPTY;
                }
            }
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (INVENTORY_BLOCK || amount == 0) return ItemStack.EMPTY;

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
                this.controller.markDirtyAndNotify();

                return decr;
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true;
        }
    }

    private static class QueueElement {
        public int slot;
        public ItemStack itemStack;
    }

    private final Random random = new Random();

    public ItemHandler itemHandler = new ItemHandler(this);
    public NonNullList<ItemStack> inventory = NonNullList.create();

    public BlockPos origin = null;
    public BlockPos end = null;

    public int rawX = ImpracticalConfig.BOUNDS_OPTIONS.defaultX.get();
    public int rawY = ImpracticalConfig.BOUNDS_OPTIONS.defaultY.get();
    public int rawZ = ImpracticalConfig.BOUNDS_OPTIONS.defaultZ.get();

    public BlockPos offset = BlockPos.ZERO;

    public int height = 1;
    public int xLength = 1;
    public int zLength = 1;
    public int totalSize;

    public boolean isEmpty;

    public SortingType sortingType = SortingType.ROWS;

    private int scanCounter = 0;

    public boolean showBounds = false;
    private boolean shouldShiftInventory = false;

    public long[] slotToWorldMap = new long[0];

    // [Y][X][Z]
    public int[][][] worldToSlotMap = new int[0][0][0];
    public boolean[][][] worldOcclusionMap = new boolean[0][0][0];

    // Block Queue
    private final ArrayDeque<QueueElement> blockQueue = new ArrayDeque<>();
    private int blockQueueTickCounter = 0;

    @Override
    public void writeToDisk(CompoundTag compound) {
        if (isReady()) {
            compound.putLong("origin", origin.asLong());
            compound.putLong("end", end.asLong());

            compound.putInt("rawX", rawX);
            compound.putInt("rawY", rawY);
            compound.putInt("rawZ", rawZ);

            compound.putLong("offset", offset.asLong());

            compound.putInt("height", height);
            compound.putInt("xLength", xLength);
            compound.putInt("zLength", zLength);

            compound.putBoolean("isEmpty", isInventoryEmpty());

            compound.putInt("sortingType", sortingType.ordinal());

            compound.putBoolean("shouldShiftInventory", shouldShiftInventory);

            ListTag nbt_slotToWorldMap = new ListTag();
            for (long l : slotToWorldMap) {
                nbt_slotToWorldMap.add(LongTag.valueOf(l));
            }
            compound.put("slotToWorldMap", nbt_slotToWorldMap);

            ListTag nbt_worldToSlotMap = new ListTag();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < xLength; x++) {
                    for (int z = 0; z < zLength; z++) {
                        int slot = worldToSlotMap[y][x][z];
                        if (slot != -1) {
                            CompoundTag tag = new CompoundTag();

                            tag.putInt("_x", x);
                            tag.putInt("_y", y);
                            tag.putInt("_z", z);
                            tag.putInt("slot", slot);

                            nbt_worldToSlotMap.add(tag);
                        }
                    }
                }
            }
            compound.put("worldToSlotMap", nbt_worldToSlotMap);

            ListTag nbt_worldOcclusionMap = new ListTag();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < xLength; x++) {
                    for (int z = 0; z < zLength; z++) {
                        if (worldOcclusionMap[y][x][z]) {
                            CompoundTag tag = new CompoundTag();

                            tag.putInt("_x", x);
                            tag.putInt("_y", y);
                            tag.putInt("_z", z);

                            nbt_worldOcclusionMap.add(tag);
                        }
                    }
                }
            }
            compound.put("worldOcclusionMap", nbt_worldOcclusionMap);

            CompoundTag inv = new CompoundTag();
            ContainerHelper.saveAllItems(inv, inventory);
            compound.put("inventory", inv);

            // Block Queue
            ListTag nbt_blockQueue = new ListTag();
            for (QueueElement element : blockQueue) {
                CompoundTag tag = new CompoundTag();

                tag.putInt("slot", element.slot);

                CompoundTag item = new CompoundTag();
                element.itemStack.save(item);
                tag.put("item", item);

                nbt_blockQueue.add(tag);
            }
            compound.put("blockQueue", nbt_blockQueue);

            compound.putInt("blockQueueCounter", blockQueueTickCounter);
        }
    }

    @Override
    public void readFromDisk(CompoundTag compound) {
        if (compound.contains("origin") && compound.contains("end")) {
            origin = BlockPos.of(compound.getLong("origin"));
            end = BlockPos.of(compound.getLong("end"));

            rawX = compound.getInt("rawX");
            rawY = compound.getInt("rawY");
            rawZ = compound.getInt("rawZ");

            offset = BlockPos.of(compound.getLong("offset"));

            height = compound.getInt("height");
            xLength = compound.getInt("xLength");
            zLength = compound.getInt("zLength");
            totalSize = height * xLength * zLength;

            isEmpty = compound.getBoolean("isEmpty");

            sortingType = SortingType.VALUES[compound.getInt("sortingType")];

            shouldShiftInventory = compound.getBoolean("shouldShiftInventory");

            inventory = NonNullList.withSize(totalSize, ItemStack.EMPTY);

            slotToWorldMap = new long[totalSize];
            ListTag nbt_slotToWorldMap = compound.getList("slotToWorldMap", 4);
            for (int i = 0; i < nbt_slotToWorldMap.size(); i++) {
                slotToWorldMap[i] = ((LongTag) nbt_slotToWorldMap.get(i)).getAsLong();
            }

            worldToSlotMap = new int[height][xLength][zLength];
            ListTag nbt_worldToSlotMap = compound.getList("worldToSlotMap", 10);
            for (int i = 0; i < nbt_worldToSlotMap.size(); i++) {
                CompoundTag tag = nbt_worldToSlotMap.getCompound(i);

                int x = tag.getInt("_x");
                int y = tag.getInt("_y");
                int z = tag.getInt("_z");
                int slot = tag.getInt("slot");

                worldToSlotMap[y][x][z] = slot;
            }

            worldOcclusionMap = new boolean[height][xLength][zLength];
            ListTag nbt_worldOcclusionMap = compound.getList("worldOcclusionMap", 10);
            for (int i = 0; i < nbt_worldOcclusionMap.size(); i++) {
                CompoundTag tag = nbt_worldOcclusionMap.getCompound(i);

                int x = tag.getInt("_x");
                int y = tag.getInt("_y");
                int z = tag.getInt("_z");

                worldOcclusionMap[y][x][z] = true;
            }

            CompoundTag inv = compound.getCompound("inventory");
            ContainerHelper.loadAllItems(inv, inventory);

            // Block Queue
            ListTag nbt_blockQueue = compound.getList("blockQueue", 10);
            for (int i = 0; i < nbt_blockQueue.size(); i++) {
                CompoundTag tag = nbt_blockQueue.getCompound(i);
                QueueElement element = new QueueElement();
                element.slot = tag.getInt("slot");
                element.itemStack = ItemStack.of(tag.getCompound("item"));
                blockQueue.add(element);
            }

            blockQueueTickCounter = compound.getInt("blockQueueCounter");
        }
    }

    public void initialize(Direction direction) {
        if (this.level != null && !level.isClientSide()) {
            if (direction != null) {
                updateRawBounds(direction, rawX, rawY, rawZ);
            }
        }
    }

    public void setShowBounds(boolean showBounds) {
        this.showBounds = showBounds;
    }

    public void setSortingType(SortingType sortingType) {
        this.sortingType = sortingType;
        if (this.level != null) {
            this.updateRawBounds(this.level.getBlockState(getBlockPos()).getValue(ControllerBlock.FACING), rawX, rawY, rawZ);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ControllerBlockEntity controller) {
        if (controller.origin == null || controller.end == null || level == null) {
            return;
        }

        if (controller.shouldShiftInventory) {
            controller.shiftInventory();
            controller.shouldShiftInventory = false;
        }

        controller.blockQueueTickCounter++;

        if (ImpracticalConfig.BLOCK_QUEUE_OPTIONS.blockUpdateRate.get() == -1 || controller.blockQueueTickCounter >= ImpracticalConfig.BLOCK_QUEUE_OPTIONS.blockUpdateRate.get()) {
            if (ImpracticalConfig.BLOCK_QUEUE_OPTIONS.blockUpdateBatch.get() == -1) {
                for (int i = 0; i < controller.blockQueue.size(); i++) {
                    QueueElement element = controller.blockQueue.pop();
                    if (!controller.setBlock(element.slot, element.itemStack)) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), element.itemStack.copy());
                    }
                }
            } else {
                for (int i = 0; i < Math.min(controller.blockQueue.size(), ImpracticalConfig.BLOCK_QUEUE_OPTIONS.blockUpdateBatch.get()); i++) {
                    QueueElement element = controller.blockQueue.pop();
                    if (!controller.setBlock(element.slot, element.itemStack)) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), element.itemStack.copy());
                    }
                }
            }

            controller.blockQueueTickCounter = 0;
        }

        if (level.getGameTime() % 10 == 0) {
            // Search for interfaces
            for (int y = -1; y <= controller.height; y++) {
                for (int z = -1; z <= controller.zLength; z++) {
                    for (int x = -1; x <= controller.xLength; x++) {
                        if (y == -1 || y == controller.height || z == -1 || z == controller.zLength || x == -1 || x == controller.xLength) {
                            BlockPos originAdd = controller.origin.offset(x, y, z);
                            if (level.getBlockState(originAdd).getBlock() == ModBlocks.CONTROLLER_INTERFACE.get()) {
                                ControllerInterfaceBlockEntity controllerInterface = (ControllerInterfaceBlockEntity) level.getBlockEntity(originAdd);
                                if (controllerInterface != null) {
                                    controllerInterface.registerController(controller);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (controller.scanCounter >= ImpracticalConfig.BLOCK_QUEUE_OPTIONS.blockUpdateRate.get()) {
            // Search for blocks to add to inventory
            for (int y = 0; y < controller.height; y++) {
                for (int z = 0; z < controller.zLength; z++) {
                    for (int x = 0; x < controller.xLength; x++) {
                        if (!controller.worldOcclusionMap[y][x][z]) {
                            BlockPos originAdd = new BlockPos(x, y, z).offset(controller.origin);
                            Block block = state.getBlock();

                            if (block != ModBlocks.CONTROLLER.get() && block != ModBlocks.ITEM_BLOCK.get() && !level.getBlockState(originAdd).isAir()) {
                                ItemStack stack = new ItemStack(block);
                                level.setBlock(originAdd, Blocks.AIR.defaultBlockState(), 2);

                                int slot = controller.getSlotForPosition(originAdd);
                                if (slot == -1) {
                                    int i;
                                    for (i = 0; i < controller.totalSize; i++) {
                                        long world = controller.slotToWorldMap[i];
                                        if (world == -1) {
                                            controller.slotToWorldMap[i] = getLongFromPosition(x, y, z);
                                            controller.worldToSlotMap[y][x][z] = i;
                                            break;
                                        }
                                    }

                                    slot = i;
                                }

                                controller.setInventorySlotContents(slot, stack, false, true, true);
                            }
                        }
                    }
                }
            }
        } else {
            controller.scanCounter++;
        }
    }

    public boolean isReady() {
        return origin != null && end != null;
    }

    public void updateOffset(int x, int y, int z) {
        this.offset = new BlockPos(x, y, z);
        this.updateRawBounds(level.getBlockState(getBlockPos()).getValue(ControllerBlock.FACING), rawX, rawY, rawZ);
    }

    public void updateRawBounds(Direction direction, int x, int y, int z) {
        Direction posX = direction.getClockWise();
        Direction negX = posX.getOpposite();

        int modx = 1;
        if (x == 1) modx = 0;
        else if (x % 2 == 0) modx = x / 2;
        else modx = (x - 1) / 2;

        this.rawX = x;
        this.rawY = y;
        this.rawZ = z;

        //TODO: offsets

        BlockPos origin = new BlockPos(getBlockPos());
        origin = origin.relative(negX, modx);
        origin = origin.relative(Direction.UP, 0);
        origin = origin.relative(direction, 1);

        BlockPos end = new BlockPos(getBlockPos());
        end = end.relative(posX, modx);
        end = end.relative(Direction.UP, y);
        end = end.relative(direction, z);

        BlockPos low = new BlockPos(
                Math.min(origin.getX(), end.getX()),
                Math.min(origin.getY(), end.getY()),
                Math.min(origin.getZ(), end.getZ())
        ).offset(offset);

        BlockPos high = new BlockPos(
                Math.max(origin.getX(), end.getX()),
                Math.max(origin.getY(), end.getY()),
                Math.max(origin.getZ(), end.getZ())
        ).offset(offset);

        setBounds(low, high);
    }

    public void setBounds(BlockPos nOrigin, BlockPos nEnd) {
        INVENTORY_BLOCK = true;

        if (this.level == null) return;

        boolean clear = this.origin != null && this.end != null;
        BlockPos oldOrigin = this.origin;

        NonNullList<ItemStack> currentInventory = NonNullList.create();
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) currentInventory.add(stack.copy());
        }

        // Clear everything old
        if (clear) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < zLength; z++) {
                    for (int x = 0; x < xLength; x++) {
                        BlockPos pos = oldOrigin.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);
                        if (state.getBlock() == ModBlocks.ITEM_BLOCK.get()) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        this.origin = nOrigin;
        this.end = nEnd;

        height = nEnd.getY() - origin.getY();
        xLength = 1 + nEnd.getX() - origin.getX();
        zLength = 1 + nEnd.getZ() - origin.getZ();

        worldOcclusionMap = new boolean[height][xLength][zLength];

        // Occlusion map gets prefilled with all phantom blocks
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < zLength; z++) {
                for (int x = 0; x < xLength; x++) {
                    BlockState state = level.getBlockState(origin.offset(x, y, z));
                    if (state.getBlock() == ModBlocks.PHANTOM.get()) {
                        PhantomBlock.EnumType type = state.getValue(PhantomBlock.TYPE);
                        if (type == PhantomBlock.EnumType.BLOCK) {
                            worldOcclusionMap[y][x][z] = true;
                        } else if (type == PhantomBlock.EnumType.COLUMN) {
                            for (int i = 0; i < height; i++) worldOcclusionMap[i][x][z] = true;
                        }
                    }
                }
            }
        }

        sortingType.getSizeCalculator().calculate(this);

        inventory = NonNullList.withSize(totalSize, ItemStack.EMPTY);

        slotToWorldMap = new long[totalSize];
        worldToSlotMap = new int[height][xLength][zLength];

        if (sortingType.isBaked()) {
            sortingType.getPositionHandler().bake(this);
        } else {
            int slot = 0;
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < zLength; z++) {
                    for (int x = 0; x < xLength; x++) {
                        if (!worldOcclusionMap[y][x][z]) {
                            slotToWorldMap[slot] = -1;
                            worldToSlotMap[y][x][z] = -1;

                            slot++;
                        }
                    }
                }
            }
        }

        if (clear) {
            int slot = 0;
            for (int i = 0; i < currentInventory.size(); i++) {
                ItemStack copy = currentInventory.get(i);
                if (!copy.isEmpty()) {
                    if (i < totalSize) {
                        setInventorySlotContents(slot, copy, false, true, false);
                    } else {
                        BlockPos pos = getBlockPos();
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), copy);
                    }
                    slot++;
                }
            }
        }

        INVENTORY_BLOCK = false;
    }

    public boolean isInventoryEmpty() {
        boolean empty = true;
        for (ItemStack stack : inventory) if (!stack.isEmpty()) empty = false;
        return empty;
    }

    public void onBlockBreak() {
        ItemBlockEntity.DROPS = false;

        for (int i = 0; i < this.totalSize; i++) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty() && this.level != null) {
                BlockPos pos = BlockPos.of(this.slotToWorldMap[i]).offset(this.origin);
                BlockEntity blockEntity = this.level.getBlockEntity(pos);

                if (ImpracticalConfig.GENERAL_OPTIONS.dropBlocks.get()) {
                    Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), stack);
                    this.setBlock(i, ItemStack.EMPTY);
                } else {
                    if (blockEntity instanceof ItemBlockEntity) {
                        Item item = getStackForPosition(pos).getItem();
                        if (item instanceof BlockItem && !BlockOverrides.shouldTreatAsItem(item)) {
                            this.level.setBlock(pos, Block.byItem(item).defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        ItemBlockEntity.DROPS = true;
    }

    public int getSlotForPosition(BlockPos pos) {
        pos = pos.subtract(this.origin);
        if (pos.getX() < 0 || pos.getY() < 0 || pos.getZ() < 0) return -1;

        try {
            return this.worldToSlotMap[pos.getY()][pos.getX()][pos.getZ()];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return -1;
        }
    }

    public ItemStack getStackForPosition(BlockPos pos) {
        return getStackInSlot(getSlotForPosition(pos));
    }

    public ItemStack getStackInSlot(int slot) {
        if (slot == -1) return ItemStack.EMPTY;
        return this.inventory.get(slot);
    }

    public BlockPos getNextRandomPosition() {
        int x = this.random.nextInt(xLength);
        int y = 0;
        int z = this.random.nextInt(zLength);

        boolean failed = false;

        if (this.level == null) failed = true;

        BlockPos pos = new BlockPos(x, y, z).offset(this.origin);
        while (this.worldOcclusionMap[y][x][z] || !this.level.getBlockState(pos).isAir()) {
            pos = pos.above();
            y++;

            if (y >= this.height) {
                failed = true;
                break;
            }
        }

        if (failed) return getNextRandomPosition();

        pos = pos.subtract(this.origin);
        return pos;
    }

    public void setInventorySlotContents(int slot, ItemStack itemStack, boolean shouldShift, boolean shouldUpdateBlock, boolean queueBlockUpdate) {
        if (!itemStack.isEmpty()) {
            if (!this.sortingType.isBaked()) {
                this.sortingType.getPositionHandler().runtime(this, slot);
            }
        }

        if (!this.inventory.isEmpty()) {
            this.inventory.set(slot, itemStack);
        }

        if (shouldShift) {
            if (itemStack.isEmpty()) {
                this.shouldShiftInventory = true;
            }
        }

        if (shouldUpdateBlock) {
            if (queueBlockUpdate) {
                this.blockQueueTickCounter = 0;

                QueueElement element = new QueueElement();
                element.slot = slot;
                element.itemStack = itemStack;
                this.blockQueue.add(element);
            } else {
                setBlock(slot, itemStack);
            }
        }
    }

    private void shiftInventory() {
        NonNullList<ItemStack> shifted = NonNullList.withSize(this.totalSize, ItemStack.EMPTY);

        int target = 0;
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) {
                shifted.set(target, stack.copy());
                target++;
            }
        }

        this.inventory = shifted;

        for (int i = 0; i < this.totalSize; i++) {
            setBlock(i, getStackInSlot(i));
        }
    }

    private boolean setBlock(int slot, ItemStack itemStack) {
        ItemBlockEntity.DROPS = false;

        if (slot == -1 || this.level == null) return false;

        if (slot >= this.slotToWorldMap.length) return false;

        long lpos = this.slotToWorldMap[slot];
        if (lpos == -1) return false;

        BlockPos pos = BlockPos.of(lpos);

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        pos = pos.offset(this.origin);

        BlockState state = this.level.getBlockState(pos);

        if (itemStack.isEmpty()) {
            if (state.getBlock() == ModBlocks.ITEM_BLOCK.get()) {
                if (this.sortingType == SortingType.MESSY) {
                    this.slotToWorldMap[slot] = -1;
                    this.worldToSlotMap[y][x][z] = -1;
                }
                this.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        } else {
            if (state.getBlock() == ModBlocks.ITEM_BLOCK.get()) {
                BlockEntity blockEntity = this.level.getBlockEntity(pos);
                if (blockEntity instanceof ItemBlockEntity)
                    ((ItemBlockEntity) blockEntity).updateItemBlock(itemStack);
            } else {
                this.level.setBlock(pos, ModBlocks.ITEM_BLOCK.get().defaultBlockState(), 2);
                BlockEntity blockEntity = this.level.getBlockEntity(pos);
                if (blockEntity instanceof ItemBlockEntity) {
                    ((ItemBlockEntity) blockEntity).setController(this);
                    ((ItemBlockEntity) blockEntity).updateItemBlock(itemStack);
                }
            }
        }
        ItemBlockEntity.DROPS = true;

        return true;
    }

    public int getRedstoneLevel() {
        int i = 0;
        float f = 0;

        for (int j = 0; j < this.totalSize; j++) {
            ItemStack stack = getStackInSlot(j);
            if (!stack.isEmpty()) {
                f += (float) stack.getCount() / (float) Math.min(getMaxStackSize(stack), stack.getMaxStackSize());
                ++i;
            }
        }

        f = f / (float) this.totalSize;

        return (int) Math.floor(f * 14F) + (i > 0 ? 1 : 0);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        return !INVENTORY_BLOCK && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && isReady();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!INVENTORY_BLOCK && isReady())
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) itemHandler;
        return super.getCapability(capability, facing);
    }
}