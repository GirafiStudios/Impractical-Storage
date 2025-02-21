package com.girafi.impstorage.init;

import com.girafi.impstorage.lib.ModInfo;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEM_DEFERRED = DeferredRegister.create(ForgeRegistries.ITEMS, ModInfo.ID);
    public static final Collection<RegistryObject<Item>> ITEMS_FOR_TAB_LIST = new ArrayList<>();

    /**
     * Registers an item
     *
     * @param initializer The item initializer
     * @param name        The name to register the item with
     * @return The Item that was registered
     */
    public static RegistryObject<Item> registerNoTab(@Nonnull Supplier<Item> initializer, @Nonnull String name) {
        return ITEM_DEFERRED.register(name, initializer);
    }

    /**
     * Registers an item & add the item to the Aquaculture creative tab
     *
     * @param initializer The item initializer
     * @param name        The name to register the item with
     * @return The Item that was registered
     */
    public static RegistryObject<Item> register(@Nonnull Supplier<Item> initializer, @Nonnull String name) {
        RegistryObject<Item> registryObject = registerNoTab(initializer, name);
        ITEMS_FOR_TAB_LIST.add(registryObject);
        return registryObject;
    }
}