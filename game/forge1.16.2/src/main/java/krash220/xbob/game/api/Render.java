package krash220.xbob.game.api;

import krash220.xbob.game.api.math.MatrixStack;
import krash220.xbob.mixin.GameRendererAccessor;
import mirsario.cameraoverhaul.core.callbacks.ModifyCameraTransformCallback;
import mirsario.cameraoverhaul.core.structures.Transform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

public class Render {

    public static int getScaledWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    public static int getScaledHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    @SuppressWarnings("resource")
    public static int getBlitOffset() {
        return Minecraft.getInstance().gui.getBlitOffset();
    }

    public static boolean isDebugCrosshair() {
        Minecraft mc = Minecraft.getInstance();

        return mc.options.hideGui || mc.options.getCameraType() != PointOfView.FIRST_PERSON || mc.options.renderDebug && !mc.player.isReducedDebugInfo() && !mc.options.reducedDebugInfo;
    }

    public static void bobView(MatrixStack mat, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        ((GameRendererAccessor) mc.gameRenderer).bobHurt(mat.mat, partialTicks);
        if (mc.options.bobView) {
            ((GameRendererAccessor) mc.gameRenderer).bobView(mat.mat, partialTicks);
        }
    }

    private static Boolean coh = null;

    public static void modCamera(MatrixStack mat) {
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

        mat.mat.last().pose().multiply(mc.gameRenderer.getProjectionMatrix(mc.gameRenderer.getMainCamera(), partialTicks, true));
    }

    @SuppressWarnings("resource")
    public static float getReachDistance() {
        return Minecraft.getInstance().gameMode.getPickRange();
    }

    public static void distortion(MatrixStack mat, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        float f = MathHelper.lerp(partialTicks, mc.player.oPortalTime, mc.player.portalTime) * mc.options.screenEffectScale * mc.options.screenEffectScale;
        if (f > 0.0F) {
           int i = mc.player.hasEffect(Effects.CONFUSION) ? 7 : 20;
           float f1 = 5.0F / (f * f + 5.0F) - f * 0.04F;
           f1 = f1 * f1;
           mat.rotate((((GameRendererAccessor) mc.gameRenderer).getTick() + partialTicks) * i, 0.0F, MathHelper.SQRT_OF_TWO / 2.0F, MathHelper.SQRT_OF_TWO / 2.0F);
           mat.scale(1.0F / f1, 1.0F, 1.0F);
           float f2 = -(((GameRendererAccessor) mc.gameRenderer).getTick() + partialTicks) * i;
           mat.rotate(f2, 0.0F, MathHelper.SQRT_OF_TWO / 2.0F, MathHelper.SQRT_OF_TWO / 2.0F);
        }
    }

    public static float getCenterDepth() {
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.getCameraEntity();
        float partialTicks = mc.getFrameTime();

        if (entity != null && mc.level != null) {
            Vector3d vector3d = entity.getEyePosition(partialTicks);
            Vector3d vector3d1 = entity.getViewVector(partialTicks);
            Vector3d vector3d2 = vector3d.add(vector3d1.x * 1000F, vector3d1.y * 1000F, vector3d1.z * 1000F);
            RayTraceResult result = mc.level.clip(new RayTraceContext(vector3d, vector3d2, RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.NONE, entity));

            if (result.getType() != RayTraceResult.Type.MISS) {
                Vector3d begin = entity.getEyePosition(partialTicks);
                Vector3d end = result.getLocation();

                return (float) end.distanceTo(begin);
            }
        }

        return 1000F;
    }
}
