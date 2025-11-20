package net.fretux.ascend.item;

import net.fretux.ascend.network.PacketHandler;
import net.fretux.ascend.network.ServerboundShrineChoicePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RemembranceEssenceItem extends Item {

    public RemembranceEssenceItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.sendSystemMessage(Component.literal("THE ESSENCE WHISPERS..."));
            PacketHandler.sendToPlayerOpenEssenceUI((ServerPlayer) player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    
    public static void consume(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack item = player.getItemInHand(hand);
            if (item.getItem() instanceof RemembranceEssenceItem) {
                item.shrink(1);
                return;
            }
        }
    }
}
