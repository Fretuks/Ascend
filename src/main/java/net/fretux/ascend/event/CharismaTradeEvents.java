package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.ascend.player.StatEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

@EventBusSubscriber(modid = AscendMod.MODID)
public class CharismaTradeEvents {

    @SubscribeEvent
    public static void onOpenMerchant(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        AbstractContainerMenu menu = event.getContainer();
        if (!(menu instanceof MerchantMenu merchantMenu)) return;

        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
        int cha = stats.getAttributeLevel("charisma");
        if (cha <= 0) return;

        double discountFraction = StatEffects.getCharismaTradeDiscount(cha);
        if (discountFraction <= 0.0d) return;

        var offers = merchantMenu.getOffers();
        if (offers == null) return;

        for (MerchantOffer offer : offers) {
            if (offer == null) continue;

            // Base cost of first input (emeralds, etc.)
            int base = offer.getBaseCostA().getCount();
            if (base <= 0) continue;

            // Desired discounted cost
            int target = (int) Math.max(1, Math.floor(base * (1.0d - discountFraction)));

            // Current special price diff already includes:
            // popularity, hero-of-village, etc.
            int currentSpecial = offer.getSpecialPriceDiff();

            // We want: base + newSpecial = target
            int newSpecial = target - base;

            // Adjust relative to current to avoid stacking each open.
            int delta = newSpecial - currentSpecial;
            if (delta != 0) {
                offer.addToSpecialPriceDiff(delta);
            }
        }
    }
}