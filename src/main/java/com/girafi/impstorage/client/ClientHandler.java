package com.girafi.impstorage.client;

import com.girafi.impstorage.block.tile.TileConveyor;
import com.girafi.impstorage.client.event.ControllerBoundsRenderer;
import com.girafi.impstorage.client.model.BaseModelLoader;
import com.girafi.impstorage.client.render.tile.RenderTileConveyor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.MinecraftForge;

public class ClientHandler {

    public static void setupClient() {
        ModelLoaderRegistry.registerLoader(new BaseModelLoader());

        ClientRegistry.bindTileEntitySpecialRenderer(TileConveyor.class, new RenderTileConveyor());

        MinecraftForge.EVENT_BUS.register(ControllerBoundsRenderer.class);
    }
}
