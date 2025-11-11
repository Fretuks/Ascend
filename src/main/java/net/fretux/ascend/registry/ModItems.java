package net.fretux.ascend.registry;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.item.MoonseyeTome;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AscendMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AscendMod.MODID);
    public static final RegistryObject<Item> MOONSEYE_TOME = ITEMS.register(
            "moonseye_tome",
            () -> new MoonseyeTome(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE))
    );
    public static final RegistryObject<CreativeModeTab> ASCEND_TAB = TABS.register("ascend_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ascend"))
                    .icon(() -> new ItemStack(MOONSEYE_TOME.get()))
                    .displayItems((params, output) -> {
                        output.accept(MOONSEYE_TOME.get());
                    })
                    .build()
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        TABS.register(eventBus);
    }
}