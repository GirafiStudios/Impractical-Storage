package com.girafi.impstorage.proxy;

import com.girafi.impstorage.block.tile.TileController;
import com.girafi.impstorage.block.tile.TileControllerInterface;
import com.girafi.impstorage.block.tile.TileConveyor;
import com.girafi.impstorage.block.tile.TileItemBlock;
import com.girafi.impstorage.core.BlockOverrides;
import com.girafi.impstorage.core.handler.GuiHandler;
import com.girafi.impstorage.lib.ModInfo;
import com.girafi.impstorage.network.PacketHandler;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber
public class CommonProxy implements IProxy {

    public static final String CATEGORY_BOUNDS = "bounds";
    public static final String CATEGORY_BLOCK_QUEUE = "block_queue";
    public static final String CATEGORY_RATES = "rates";

    public static int defaultX = 8;
    public static int defaultY = 8;
    public static int defaultZ = 8;

    public static int maxX = 64;
    public static int maxY = 64;
    public static int maxZ = 64;

    public static boolean dropBlocks = false;

    public static int blockUpdateBatch = -1;
    public static int blockUpdateRate = -1;

    public static int zoneUpdateRate = 1;

    public static boolean useBlockQueue() {
        return blockUpdateBatch != -1 && blockUpdateRate != -1;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        BlockOverrides.initialize();

        PacketHandler.initialize();
        GuiHandler.register();

        GameRegistry.registerTileEntity(TileItemBlock.class, ModInfo.ID + ":item_block");
        GameRegistry.registerTileEntity(TileController.class, ModInfo.ID + ":controller");
        GameRegistry.registerTileEntity(TileControllerInterface.class, ModInfo.ID + ":controller_interface");
        GameRegistry.registerTileEntity(TileConveyor.class, ModInfo.ID + ":conveyor");
    }

    @Override
    public void init(FMLInitializationEvent event) {

    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Override
    public void readConfigurationFile(Configuration configuration) {
        configuration.addCustomCategoryComment(CATEGORY_BOUNDS, "Control for the default and max bounds of the controller area");
        configuration.addCustomCategoryComment(CATEGORY_BLOCK_QUEUE, "Block queue will batch and delay the placing of new blocks in the world");
        configuration.addCustomCategoryComment(CATEGORY_RATES, "Various tick rates");

        maxX = configuration.getInt(
                "maxX",
                CATEGORY_BOUNDS,
                maxX,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                "Total max size on the X axis a Controller zone can take up"
        );

        maxY = configuration.getInt(
                "maxY",
                CATEGORY_BOUNDS,
                maxY,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                "Total max size on the Y axis a Controller zone can take up"
        );

        maxZ = configuration.getInt(
                "maxZ",
                CATEGORY_BOUNDS,
                maxZ,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                "Total max size on the Y axis a Controller zone can take up"
        );

        defaultX = getBound(configuration, "defaultX", 8, 1, maxX);
        defaultY = getBound(configuration, "defaultY", 8, 1, maxY);
        defaultZ = getBound(configuration, "defaultZ", 8, 1, maxZ);

        dropBlocks = configuration.getBoolean(
                "dropBlocks",
                Configuration.CATEGORY_GENERAL,
                false,
                "Whether the Controller should drop all Blocks it's holding when broken, or whether it should place them in the world"
        );

        blockUpdateBatch = configuration.getInt(
                "blockUpdateBatchCount",
                CATEGORY_BLOCK_QUEUE,
                -1,
                -1,
                Integer.MAX_VALUE,
                "How many blocks should be placed each time an update is triggered (based on blockUpdateRate). If set to -1, blocks will simply be set as they're added to the inventory");

        blockUpdateRate = configuration.getInt(
                "blockUpdateRate",
                CATEGORY_BLOCK_QUEUE,
                -1,
                -1,
                Integer.MAX_VALUE,
                "How often (in ticks) should new blocks be placed. If set to -1, blocks will simply be set as they're added to the inventory");

        zoneUpdateRate = configuration.getInt(
                "zoneUpdateRate",
                CATEGORY_RATES,
                1,
                1,
                Integer.MAX_VALUE,
                "How often (in ticks) should new Blocks placed in a Controller's area (by player, machine, etc) be added to the inventory"
        );
    }

    private static int getBound(Configuration configuration, String key, int defaultValue, int min, int max) {
        return clamp(configuration.getInt(
                key,
                CATEGORY_BOUNDS,
                defaultValue,
                min,
                max,
                ""
        ), min, max);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) value = min;
        if (value > max) value = max;
        return value;
    }
}
