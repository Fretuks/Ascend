package net.fretux.ascend;

import net.fretux.ascend.block.ModBlocks;
import net.fretux.ascend.command.AscendCommand;
import net.fretux.ascend.config.AscendConfig;
import net.fretux.ascend.item.ModCreativeModeTabs;
import net.fretux.ascend.item.ModItems;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(AscendMod.MODID)
public class AscendMod {
    public static final String MODID = "ascend";

    public AscendMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onCommonSetup);
        PlayerStatsProvider.ATTACHMENT_TYPES.register(modEventBus);
        System.out.println("[Ascend] Mod loaded! Side=" + FMLEnvironment.dist);
        modContainer.registerConfig(ModConfig.Type.COMMON, AscendConfig.COMMON_SPEC);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        NeoForge.EVENT_BUS.register(AscendCommand.class);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            System.out.println("[Ascend] Registering packets... Side=" + FMLEnvironment.dist);
        });
    }
}