package com.girafi.impstorage.client.event;

import com.girafi.impstorage.block.tile.TileController;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;

public class ControllerBoundsRenderer {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onWorldRenderLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Entity entity = mc.getRenderViewEntity();

        double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.getPartialTicks();
        double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.getPartialTicks();
        double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.getPartialTicks();

        GlStateManager.translate(-posX, -posY, -posZ);
        GlStateManager.glLineWidth(2.5F);

        Level level = entity.level();
        int x1 = (int) entity.getX();
        int z1 = (int) entity.getZ();

        LevelChunk chunks[] = new Chunk[9];

        chunks[4] = level.getChunkAt(new BlockPos(x1, 1, z1));
        int cX = chunks[4].x;
        int cZ = chunks[4].z;

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
                if (obj instanceof TileController) {
                    TileController controller = (TileController) obj;
                    if (controller.isReady() && controller.showBounds) {
                        BlockPos[] pair = new BlockPos[2];
                        pair[0] = controller.origin;
                        pair[1] = controller.origin.add(controller.xLength, controller.height, controller.zLength);
                        boxes.add(pair);
                    }
                }
            }
        }

        BlockPos[] renderPair;
        while (boxes.size() > 0) {
            renderPair = boxes.pop();

            BlockPos start = renderPair[0];
            BlockPos end = renderPair[1];

            RenderGlobal.drawBoundingBox(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ(), 1, 1, 1, 1);
        }

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GL11.glPopAttrib();
        GlStateManager.popMatrix();
    }
}