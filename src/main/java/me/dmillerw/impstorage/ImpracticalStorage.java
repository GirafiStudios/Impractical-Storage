package me.dmillerw.impstorage;

import me.dmillerw.impstorage.lib.ModInfo;
import me.dmillerw.impstorage.proxy.IProxy;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

/**
 * @author dmillerw
 */
@Mod(modid = ModInfo.ID, name = ModInfo.NAME, version = ModInfo.VERSION, updateJSON = ModInfo.UPDATE_JSON)
public class ImpracticalStorage {

    @Mod.Instance(ModInfo.ID)
    public static ImpracticalStorage instance;

    @SidedProxy(
            serverSide = "me.dmillerw.impstorage.proxy.CommonProxy",
            clientSide = "me.dmillerw.impstorage.proxy.ClientProxy")
    public static IProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration configuration = new Configuration(new File(event.getModConfigurationDirectory(), "ImpracticalStorage.cfg"));

        proxy.readConfigurationFile(configuration);

        if (configuration.hasChanged()) {
            configuration.save();
        }

        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
