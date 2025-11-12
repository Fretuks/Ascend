package net.fretux.ascend.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.fretux.ascend.AscendMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = AscendMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class KeyBindings {
    public static final KeyMapping OPEN_STATS = new KeyMapping(
            "key.ascend.open_stats",
            InputConstants.KEY_V,
            "key.categories.inventory"
    );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_STATS);
    }
}
