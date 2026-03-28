package net.fretux.ascend.network;

import net.fretux.ascend.client.ClientPacketHandlers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundOpenEssenceScreenPacket {
    public ClientboundOpenEssenceScreenPacket() {}

    public static void handle(ClientboundOpenEssenceScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.openShrineScreen(true));
        });
        ctx.get().setPacketHandled(true);
    }
}
