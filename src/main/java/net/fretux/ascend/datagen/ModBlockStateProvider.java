package net.fretux.ascend.datagen;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, AscendMod.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile shrineModel = models().getExistingFile(modLoc("block/shrine_of_remembrance"));
        simpleBlockWithItem(ModBlocks.SHRINE_OF_REMEMBRANCE.get(), shrineModel);
    }
}