package com.girafi.impstorage.client.render.blockentity;

import com.girafi.impstorage.block.ConveyorBlock;
import com.girafi.impstorage.block.blockentity.ConveyorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class ConveyorBlockRenderer<T extends ConveyorBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockRenderDispatcher blockRenderer;

    public ConveyorBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(@Nonnull T conveyor, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        Level level = conveyor.getLevel();
        BlockPos pos = conveyor.getBlockPos();
        BlockState conveyorState = conveyor.getConveyorState();

        if (conveyorState == null) return;

        if (level != null && level.getBlockEntity(pos) instanceof ConveyorBlockEntity) {
            poseStack.pushPose();

            poseStack.translate(conveyor.getOffsetX(partialTicks), conveyor.getOffsetY(partialTicks) + 1, conveyor.getOffsetZ(partialTicks));

            this.renderBlock(pos.above(), conveyorState, poseStack, buffer, level, false, combinedOverlay);

            poseStack.translate(0, 0, 0);

            poseStack.popPose();
        }
    }


    private void renderBlock(BlockPos pos, BlockState state, PoseStack poseStack, MultiBufferSource buffer, Level level, boolean b, int combinedOverlay) {
        RenderType renderType = ItemBlockRenderTypes.getMovingBlockRenderType(state);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        this.blockRenderer.getModelRenderer().tesselateBlock(level, this.blockRenderer.getBlockModel(state), state, pos, poseStack, vertexConsumer, b, RandomSource.create(), state.getSeed(pos), combinedOverlay);
    }
}