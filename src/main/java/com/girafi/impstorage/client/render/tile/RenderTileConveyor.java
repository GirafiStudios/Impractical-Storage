package com.girafi.impstorage.client.render.tile;

import com.girafi.impstorage.block.tile.TileConveyor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RenderTileConveyor extends TileEntitySpecialRenderer<TileConveyor> {

    private BlockRendererDispatcher blockRenderer;

    @Override
    public void render(TileConveyor te, double x, double y, double z, float partialTicks, int destroyStage, float _p_render_8) {
        if (blockRenderer == null) blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();

        BlockPos blockpos = te.getPos();
        BlockState BlockState = te.getBlockState();
        if (BlockState == null)
            return;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        RenderHelper.disableStandardItemLighting();

        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(7425);
        } else {
            GlStateManager.shadeModel(7424);
        }

        vertexbuffer.begin(7, DefaultVertexFormats.BLOCK);
        vertexbuffer.setTranslation(x - (double) blockpos.getX() + te.getOffsetX(partialTicks), y - (double) blockpos.getY() + te.getOffsetY(partialTicks), z - (double) blockpos.getZ() + te.getOffsetZ(partialTicks));

        Level world = this.getWorld();

        this.renderStateModel(blockpos.up(), BlockState, vertexbuffer, world, false);

        vertexbuffer.setTranslation(0, 0, 0);

        tessellator.draw();
        RenderHelper.enableStandardItemLighting();
    }

    private boolean renderStateModel(BlockPos pos, BlockState state, BufferBuilder buffer, Level p_188186_4_, boolean checkSides) {
        return this.blockRenderer.getBlockModelRenderer().renderModel(p_188186_4_, this.blockRenderer.getModelForState(state), state, pos, buffer, checkSides);
    }
}
