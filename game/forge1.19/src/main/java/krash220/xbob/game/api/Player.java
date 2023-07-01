package krash220.xbob.game.api;

import krash220.xbob.mixin.CameraAccessor;
import krash220.xbob.mixin.ItemInHandRendererAccessor;
import krash220.xbob.mixin.LivingEntityAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;

public class Player {

    private static Pose lastPose;

    public static float spinProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player.isAutoSpinAttack()) {
            return Mth.clamp((20 - ((LivingEntityAccessor) mc.player).getAutoSpinAttackTicks() + partialTicks) / 20.0f, 0.0f, 1.0f);
        }

        return 0.0f;
    }

    public static float sneakProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();

        if (camera.getEntity() != null) {
            Entity entity = camera.getEntity();
            Pose pose = entity.getPose();

            if (pose != Pose.STANDING) {
                lastPose = pose;
            }

            if (entity.getPose() == Pose.STANDING && lastPose == Pose.CROUCHING || entity.getPose() == Pose.CROUCHING) {
                float standing = entity.getEyeHeight(Pose.STANDING);
                float crouching = entity.getEyeHeight(Pose.CROUCHING);

                float eyeHeight = ((CameraAccessor) camera).getEyeHeight();
                float eyeHeightOld = ((CameraAccessor) camera).getEyeHeightOld();

                return (Mth.lerp(partialTicks, eyeHeightOld, eyeHeight) - standing) / (crouching - standing);
            }
        }

        return 0.0f;
    }

    public static float swingProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        LocalPlayer player = mc.player;

        if (camera.getEntity() == player && player.swinging) {
            return player.getAttackAnim(partialTicks);
        }

        return 1.0f;
    }

    public static float usingItemProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        LocalPlayer player = mc.player;

        if (camera.getEntity() == player && player.isUsingItem()) {
            ItemStack item = player.getUseItem();
            return (item.getUseDuration() - (player.getUseItemRemainingTicks() - partialTicks + 1.0f)) / item.getUseDuration();
        }

        return 1.0f;
    }

    public static float getItemShake(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        LocalPlayer player = mc.player;

        if (camera.getEntity() == player && player.isUsingItem()) {
            ItemStack item = player.getUseItem();
            float f = item.getUseDuration() - (player.getUseItemRemainingTicks() - partialTicks + 1.0f);
            float f0 = 0.1f;

            if (item.getUseAnimation() == UseAnim.CROSSBOW && !CrossbowItem.isCharged(item) && player.getUseItemRemainingTicks() > 0) {
                f0 = Math.min(f / CrossbowItem.getChargeDuration(item), 1.0f);
            } else if (item.getUseAnimation() == UseAnim.BOW) {
                f0 = f / 20.0f;
                f0 = (f0 * f0 + f0 * 2.0f) / 3.0f;
                f0 = Math.min(f0, 1.0f);
            } else if (item.getUseAnimation() == UseAnim.SPEAR) {
                f0 = Math.min(f / 10.0f, 1.0f);
            } else {
                return 0.0f;
            }

            return Mth.sin((f - 0.1f) * 1.3f) * (f0 - 0.1f);
        }

        return 0.0f;
    }

    public static ItemType getUsingType() {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        LocalPlayer player = mc.player;

        if (camera.getEntity() == player && player.isUsingItem()) {
            ItemStack item = player.getUseItem();

            switch (item.getUseAnimation()) {
            case EAT:
            case DRINK:
                return ItemType.EATING;
            case BOW:
                return ItemType.BOW;
            case SPEAR:
                return ItemType.SPEAR;
            default:
                break;
            }

            if (item.getItem() == Items.CROSSBOW && !CrossbowItem.isCharged(item)) {
                return ItemType.CROSSBOW;
            }
        }

        return ItemType.NONE;
    }

    public static float changeItemProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        ItemInHandRenderer fp = mc.gameRenderer.itemInHandRenderer;
        ItemInHandRendererAccessor accessor = (ItemInHandRendererAccessor) fp;
        Camera camera = mc.gameRenderer.getMainCamera();
        LocalPlayer player = mc.player;

        if (camera.getEntity() == player && !player.isUsingItem() && !player.isHandsBusy() && !player.isAutoSpinAttack()) {
            return (float) (accessor.getMainHandHeight() - accessor.getPrevMainHandHeight()) * partialTicks + accessor.getPrevMainHandHeight();
        }

        return 1.0f;
    }

    public enum ItemType {
        NONE,
        EATING,
        BOW,
        SPEAR,
        CROSSBOW
    }
}
