package com.girafi.impstorage.network.packet;

import com.girafi.impstorage.block.ControllerBlock;
import com.girafi.impstorage.block.blockentity.ControllerBlockEntity;
import com.girafi.impstorage.lib.data.SortingType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SControllerConfig {
    protected BlockPos destination;

    protected int boundX;
    protected int boundY;
    protected int boundZ;

    protected int offsetX;
    protected int offsetY;
    protected int offsetZ;

    protected SortingType sortingType;

    private boolean dimensions;
    private boolean offset;
    private boolean sort;

    public SControllerConfig(BlockPos destination, boolean dimensions, int boundX, int boundY, int boundZ, boolean offset, int offsetX, int offsetY, int offsetZ, boolean sort, SortingType sortingType) {
        this.destination = destination;
        this.dimensions = dimensions;
        this.boundX = boundX;
        this.boundY = boundY;
        this.boundZ = boundZ;
        this.offset = offset;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.sort = sort;
        this.sortingType = sortingType;
    }

    public static void encode(SControllerConfig packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.destination);

        buf.writeBoolean(packet.dimensions);
        if (packet.dimensions) {
            buf.writeInt(packet.boundX);
            buf.writeInt(packet.boundY);
            buf.writeInt(packet.boundZ);
        }

        buf.writeBoolean(packet.offset);
        if (packet.offset) {
            buf.writeInt(packet.offsetX);
            buf.writeInt(packet.offsetY);
            buf.writeInt(packet.offsetZ);
        }

        buf.writeBoolean(packet.sort);
        if (packet.sort) {
            buf.writeInt(packet.sortingType.ordinal());
        }
    }

    public static SControllerConfig decode(FriendlyByteBuf buf) {
        return new SControllerConfig(buf.readBlockPos(),
                buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readBoolean(), SortingType.VALUES[buf.readInt()]);
    }

    public static class Handler {
        public static void handle(SControllerConfig message, Supplier<NetworkEvent.Context> ctx) {
                Level level = ctx.get().getSender().level();
                BlockState state = level.getBlockState(message.destination);
                BlockEntity blockEntity = level.getBlockEntity(message.destination);
                if (blockEntity != null && blockEntity instanceof ControllerBlockEntity controller) {

                    if (message.sort) {
                        System.out.println("setSortingType");
                        controller.setSortingType(message.sortingType);
                    }

                    if (!controller.isInventoryEmpty()) {
                        return;
                    }

                    if (message.dimensions) {
                        System.out.println("dimensions");
                        controller.updateRawBounds(state.getValue(ControllerBlock.FACING), message.boundX, message.boundY, message.boundZ);
                    }

                    if (message.offset) {
                        System.out.println("updateOffset");
                        controller.updateOffset(message.offsetX, message.offsetY, message.offsetZ);
                    }

                    controller.markDirtyAndNotify();
                }
            ctx.get().setPacketHandled(true);
        }
    }
}