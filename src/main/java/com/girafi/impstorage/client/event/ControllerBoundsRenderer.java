package com.girafi.impstorage.client.event;

import com.girafi.impstorage.block.blockentity.ControllerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;

public class ControllerBoundsRenderer {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderBorder(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            Level level = mc.level;

            if (level != null) {
                Camera camera = mc.getBlockEntityRenderDispatcher().camera;

                PoseStack poseStack = event.getPoseStack();
                MultiBufferSource buffer = mc.renderBuffers().bufferSource();
                VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());

                //Check nearby chunks, to not only render in the same chunk the Controller is in
                LevelChunk[] chunks = new LevelChunk[9];
                chunks[4] = level.getChunkAt(new BlockPos(camera.getBlockPosition().getX(), 1, camera.getBlockPosition().getZ()));
                int cX = chunks[4].getPos().x;
                int cZ = chunks[4].getPos().z;

                chunks[0] = level.getChunk(cX - 1, cZ - 1);
                chunks[1] = level.getChunk(cX, cZ - 1);
                chunks[2] = level.getChunk(cX + 1, cZ - 1);

                chunks[3] = level.getChunk(cX - 1, cZ);
                chunks[5] = level.getChunk(cX + 1, cZ);

                chunks[6] = level.getChunk(cX - 1, cZ + 1);
                chunks[7] = level.getChunk(cX, cZ + 1);
                chunks[8] = level.getChunk(cX + 1, cZ + 1);

                HashMap<BlockPos[], BlockPos> boxes = new HashMap<>();
                for (int c = 0; c < 9; ++c) {
                    for (BlockEntity obj : chunks[c].getBlockEntities().values()) {
                        if (obj instanceof ControllerBlockEntity controller) {
                            if (controller.isReady() && controller.showBounds) {
                                BlockPos[] pair = new BlockPos[2];
                                pair[0] = controller.origin;
                                pair[1] = controller.origin.offset(controller.xLength, controller.height, controller.zLength);

                                boxes.put(pair, controller.getBlockPos());
                            } else {
                                return;
                            }
                        }
                    }
                }

                if (!boxes.isEmpty()) {
                    boxes.forEach((renderPair, controllerPos) -> {
                        poseStack.pushPose();
                        BlockPos origin = renderPair[0];
                        BlockPos originOffset = renderPair[1];

                        poseStack.translate(-controllerPos.getX(), -controllerPos.getY(), -controllerPos.getZ());
                        poseStack.translate(controllerPos.getX() - camera.getPosition().x, controllerPos.getY() - camera.getPosition().y, controllerPos.getZ() - camera.getPosition().z);

                        LevelRenderer.renderLineBox(poseStack, vertexConsumer, origin.getX(), origin.getY(), origin.getZ(), originOffset.getX(), originOffset.getY(), originOffset.getZ(), 1, 1, 1, 1);
                        poseStack.popPose();
                    });
                }
            }
        }
    }
}