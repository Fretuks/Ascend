package net.fretux.ascend.network;

import net.fretux.ascend.client.screen.ShrineScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundOpenEssenceScreenPacket {
    public ClientboundOpenEssenceScreenPacket() {}

    public static void handle(ClientboundOpenEssenceScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new ShrineScreen(true));
        });
        ctx.get().setPacketHandled(true);
    }
}
