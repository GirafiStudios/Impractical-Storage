package com.girafi.impstorage.client;

import com.girafi.impstorage.block.blockentity.ControllerBlockEntity;
import com.girafi.impstorage.client.event.ControllerBoundsRenderer;
import com.girafi.impstorage.client.render.blockentity.ConveyorBlockRenderer;
import com.girafi.impstorage.client.screen.ControllerScreen;
import com.girafi.impstorage.init.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.common.MinecraftForge;

public class ClientHandler {

    public static void setupClient() {
        BlockEntityRenderers.register(ModBlockEntities.CONVEYOR.get(), ConveyorBlockRenderer::new);

        MinecraftForge.EVENT_BUS.register(ControllerBoundsRenderer.class);
    }

    public static void openControllerScreen(ControllerBlockEntity controller) {
        Minecraft.getInstance().setScreen(new ControllerScreen(controller));
    }
}