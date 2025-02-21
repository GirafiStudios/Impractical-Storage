package me.dmillerw.impstorage.core;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import me.dmillerw.impstorage.ImpracticalStorage;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author dmillerw
 */
public class BlockOverrides {

    private static Map<Block, RenderAs> map = Maps.newHashMap();

    public static void initialize() {
        Type mapType = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
        //TODO Merging/loading from config folder
        Reader reader = new InputStreamReader(ImpracticalStorage.class.getResourceAsStream("/assets/impstorage/mod/BlockOverrides.json"));

        // Example File:
        // {
        //   "minecraft": {
        //     "sapling": "block"
        //   }
        // }
        //
        // Would override our determination for what a sapling is, and treat it as a block that should be rendered
        // in world, and be unable to stack in crates

        Map<String, Map<String, String>> data = (new GsonBuilder().create()).fromJson(reader, mapType);

        for (String key : data.keySet()) {
            Map<String, String> values = data.get(key);
            for (Map.Entry<String, String> value : values.entrySet()) {
                Block block = Block.getBlockFromName(key + ":" + value.getKey());
                if (block != null) {
                    RenderAs renderAs = null;
                    if (value.getValue().equalsIgnoreCase("block")) renderAs = RenderAs.BLOCK;
                    else if (value.getValue().equalsIgnoreCase("item")) renderAs = RenderAs.ITEM;
                    if (renderAs != null) map.put(block, renderAs);
                }
            }
        }
    }

    public static boolean shouldTreatAsItem(Block block) {
        RenderAs override = map.get(block);
        if (override != null) return override == RenderAs.ITEM;

        return block instanceof BlockBush ||
                block instanceof BlockWeb ||
                block instanceof BlockHopper ||
                block instanceof BlockTorch ||
                block instanceof BlockRailBase ||
                block instanceof BlockTripWire ||
                block instanceof BlockTripWireHook ||
                block instanceof BlockLever ||
                block instanceof BlockVine;
    }

    public static boolean shouldTreatAsItem(Item item) {
        return (item instanceof ItemBlock ? shouldTreatAsItem(Block.getBlockFromItem(item)) : true);
    }

    public static enum RenderAs { BLOCK, ITEM }
}
