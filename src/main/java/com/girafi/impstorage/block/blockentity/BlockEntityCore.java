package com.girafi.impstorage.block.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class BlockEntityCore extends BlockEntity {

    public BlockEntityCore(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = this.saveWithoutMetadata();
        super.serializeNBT();
        writeToDisk(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(@Nonnull CompoundTag compound) {
        super.deserializeNBT(compound);
        readFromDisk(compound);
    }

    public void writeToDisk(CompoundTag compound) {
    }

    public void readFromDisk(CompoundTag compound) {
    }

    public void markDirtyAndNotify() {
        if (this.level != null) {
            BlockState state = level.getBlockState(getBlockPos());
            level.setBlocksDirty(getBlockPos(), state, state);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
}