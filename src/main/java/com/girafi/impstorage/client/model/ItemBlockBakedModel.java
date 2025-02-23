package com.girafi.impstorage.client.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

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

    private final Set<String> renderBlacklist = Sets.newHashSet();
    private final VertexFormat format;
    private final TextureAtlasSprite wood;

    public ItemBlockBakedModel(VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this.format = format;
        this.wood = bakedTextureGetter.apply(new ResourceLocation("impstorage:blocks/wood_crate"));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    @Nonnull
    public List<BakedQuad> getQuads(BlockState state, @Nullable Direction direction, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        boolean error = false;
        List<BakedQuad> quads = Lists.newArrayList();

        /*ItemStack stack = state.getValue(ItemBlockBlock.ITEM); //TODO. Need the mod to be able to run, to play around with this. No obvious way to get the stack from the block. Maybe Block#getAppearance or handleItemState might be useful

        if (stack == null || stack.isEmpty() || stack.getItem() == null) {
            error = true;
        }

        if (!error) {
            try {
                quads = safeGetQuads(state, direction, rand);
            } catch (Exception ex) {
                renderBlacklist.add(state.getValue(ItemBlockBlock.ITEM).getItem().getRegistryName().toString());
                error = true;
            }
        }

        if (error) {
            BlockState s = ModBlocks.WOOD_CRATE.get().defaultBlockState();
            quads = rendererDispatcher().getRenderer(s).getQuads(s, direction, rand);
        }*/

        return quads;
    }

    @OnlyIn(Dist.CLIENT)
    private List<BakedQuad> safeGetQuads(BlockState state, @Nullable Direction side, RandomSource rand) {
        //ItemStack itemStack = state.getValue(ItemBlockBlock.ITEM); //TODO Same as above

        List<BakedQuad> quads = Lists.newArrayList();

        /*Block renderBlock;
        if (itemStack.isEmpty() || renderBlacklist.contains(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString())) {
            renderBlock = ModBlocks.WOOD_CRATE.get();
        } else {
            if (BlockOverrides.shouldTreatAsItem(itemStack.getItem())) {
                renderBlock = ModBlocks.WOOD_CRATE.get();

                BakedModel model = renderItem().getItemModelShaper().getItemModel(itemStack);
                TextureAtlasSprite texture = model.getParticleIcon();
                if (texture == null)
                    texture = Minecraft.getInstance().getModelManager().getMissingModel().getParticleIcon();

                BlockElementFace blockPartFace = new BlockElementFace(side, 0, texture.toString(), new BlockFaceUV(new float[]{0, 0, 16, 16}, 0));
                BlockElementRotation blockPartRotation = new BlockElementRotation(new Vector3f(0, 0, 0), Direction.Axis.X, 0, false);

                final float shrink = 2.5F;

                if (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT) {
                    if (side != null) {
                        if (side != Direction.UP && side != Direction.DOWN) {
                            final float minX = side == Direction.EAST || side == Direction.WEST ? -0.005F : shrink;
                            final float maxX = side == Direction.EAST || side == Direction.WEST ? 16.005f : 16 - shrink;
                            final float minZ = side == Direction.EAST || side == Direction.WEST ? shrink : -0.005F;
                            final float maxZ = side == Direction.EAST || side == Direction.WEST ? 16 - shrink : 16.005F;

                            BakedQuad itemQuad = new FaceBakery().bakeQuad(new Vector3f(minX, shrink, minZ), new Vector3f(maxX, 16 - shrink, maxZ), blockPartFace, model.getParticleIcon(), side, ModelRotation.X0_Y0, blockPartRotation, true, true);
                            quads.add(itemQuad);
                        } else if (side == Direction.UP) {
                            final float minX = shrink;
                            final float maxX = 16 - shrink;
                            final float minZ = shrink;
                            final float maxZ = 16 - shrink;

                            BakedQuad itemQuad = new FaceBakery().bakeQuad(new Vector3f(minX, 16.005F, minZ), new Vector3f(maxX, 16.005F, maxZ), blockPartFace, model.getParticleIcon(), side, ModelRotation.X0_Y0, blockPartRotation, true, true);
                            quads.add(itemQuad);
                        }
                    }
                }
            } else {
                renderBlock = Block.byItem(itemStack.getItem());
                if (renderBlock == null || renderBlock == Blocks.AIR) renderBlock = ModBlocks.WOOD_CRATE.get();
            }
        }

        if (!renderBlock.canRenderInLayer(renderBlock.defaultBlockState(), MinecraftForgeClient.getRenderLayer()))
            return quads;

        BlockState renderState = renderBlock.getStateFromMeta(renderMeta);
        BakedModel model = rendererDispatcher().getModelForState(renderState);

        quads.addAll(model.getQuads(renderState, side, rand));*/

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

    @Override
    @Nonnull
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}