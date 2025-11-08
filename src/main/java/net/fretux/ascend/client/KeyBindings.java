package net.fretux.ascend.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.fretux.ascend.AscendMod;

@Mod.EventBusSubscriber(modid = AscendMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
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
