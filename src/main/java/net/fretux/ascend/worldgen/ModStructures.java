package net.fretux.ascend.worldgen;

import net.fretux.ascend.AscendMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, AscendMod.MODID);

    public static final RegistryObject<StructureType<SimpleShrineStructure>> SHRINE =
            STRUCTURE_TYPES.register("shrine_of_remembrance",
                    () -> () -> SimpleShrineStructure.CODEC);
}