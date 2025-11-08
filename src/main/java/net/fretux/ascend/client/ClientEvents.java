package net.fretux.ascend.client;

import net.fretux.ascend.client.screen.StatsScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.fretux.ascend.AscendMod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = AscendMod.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindings.OPEN_STATS.consumeClick()) {
            Minecraft.getInstance().setScreen(new StatsScreen());
        }
    }
}
