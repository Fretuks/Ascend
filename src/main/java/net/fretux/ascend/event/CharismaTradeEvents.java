package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.ascend.player.StatEffects;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = AscendMod.MODID)
public class CharismaTradeEvents {

    @SubscribeEvent
    public static void onOpenMerchant(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        AbstractContainerMenu menu = event.getContainer();
        if (!(menu instanceof MerchantMenu merchantMenu)) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            int cha = stats.getAttributeLevel("charisma");
            if (cha <= 0) return;
            Merchant merchant = resolveMerchant(merchantMenu);
            if (merchant instanceof Villager villager) {
                int reputationBonus = StatEffects.getCharismaVillagerReputationBonus(cha);
                if (reputationBonus > 0) {
                    villager.getGossips().add(player.getUUID(), GossipType.MINOR_POSITIVE, reputationBonus);
                }
            }
            double discountFraction = StatEffects.getCharismaTradeDiscount(cha);
            if (discountFraction <= 0.0d) return;
            var offers = merchantMenu.getOffers();
            if (offers == null) return;
            for (MerchantOffer offer : offers) {
                if (offer == null) continue;
                int base = offer.getBaseCostA().getCount();
                if (base <= 0) continue;
                int target = (int) Math.max(1, Math.floor(base * (1.0d - discountFraction)));
                int currentSpecial = offer.getSpecialPriceDiff();
                int newSpecial = target - base;
                int delta = newSpecial - currentSpecial;
                if (delta != 0) {
                    offer.addToSpecialPriceDiff(delta);
                }
            }
        });
    }

    private static Merchant resolveMerchant(MerchantMenu merchantMenu) {
        for (Field field : MerchantMenu.class.getDeclaredFields()) {
            if (!Merchant.class.isAssignableFrom(field.getType())) {
                continue;
            }
            field.setAccessible(true);
            try {
                return (Merchant) field.get(merchantMenu);
            } catch (IllegalAccessException ignored) {
                return null;
            }
        }
        return null;
    }
}
