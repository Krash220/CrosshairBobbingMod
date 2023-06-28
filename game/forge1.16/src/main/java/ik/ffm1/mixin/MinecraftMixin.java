package ik.ffm1.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    private MainWindow window;

    @Overwrite
    public void updateTitle() {
        this.window.setTitle(this.createTitle() + " <ForgeFabricMixinOne>");
    }

    @Shadow
    private String createTitle() {
        return null;
    }
}
