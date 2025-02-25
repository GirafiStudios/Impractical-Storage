package com.girafi.impstorage.network.packet;

import com.girafi.impstorage.block.ControllerBlock;
import com.girafi.impstorage.block.blockentity.ControllerBlockEntity;
import com.girafi.impstorage.lib.data.SortingType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ControllerConfigPacket {
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

    public ControllerConfigPacket(BlockPos destination, boolean dimensions, int boundX, int boundY, int boundZ, boolean offset, int offsetX, int offsetY, int offsetZ, boolean sort, SortingType sortingType) {
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

    public static void encode(ControllerConfigPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.destination);

        buf.writeBoolean(packet.dimensions);
        buf.writeInt(packet.boundX);
        buf.writeInt(packet.boundY);
        buf.writeInt(packet.boundZ);


        buf.writeBoolean(packet.offset);
        buf.writeInt(packet.offsetX);
        buf.writeInt(packet.offsetY);
        buf.writeInt(packet.offsetZ);

        buf.writeBoolean(packet.sort);
        buf.writeInt(packet.sortingType.ordinal());
    }

    public static ControllerConfigPacket decode(FriendlyByteBuf buf) {
        return new ControllerConfigPacket(buf.readBlockPos(),
                buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readBoolean(), SortingType.VALUES[buf.readInt()]);
    }

    public static boolean handle(ControllerConfigPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            //Sync changes on client, to make them change properly in the Controller Screen
            handleSync(message, Minecraft.getInstance().level);

            //Sync changes to make sure they get saved for NBT properly
            Level level = ctx.get().getSender().level();
            if (level != null) {
                handleSync(message, level);
            }
        });
        return true;
    }

    protected static void handleSync(ControllerConfigPacket message, Level level) {
        if (level != null) {
            BlockState state = level.getBlockState(message.destination);
            BlockEntity blockEntity = level.getBlockEntity(message.destination);
            if (blockEntity instanceof ControllerBlockEntity controller) {

                if (message.sort) {
                    controller.setSortingType(message.sortingType);
                }

                if (!controller.isInventoryEmpty()) {
                    return;
                }

                if (message.dimensions) {
                    controller.updateRawBounds(state.getValue(ControllerBlock.FACING), message.boundX, message.boundY, message.boundZ);
                }

                if (message.offset) {
                    controller.updateOffset(message.offsetX, message.offsetY, message.offsetZ);
                }
                controller.setChanged();
            }
        }
    }
}