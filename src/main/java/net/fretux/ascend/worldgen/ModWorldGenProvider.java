package net.fretux.ascend.worldgen;

import net.fretux.ascend.AscendMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Generates structure and worldgen JSON entries for the Shrine of Remembrance.
 */
public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {

    public static final ResourceKey<Structure> SHRINE =
            ResourceKey.create(Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath(AscendMod.MODID, "shrine_of_remembrance"));

    public static final ResourceKey<StructureSet> SHRINE_SET =
            ResourceKey.create(Registries.STRUCTURE_SET,
                    ResourceLocation.fromNamespaceAndPath(AscendMod.MODID, "shrine_set"));

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, new RegistrySetBuilder()
                        .add(Registries.STRUCTURE, ModWorldGenProvider::bootstrapStructures)
                        .add(Registries.STRUCTURE_SET, ModWorldGenProvider::bootstrapStructureSets),
                Set.of(AscendMod.MODID));
    }

    private static void bootstrapStructures(BootstapContext<Structure> ctx) {
        ctx.register(
                SHRINE,
                new SimpleShrineStructure(
                        new Structure.StructureSettings(
                                ctx.lookup(Registries.BIOME)
                                        .getOrThrow(BiomeTags.IS_OVERWORLD),
                                Map.of(),
                                GenerationStep.Decoration.SURFACE_STRUCTURES,
                                TerrainAdjustment.NONE
                        )
                )
        );
    }

    private static void bootstrapStructureSets(BootstapContext<StructureSet> ctx) {
        ctx.register(
                SHRINE_SET,
                new StructureSet(
                        List.of(new StructureSet.StructureSelectionEntry(
                                ctx.lookup(Registries.STRUCTURE).getOrThrow(SHRINE), 1)),
                        new RandomSpreadStructurePlacement(
                                32,
                                8,
                                RandomSpreadType.LINEAR,
                                1234567
                        )
                )
        );
    }
}
