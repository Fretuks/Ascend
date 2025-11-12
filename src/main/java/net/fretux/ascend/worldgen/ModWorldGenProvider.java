package net.fretux.ascend.worldgen;

import com.mojang.serialization.Codec;
import net.fretux.ascend.AscendMod;
import net.fretux.ascend.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Generates structure and worldgen JSON entries for the Shrine of Remembrance.
 */
public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SHRINE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(AscendMod.MODID, "shrine_of_remembrance"));
    public static final ResourceKey<PlacedFeature> SHRINE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(AscendMod.MODID, "shrine_of_remembrance"));

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, new RegistrySetBuilder()
                .add(Registries.CONFIGURED_FEATURE, ModWorldGenProvider::bootstrapConfigured)
                .add(Registries.PLACED_FEATURE, ModWorldGenProvider::bootstrapPlaced),
                Set.of(AscendMod.MODID));
    }

    private static void bootstrapConfigured(BootstapContext<ConfiguredFeature<?, ?>> ctx) {
        ctx.register(SHRINE_CONFIGURED,
                new ConfiguredFeature<>(Feature.NO_OP, NoneFeatureConfiguration.INSTANCE));
    }

    private static void bootstrapPlaced(BootstapContext<PlacedFeature> ctx) {
        var configured = ctx.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(SHRINE_CONFIGURED);
        ctx.register(SHRINE_PLACED,
                new PlacedFeature(configured, List.of(
                        RarityFilter.onAverageOnceEvery(400) // roughly like mineshafts
                )));
    }
}
