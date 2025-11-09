package net.fretux.ascend.network;

import net.fretux.ascend.AscendMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel INSTANCE;
    private static int id = 0;

    public static void register() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(AscendMod.MODID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        INSTANCE.messageBuilder(ServerboundSpendPointPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundSpendPointPacket::toBytes)
                .decoder(ServerboundSpendPointPacket::new)
                .consumerMainThread(ServerboundSpendPointPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundSyncStatsPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundSyncStatsPacket::toBytes)
                .decoder(ClientboundSyncStatsPacket::new)
                .consumerMainThread(ClientboundSyncStatsPacket::handle)
                .add();
    }
}