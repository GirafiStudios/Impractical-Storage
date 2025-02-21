package com.girafi.impstorage.client.render.tile;

import com.girafi.impstorage.block.tile.TileConveyor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RenderTileConveyor extends TileEntitySpecialRenderer<TileConveyor> {

    private BlockRendererDispatcher blockRenderer;

    @Override
    public void render(TileConveyor te, double x, double y, double z, float partialTicks, int destroyStage, float _p_render_8) {
        if (blockRenderer == null) blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();

        BlockPos blockpos = te.getPos();
        IBlockState iblockstate = te.getBlockState();
        if (iblockstate == null)
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

        World world = this.getWorld();

        this.renderStateModel(blockpos.up(), iblockstate, vertexbuffer, world, false);

        vertexbuffer.setTranslation(0, 0, 0);

        tessellator.draw();
        RenderHelper.enableStandardItemLighting();
    }

    private boolean renderStateModel(BlockPos pos, IBlockState state, BufferBuilder buffer, World p_188186_4_, boolean checkSides) {
        return this.blockRenderer.getBlockModelRenderer().renderModel(p_188186_4_, this.blockRenderer.getModelForState(state), state, pos, buffer, checkSides);
    }
}
