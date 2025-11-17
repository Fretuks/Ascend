package net.fretux.ascend.worldgen;

import net.fretux.ascend.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class ShrinePiece extends StructurePiece {

    private final BlockPos pos;

    public ShrinePiece(BlockPos pos) {
        super(ModStructurePieces.SHRINE_PIECE.get(),
                0,
                new BoundingBox(
                        pos.getX(), pos.getY() - 5, pos.getZ(),
                        pos.getX(), pos.getY() + 5, pos.getZ()
                )
        );
        this.pos = pos;
    }

    public ShrinePiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructurePieces.SHRINE_PIECE.get(), tag);
        this.pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager manager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox box,
            ChunkPos chunkPos,
            BlockPos structurePos
    ) {
        if (box.isInside(pos)) {
            level.setBlock(pos, ModBlocks.SHRINE_OF_REMEMBRANCE.get().defaultBlockState(), 3);
        }
    }
}