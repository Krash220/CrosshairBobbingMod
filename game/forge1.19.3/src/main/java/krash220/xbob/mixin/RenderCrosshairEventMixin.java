package krash220.xbob.mixin;

import com.tacz.guns.api.client.animation.statemachine.AnimationStateContext;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IGun;
import krash220.xbob.game.api.bus.GuiBus;
import krash220.xbob.game.api.math.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.tacz.guns.client.event.RenderCrosshairEvent")
public class RenderCrosshairEventMixin {

    @Inject(method = "onRenderOverlay", at = @At("HEAD"), remap = false)
    private static void onRenderOverlayBegin(RenderGuiOverlayEvent.Pre event, CallbackInfo ci) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }

            if (!IGun.mainhandHoldGun(player)) {
                return;
            }

            MatrixStack mat = new MatrixStack(event.getPoseStack());

            GuiBus.doPre(mat, event.getPartialTick());
        }
    }

    @Inject(method = "onRenderOverlay", at = @At("RETURN"), remap = false)
    private static void onRenderOverlayEnd(RenderGuiOverlayEvent.Pre event, CallbackInfo ci) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }

            if (!IGun.mainhandHoldGun(player)) {
                return;
            }

            MatrixStack mat = new MatrixStack(event.getPoseStack());

            GuiBus.doPost(mat, event.getPartialTick());
        }
    }

    // 为什么要隐藏准星！！！！！！！！
    // 感觉后续好容易炸= =
    @Redirect(method = "onRenderOverlay", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/entity/ReloadState\u0024StateType;isReloading()Z"), remap = false)
    private static boolean isReloading(ReloadState.StateType state)
    {
        return false;
    }

    @Redirect(method = "lambda\u0024onRenderOverlay\u00240", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/client/animation/statemachine/AnimationStateContext;shouldHideCrossHair()Z"), remap = false)
    private static boolean shouldHideCrossHair(AnimationStateContext ctx) {
        return false;
    }
}
