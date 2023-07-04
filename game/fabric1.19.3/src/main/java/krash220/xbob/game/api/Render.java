package krash220.xbob.game.api;

import krash220.xbob.game.api.math.MatrixStack;
import krash220.xbob.mixin.GameRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;

public class Render {

    public static int getScaledWidth() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }

    public static int getScaledHeight() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }

    public static int getBlitOffset() {
        return 0;
    }

    public static boolean isDebugCrosshair() {
        MinecraftClient mc = MinecraftClient.getInstance();

        return mc.options.debugEnabled && !mc.options.hudHidden && !mc.player.hasReducedDebugInfo() && !mc.options.getReducedDebugInfo().getValue().booleanValue();
    }

    public static void bobView(MatrixStack mat, float partialTicks) {
        MinecraftClient mc = MinecraftClient.getInstance();

        ((GameRendererAccessor) mc.gameRenderer).bobViewWhenHurt(mat.mat, partialTicks);
        if (mc.options.getBobView().getValue().booleanValue()) {
            ((GameRendererAccessor) mc.gameRenderer).bobView(mat.mat, partialTicks);
        }
    }

    public static void updateCameraMatrix(MatrixStack mat, float partialTicks) {
        MinecraftClient mc = MinecraftClient.getInstance();

        mat.mat.multiplyPositionMatrix(mc.gameRenderer.getBasicProjectionMatrix(((GameRendererAccessor) mc.gameRenderer).getFov(mc.gameRenderer.getCamera(), partialTicks, true)));
    }

    @SuppressWarnings("resource")
    public static float getReachDistance() {
        return MinecraftClient.getInstance().interactionManager.getReachDistance();
    }

    public static void distortion(MatrixStack mat, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();

        float g = mc.options.getDistortionEffectScale().getValue().floatValue();
        float f = MathHelper.lerp(tickDelta, mc.player.lastNauseaStrength, mc.player.nextNauseaStrength) * g * g;
        if (f > 0.0F) {
           int i = mc.player.hasStatusEffect(StatusEffects.NAUSEA) ? 7 : 20;
           float f1 = 5.0F / (f * f + 5.0F) - f * 0.04F;
           f1 = f1 * f1;
           mat.rotate((((GameRendererAccessor) mc.gameRenderer).getTicks() + tickDelta) * i, 0.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F);
           mat.scale(1.0F / f1, 1.0F, 1.0F);
           float f2 = -(((GameRendererAccessor) mc.gameRenderer).getTicks() + tickDelta) * i;
           mat.rotate(f2, 0.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F, MathHelper.SQUARE_ROOT_OF_TWO / 2.0F);
        }
    }
}
