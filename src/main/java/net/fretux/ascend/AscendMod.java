package net.fretux.ascend;

import net.fretux.ascend.command.AscendCommand;
import net.fretux.ascend.config.AscendConfig;
import net.fretux.ascend.network.PacketHandler;
import net.fretux.ascend.registry.ModBlocks;
import net.fretux.ascend.registry.ModItems;
import net.fretux.ascend.worldgen.ModStructurePieces;
import net.fretux.ascend.worldgen.ModStructures;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
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
        MinecraftForge.EVENT_BUS.register(AscendCommand.class);
        System.out.println("[Ascend] Mod loaded! Side=" + FMLEnvironment.dist);
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AscendConfig.COMMON_SPEC);
        ModItems.register(modBus);
        ModBlocks.register(modBus);
        ModStructures.STRUCTURE_TYPES.register(modBus);
        ModStructurePieces.PIECES.register(modBus);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.register();
            System.out.println("[Ascend] Registering packets... Side=" + FMLEnvironment.dist);
        });
    }
}