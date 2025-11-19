package net.fretux.ascend.compat;

import net.minecraftforge.fml.ModList;

public class AscendMMCompat {
    public static boolean isMindMotionPresent() {
        return ModList.get().isLoaded("mindmotion");
    }
}
