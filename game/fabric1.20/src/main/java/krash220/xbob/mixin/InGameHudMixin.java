package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import krash220.xbob.game.api.bus.GuiBus;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"))
    public void beforeRender(DrawContext context, CallbackInfo ci) {
        GuiBus.pre(context.getMatrices());
    }

    @Inject(method = "renderCrosshair", at = @At("RETURN"))
    public void afterRender(DrawContext context, CallbackInfo ci) {
        GuiBus.post(context.getMatrices());
    }
}
