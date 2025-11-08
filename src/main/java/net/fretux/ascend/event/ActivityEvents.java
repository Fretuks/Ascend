package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.level.BlockEvent;

@Mod.EventBusSubscriber(modid = AscendMod.MODID)
public class ActivityEvents {

    private static int agilityTickCounter = 0;
    private static int fortitudeTickCounter = 0;
    private static int willpowerTickCounter = 0;

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide) {
            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                stats.addAscendXP(5);
                PlayerStatsProvider.sync(player);
            });
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide) return;

        Block block = event.getState().getBlock();
        String name = block.getName().getString().toLowerCase();

        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            if (name.contains("log") || name.contains("ore") || name.contains("stone")) {
                stats.addAscendXP(2);
                PlayerStatsProvider.sync(player);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerDamaged(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                int gain = Math.min((int) event.getAmount() * 2, 10);
                stats.addAscendXP(gain);
                PlayerStatsProvider.sync(player);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTickFortitude(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide || event.phase != TickEvent.Phase.END) return;

        fortitudeTickCounter++;
        if (fortitudeTickCounter % 100 == 0) {
            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                boolean hasNegativeEffect = player.getActiveEffects().stream()
                        .anyMatch(e -> e.getEffect().getCategory() == MobEffectCategory.HARMFUL);
                if (player.getFoodData().getFoodLevel() <= 6 || hasNegativeEffect) {
                    stats.addAscendXP(2);
                    PlayerStatsProvider.sync(player);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTickAgility(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide || event.phase != TickEvent.Phase.END) return;
        agilityTickCounter++;
        if (agilityTickCounter % 40 != 0) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            boolean didAgileThing =
                    player.isSprinting()
                            || player.isCrouching()
                            || Math.abs(player.getDeltaMovement().y) > 0.25
                            || player.onClimbable();
            if (didAgileThing) {
                stats.addAscendXP(1);
                PlayerStatsProvider.sync(player);
            }
        });
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            stats.addAscendXP(3);
            PlayerStatsProvider.sync(player);
        });
    }

    @SubscribeEvent
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            stats.addAscendXP(2);
            PlayerStatsProvider.sync(player);
        });
    }

    @SubscribeEvent
    public static void onPlayerTickWillpower(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide || event.phase != TickEvent.Phase.END) return;
        willpowerTickCounter++;
        if (willpowerTickCounter % 200 != 0) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            boolean hasNegativeEffect = player.getActiveEffects().stream()
                    .anyMatch(e -> e.getEffect().getCategory() == MobEffectCategory.HARMFUL);
            boolean lowHealth = player.getHealth() <= player.getMaxHealth() * 0.25f;
            BlockPos pos = player.blockPosition();
            int blockLight = player.level().getBrightness(LightLayer.BLOCK, pos);
            int skyLight = player.level().getBrightness(LightLayer.SKY, pos);
            int light = Math.max(blockLight, skyLight);
            boolean inDarkness = light <= 3;
            if (lowHealth || (hasNegativeEffect && inDarkness)) {
                stats.addAscendXP(1);
                PlayerStatsProvider.sync(player);
            }
        });
    }

    @SubscribeEvent
    public static void onVillagerTrade(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager)) return;
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            stats.addAscendXP(5);
            PlayerStatsProvider.sync(player);
        });
    }

    @SubscribeEvent
    public static void onWeaponUsed(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (event.getEntity() == player) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            var heldItem = player.getMainHandItem();
            if (heldItem.isEmpty()) return;
            double attackSpeed = player.getAttribute(
                    net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED
            ).getBaseValue();
            stats.addAscendXP(3);
            PlayerStatsProvider.sync(player);
        });
    }

    @SubscribeEvent
    public static void onPotionUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (event.getItemStack().getItem().getDescriptionId().contains("potion")) {
            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                stats.addAscendXP(3);
                PlayerStatsProvider.sync(player);
            });
        }
    }

    @SubscribeEvent
    public static void onGainPotionEffect(net.minecraftforge.event.entity.living.MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (event.getEffectInstance().getEffect().getCategory() == net.minecraft.world.effect.MobEffectCategory.BENEFICIAL) {
            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                stats.addAscendXP(2);
                PlayerStatsProvider.sync(player);
            });
        }
    }

    @SubscribeEvent
    public static void onItemSmeltedForMagic(PlayerEvent.ItemSmeltedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        String itemName = event.getSmelting().getDisplayName().getString().toLowerCase();
        if (itemName.contains("potion") || itemName.contains("elixir")) {
            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                stats.addAscendXP(3);
                PlayerStatsProvider.sync(player);
            });
        }
    }
}