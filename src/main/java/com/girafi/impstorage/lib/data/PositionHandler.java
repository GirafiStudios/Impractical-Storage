package com.girafi.impstorage.lib.data;

import com.girafi.impstorage.block.blockentity.ControllerBlockEntity;
import net.minecraft.core.BlockPos;

public abstract class PositionHandler {
    public static final byte BAKED = 0;
    public static final byte RUNTIME = 1;

    public static final PositionHandler ROW_HANDLER = new PositionHandler() {

        @Override
        public void bake(ControllerBlockEntity tile) {
            int slot = 0;
            for (int y = 0; y < tile.height; y++) {
                for (int z = 0; z < tile.zLength; z++) {
                    for (int x = 0; x < tile.xLength; x++) {
                        if (!tile.worldOcclusionMap[y][x][z]) {
                            tile.slotToWorldMap[slot] = ControllerBlockEntity.getLongFromPosition(x, y, z);
                            tile.worldToSlotMap[y][x][z] = slot;

                            slot++;
                        }
                    }
                }
            }
        }
    };

    public static final PositionHandler COLUMN_HANDLER = new PositionHandler() {

        @Override
        public void bake(ControllerBlockEntity tile) {
            int slot = 0;
            for (int x = 0; x < tile.xLength; x++) {
                for (int z = 0; z < tile.zLength; z++) {
                    for (int y = 0; y < tile.height; y++) {
                        if (!tile.worldOcclusionMap[y][x][z]) {
                            tile.slotToWorldMap[slot] = ControllerBlockEntity.getLongFromPosition(x, y, z);
                            tile.worldToSlotMap[y][x][z] = slot;

                            slot++;
                        }
                    }
                }
            }
        }
    };

    /*public static final PositionHandler PYRAMID_HANDLER = new PositionHandler() { //TODO Reimplement
        @Override
        public void bake(TileController tile) {
            int slot = 0;
            for (int y = 0; y < tile.height; y++) {
                for (int x = y; x < tile.xLength - y; x++) {
                    for (int z = y; z < tile.zLength - y; z++) {
                        if (!tile.worldOcclusionMap[y][x][z]) {
                            tile.slotToWorldMap[slot] = TileController.getLongFromPosition(x, y, z);
                            tile.worldToSlotMap[y][x][z] = slot;

                            slot++;
                        }
                    }
                }
            }
        }
    };*/

    public static final PositionHandler MESSY_HANDLER = new PositionHandler() {

        @Override
        public void runtime(ControllerBlockEntity tile, int slot) {
            long longPos = tile.slotToWorldMap[slot];
            if (longPos == -1) {
                BlockPos pos = tile.getNextRandomPosition();
                tile.slotToWorldMap[slot] = pos.asLong();
                tile.worldToSlotMap[pos.getY()][pos.getX()][pos.getZ()] = slot;
            }
        }
    };

    public void bake(ControllerBlockEntity tile) {
    }

    public void runtime(ControllerBlockEntity tile, int slot) {
    }
}