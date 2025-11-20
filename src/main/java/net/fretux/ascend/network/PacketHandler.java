package net.fretux.ascend.network;

import net.fretux.ascend.AscendMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
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

        INSTANCE.registerMessage(id++, ServerboundShrineChoicePacket.class,
                ServerboundShrineChoicePacket::toBytes,
                ServerboundShrineChoicePacket::new,
                ServerboundShrineChoicePacket::handle);
        INSTANCE.registerMessage(id++,
                ClientboundOpenEssenceScreenPacket.class,
                (msg, buf) -> {
                },              
                buf -> new ClientboundOpenEssenceScreenPacket(),
                ClientboundOpenEssenceScreenPacket::handle
        );
    }

    public static void sendToPlayerOpenEssenceUI(ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundOpenEssenceScreenPacket());
    }
}