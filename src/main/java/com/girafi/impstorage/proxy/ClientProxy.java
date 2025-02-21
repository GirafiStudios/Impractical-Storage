package com.girafi.impstorage.proxy;

import com.girafi.impstorage.block.tile.TileConveyor;
import com.girafi.impstorage.client.event.ControllerBoundsRenderer;
import com.girafi.impstorage.client.model.BaseModelLoader;
import com.girafi.impstorage.client.render.tile.RenderTileConveyor;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        ModelLoaderRegistry.registerLoader(new BaseModelLoader());

        ClientRegistry.bindTileEntitySpecialRenderer(TileConveyor.class, new RenderTileConveyor());

        MinecraftForge.EVENT_BUS.register(ControllerBoundsRenderer.class);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public void readConfigurationFile(Configuration configuration) {
        super.readConfigurationFile(configuration);
    }
}
