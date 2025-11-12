package net.fretux.ascend.registry;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.block.ShrineOfRemembranceBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AscendMod.MODID);

    public static final RegistryObject<Block> SHRINE_OF_REMEMBRANCE = BLOCKS.register(
            "shrine_of_remembrance",
            () -> new ShrineOfRemembranceBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(-1.0F, 3600000.0F) // unbreakable
                            .noLootTable()
                            .lightLevel(s -> 6)
                            .noOcclusion()
                            .requiresCorrectToolForDrops()
            )
    );

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}