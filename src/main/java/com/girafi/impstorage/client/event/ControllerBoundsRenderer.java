package com.girafi.impstorage.client.event;

import com.girafi.impstorage.block.blockentity.ControllerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayDeque;

public class ControllerBoundsRenderer {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderBorder(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) { //TODO Check if this works
            Minecraft mc = Minecraft.getInstance();
            if (mc.cameraEntity == null) {
                return;
            }
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource buffer = mc.renderBuffers().bufferSource();
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());

            poseStack.pushPose();
            //GlStateManager.disableTexture2D();
            //GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
            //GlStateManager.disableLighting();
            //GlStateManager.enableBlend();
            //GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            Entity entity = mc.cameraEntity;

            /*double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.getPartialTick(); //TODO Is this part needed?
            double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.getPartialTick();
            double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.getPartialTick();

            poseStack.translate(-posX, -posY, -posZ);*/
            //GlStateManager.glLineWidth(2.5F);

            Level level = entity.level();
            int x1 = (int) entity.getX();
            int z1 = (int) entity.getZ();

            LevelChunk[] chunks = new LevelChunk[9];
            ChunkPos chunkPos = entity.chunkPosition();

            chunks[4] = level.getChunkAt(new BlockPos(x1, 1, z1));
            int cX = chunkPos.x; //TODO Not sure if this works
            int cZ = chunkPos.z; //TODO Not sure if this works

            chunks[0] = level.getChunk(cX - 1, cZ - 1);
            chunks[1] = level.getChunk(cX, cZ - 1);
            chunks[2] = level.getChunk(cX + 1, cZ - 1);

            chunks[3] = level.getChunk(cX - 1, cZ);
            chunks[5] = level.getChunk(cX + 1, cZ);

            chunks[6] = level.getChunk(cX - 1, cZ + 1);
            chunks[7] = level.getChunk(cX, cZ + 1);
            chunks[8] = level.getChunk(cX + 1, cZ + 1);

            ArrayDeque<BlockPos[]> boxes = new ArrayDeque<>();
            for (int c = 0; c < 9; ++c) {
                for (BlockEntity obj : chunks[c].getBlockEntities().values()) {
                    if (obj instanceof ControllerBlockEntity controller) {
                        if (controller.isReady() && controller.showBounds) {
                            BlockPos[] pair = new BlockPos[2];
                            pair[0] = controller.origin;
                            pair[1] = controller.origin.offset(controller.xLength, controller.height, controller.zLength);
                            boxes.add(pair);
                        }
                    }
                }
            }

            BlockPos[] renderPair;
            while (!boxes.isEmpty()) {
                renderPair = boxes.pop();

                BlockPos start = renderPair[0];
                BlockPos end = renderPair[1];

                LevelRenderer.renderLineBox(poseStack, vertexConsumer, start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ(), 1, 1, 1, 1);
            }

            //GlStateManager.enableTexture2D();
            //GlStateManager.enableLighting();
            //GlStateManager.disableBlend();
            //GL11.glPopAttrib();
            poseStack.popPose();
        }
    }
}