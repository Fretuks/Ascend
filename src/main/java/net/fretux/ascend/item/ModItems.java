package net.fretux.ascend.item;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.item.custom.MoonseyeTome;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AscendMod.MODID);

    public static final DeferredItem<Item> MOONSEYETOME =
            ITEMS.register("moonseye_tome",
                    () -> new MoonseyeTome(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

