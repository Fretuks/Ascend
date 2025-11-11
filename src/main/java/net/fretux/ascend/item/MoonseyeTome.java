package net.fretux.ascend.item;

import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MoonseyeTome extends Item {
    public MoonseyeTome(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            int gained;
            if (!stats.hasUsedMoonseye()) {
                gained = 100;
                stats.setHasUsedMoonseye(true);
            } else {
                gained = 50;
            }
            stats.addKnowledge(gained);
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("item.ascend.moonseye_tome.used", gained));
            player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
            stack.shrink(1);
            PlayerStatsProvider.sync(player);
        });
        return InteractionResultHolder.consume(stack);
    }
}