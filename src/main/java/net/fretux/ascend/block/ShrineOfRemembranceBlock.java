package net.fretux.ascend.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class ShrineOfRemembranceBlock extends Block {
    public ShrineOfRemembranceBlock(Properties props) {
        super(props);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand,
                                 net.minecraft.world.phys.BlockHitResult hit) {
        if (level.isClientSide) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new net.fretux.ascend.client.screen.ShrineScreen());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}
