package net.fretux.ascend.client;

import net.fretux.ascend.client.screen.StatsScreen;
import net.minecraft.client.Minecraft;
import net.fretux.ascend.AscendMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = AscendMod.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindings.OPEN_STATS.consumeClick()) {
            Minecraft.getInstance().setScreen(new StatsScreen());
        }
    }
}
