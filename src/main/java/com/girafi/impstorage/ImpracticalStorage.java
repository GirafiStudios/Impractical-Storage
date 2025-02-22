package com.girafi.impstorage;

import com.girafi.impstorage.client.ClientHandler;
import com.girafi.impstorage.core.BlockOverrides;
import com.girafi.impstorage.init.ModMenuTypes;
import com.girafi.impstorage.lib.ImpracticalConfig;
import com.girafi.impstorage.init.ModBlockEntities;
import com.girafi.impstorage.init.ModBlocks;
import com.girafi.impstorage.init.ModItems;
import com.girafi.impstorage.lib.ModInfo;
import com.girafi.impstorage.network.PacketHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(value = ModInfo.ID)
public class ImpracticalStorage {
    public static ImpracticalStorage instance;
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModInfo.ID);
    public static final RegistryObject<CreativeModeTab> GROUP = CREATIVE_TABS.register("tab", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(ModBlocks.WOOD_CRATE.get()))
            .title(Component.translatable("tabs." + ModInfo.ID + ".tab"))
            .displayItems((featureFlagSet, tabOutput) -> {
                ModItems.ITEMS_FOR_TAB_LIST.forEach(registryObject -> tabOutput.accept(new ItemStack(registryObject.get())));
            }).build()
    );

    public ImpracticalStorage() {
        instance = this;
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setupCommon);
        modBus.addListener(this::setupClient);
        this.registerDeferredRegistries(modBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ImpracticalConfig.spec);
    }

    private void setupCommon(FMLCommonSetupEvent event) {
        event.enqueueWork(BlockOverrides::initialize);
        event.enqueueWork(PacketHandler::initialize);
    }

    private void setupClient(FMLClientSetupEvent event) {
        event.enqueueWork(ClientHandler::setupClient);
    }

    public void registerDeferredRegistries(IEventBus modBus) {
        ModItems.ITEM_DEFERRED.register(modBus);
        ModBlocks.BLOCK_DEFERRED.register(modBus);
        CREATIVE_TABS.register(modBus);
        ModMenuTypes.MENU_DEFERRED.register(modBus);
        ModBlockEntities.BLOCK_ENTITY_DEFERRED.register(modBus);
    }
}