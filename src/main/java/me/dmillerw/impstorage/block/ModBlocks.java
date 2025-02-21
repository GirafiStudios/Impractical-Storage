package me.dmillerw.impstorage.block;

import me.dmillerw.impstorage.block.item.ItemBlockCrate;
import me.dmillerw.impstorage.block.item.ItemBlockPhantom;
import me.dmillerw.impstorage.lib.ModInfo;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * @author dmillerw
 */
@GameRegistry.ObjectHolder(ModInfo.ID)
public class ModBlocks {

    public static final BlockItemBlock item_block = null;
    @GameRegistry.ObjectHolder(ModInfo.ID + ":item_block")
    public static final ItemBlock item_block_item = null;

    public static final BlockController controller = null;
    @GameRegistry.ObjectHolder(ModInfo.ID + ":controller")
    public static final ItemBlock controller_item = null;

    public static final BlockControllerInterface controller_interface = null;
    @GameRegistry.ObjectHolder(ModInfo.ID + ":controller_interface")
    public static final ItemBlock controller_interface_item = null;

    public static final BlockCrate crate = null;
    @GameRegistry.ObjectHolder(ModInfo.ID + ":crate")
    public static final ItemBlock crate_item = null;

    public static final BlockPhantom phantom = null;
    @GameRegistry.ObjectHolder(ModInfo.ID + ":phantom")
    public static final ItemBlock phantom_item = null;

    public static final BlockConveyor conveyor = null;
    @GameRegistry.ObjectHolder(ModInfo.ID + ":conveyor")
    public static final ItemBlock conveyor_item = null;

    public static final BlockGravityInducer gravity_inducer = null;
    @GameRegistry.ObjectHolder(ModInfo.ID + ":gravity_inducer")
    public static final ItemBlock gravity_inducer_item = null;

    public static final BlockItemizer itemizer = null;
    @GameRegistry.ObjectHolder(ModInfo.ID + ":itemizer")
    public static final ItemBlock itemizer_item = null;

    @Mod.EventBusSubscriber
    public static class RegistrationHandler {

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().registerAll(
                    new BlockItemBlock().setRegistryName(ModInfo.ID, "item_block"),
                    new BlockController().setRegistryName(ModInfo.ID, "controller"),
                    new BlockControllerInterface().setRegistryName(ModInfo.ID, "controller_interface"),
                    new BlockCrate().setRegistryName(ModInfo.ID, "crate"),
                    new BlockPhantom().setRegistryName(ModInfo.ID, "phantom"),
                    new BlockConveyor().setRegistryName(ModInfo.ID, "conveyor"),
                    new BlockGravityInducer().setRegistryName(ModInfo.ID, "gravity_inducer"),
                    new BlockItemizer().setRegistryName(ModInfo.ID, "itemizer")
            );
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(
                    new ItemBlock(item_block).setRegistryName(ModInfo.ID, "item_block"),
                    new ItemBlock(controller).setRegistryName(ModInfo.ID, "controller"),
                    new ItemBlock(controller_interface).setRegistryName(ModInfo.ID, "controller_interface"),
                    new ItemBlockCrate(crate).setRegistryName(ModInfo.ID, "crate"),
                    new ItemBlockPhantom(phantom).setRegistryName(ModInfo.ID, "phantom"),
                    new ItemBlock(conveyor).setRegistryName(ModInfo.ID, "conveyor"),
                    new ItemBlock(gravity_inducer).setRegistryName(ModInfo.ID, "gravity_inducer"),
                    new ItemBlock(itemizer).setRegistryName(ModInfo.ID, "itemizer")
            );
        }
    }
}
