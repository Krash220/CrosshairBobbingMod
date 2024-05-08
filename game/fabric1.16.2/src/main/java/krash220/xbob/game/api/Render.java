package krash220.xbob.game.api;

import krash220.xbob.game.api.math.MatrixStack;
import krash220.xbob.mixin.GameRendererAccessor;
import mirsario.cameraoverhaul.core.callbacks.ModifyCameraTransformCallback;
import mirsario.cameraoverhaul.core.structures.Transform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class Render {

    public static int getScaledWidth() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }

    public static int getScaledHeight() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }

    @SuppressWarnings("resource")
    public static int getBlitOffset() {
        return MinecraftClient.getInstance().inGameHud.getZOffset();
    }

    public static boolean isDebugCrosshair() {
        MinecraftClient mc = MinecraftClient.getInstance();

        return mc.options.hudHidden || mc.options.getPerspective() != Perspective.FIRST_PERSON || mc.options.debugEnabled && !mc.player.hasReducedDebugInfo() && !mc.options.reducedDebugInfo;
    }

    public static void bobView(MatrixStack mat, float partialTicks) {
        MinecraftClient mc = MinecraftClient.getInstance();

        ((GameRendererAccessor) mc.gameRenderer).bobViewWhenHurt(mat.mat, partialTicks);
        if (mc.options.bobView) {
            ((GameRendererAccessor) mc.gameRenderer).bobView(mat.mat, partialTicks);
        }
    }

    private static Boolean coh = null;

    public static void camOverhaul(MatrixStack mat) {
        if (coh == null) {
            try {
                Class.forName("mirsario.cameraoverhaul.core.callbacks.ModifyCameraTransformCallback");
                coh = true;
            } catch (ClassNotFoundException e) {
                coh = false;
            }
        }

        if (coh) {
            Transform t = ModifyCameraTransformCallback.EVENT.Invoker().ModifyCameraTransform(null, new Transform());

            mat.rotate((float) t.eulerRot.z, 0, 0, 1);
            mat.rotate((float) t.eulerRot.x, 1, 0, 0);
            mat.rotate((float) t.eulerRot.y, 0, 1, 0);
        }
    }

    public static void updateCameraMatrix(MatrixStack mat, float partialTicks) {
        MinecraftClient mc = MinecraftClient.getInstance();

        mat.mat.peek().getModel().multiply(mc.gameRenderer.getBasicProjectionMatrix(mc.gameRenderer.getCamera(), partialTicks, true));
    }

    @SuppressWarnings("resource")
    public static float getReachDistance() {
        return MinecraftClient.getInstance().interactionManager.getReachDistance();
    }

    public static void distortion(MatrixStack mat, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();

        float f = MathHelper.lerp(tickDelta, mc.player.lastNauseaStrength, mc.player.nextNauseaStrength) * mc.options.distortionEffectScale * mc.options.distortionEffectScale;
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

    public static float getCenterDepth() {
        MinecraftClient mc = MinecraftClient.getInstance();
        Entity entity = mc.getCameraEntity();
        float partialTicks = mc.getTickDelta();

        if (entity != null && mc.world != null) {
            Vec3d vec3d = entity.getCameraPosVec(partialTicks);
            Vec3d vec3d2 = entity.getRotationVec(partialTicks);
            Vec3d vec3d3 = vec3d.add(vec3d2.x * 1000F, vec3d2.y * 1000F, vec3d2.z * 1000F);
            HitResult result = mc.world.raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, entity));

            if (result.getType() != HitResult.Type.MISS) {
                Vec3d begin = entity.getCameraPosVec(partialTicks);
                Vec3d end = result.getPos();

                return (float) end.distanceTo(begin);
            }
        }

        return 1000F;
    }
}
