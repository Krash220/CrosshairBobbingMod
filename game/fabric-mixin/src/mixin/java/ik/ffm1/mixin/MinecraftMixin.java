package ik.ffm1.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

@Mixin(MinecraftClient.class)
public class MinecraftMixin {

    @Shadow
    private Window window;

    @Overwrite
    public void updateWindowTitle() {
        this.window.setTitle(this.getWindowTitle() + " <ForgeFabricMixinOne>");
    }

    @Shadow
    private String getWindowTitle() {
        return null;
    }
}
