package com.girafi.impstorage.block.tile;

import com.girafi.impstorage.block.BlockConveyor;
import com.girafi.impstorage.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileConveyor extends TileCore {
    public float progress = 0.0F;
    public float previousProgress = 0.0F;
    public BlockState conveyorState = null;

    public TileConveyor(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONVEYOR.get(), pos, state);
    }

    private Direction getFacing() {
        return level.getBlockState(getBlockPos()).getValue(BlockConveyor.FACING).getOpposite();
    }

    @OnlyIn(Dist.CLIENT)
    public float getOffsetX(float ticks) {
        return (float) this.getFacing().getOpposite().getFrontOffsetX() * getProgress(ticks);
    }

    @OnlyIn(Dist.CLIENT)
    public float getOffsetY(float ticks) {
        return (float) this.getFacing().getOpposite().getFrontOffsetY() * getProgress(ticks);
    }

    @OnlyIn(Dist.CLIENT)
    public float getOffsetZ(float ticks) {
        return (float) this.getFacing().getOpposite().getFrontOffsetZ() * getProgress(ticks);
    }

    private float getProgress(float ticks) {
        return -(previousProgress + (progress - previousProgress) * ticks);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileConveyor conveyor) {
        final float STEP = 0.1F;

        conveyor.previousProgress = conveyor.progress;
        if (conveyor.progress >= 1.0F) {
            BlockPos inFront = pos.relative(conveyor.getFacing());
            if (level.getBlockState(inFront.above()).isAir()) {
                level.setBlock(inFront.above(), conveyor.getConveyorState(), 2);

                conveyor.conveyorState = null;
                conveyor.previousProgress = 0F;
                conveyor.progress = 0F;

                conveyor.markDirtyAndNotify();
            }
        } else {
            if (conveyor.getConveyorState() != null) {
                conveyor.progress += STEP;
            } else {
                // Check to see if there's a valid block above
                if (!level.getBlockState(pos.above()).isAir()) {
                    // If so, check to see if we would be placing it on a conveyor, or if there's room
                    BlockPos inFront = pos.relative(conveyor.getFacing());

                    if (level.getBlockState(inFront.above()).isAir()) {
                        BlockEntity blockEntity = level.getBlockEntity(inFront);

                        if (blockEntity instanceof TileConveyor) {
                            // and the conveyor isn't currently working on anything, we can begin
                            if (((TileConveyor) blockEntity).conveyorState == null) {
                                conveyor.conveyorState = level.getBlockState(pos.above());
                            }
                        } else {
                            conveyor.conveyorState = level.getBlockState(pos.above());
                        }

                        if (conveyor.conveyorState != null) {
                            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
                            conveyor.markDirtyAndNotify();
                        }
                    }
                } else {
                    conveyor.previousProgress = 0F;
                    conveyor.progress = 0F;
                }
            }
        }
    }

    public BlockState getConveyorState() {
        return conveyorState;
    }

    @Override
    public void writeToDisk(CompoundTag compound) {
        if (conveyorState != null) {
            compound.putInt("stateId", Block.getIdFromBlock(conveyorState.getBlock()));
            compound.putInt("stateMeta", conveyorState.getBlock().getMetaFromState(conveyorState));
        }

        compound.putFloat("previousProgress", previousProgress);
    }

    @Override
    public void readFromDisk(CompoundTag compound) {
        if (compound.contains("stateId") && compound.contains("stateMeta")) {
            this.conveyorState = Block.getBlockById(compound.getInt("stateId")).getStateFromMeta(compound.getInt("stateMeta"));
        } else {
            this.conveyorState = null;
        }

        previousProgress = compound.getFloat("previousProgress");
        progress = previousProgress;
    }
}