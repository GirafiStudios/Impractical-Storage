package com.girafi.impstorage.client.model;

import com.girafi.impstorage.block.BlockItemBlock;
import com.girafi.impstorage.core.BlockOverrides;
import com.girafi.impstorage.init.ModBlocks;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ItemBlockBakedModel implements IDynamicBakedModel {

    @OnlyIn(Dist.CLIENT)
    private static BlockEntityRenderDispatcher rendererDispatcher() {
        return Minecraft.getInstance().getBlockEntityRenderDispatcher();
    }

    @OnlyIn(Dist.CLIENT)
    private static ItemRenderer renderItem() {
        return Minecraft.getInstance().getItemRenderer();
    }

    private Set<String> renderBlacklist = Sets.newHashSet();

    private VertexFormat format;
    private TextureAtlasSprite wood;

    public ItemBlockBakedModel(VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this.format = format;
        this.wood = bakedTextureGetter.apply(new ResourceLocation("impstorage:blocks/crate_wood"));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @org.jetbrains.annotations.Nullable RenderType renderType) {
        boolean error = false;
        List<BakedQuad> quads = Lists.newArrayList();

        ItemStack stack = state.getValue(BlockItemBlock.ITEM);

        if (stack == null || stack.isEmpty() || stack.getItem() == null) {
            error = true;
        }

        if (!error) {
            try {
                quads = safeGetQuads(state, side, rand);
            } catch (Exception ex) {
                renderBlacklist.add(state.getValue(BlockItemBlock.ITEM).getItem().getRegistryName().toString());
                error = true;
            }
        }

        if (error) {
            BlockState s = ModBlocks.WOOD_CRATE.get().defaultBlockState();
            quads = rendererDispatcher().getModelForState(s).getQuads(s, side, rand);
        }

        return quads;
    }

    @OnlyIn(Dist.CLIENT)
    private List<BakedQuad> safeGetQuads(@Nullable BlockState state, @Nullable Direction side, long rand) {
        ItemStack itemStack = state.getValue(BlockItemBlock.ITEM);

        List<BakedQuad> quads = Lists.newArrayList();

        Block renderBlock;
        if (itemStack.isEmpty() || renderBlacklist.contains(itemStack.getItem().getRegistryName().toString())) {
            renderBlock = ModBlocks.WOOD_CRATE.get();
        } else {
            if (BlockOverrides.shouldTreatAsItem(itemStack.getItem())) {
                renderBlock = ModBlocks.WOOD_CRATE.get();

                BakedModel model = renderItem().getItemModelMesher().getItemModel(itemStack);
                TextureAtlasSprite texture = model.getParticleTexture();
                if (texture == null)
                    texture = rendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel().getParticleTexture();

                BlockPartFace blockPartFace = new BlockPartFace(side, 0, texture.toString(), new BlockFaceUV(new float[]{0, 0, 16, 16}, 0));
                BlockPartRotation blockPartRotation = new BlockPartRotation(new Vector3f(0, 0, 0), Direction.Axis.X, 0, false);

                final float shrink = 2.5F;

                if (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT) {
                    if (side != null) {
                        if (side != Direction.UP && side != Direction.DOWN) {
                            final float minX = side == Direction.EAST || side == Direction.WEST ? -0.005F : shrink;
                            final float maxX = side == Direction.EAST || side == Direction.WEST ? 16.005f : 16 - shrink;
                            final float minZ = side == Direction.EAST || side == Direction.WEST ? shrink : -0.005F;
                            final float maxZ = side == Direction.EAST || side == Direction.WEST ? 16 - shrink : 16.005F;

                            BakedQuad itemQuad = new FaceBakery().bakeQuad(new Vector3f(minX, shrink, minZ), new Vector3f(maxX, 16 - shrink, maxZ), blockPartFace, model.getParticleTexture(), side, ModelRotation.X0_Y0, blockPartRotation, true, true);
                            quads.add(itemQuad);
                        } else if (side == Direction.UP) {
                            final float minX = shrink;
                            final float maxX = 16 - shrink;
                            final float minZ = shrink;
                            final float maxZ = 16 - shrink;

                            BakedQuad itemQuad = new FaceBakery().bakeQuad(new Vector3f(minX, 16.005F, minZ), new Vector3f(maxX, 16.005F, maxZ), blockPartFace, model.getParticleTexture(), side, ModelRotation.X0_Y0, blockPartRotation, true, true);
                            quads.add(itemQuad);
                        }
                    }
                }
            } else {
                renderBlock = Block.byItem(itemStack.getItem());
                if (renderBlock == null || renderBlock == Blocks.AIR) renderBlock = ModBlocks.WOOD_CRATE.get();
            }
        }

        if (!renderBlock.canRenderInLayer(renderBlock.getDefaultState(), MinecraftForgeClient.getRenderLayer()))
            return quads;

        BlockState renderState = renderBlock.getStateFromMeta(renderMeta);
        BakedModel model = rendererDispatcher().getModelForState(renderState);

        quads.addAll(model.getQuads(renderState, side, rand));

        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    @Nonnull
    public TextureAtlasSprite getParticleIcon() {
        return wood;
    }
}