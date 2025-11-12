package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.config.AscendConfig;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = AscendMod.MODID)
public class ActivityEvents {

    private static int agilityTickCounter = 0;
    private static int fortitudeTickCounter = 0;
    private static int willpowerTickCounter = 0;

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide) {
            var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
                stats.addAscendXP((int) (AscendConfig.COMMON.xpPerMobKill.get() * AscendConfig.COMMON.xpMultiplier.get()));
                PlayerStatsProvider.sync(player);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide) return;
        Block block = event.getState().getBlock();
        String name = block.getName().getString().toLowerCase();
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
            if (name.contains("log") || name.contains("ore") || name.contains("stone")) {
                stats.addAscendXP((int) (AscendConfig.COMMON.xpPerBlockBreak.get() * AscendConfig.COMMON.xpMultiplier.get()));
                PlayerStatsProvider.sync(player);
            }
    }

    @SubscribeEvent
    public static void onPlayerDamaged(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
                int gain = Math.min((int) event.getOriginalDamage() / 2, 10);
                stats.addAscendXP(gain);
                PlayerStatsProvider.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTickFortitude(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        fortitudeTickCounter++;
        if (fortitudeTickCounter % 100 == 0) {
            var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
                boolean hasNegativeEffect = player.getActiveEffects().stream()
                        .anyMatch(e -> e.getEffect().value().getCategory() == MobEffectCategory.HARMFUL);
                if (player.getFoodData().getFoodLevel() <= 6 || hasNegativeEffect) {
                    stats.addAscendXP((int) (AscendConfig.COMMON.xpPerDamageTaken.get() * AscendConfig.COMMON.xpMultiplier.get()));
                    PlayerStatsProvider.sync(player);
                }
        }
    }

    @SubscribeEvent
    public static void onPlayerTickAgility(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        agilityTickCounter++;
        if (agilityTickCounter % 40 != 0) return;
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
            boolean didAgileThing =
                    player.isSprinting()
                            || player.isCrouching()
                            || Math.abs(player.getDeltaMovement().y) > 0.25
                            || player.onClimbable();
            if (didAgileThing) {
                stats.addAscendXP((int) (AscendConfig.COMMON.xpPerMovement.get() * AscendConfig.COMMON.xpMultiplier.get()));
                PlayerStatsProvider.sync(player);
            }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
            stats.addAscendXP((int) (AscendConfig.COMMON.xpPerCraft.get() * AscendConfig.COMMON.xpMultiplier.get()));
            PlayerStatsProvider.sync(player);
    }

    @SubscribeEvent
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
            stats.addAscendXP((int) (AscendConfig.COMMON.xpPerSmelt.get() * AscendConfig.COMMON.xpMultiplier.get()));
            PlayerStatsProvider.sync(player);
    }

    @SubscribeEvent
    public static void onPlayerTickWillpower(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        willpowerTickCounter++;
        if (willpowerTickCounter % 200 != 0) return;
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
            boolean hasNegativeEffect = player.getActiveEffects().stream()
                    .anyMatch(e -> e.getEffect().value().getCategory() == MobEffectCategory.HARMFUL);
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
    }

    @SubscribeEvent
    public static void onVillagerTrade(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager)) return;
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
            stats.addAscendXP((int) (AscendConfig.COMMON.xpPerTrade.get() * AscendConfig.COMMON.xpMultiplier.get()));
            PlayerStatsProvider.sync(player);
    }

    @SubscribeEvent
    public static void onWeaponUsed(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (event.getEntity() == player) return;
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
            var heldItem = player.getMainHandItem();
            if (heldItem.isEmpty()) return;
            double attackSpeed = player.getAttribute(
                    net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED
            ).getBaseValue();
            stats.addAscendXP(3);
            PlayerStatsProvider.sync(player);
    }

    @SubscribeEvent
    public static void onPotionUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (event.getItemStack().getItem().getDescriptionId().contains("potion")) {
            var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
                stats.addAscendXP((int) (AscendConfig.COMMON.xpPerPotion.get() * AscendConfig.COMMON.xpMultiplier.get()));
                PlayerStatsProvider.sync(player);
        }
    }

    @SubscribeEvent
    public static void onGainPotionEffect(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (event.getEffectInstance().getEffect().value().getCategory() == net.minecraft.world.effect.MobEffectCategory.BENEFICIAL) {
            var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
                stats.addAscendXP(2);
                PlayerStatsProvider.sync(player);
        }
    }

    @SubscribeEvent
    public static void onItemSmeltedForMagic(PlayerEvent.ItemSmeltedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        String itemName = event.getSmelting().getDisplayName().getString().toLowerCase();
        if (itemName.contains("potion") || itemName.contains("elixir")) {
            var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
                stats.addAscendXP(3);
                PlayerStatsProvider.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerDealsDamage(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        LivingEntity target = event.getEntity();
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
            int strength = stats.getAttributeLevel("strength");
            if (strength <= 0) return;
            float bypassFraction = Math.min(strength * 0.001f, 0.30f);
            int armor = target.getArmorValue();
            if (armor <= 0 || bypassFraction <= 0f) return;
            float baseDamage = event.getOriginalDamage();
            float armorFactor = (float) armor / (armor + 100.0f);
            float bonus = baseDamage * armorFactor * bypassFraction;
            event.setNewDamage(baseDamage + bonus);
    }

    @SubscribeEvent
    public static void onPlayerEvasion(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (event.getOriginalDamage() <= 0) return;
        var stats = player.getData(PlayerStatsProvider.PLAYER_STATS);
            int agility = stats.getAttributeLevel("agility");
            if (agility <= 0) return;
            double evadeChance = Math.min(agility * 0.0005d, 0.05d);
            if (player.getRandom().nextDouble() < evadeChance) {
                float original = event.getOriginalDamage();
                event.setNewDamage(original * 0.1f);
                if (player.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 8; i++) {
                        double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.6;
                        double offsetY = serverLevel.random.nextDouble() * 1.5;
                        double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.6;
                        serverLevel.sendParticles(
                                ParticleTypes.CLOUD,
                                player.getX() + offsetX,
                                player.getY() + offsetY,
                                player.getZ() + offsetZ,
                                1,
                                0, 0, 0, 0.0
                        );
                    }
                }
                player.playSound(SoundEvents.ENDERMAN_TELEPORT, 0.4f, 1.5f);
            }
    }
}