package net.fretux.ascend.item;

import net.fretux.ascend.AscendMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AscendMod.MODID);

    public static final Supplier<CreativeModeTab> ASCEND_TAB = CREATIVE_MODE_TAB.register("ascend_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.MOONSEYETOME.get()))
                    .title(Component.translatable("creativetab.ascend.ascend_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.MOONSEYETOME.get());
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
