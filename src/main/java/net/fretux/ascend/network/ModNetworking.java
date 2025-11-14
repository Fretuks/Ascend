package net.fretux.ascend.network;

import net.fretux.ascend.AscendMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = AscendMod.MODID, bus =  EventBusSubscriber.Bus.MOD)
public class ModNetworking {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0");

        registrar.playToServer(
                ServerStatsPayload.TYPE,
                ServerStatsPayload.CODEC,
                ServerStatsPayload::handleServer
        );

        registrar.playToServer(
                ServerShrinePayload.TYPE,
                ServerShrinePayload.CODEC,
                ServerShrinePayload::handleServer
        );

        registrar.playToClient(
                ClientStatsPayload.TYPE,
                ClientStatsPayload.CODEC,
                ClientStatsPayload::handleClient
        );
    }
}

