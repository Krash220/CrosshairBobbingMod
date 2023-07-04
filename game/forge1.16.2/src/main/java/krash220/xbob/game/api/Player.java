package krash220.xbob.game.api;

import krash220.xbob.mixin.ActiveRenderInfoAccessor;
import krash220.xbob.mixin.FirstPersonRendererAccessor;
import krash220.xbob.mixin.LivingEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.util.math.MathHelper;

public class Player {

    private static Pose lastPose;
    private static float lastHandHeight;
    private static int lastSlot = -1;

    public static float spinProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player.isAutoSpinAttack()) {
            return MathHelper.clamp((20 - ((LivingEntityAccessor) mc.player).getAutoSpinAttackTicks() + partialTicks) / 20.0f, 0.0f, 1.0f);
        }

        return 0.0f;
    }

    public static float sneakProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();

        if (camera.getEntity() != null) {
            Entity entity = camera.getEntity();
            Pose pose = entity.getPose();

            if (pose != Pose.STANDING) {
                lastPose = pose;
            }

            if (entity.getPose() == Pose.STANDING && lastPose == Pose.CROUCHING || entity.getPose() == Pose.CROUCHING) {
                float standing = entity.getEyeHeight(Pose.STANDING);
                float crouching = entity.getEyeHeight(Pose.CROUCHING);

                float eyeHeight = ((ActiveRenderInfoAccessor) camera).getEyeHeight();
                float eyeHeightOld = ((ActiveRenderInfoAccessor) camera).getEyeHeightOld();

                return (MathHelper.lerp(partialTicks, eyeHeightOld, eyeHeight) - standing) / (crouching - standing);
            }
        }

        return 0.0f;
    }

    public static float swingProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
        ClientPlayerEntity player = mc.player;

        if (camera.getEntity() == player && player.swinging) {
            return player.getAttackAnim(partialTicks);
        }

        return 1.0f;
    }

    public static float usingItemProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
        ClientPlayerEntity player = mc.player;

        if (camera.getEntity() == player && player.isUsingItem()) {
            ItemStack item = player.getUseItem();
            float f = item.getUseDuration() - (player.getUseItemRemainingTicks() - partialTicks + 1.0f);

            if (item.getUseAnimation() == UseAction.BOW) {
                return Math.min(f / 20f, 1.0f);
            } else if (item.getUseAnimation() == UseAction.SPEAR) {
                return Math.min(f / 10f, 1.0f);
            } else {
                return f / item.getUseDuration();
            }
        }

        return 1.0f;
    }

    public static float getItemShake(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
        ClientPlayerEntity player = mc.player;

        if (camera.getEntity() == player && player.isUsingItem()) {
            ItemStack item = player.getUseItem();
            float f = item.getUseDuration() - (player.getUseItemRemainingTicks() - partialTicks + 1.0f);
            float f0 = 0.1f;

            if (item.getUseAnimation() == UseAction.CROSSBOW && !CrossbowItem.isCharged(item) && player.getUseItemRemainingTicks() > 0) {
                f0 = Math.min(f / CrossbowItem.getChargeDuration(item), 1.0f);
            } else if (item.getUseAnimation() == UseAction.BOW) {
                f0 = f / 20.0f;
                f0 = (f0 * f0 + f0 * 2.0f) / 3.0f;
                f0 = Math.min(f0, 1.0f);
            } else if (item.getUseAnimation() == UseAction.SPEAR) {
                f0 = Math.min(f / 10.0f, 1.0f);
            } else {
                return 0.0f;
            }

            return MathHelper.sin((f - 0.1f) * 1.3f) * (f0 - 0.1f);
        }

        return 0.0f;
    }

    public static ItemType getUsingType() {
        Minecraft mc = Minecraft.getInstance();
        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
        ClientPlayerEntity player = mc.player;

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
        FirstPersonRenderer fp = mc.gameRenderer.itemInHandRenderer;
        FirstPersonRendererAccessor accessor = (FirstPersonRendererAccessor) fp;
        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
        ClientPlayerEntity player = mc.player;

        if (camera.getEntity() == player && !player.isHandsBusy() && !player.isUsingItem() && !player.swinging) {
            float height = (accessor.getMainHandHeight() - accessor.getPrevMainHandHeight()) * partialTicks + accessor.getPrevMainHandHeight();

            if (player.getMainHandItem().getItem() == accessor.getMainHandItem().getItem() && lastSlot == player.inventory.selected) {
                lastHandHeight = Math.max(lastHandHeight, height);

                return lastHandHeight;
            } else {
                lastSlot = player.inventory.selected;
                lastHandHeight = 0.0f;

                return height;
            }
        }

        lastHandHeight = 1.0f;
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
