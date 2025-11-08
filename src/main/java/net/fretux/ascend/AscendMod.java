package net.fretux.ascend;

import net.fretux.ascend.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(AscendMod.MODID)
public class AscendMod {
    public static final String MODID = "ascend";

    public AscendMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("[Ascend] Mod loaded! Side=" + FMLEnvironment.dist);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.register();
            System.out.println("[Ascend] Registering packets... Side=" + FMLEnvironment.dist);
        });
    }
}