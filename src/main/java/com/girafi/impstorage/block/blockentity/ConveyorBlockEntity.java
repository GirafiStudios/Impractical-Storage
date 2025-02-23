package com.girafi.impstorage.block.blockentity;

import com.girafi.impstorage.block.ConveyorBlock;
import com.girafi.impstorage.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class ConveyorBlockEntity extends BlockEntityCore {
    public float progress = 0.0F;
    public float previousProgress = 0.0F;
    private BlockState conveyorState = Blocks.AIR.defaultBlockState();

    public ConveyorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONVEYOR.get(), pos, state);
    }

    private Direction getFacing(@Nonnull Level level) {
        return level.getBlockState(getBlockPos()).getValue(ConveyorBlock.FACING).getOpposite();
    }

    @OnlyIn(Dist.CLIENT)
    public float getOffsetX(float ticks) {
        if (this.level != null) {
            return (float) this.getFacing(this.level).getOpposite().getStepX() * getProgress(ticks);
        }
        return 0.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public float getOffsetY(float ticks) {
        if (this.level != null) {
            return (float) this.getFacing(this.level).getOpposite().getStepY() * getProgress(ticks);
        }
        return 0.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public float getOffsetZ(float ticks) {
        if (this.level != null) {
            return (float) this.getFacing(this.level).getOpposite().getStepZ() * getProgress(ticks);
        }
        return 0.0F;
    }

    private float getProgress(float ticks) {
        return -(previousProgress + (progress - previousProgress) * ticks);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ConveyorBlockEntity conveyor) {
        final float STEP = 0.1F;
        BlockState conveyorState = conveyor.getConveyorState();

        conveyor.previousProgress = conveyor.progress;

        if (conveyor.progress >= 1.0F) {
            BlockPos inFront = pos.relative(conveyor.getFacing(level));
            BlockState inFrontAbove = level.getBlockState(inFront.above());
            if (inFrontAbove.isAir()) {
                level.setBlock(inFront.above(), conveyorState, 2);

                conveyor.conveyorState = Blocks.AIR.defaultBlockState();
                conveyor.previousProgress = 0F;
                conveyor.progress = 0F;
            }
        } else {
            if (!conveyorState.isAir()) {
                conveyor.progress += STEP;
            } else {
                // Check to see if there's a valid block above
                if (!level.getBlockState(pos.above()).isAir()) {
                    // If so, check to see if we would be placing it on a conveyor, or if there's room
                    BlockPos inFront = pos.relative(conveyor.getFacing(level));

                    if (level.getBlockState(inFront.above()).isAir()) {
                        BlockEntity blockEntity = level.getBlockEntity(inFront);

                        if (blockEntity instanceof ConveyorBlockEntity) {
                            // and the conveyor isn't currently working on anything, we can begin
                            if (((ConveyorBlockEntity) blockEntity).conveyorState.isAir()) {
                                conveyor.conveyorState = level.getBlockState(pos.above());
                            }
                        } else {
                            conveyor.conveyorState = level.getBlockState(pos.above());
                        }

                        if (!conveyor.conveyorState.isAir()) {
                            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
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
        return this.conveyorState;
    }

    @Override
    public void setLevel(@Nonnull Level level) {
        super.setLevel(level);
        if (level.holderLookup(Registries.BLOCK).get(this.conveyorState.getBlock().builtInRegistryHolder().key()).isEmpty()) {
            this.conveyorState = Blocks.AIR.defaultBlockState();
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("state", NbtUtils.writeBlockState(this.conveyorState));
        tag.putFloat("previousProgress", this.previousProgress);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        HolderGetter<Block> holderGetter = this.level != null ? this.level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup();
        this.conveyorState = NbtUtils.readBlockState(holderGetter, tag.getCompound("state"));
        this.previousProgress = tag.getFloat("previousProgress");
        this.progress = previousProgress;
    }
}