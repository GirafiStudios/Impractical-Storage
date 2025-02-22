package com.girafi.impstorage.block.tile;

import com.girafi.impstorage.block.ConveyorBlock;
import com.girafi.impstorage.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ConveyorBlockEntity extends BlockEntityCore {
    public float progress = 0.0F;
    public float previousProgress = 0.0F;
    public BlockState conveyorState = null;

    public ConveyorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONVEYOR.get(), pos, state);
    }

    private Direction getFacing() {
        return level.getBlockState(getBlockPos()).getValue(ConveyorBlock.FACING).getOpposite();
    }

    @OnlyIn(Dist.CLIENT)
    public float getOffsetX(float ticks) {
        return (float) this.getFacing().getStepX() * getProgress(ticks);
    }

    @OnlyIn(Dist.CLIENT)
    public float getOffsetY(float ticks) {
        return (float) this.getFacing().getStepY() * getProgress(ticks);
    }

    @OnlyIn(Dist.CLIENT)
    public float getOffsetZ(float ticks) {
        return (float) this.getFacing().getStepZ() * getProgress(ticks);
    }

    private float getProgress(float ticks) {
        return -(previousProgress + (progress - previousProgress) * ticks);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ConveyorBlockEntity conveyor) {
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

                        if (blockEntity instanceof ConveyorBlockEntity) {
                            // and the conveyor isn't currently working on anything, we can begin
                            if (((ConveyorBlockEntity) blockEntity).conveyorState == null) {
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
    public void writeToDisk(CompoundTag tag) {
        if (conveyorState != null) {
            tag.put("state", NbtUtils.writeBlockState(this.conveyorState));
        }

        tag.putFloat("previousProgress", this.previousProgress);
    }

    @Override
    public void readFromDisk(CompoundTag tag) {
        if (tag.contains("state") && this.level != null) {
            this.conveyorState = NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), tag.getCompound("state"));
        } else {
            this.conveyorState = null;
        }

        this.previousProgress = tag.getFloat("previousProgress");
        this.progress = previousProgress;
    }
}