package net.fretux.ascend.datagen;

import net.fretux.ascend.AscendMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AscendMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AscendDataGenerators {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        if (event.includeClient()) {
            generator.addProvider(true, new AscendItemModelProvider(generator.getPackOutput(), existingFileHelper));
        }
    }
}