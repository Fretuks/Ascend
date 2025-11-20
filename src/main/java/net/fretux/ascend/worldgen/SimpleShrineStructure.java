package net.fretux.ascend.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;

import java.util.Optional;

public class SimpleShrineStructure extends Structure {
    public static final Codec<SimpleShrineStructure> CODEC =
            simpleCodec(SimpleShrineStructure::new);

    public SimpleShrineStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SHRINE.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        BlockPos pos = ctx.chunkPos().getMiddleBlockPosition(0);

        int y = ctx.chunkGenerator().getFirstFreeHeight(
                pos.getX(),
                pos.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG,
                ctx.heightAccessor(),
                ctx.randomState()
        );
        BlockPos finalPos = new BlockPos(pos.getX(), y, pos.getZ());
        if (!isValidShrineLocation(ctx, finalPos)) {
            return Optional.empty();
        }
        return Optional.of(new GenerationStub(finalPos, builder -> {
            builder.addPiece(new ShrinePiece(finalPos));
        }));
    }

    private boolean isValidShrineLocation(GenerationContext ctx, BlockPos pos) {
        var column = ctx.chunkGenerator().getBaseColumn(
                pos.getX(),
                pos.getZ(),
                ctx.heightAccessor(),
                ctx.randomState()
        );
        var groundState = column.getBlock(pos.getY() - 1);
        if (!groundState.getFluidState().isEmpty()) return false;
        if (!groundState.isSolid()) return false;
        if (groundState.canBeReplaced()) return false;
        if (groundState.is(net.minecraft.tags.BlockTags.SAND)) return false;
        if (groundState.is(net.minecraft.tags.BlockTags.SNOW)) return false;
        if (groundState.is(net.minecraft.tags.BlockTags.LEAVES)) return false;
        return true;
    }
}
