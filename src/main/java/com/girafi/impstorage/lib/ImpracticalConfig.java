package com.girafi.impstorage.lib;

import net.minecraftforge.common.ForgeConfigSpec;

public class ImpracticalConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final BlockQueueOptions BLOCK_QUEUE_OPTIONS = new BlockQueueOptions(BUILDER);
    public static final BoundsOptions BOUNDS_OPTIONS = new BoundsOptions(BUILDER);
    public static final GeneralOptions GENERAL_OPTIONS = new GeneralOptions(BUILDER);

    public static class BlockQueueOptions {
        static final String BLOCK_QUEUE_OPTIONS = "block queue";
        public ForgeConfigSpec.IntValue blockUpdateBatch;
        public ForgeConfigSpec.IntValue blockUpdateRate;

        BlockQueueOptions(ForgeConfigSpec.Builder builder) {
            builder.push(BLOCK_QUEUE_OPTIONS).comment("Block queue will batch and delay the placing of new blocks in the world");
            blockUpdateBatch = builder.defineInRange("How many blocks should be placed each time an update is triggered (based on blockUpdateRate). If set to -1, blocks will simply be set as they're added to the inventory", -1, -1, Integer.MAX_VALUE);
            blockUpdateRate = builder.defineInRange("How often (in ticks) should new blocks be placed. If set to -1, blocks will simply be set as they're added to the inventory", -1, -1, Integer.MAX_VALUE);
        }
    }

    public static class BoundsOptions {
        static final String BOUNDS_OPTIONS = "bounds";
        public ForgeConfigSpec.IntValue defaultX;
        public ForgeConfigSpec.IntValue defaultY;
        public ForgeConfigSpec.IntValue defaultZ;
        public ForgeConfigSpec.IntValue maxX;
        public ForgeConfigSpec.IntValue maxY;
        public ForgeConfigSpec.IntValue maxZ;

        BoundsOptions(ForgeConfigSpec.Builder builder) {
            builder.push(BOUNDS_OPTIONS).comment("Control for the default and max bounds of the controller area");
            defaultX = builder.defineInRange("defaultX", 8, 1, 512);
            defaultY = builder.defineInRange("defaultY", 8, 1, 512);
            defaultZ = builder.defineInRange("defaultZ", 8, 1, 512);
            maxX = builder.defineInRange("Total max size on the X axis a Controller zone can take up", 64, Integer.MIN_VALUE, Integer.MAX_VALUE);
            maxY = builder.defineInRange("Total max size on the Y axis a Controller zone can take up", 64, Integer.MIN_VALUE, Integer.MAX_VALUE);
            maxZ = builder.defineInRange("Total max size on the Z axis a Controller zone can take up", 64, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
    }

    public static class GeneralOptions {
        static final String GENERAL_OPTIONS = "general";
        public ForgeConfigSpec.BooleanValue dropBlocks;
        public ForgeConfigSpec.IntValue zoneUpdateRate;

        GeneralOptions(ForgeConfigSpec.Builder builder) {
            builder.push(GENERAL_OPTIONS);
            dropBlocks = builder.define("Whether the Controller should drop all Blocks it's holding when broken, or whether it should place them in the world", false);
            zoneUpdateRate = builder.defineInRange("How often (in ticks) should new Blocks placed in a Controller's area (by player, machine, etc) be added to the inventory", 1, 1, Integer.MAX_VALUE);
        }
    }

    public static ForgeConfigSpec spec = BUILDER.build();
}