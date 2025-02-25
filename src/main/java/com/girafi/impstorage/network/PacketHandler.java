package com.girafi.impstorage.network;

import com.girafi.impstorage.lib.ModInfo;
import com.girafi.impstorage.network.packet.ControllerConfigPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ModInfo.ID, "impractical_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void initialize() {
        CHANNEL.messageBuilder(ControllerConfigPacket.class, 0, NetworkDirection.PLAY_TO_SERVER).encoder(ControllerConfigPacket::encode).decoder(ControllerConfigPacket::decode).consumerNetworkThread(ControllerConfigPacket::handle).add();
    }
}