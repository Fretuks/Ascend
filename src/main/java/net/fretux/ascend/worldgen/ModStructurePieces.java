package net.fretux.ascend.worldgen;

import net.fretux.ascend.AscendMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructurePieces {

    public static final DeferredRegister<StructurePieceType> PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, AscendMod.MODID);

    public static final RegistryObject<StructurePieceType> SHRINE_PIECE =
            PIECES.register("shrine_piece",
                    () -> ShrinePiece::new);
}