package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import krash220.xbob.game.api.bus.GuiBus;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"))
    public void beforeRender(MatrixStack mat, CallbackInfo ci) {
        GuiBus.pre(mat);
    }

    @Inject(method = "renderCrosshair", at = @At("RETURN"))
    public void afterRender(MatrixStack mat, CallbackInfo ci) {
        GuiBus.post(mat);
    }
}
