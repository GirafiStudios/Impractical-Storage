package com.girafi.impstorage.network;

import com.girafi.impstorage.lib.ModInfo;
import com.girafi.impstorage.network.packet.SControllerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ModInfo.ID, "impractical_channel"))
            .clientAcceptedVersions(v -> true)
            .serverAcceptedVersions(v -> true)
            .networkProtocolVersion(() -> "IMPSTORAGE1")
            .simpleChannel();

    public static void initialize() {
        CHANNEL.registerMessage(0, SControllerConfig.class, SControllerConfig::encode, SControllerConfig::decode, SControllerConfig.Handler::handle);
    }
}