package com.girafi.impstorage.block.tile;

import com.girafi.impstorage.block.BlockController;
import com.girafi.impstorage.block.BlockPhantom;
import com.girafi.impstorage.core.BlockOverrides;
import com.girafi.impstorage.lib.ImpracticalConfig;
import com.girafi.impstorage.init.ModBlockEntities;
import com.girafi.impstorage.init.ModBlocks;
import com.girafi.impstorage.lib.data.SortingType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Random;

public class TileController extends TileCore {
    private static final int NUM_X_BITS = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;

    private static final int Y_SHIFT = NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;

    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

    private static final int MAX_BLOCK_STACK_SIZE = 1;
    private static final int MAX_ITEM_STACK_SIZE = 16;

    public TileController(BlockPos pos, BlockState state) {
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
        private TileController controller;

        private ItemHandler(TileController controller) {
            this.controller = controller;
        }

        @Override
        public int getSlots() {
            return controller.totalSize;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return controller.getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (INVENTORY_BLOCK)
                return ItemStack.EMPTY;

            if (stack.isEmpty())
                return ItemStack.EMPTY;

            ItemStack stackInSlot = controller.getStackInSlot(slot);

            int m;
            if (!stackInSlot.isEmpty()) {
                if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
                    return stack;

                m = Math.min(stack.getMaxStackSize(), getMaxStackSize(stackInSlot)) - stackInSlot.getCount();

                if (stack.getCount() <= m) {
                    if (!simulate) {
                        ItemStack copy = stack.copy();
                        copy.grow(stackInSlot.getCount());
                        controller.setInventorySlotContents(slot, copy, false, true, true);
                        controller.markDirty();
                    }

                    return ItemStack.EMPTY;
                } else {
                    // copy the stack to not modify the original one
                    stack = stack.copy();
                    if (!simulate) {
                        ItemStack copy = stack.splitStack(m);
                        copy.grow(stackInSlot.getCount());
                        controller.setInventorySlotContents(slot, copy, false, true, true);
                        controller.markDirty();
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
                        controller.setInventorySlotContents(slot, stack.splitStack(m), false, true, true);
                        controller.markDirty();
                        return stack;
                    } else {
                        stack.shrink(m);
                        return stack;
                    }
                } else {
                    if (!simulate) {
                        controller.setInventorySlotContents(slot, stack, false, true, true);
                        controller.markDirty();
                    }
                    return ItemStack.EMPTY;
                }
            }
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (INVENTORY_BLOCK)
                return ItemStack.EMPTY;

            if (amount == 0)
                return ItemStack.EMPTY;

            ItemStack stackInSlot = controller.getStackInSlot(slot);

            if (stackInSlot.isEmpty())
                return ItemStack.EMPTY;

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
                ItemStack old = controller.getStackInSlot(slot);
                ItemStack decr = old.splitStack(m);

                controller.setInventorySlotContents(slot, old, true, true, true);
                controller.markDirty();

                return decr;
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
    }

    private static class QueueElement {
        public int slot;
        public ItemStack itemStack;
    }

    private final Random random = new Random();

    public ItemHandler itemHandler = new ItemHandler(this);
    public NonNullList<ItemStack> inventory = NonNullList.create();

    public BlockPos.MutableBlockPos origin = null;
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
    private ArrayDeque<QueueElement> blockQueue = new ArrayDeque<>();
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

            NBTTagList nbt_slotToWorldMap = new NBTTagList();
            for (long l : slotToWorldMap) {
                nbt_slotToWorldMap.appendTag(new NBTTagLong(l));
            }
            compound.setTag("slotToWorldMap", nbt_slotToWorldMap);

            NBTTagList nbt_worldToSlotMap = new NBTTagList();
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

                            nbt_worldToSlotMap.appendTag(tag);
                        }
                    }
                }
            }
            compound.setTag("worldToSlotMap", nbt_worldToSlotMap);

            NBTTagList nbt_worldOcclusionMap = new NBTTagList();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < xLength; x++) {
                    for (int z = 0; z < zLength; z++) {
                        if (worldOcclusionMap[y][x][z]) {
                            CompoundTag tag = new CompoundTag();

                            tag.putInt("_x", x);
                            tag.putInt("_y", y);
                            tag.putInt("_z", z);

                            nbt_worldOcclusionMap.appendTag(tag);
                        }
                    }
                }
            }
            compound.setTag("worldOcclusionMap", nbt_worldOcclusionMap);

            CompoundTag inv = new CompoundTag();
            ItemStackHelper.saveAllItems(inv, inventory);
            compound.setTag("inventory", inv);

            // Block Queue
            NBTTagList nbt_blockQueue = new NBTTagList();
            for (QueueElement element : blockQueue) {
                CompoundTag tag = new CompoundTag();

                tag.putInt("slot", element.slot);

                CompoundTag item = new CompoundTag();
                element.itemStack.writeToNBT(item);
                tag.setTag("item", item);

                nbt_blockQueue.appendTag(tag);
            }
            compound.setTag("blockQueue", nbt_blockQueue);

            compound.putInt("blockQueueCounter", blockQueueTickCounter);
        }
    }

    @Override
    public void readFromDisk(CompoundTag compound) {
        if (compound.contains("origin") && compound.contains("end")) {
            origin = BlockPos.fromLong(compound.getLong("origin"));
            end = BlockPos.fromLong(compound.getLong("end"));

            rawX = compound.getInt("rawX");
            rawY = compound.getInt("rawY");
            rawZ = compound.getInt("rawZ");

            offset = BlockPos.fromLong(compound.getLong("offset"));

            height = compound.getInt("height");
            xLength = compound.getInt("xLength");
            zLength = compound.getInt("zLength");
            totalSize = height * xLength * zLength;

            isEmpty = compound.getBoolean("isEmpty");

            sortingType = SortingType.VALUES[compound.getInt("sortingType")];

            shouldShiftInventory = compound.getBoolean("shouldShiftInventory");

            inventory = NonNullList.withSize(totalSize, ItemStack.EMPTY);

            slotToWorldMap = new long[totalSize];
            NBTTagList nbt_slotToWorldMap = compound.getTagList("slotToWorldMap", Constants.NBT.TAG_LONG);
            for (int i = 0; i < nbt_slotToWorldMap.tagCount(); i++) {
                slotToWorldMap[i] = ((NBTTagLong) nbt_slotToWorldMap.get(i)).getLong();
            }

            worldToSlotMap = new int[height][xLength][zLength];
            NBTTagList nbt_worldToSlotMap = compound.getTagList("worldToSlotMap", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < nbt_worldToSlotMap.tagCount(); i++) {
                CompoundTag tag = nbt_worldToSlotMap.getCompoundTagAt(i);

                int x = tag.getInt("_x");
                int y = tag.getInt("_y");
                int z = tag.getInt("_z");
                int slot = tag.getInt("slot");

                worldToSlotMap[y][x][z] = slot;
            }

            worldOcclusionMap = new boolean[height][xLength][zLength];
            NBTTagList nbt_worldOcclusionMap = compound.getTagList("worldOcclusionMap", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < nbt_worldOcclusionMap.tagCount(); i++) {
                CompoundTag tag = nbt_worldOcclusionMap.getCompoundTagAt(i);

                int x = tag.getInt("_x");
                int y = tag.getInt("_y");
                int z = tag.getInt("_z");

                worldOcclusionMap[y][x][z] = true;
            }

            CompoundTag inv = compound.getCompoundTag("inventory");
            ItemStackHelper.loadAllItems(inv, inventory);

            // Block Queue
            NBTTagList nbt_blockQueue = compound.getTagList("blockQueue", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < nbt_blockQueue.tagCount(); i++) {
                CompoundTag tag = nbt_blockQueue.getCompoundTagAt(i);
                QueueElement element = new QueueElement();
                element.slot = tag.getInt("slot");
                element.itemStack = new ItemStack(tag.getCompoundTag("item"));
                blockQueue.add(element);
            }

            blockQueueTickCounter = compound.getInt("blockQueueCounter");
        }
    }

    public void initialize(Direction direction) {
        if (!level.isClientSide()) {
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
        this.updateRawBounds(level.getBlockState(getBlockPos()).getValue(BlockController.FACING), rawX, rawY, rawZ);
    }

    public void serverTick() {
        if (origin == null || end == null) {
            return;
        }

        if (shouldShiftInventory) {
            shiftInventory();
            shouldShiftInventory = false;
        }

        blockQueueTickCounter++;

        if (ImpracticalConfig.BLOCK_QUEUE_OPTIONS.blockUpdateRate.get() == -1 || blockQueueTickCounter >= ImpracticalConfig.BLOCK_QUEUE_OPTIONS.blockUpdateRate.get()) {
            BlockPos pos = getBlockPos();
            if (ImpracticalConfig.BLOCK_QUEUE_OPTIONS.blockUpdateBatch.get() == -1) {
                for (int i = 0; i < blockQueue.size(); i++) {
                    QueueElement element = blockQueue.pop();
                    if (!setBlock(element.slot, element.itemStack)) {
                        InventoryHelper.spawnItemStack(level, pos.getX(), pos.getY(), pos.getZ(), element.itemStack.copy());
                    }
                }
            } else {
                for (int i = 0; i < Math.min(blockQueue.size(), CommonProxy.blockUpdateBatch); i++) {
                    QueueElement element = blockQueue.pop();
                    if (!setBlock(element.slot, element.itemStack)) {
                        InventoryHelper.spawnItemStack(level, pos.getX(), pos.getY(), pos.getZ(), element.itemStack.copy());
                    }
                }
            }

            blockQueueTickCounter = 0;
        }

        if (level.getTotalWorldTime() % 10 == 0) {
            // Search for interfaces
            for (int y = -1; y <= height; y++) {
                for (int z = -1; z <= zLength; z++) {
                    for (int x = -1; x <= xLength; x++) {
                        if (y == -1 || y == height || z == -1 || z == zLength || x == -1 || x == xLength) {
                            BlockPos.MutableBlockPos pos = origin.add(x, y, z);
                            if (level.getBlockState(pos).getBlock() == ModBlocks.CONTROLLER_INTERFACE.get()) {
                                TileControllerInterface controllerInterface = (TileControllerInterface) level.getBlockEntity(pos);
                                if (controllerInterface != null) {
                                    controllerInterface.registerController(this);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (scanCounter >= CommonProxy.blockUpdateRate) {
            // Search for blocks to add to inventory
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < zLength; z++) {
                    for (int x = 0; x < xLength; x++) {
                        if (!worldOcclusionMap[y][x][z]) {
                            BlockPos pos = new BlockPos(x, y, z).add(origin);
                            BlockState state = world.getBlockState(pos);
                            Block block = state.getBlock();

                            if (block != ModBlocks.controller && block != ModBlocks.item_block && !world.isAirBlock(pos)) {
                                ItemStack stack = new ItemStack(block, 1, block.damageDropped(state));
                                world.setBlockToAir(pos);

                                int slot = getSlotForPosition(pos);
                                if (slot == -1) {
                                    int i;
                                    for (i = 0; i < totalSize; i++) {
                                        long world = slotToWorldMap[i];
                                        if (world == -1) {
                                            slotToWorldMap[i] = getLongFromPosition(x, y, z);
                                            worldToSlotMap[y][x][z] = i;
                                            break;
                                        }
                                    }

                                    slot = i;
                                }

                                setInventorySlotContents(slot, stack, false, true, true);
                            }
                        }
                    }
                }
            }
        } else {
            scanCounter++;
        }
    }

    public boolean isReady() {
        return origin != null && end != null;
    }

    public void updateOffset(int x, int y, int z) {
        this.offset = new BlockPos(x, y, z);
        this.updateRawBounds(level.getBlockState(getBlockPos()).getValue(BlockController.FACING), rawX, rawY, rawZ);
    }

    public void updateRawBounds(Direction direction, int x, int y, int z) {
        Direction posX = direction.rotateY();
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
        ).add(offset);

        BlockPos high = new BlockPos(
                Math.max(origin.getX(), end.getX()),
                Math.max(origin.getY(), end.getY()),
                Math.max(origin.getZ(), end.getZ())
        ).add(offset);

        setBounds(low, high);
    }

    public void setBounds(BlockPos nOrigin, BlockPos nEnd) {
        INVENTORY_BLOCK = true;

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
                        BlockPos pos = oldOrigin.add(x, y, z);
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
                    BlockState state = level.getBlockState(origin.add(x, y, z));
                    if (state.getBlock() == ModBlocks.PHANTOM.get()) {
                        BlockPhantom.EnumType type = state.getValue(BlockPhantom.TYPE);
                        if (type == BlockPhantom.EnumType.BLOCK) {
                            worldOcclusionMap[y][x][z] = true;
                        } else if (type == BlockPhantom.EnumType.COLUMN) {
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
                        InventoryHelper.spawnItemStack(level, pos.getX(), pos.getY(), pos.getZ(), copy);
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
        TileItemBlock.DROPS = false;

        for (int i = 0; i < totalSize; i++) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty()) {
                BlockPos pos = BlockPos.fromLong(slotToWorldMap[i]).add(origin);
                BlockEntity tileEntity = level.getBlockEntity(pos);

                if (ImpracticalConfig.GENERAL_OPTIONS.dropBlocks.get()) {
                    InventoryHelper.spawnItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                    setBlock(i, ItemStack.EMPTY);
                } else {
                    if (tileEntity instanceof TileItemBlock) {
                        TileItemBlock itemBlock = (TileItemBlock) tileEntity;
                        Item item = getStackForPosition(pos).getItem();
                        if (item instanceof BlockItem && !BlockOverrides.shouldTreatAsItem(item)) {
                            level.setBlock(pos, Block.getBlockFromItem(item).getStateFromMeta(itemBlock.item.getMetadata()), 2);
                        }
                    }
                }
            }
        }

        TileItemBlock.DROPS = true;
    }

    public int getSlotForPosition(BlockPos pos) {
        pos = pos.subtract(origin);
        if (pos.getX() < 0 || pos.getY() < 0 || pos.getZ() < 0)
            return -1;

        try {
            return worldToSlotMap[pos.getY()][pos.getX()][pos.getZ()];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return -1;
        }
    }

    public ItemStack getStackForPosition(BlockPos pos) {
        return getStackInSlot(getSlotForPosition(pos));
    }

    public ItemStack getStackInSlot(int slot) {
        if (slot == -1) return ItemStack.EMPTY;
        return inventory.get(slot);
    }

    public BlockPos getNextRandomPosition() {
        int x = random.nextInt(xLength);
        int y = 0;
        int z = random.nextInt(zLength);

        boolean failed = false;

        BlockPos pos = new BlockPos(x, y, z).add(origin);
        while (worldOcclusionMap[y][x][z] || !level.getBlockState(pos).isAir()) {
            pos = pos.above();
            y++;

            if (y >= height) {
                failed = true;
                break;
            }
        }

        if (failed) return getNextRandomPosition();

        pos = pos.subtract(origin);
        return pos;
    }

    public void setInventorySlotContents(int slot, ItemStack itemStack, boolean shouldShift, boolean shouldUpdateBlock, boolean queueBlockUpdate) {
        if (!itemStack.isEmpty()) {
            if (!sortingType.isBaked()) {
                sortingType.getPositionHandler().runtime(this, slot);
            }
        }

        if (!inventory.isEmpty()) {
            inventory.set(slot, itemStack);
        }

        if (shouldShift) {
            if (itemStack.isEmpty()) {
                shouldShiftInventory = true;
            }
        }

        if (shouldUpdateBlock) {
            if (queueBlockUpdate) {
                blockQueueTickCounter = 0;

                QueueElement element = new QueueElement();
                element.slot = slot;
                element.itemStack = itemStack;
                blockQueue.add(element);
            } else {
                setBlock(slot, itemStack);
            }
        }
    }

    private void shiftInventory() {
        NonNullList<ItemStack> shifted = NonNullList.withSize(totalSize, ItemStack.EMPTY);

        int target = 0;
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                shifted.set(target, stack.copy());
                target++;
            }
        }

        this.inventory = shifted;

        for (int i = 0; i < totalSize; i++) {
            setBlock(i, getStackInSlot(i));
        }
    }

    private boolean setBlock(int slot, ItemStack itemStack) {
        TileItemBlock.DROPS = false;

        if (slot == -1)
            return false;

        if (slot >= slotToWorldMap.length)
            return false;

        long lpos = slotToWorldMap[slot];
        if (lpos == -1)
            return false;

        BlockPos pos = BlockPos.fromLong(lpos);

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        pos = pos.add(origin);

        BlockState state = level.getBlockState(pos);

        if (itemStack.isEmpty()) {
            if (state != null && state.getBlock() == ModBlocks.ITEM_BLOCK.get()) {
                if (sortingType == SortingType.MESSY) {
                    slotToWorldMap[slot] = -1;
                    worldToSlotMap[y][x][z] = -1;
                }

                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        } else {
            if (state != null && state.getBlock() == ModBlocks.ITEM_BLOCK.get()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null && blockEntity instanceof TileItemBlock)
                    ((TileItemBlock) blockEntity).updateItemBlock(itemStack);
            } else {
                level.setBlock(pos, ModBlocks.ITEM_BLOCK.get().defaultBlockState(), 2);
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null && blockEntity instanceof TileItemBlock) {
                    ((TileItemBlock) blockEntity).setController(this);
                    ((TileItemBlock) blockEntity).updateItemBlock(itemStack);
                }
            }
        }

        TileItemBlock.DROPS = true;

        return true;
    }

    public int getRedstoneLevel() {
        int i = 0;
        float f = 0;

        for (int j = 0; j < totalSize; j++) {
            ItemStack stack = getStackInSlot(j);
            if (!stack.isEmpty()) {
                f += (float) stack.getCount() / (float) Math.min(getMaxStackSize(stack), stack.getMaxStackSize());
                ++i;
            }
        }

        f = f / (float) totalSize;

        return (int) Math.floor(f * 14F) + (i > 0 ? 1 : 0);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        return !INVENTORY_BLOCK && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && isReady();
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!INVENTORY_BLOCK && isReady())
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) itemHandler;
        return super.getCapability(capability, facing);
    }
}
