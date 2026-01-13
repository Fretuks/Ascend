package net.fretux.ascend.event;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.config.AscendConfig;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.ascend.player.StatEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.npc.GossipType;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraft.world.phys.AABB;

import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = AscendMod.MODID)
public class ActivityEvents {

    private static int agilityTickCounter = 0;
    private static int fortitudeTickCounter = 0;
    private static int willpowerTickCounter = 0;
    private static int charismaTickCounter = 0;

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide) {
            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                stats.addAscendXP((int) (AscendConfig.COMMON.xpPerMobKill.get() * AscendConfig.COMMON.xpMultiplier.get()));
                LivingEntity mob = event.getEntity();
                if (mob.getMaxHealth() > 100f || mob.getType().toString().toLowerCase().contains("boss")) {
                    stats.addKnowledgeScaled(2);
                }
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
                stats.addAscendXP((int) (AscendConfig.COMMON.xpPerBlockBreak.get() * AscendConfig.COMMON.xpMultiplier.get()));
                PlayerStatsProvider.sync(player);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerDamaged(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                int gain = Math.min((int) event.getAmount() / 2, 10);
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
                    stats.addAscendXP((int) (AscendConfig.COMMON.xpPerDamageTaken.get() * AscendConfig.COMMON.xpMultiplier.get()));
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
                stats.addAscendXP((int) (AscendConfig.COMMON.xpPerMovement.get() * AscendConfig.COMMON.xpMultiplier.get()));
                PlayerStatsProvider.sync(player);
            }
        });
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            stats.addAscendXP((int) (AscendConfig.COMMON.xpPerCraft.get() * AscendConfig.COMMON.xpMultiplier.get()));
            PlayerStatsProvider.sync(player);
        });
    }

    @SubscribeEvent
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            stats.addAscendXP((int) (AscendConfig.COMMON.xpPerSmelt.get() * AscendConfig.COMMON.xpMultiplier.get()));
            PlayerStatsProvider.sync(player);
        });
    }

    @SubscribeEvent
    public static void onPlayerTickWillpower(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide || event.phase != TickEvent.Phase.END) return;
        willpowerTickCounter++;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            int willpower = stats.getAttributeLevel("willpower");
            if (willpower > 0 && willpowerTickCounter % 80 == 0) {
                float regen = StatEffects.getWillpowerHealthRegen(willpower);
                if (regen > 0.0f && player.getHealth() < player.getMaxHealth()) {
                    player.heal(regen);
                }
            }
            if (willpowerTickCounter % 200 != 0) return;
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
    public static void onPlayerTickCharisma(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide || event.phase != TickEvent.Phase.END) return;
        charismaTickCounter++;
        if (charismaTickCounter % 40 != 0) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            int charisma = stats.getAttributeLevel("charisma");
            if (charisma <= 0) return;
            double radius = 16.0d;
            AABB range = player.getBoundingBox().inflate(radius);
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, range,
                    candidate -> isCharismaAlly(player, candidate))) {
                StatEffects.applyCharismaAllyBuff(entity, charisma);
            }
        });
    }

    @SubscribeEvent
    public static void onCharismaAllyTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (!isCharismaAllyCandidate(entity)) return;
        if (entity.tickCount % 40 != 0) return;
        double radius = 16.0d;
        if (hasNearbyCharismaSource(entity, radius)) return;
        StatEffects.clearCharismaAllyBuff(entity);
    }

    @SubscribeEvent
    public static void onVillagerTrade(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager)) return;
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            int charisma = stats.getAttributeLevel("charisma");
            if (charisma > 0) {
                int reputationBonus = StatEffects.getCharismaVillagerReputationBonus(charisma);
                if (reputationBonus > 0 && event.getTarget() instanceof Villager villager) {
                    villager.getGossips().add(player.getUUID(), GossipType.MINOR_POSITIVE, reputationBonus);
                }
            }
            stats.addAscendXP((int) (AscendConfig.COMMON.xpPerTrade.get() * AscendConfig.COMMON.xpMultiplier.get()));
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
            double attackSpeed = Objects.requireNonNull(player.getAttribute(
                    Attributes.ATTACK_SPEED
            )).getBaseValue();
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
                stats.addAscendXP((int) (AscendConfig.COMMON.xpPerPotion.get() * AscendConfig.COMMON.xpMultiplier.get()));
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

    @SubscribeEvent
    public static void onPlayerDealsDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            float damage = event.getAmount();
            var stack = player.getMainHandItem();
            if (!stack.isEmpty()) {
                double multiplier = 1.0d;
                if (stack.getItem() instanceof net.minecraft.world.item.AxeItem) {
                    int heavy = stats.getAttributeLevel("heavy_scaling");
                    multiplier = StatEffects.getHeavyWeaponScalingMultiplier(heavy);
                } else if (stack.getItem() instanceof net.minecraft.world.item.SwordItem) {
                    int medium = stats.getAttributeLevel("medium_scaling");
                    multiplier = StatEffects.getWeaponScalingMultiplier(medium);
                } else {
                    int light = stats.getAttributeLevel("light_scaling");
                    multiplier = StatEffects.getLightWeaponScalingMultiplier(light);
                }
                damage *= (float) multiplier;
            }
            event.setAmount(damage);
        });
    }

    @SubscribeEvent
    public static void onPlayerEvasion(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        if (event.getAmount() <= 0) return;
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
            int agility = stats.getAttributeLevel("agility");
            if (agility <= 0) return;
            double evadeChance = Math.min(agility * 0.0005d, 0.05d);
            if (player.getRandom().nextDouble() < evadeChance) {
                float original = event.getAmount();
                event.setAmount(original * 0.1f);
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
        });
    }

    private static boolean isCharismaAlly(Player player, LivingEntity entity) {
        if (entity instanceof TamableAnimal tame) {
            return tame.isTame() && tame.isOwnedBy(player);
        }
        if (entity instanceof AbstractHorse horse) {
            return horse.isTamed() && player.getUUID().equals(horse.getOwnerUUID());
        }
        if (entity instanceof IronGolem ironGolem) {
            return ironGolem.isPlayerCreated();
        }
        if (entity instanceof SnowGolem snowGolem) {
            return snowGolem.isPlayerCreated();
        }
        return false;
    }

    private static boolean isCharismaAllyCandidate(LivingEntity entity) {
        if (entity instanceof TamableAnimal tame) {
            return tame.isTame();
        }
        if (entity instanceof AbstractHorse horse) {
            return horse.isTamed();
        }
        if (entity instanceof IronGolem ironGolem) {
            return ironGolem.isPlayerCreated();
        }
        if (entity instanceof SnowGolem snowGolem) {
            return snowGolem.isPlayerCreated();
        }
        return false;
    }

    private static boolean hasNearbyCharismaSource(LivingEntity entity, double radius) {
        if (entity instanceof TamableAnimal tame) {
            Player owner = tame.getOwner() instanceof Player player ? player : null;
            return owner != null && owner.distanceToSqr(entity) <= radius * radius && hasCharisma(owner);
        }
        if (entity instanceof AbstractHorse horse) {
            UUID ownerId = horse.getOwnerUUID();
            if (ownerId == null) return false;
            Player owner = entity.level().getPlayerByUUID(ownerId);
            return owner != null && owner.distanceToSqr(entity) <= radius * radius && hasCharisma(owner);
        }
        if (entity instanceof IronGolem || entity instanceof SnowGolem) {
            AABB range = entity.getBoundingBox().inflate(radius);
            for (Player player : entity.level().getEntitiesOfClass(Player.class, range)) {
                if (hasCharisma(player)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasCharisma(Player player) {
        return player.getCapability(PlayerStatsProvider.PLAYER_STATS)
                .map(stats -> stats.getAttributeLevel("charisma") > 0)
                .orElse(false);
    }
}
