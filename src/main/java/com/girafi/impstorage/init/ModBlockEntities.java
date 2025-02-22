package com.girafi.impstorage.init;

import com.girafi.impstorage.block.tile.ControllerBlockEntity;
import com.girafi.impstorage.block.tile.ControllerInterfaceBlockEntity;
import com.girafi.impstorage.block.tile.ConveyorBlockEntity;
import com.girafi.impstorage.block.tile.ItemBlockEntity;
import com.girafi.impstorage.lib.ModInfo;
import com.mojang.datafixers.types.Type;
import net.minecraft.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_DEFERRED = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ModInfo.ID);
    public static final RegistryObject<BlockEntityType<ItemBlockEntity>> ITEM_BLOCK = register("neptunes_bounty", () -> BlockEntityType.Builder.of(ItemBlockEntity::new, ModBlocks.ITEM_BLOCK.get()));
    public static final RegistryObject<BlockEntityType<ControllerBlockEntity>> CONTROLLER = register("controller", () -> BlockEntityType.Builder.of(ControllerBlockEntity::new, ModBlocks.CONTROLLER.get()));
    public static final RegistryObject<BlockEntityType<ControllerInterfaceBlockEntity>> CONTROLLER_INTERFACE = register("controller_interface", () -> BlockEntityType.Builder.of(ControllerInterfaceBlockEntity::new, ModBlocks.CONTROLLER_INTERFACE.get()));
    public static final RegistryObject<BlockEntityType<ConveyorBlockEntity>> CONVEYOR = register("conveyor", () -> BlockEntityType.Builder.of(ConveyorBlockEntity::new, ModBlocks.CONVEYOR.get()));

    public static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(@Nonnull String name, @Nonnull Supplier<BlockEntityType.Builder<T>> initializer) {
        Type<?> type = Util.fetchChoiceType(References.BLOCK_ENTITY, ModInfo.ID + ":" + name);
        return BLOCK_ENTITY_DEFERRED.register(name, () -> initializer.get().build(type));
    }
}