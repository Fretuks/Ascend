package net.fretux.ascend.datagen;

import net.fretux.ascend.registry.ModBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;

import java.util.Set;

public class ModBlockLootProvider extends BlockLootSubProvider {

    public ModBlockLootProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        
    }

    @Override
    protected Iterable<net.minecraft.world.level.block.Block> getKnownBlocks() {
        return Set.of(ModBlocks.SHRINE_OF_REMEMBRANCE.get());
    }
}