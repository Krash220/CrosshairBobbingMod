package krash220.xbob.game.api;

import krash220.xbob.mixin.CameraAccessor;
import krash220.xbob.mixin.HeldItemRendererAccessor;
import krash220.xbob.mixin.LivingEntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;

public class Player {

    private static EntityPose lastPose;

    public static float spinProgress(float partialTicks) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player.isUsingRiptide()) {
            return MathHelper.clamp((20 - ((LivingEntityAccessor) mc.player).getRiptideTicks() + partialTicks) / 20.0f, 0.0f, 1.0f);
        }

        return 0.0f;
    }

    public static float sneakProgress(float partialTicks) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();

        if (camera.getFocusedEntity() != null) {
            Entity entity = camera.getFocusedEntity();
            EntityPose pose = entity.getPose();

            if (pose != EntityPose.STANDING) {
                lastPose = pose;
            }

            if (entity.getPose() == EntityPose.STANDING && lastPose == EntityPose.CROUCHING || entity.getPose() == EntityPose.CROUCHING) {
                float standing = entity.getEyeHeight(EntityPose.STANDING);
                float crouching = entity.getEyeHeight(EntityPose.CROUCHING);

                float eyeHeight = ((CameraAccessor) camera).getCameraY();
                float eyeHeightOld = ((CameraAccessor) camera).getLastCameraY();

                return (MathHelper.lerp(partialTicks, eyeHeightOld, eyeHeight) - standing) / (crouching - standing);
            }
        }

        return 0.0f;
    }

    public static float swingProgress(float partialTicks) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        ClientPlayerEntity player = mc.player;

        if (camera.getFocusedEntity() == player && player.handSwinging) {
            return player.getHandSwingProgress(partialTicks);
        }

        return 1.0f;
    }

    public static float usingItemProgress(float partialTicks) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        ClientPlayerEntity player = mc.player;

        if (camera.getFocusedEntity() == player && player.isUsingItem()) {
            ItemStack item = player.getActiveItem();
            return (item.getMaxUseTime() - (player.getItemUseTimeLeft() - partialTicks + 1.0f)) / item.getMaxUseTime();
        }

        return 1.0f;
    }

    public static float getItemShake(float partialTicks) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        ClientPlayerEntity player = mc.player;

        if (camera.getFocusedEntity() == player && player.isUsingItem()) {
            ItemStack item = player.getActiveItem();
            float f = item.getMaxUseTime() - (player.getItemUseTimeLeft() - partialTicks + 1.0f);
            float f0 = 0.1f;

            if (item.getUseAction() == UseAction.CROSSBOW && !CrossbowItem.isCharged(item) && player.getItemUseTimeLeft() > 0) {
                f0 = Math.min(f / CrossbowItem.getPullTime(item), 1.0f);
            } else if (item.getUseAction() == UseAction.BOW) {
                f0 = f / 20.0f;
                f0 = (f0 * f0 + f0 * 2.0f) / 3.0f;
                f0 = Math.min(f0, 1.0f);
            } else if (item.getUseAction() == UseAction.SPEAR) {
                f0 = Math.min(f / 10.0f, 1.0f);
            } else {
                return 0.0f;
            }

            return MathHelper.sin((f - 0.1f) * 1.3f) * (f0 - 0.1f);
        }

        return 0.0f;
    }

    public static ItemType getUsingType() {
        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();
        ClientPlayerEntity player = mc.player;

        if (camera.getFocusedEntity() == player && player.isUsingItem()) {
            ItemStack item = player.getActiveItem();

            switch (item.getUseAction()) {
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
        MinecraftClient mc = MinecraftClient.getInstance();
        HeldItemRenderer fp = mc.gameRenderer.firstPersonRenderer;
        HeldItemRendererAccessor accessor = (HeldItemRendererAccessor) fp;
        Camera camera = mc.gameRenderer.getCamera();
        ClientPlayerEntity player = mc.player;

        if (camera.getFocusedEntity() == player && !player.isUsingItem() && !player.isRiding() && !player.isUsingRiptide()) {
            return (float) (accessor.getEquipProgressMainHand() - accessor.getPrevEquipProgressMainHand()) * partialTicks + accessor.getPrevEquipProgressMainHand();
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
