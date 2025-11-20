package net.fretux.ascend.datagen;

import net.fretux.ascend.registry.ModBlocks;
import net.fretux.ascend.registry.ModItems;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;

import java.util.Set;

public class ModBlockLootProvider extends BlockLootSubProvider {

    public ModBlockLootProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.add(ModBlocks.SHRINE_OF_REMEMBRANCE.get(),
                createSingleItemTable(ModItems.REMEMBRANCE_ESSENCE.get()));
    }

    @Override
    protected Iterable<net.minecraft.world.level.block.Block> getKnownBlocks() {
        return Set.of(ModBlocks.SHRINE_OF_REMEMBRANCE.get());
    }
}