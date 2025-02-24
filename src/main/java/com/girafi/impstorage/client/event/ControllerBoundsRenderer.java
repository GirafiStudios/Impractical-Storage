package com.girafi.impstorage.client.event;

import com.girafi.impstorage.block.blockentity.ControllerBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayDeque;

public class ControllerBoundsRenderer {
    private static final int CELL_BORDER = FastColor.ARGB32.color(255, 0, 155, 155);
    private static final int YELLOW = FastColor.ARGB32.color(255, 255, 255, 0);

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderBorder(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) { //TODO Check if this works
            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;


            if (level != null) {
                Camera camera = mc.getBlockEntityRenderDispatcher().camera;

                PoseStack poseStack = event.getPoseStack();
                MultiBufferSource buffer = mc.renderBuffers().bufferSource();
                VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lineStrip());

                poseStack.pushPose();

                ArrayDeque<BlockPos[]> boxes = new ArrayDeque<>();
                for (int c = 0; c < 9; ++c) {
                    for (BlockEntity obj : level.getChunkAt(camera.getBlockPosition()).getBlockEntities().values()) {
                        if (obj instanceof ControllerBlockEntity controller) {
                            if (controller.isReady() && controller.showBounds) {
                                BlockPos[] pair = new BlockPos[3];
                                pair[0] = controller.origin;
                                pair[1] = controller.origin.offset(controller.xLength, controller.height, controller.zLength);
                                pair[2] = controller.getBlockPos();

                                boxes.add(pair);
                            } else {
                                return;
                            }
                        }
                    }
                }

                BlockPos[] renderPair;
                while (!boxes.isEmpty()) {
                    renderPair = boxes.pop();

                    BlockPos origin = renderPair[0];
                    BlockPos originOffset = renderPair[1];
                    BlockPos controllerPos = renderPair[2];


                    poseStack.translate(-controllerPos.getX(), -controllerPos.getY(), -controllerPos.getZ()); //TODO Fix translation

                    LevelRenderer.renderLineBox(poseStack, vertexConsumer, origin.getX(), origin.getY(), origin.getZ(), originOffset.getX(), originOffset.getY(), originOffset.getZ(), 1, 1, 1, 1);
                }

                poseStack.translate(0, 0, 0);

                poseStack.popPose();
            }
        }
    }

    private static void renderPing(double px, double py, double pz, PoseStack poseStack, Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(px, py, pz);

        PoseStack.Pose matrixEntry = poseStack.last();
        Matrix4f matrix4f = matrixEntry.pose();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.debugLineStrip(1.0F));
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        System.out.println("Test");

        float min = -0.25F - (0.25F * (float) 20 / 20F);
        float max = 0.25F + (0.25F * (float) 20 / 20F);

        int x = 32 * 1;
        int y = 0;
        float f = (float) (0.009999999776482582D / (double) 256);
        float f1 = (float) (0.009999999776482582D / (double) 256);
        float minU = (float) x / (float) ((double) 256) + f;
        float maxU = (float) (x + 32) / (float) ((double) 256) - f;
        float minV = (float) y / (float) 256 + f1;
        float maxV = (float) (y + 32) / (float) 256 - f1;

        // Block Overlay Background
        renderPosTexColorNoZ(vertexBuilder, matrix4f, min, max, minU, maxV, 150, 150, 150, 255);
        renderPosTexColorNoZ(vertexBuilder, matrix4f, max, max, maxU, maxV, 150, 150, 150, 255);
        renderPosTexColorNoZ(vertexBuilder, matrix4f, max, min, maxU, minV, 150, 150, 150, 255);
        renderPosTexColorNoZ(vertexBuilder, matrix4f, min, min, minU, minV, 150, 150, 150, 255);

        poseStack.popPose();
    }

    public static void renderPosTexColorNoZ(VertexConsumer builder, Matrix4f matrix4f, float x, float y, float u, float v, float r, float g, float b, float a) {
        builder.vertex(matrix4f, x, y, 0).uv(u, v).color(r, g, b, a).endVertex();
    }
}