package krash220.xbob.game.api;

import krash220.xbob.game.api.math.MatrixStack;
import krash220.xbob.mixin.GameRendererAccessor;
import mirsario.cameraoverhaul.core.callbacks.ModifyCameraTransformCallback;
import mirsario.cameraoverhaul.core.structures.Transform;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Render {

    public static int getScaledWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    public static int getScaledHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    public static int getBlitOffset() {
        return 0;
    }

    public static boolean isDebugCrosshair() {
        Minecraft mc = Minecraft.getInstance();

        return mc.options.hideGui || mc.options.getCameraType() != CameraType.FIRST_PERSON || mc.options.renderDebug && !mc.player.isReducedDebugInfo() && !mc.options.reducedDebugInfo().get();
    }

    public static void bobView(MatrixStack mat, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        ((GameRendererAccessor) mc.gameRenderer).bobHurt(mat.mat, partialTicks);
        if (mc.options.bobView().get().booleanValue()) {
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
        Minecraft mc = Minecraft.getInstance();

        mat.mat.mulPoseMatrix(mc.gameRenderer.getProjectionMatrix(((GameRendererAccessor) mc.gameRenderer).getFov(mc.gameRenderer.getMainCamera(), partialTicks, true)));
    }

    @SuppressWarnings("resource")
    public static float getReachDistance() {
        return Minecraft.getInstance().gameMode.getPickRange();
    }

    public static void distortion(MatrixStack mat, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        float g = mc.options.screenEffectScale().get().floatValue();
        float f = Mth.lerp(partialTicks, mc.player.oSpinningEffectIntensity, mc.player.spinningEffectIntensity) * g * g;
        if (f > 0.0F) {
           int i = mc.player.hasEffect(MobEffects.CONFUSION) ? 7 : 20;
           float f1 = 5.0F / (f * f + 5.0F) - f * 0.04F;
           f1 = f1 * f1;
           mat.rotate((((GameRendererAccessor) mc.gameRenderer).getTick() + partialTicks) * i, 0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
           mat.scale(1.0F / f1, 1.0F, 1.0F);
           float f2 = -(((GameRendererAccessor) mc.gameRenderer).getTick() + partialTicks) * i;
           mat.rotate(f2, 0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
        }
    }

    public static float getCenterDepth() {
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.getCameraEntity();
        float partialTicks = mc.getFrameTime();

        if (entity != null && mc.level != null) {
            Vec3 vec3 = entity.getEyePosition(partialTicks);
            Vec3 vec31 = entity.getViewVector(partialTicks);
            Vec3 vec32 = vec3.add(vec31.x * 1000F, vec31.y * 1000F, vec31.z * 1000F);
            HitResult result = mc.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));

            if (result.getType() != HitResult.Type.MISS) {
                Vec3 begin = entity.getEyePosition(partialTicks);
                Vec3 end = result.getLocation();

                return (float) end.distanceTo(begin);
            }
        }

        return 1000F;
    }
}
