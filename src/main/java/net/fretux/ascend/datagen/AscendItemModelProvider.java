package net.fretux.ascend.datagen;

import net.fretux.ascend.AscendMod;
import net.fretux.ascend.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class AscendItemModelProvider extends ItemModelProvider {

    public AscendItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AscendMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        assert ModItems.MOONSEYE_TOME.getId() != null;
        basicItem(ModItems.MOONSEYE_TOME.getId().getPath());
    }

    private void basicItem(String name) {
        getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", AscendMod.MODID + ":item/" + name);
    }

    @Override
    public @NotNull String getName() {
        return "Ascend Item Models";
    }
}