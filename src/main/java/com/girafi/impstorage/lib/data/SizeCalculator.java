package com.girafi.impstorage.lib.data;

import com.girafi.impstorage.block.blockentity.ControllerBlockEntity;
import com.girafi.impstorage.init.ModBlocks;
import net.minecraft.core.BlockPos;

public abstract class SizeCalculator {

    private static boolean isBlockOccluded(ControllerBlockEntity tile, int x, int y, int z) {
        if (tile.getLevel() == null) return false;
        return tile.worldOcclusionMap[y][x][z] || (!tile.getLevel().getBlockState(tile.origin.offset(x, y, z)).isAir() && tile.getLevel().getBlockState(new BlockPos(x, y, z)).getBlock() != ModBlocks.ITEM_BLOCK.get());
    }

    public static final SizeCalculator DEFAULT = new SizeCalculator() {

        @Override
        public void calculate(ControllerBlockEntity tile) {
            int occludedSpots = 0;
            for (int y = 0; y < tile.height; y++) {
                for (int z = 0; z < tile.zLength; z++) {
                    for (int x = 0; x < tile.xLength; x++) {
                        if (SizeCalculator.isBlockOccluded(tile, x, y, z)) {
                            tile.worldOcclusionMap[y][x][z] = true;
                            occludedSpots++;
                        }
                    }
                }
            }

            tile.totalSize = (tile.height * tile.xLength * tile.zLength) - occludedSpots;
        }
    };

    /*public static final SizeCalculator PYRAMID = new SizeCalculator() { //TODO Reimplement

        @Override
        public void calculate(TileController tile) {
            int size = 0;
            int occludedSpots = 0;
            for (int y = 0; y < tile.height; y++) {
                for (int x = 0; x < tile.xLength - y; x++) {
                    for (int z = y; z < tile.zLength - y; z++) {
                        if (SizeCalculator.isBlockOccluded(tile, x, y, z)) {
                            tile.worldOcclusionMap[y][x][z] = true;
                            occludedSpots++;
                        } else {
                            size++;
                        }
                    }
                }
            }

            tile.totalSize = size;
        }
    };*/

    public void calculate(ControllerBlockEntity tile) {
    }
}