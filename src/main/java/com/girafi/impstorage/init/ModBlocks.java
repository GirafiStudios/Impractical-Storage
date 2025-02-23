package com.girafi.impstorage.init;

import com.girafi.impstorage.block.*;
import com.girafi.impstorage.lib.ModInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCK_DEFERRED = DeferredRegister.create(ForgeRegistries.BLOCKS, ModInfo.ID);

    public static final RegistryObject<Block> ITEM_BLOCK = registerNoTab(ItemBlockBlock::new, "item_block", null);
    public static final RegistryObject<Block> CONTROLLER = register(ControllerBlock::new, "controller");
    public static final RegistryObject<Block> CONTROLLER_INTERFACE = register(ControllerInterfaceBlock::new, "controller_interface");
    public static final RegistryObject<Block> WOOD_CRATE = register(() -> new CrateBlock(8), "wood_crate");
    public static final RegistryObject<Block> IRON_CRATE = register(() -> new CrateBlock(16), "iron_crate");
    public static final RegistryObject<Block> GOLD_CRATE = register(() -> new CrateBlock(32), "gold_crate");
    public static final RegistryObject<Block> DIAMOND_CRATE = register(() -> new CrateBlock(64), "diamond_crate");
    public static final RegistryObject<Block> OBSIDIAN_CRATE = register(() -> new CrateBlock(8, 64), "obsidian_crate");
    public static final RegistryObject<Block> PHANTOM = registerNoTab(PhantomBlock::new, "phantom", null);
    public static final RegistryObject<Block> CONVEYOR = register(ConveyorBlock::new, "conveyor");
    public static final RegistryObject<Block> GRAVITY_INDUCER = register(GravityInducerBlock::new, "gravity_inducer");
    public static final RegistryObject<Block> ITEMIZER = register(ItemizerBlock::new, "itemizer");

    /**
     * Same as {@link ModBlocks#register(Supplier, String, Item.Properties)}, but have group set by default
     */
    public static RegistryObject<Block> register(Supplier<Block> supplier, @Nonnull String name) {
        return register(supplier, name, new Item.Properties());
    }

    /**
     * Registers a block with a basic BlockItem, with no tab
     *
     * @param supplier The block to register
     * @param name     The name to register the block with
     * @return The Block that was registered
     */
    public static RegistryObject<Block> registerNoTab(Supplier<Block> supplier, @Nonnull String name, @Nullable Item.Properties properties) {
        RegistryObject<Block> block = BLOCK_DEFERRED.register(name, supplier);

        ModItems.registerNoTab(() -> new BlockItem(block.get(), Objects.requireNonNullElseGet(properties, Item.Properties::new)), name);

        return block;
    }

    /**
     * Registers a block with a basic BlockItem
     *
     * @param supplier The block to register
     * @param name     The name to register the block with
     * @return The Block that was registered
     */
    public static RegistryObject<Block> register(Supplier<Block> supplier, @Nonnull String name, @Nullable Item.Properties properties) {
        RegistryObject<Block> block = BLOCK_DEFERRED.register(name, supplier);

        ModItems.register(() -> new BlockItem(block.get(), Objects.requireNonNullElseGet(properties, Item.Properties::new)), name);

        return block;
    }

    public static Boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return true;
    }

    public static Boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos, EntityType<?> entityType) {
        return false;
    }
}