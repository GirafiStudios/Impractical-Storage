package me.dmillerw.impstorage.network;

import me.dmillerw.impstorage.lib.ModInfo;
import me.dmillerw.impstorage.network.packet.SControllerConfig;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author dmillerw
 */
public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.ID);
    public static void initialize() {
        INSTANCE.registerMessage(SControllerConfig.Handler.class, SControllerConfig.class, 1, Side.SERVER);
    }
}
