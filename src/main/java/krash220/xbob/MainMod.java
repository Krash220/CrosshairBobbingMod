package krash220.xbob;

import krash220.xbob.game.api.Loader;
import krash220.xbob.game.api.Logger;
import krash220.xbob.game.api.Player;
import krash220.xbob.game.api.Player.ItemType;
import krash220.xbob.game.api.Render;
import krash220.xbob.game.api.bus.GuiBus;
import krash220.xbob.game.api.math.MatrixStack;

public class MainMod {

    private MatrixStack matrix;
    private float swingRandom;

    public MainMod() {
        Logger.info("Hello, ${MOD_NAME}!");
        Logger.info("Platform: {}, Minecraft: {}, isClient: {}", Loader.getPlatform(), Loader.getVersion(), Loader.isClient());

        if (Loader.isClient()) {
            this.matrix = new MatrixStack();
            GuiBus.registerRenderCrosshair(this::preRenderCrossHair, this::postRenderCrossHair);
        }
    }

    public void preRenderCrossHair(MatrixStack mat, float partialTicks) {
        mat.push();

        if (!Render.isDebugCrosshair()) {
            this.matrix.identity();

            Render.updateCameraMatrix(this.matrix, partialTicks);
            Render.bobView(this.matrix, partialTicks);
            Render.distortion(this.matrix, partialTicks);
            
            float item = 1.0f - Player.changeItemProgress(partialTicks);
            float swing = Player.swingProgress(partialTicks);

            if (swing >= 0.95f) {
                this.swingRandom = (float) (Math.random() * 35f + 5f);
            }

            this.matrix.translate(item * 0.1 * swing, item * -0.3, 0.0);
            this.matrix.translate(0.0, Player.getItemShake(partialTicks) * 0.004f, 0.0);

            float[] pos = this.matrix.multiplyVector(0f, 0f, -Render.getReachDistance(), 1f);
            float[] rot = this.matrix.multiplyVector(0f, 1f, -Render.getReachDistance(), 1f);

            float offsetX = pos[0] / pos[3];
            float offsetY = pos[1] / pos[3];
            float rotOffX = rot[0] / rot[3] - pos[0] / pos[3];
            float rotOffY = rot[1] / rot[3] - pos[1] / pos[3];

            float angle = (float) Math.toDegrees(Math.atan2(rotOffX, rotOffY));

            angle += (Math.sin((Player.spinProgress(partialTicks) * 0.5 + 0.5) * Math.PI * 0.5) - Math.sin(0.25 * Math.PI)) / (1.0 - Math.sin(0.25 * Math.PI)) * 720f;
            angle += -Player.sneakProgress(partialTicks) * 70f;
            angle += Math.sin((swing * 0.5 + 0.5) * Math.PI) * this.swingRandom;

            ItemType using = Player.getUsingType();
            float usingProgress = Player.usingItemProgress(partialTicks);

            if (using == ItemType.EATING) {
                offsetY += Math.pow(Math.min(1.0, usingProgress * 5.0), 0.5) * -0.15;

                if (usingProgress > 0.9) {
                    offsetY += Math.min(1.0, (usingProgress - 0.9) * 10.0) * 0.15;
                }
            } else if (using == ItemType.CROSSBOW) {
                offsetY += Math.pow(Math.min(1.0, usingProgress * 10.0), 0.5) * -0.6;

                if (usingProgress > 0.9) {
                    offsetY += Math.pow(Math.min(1.0, (usingProgress - 0.95) * 20.0), 0.5) * 0.6;
                }
            }

            if (using == ItemType.EATING) {
                if (usingProgress > 0.2 && usingProgress < 0.9) {
                    angle += Math.sin((usingProgress - 0.2) / 0.7 * 4.0 * Math.PI) * -30.0;
                } else if (usingProgress >= 0.9) {
                    angle += Math.min(1.0, (usingProgress - 0.9) * 10.0) * -360.0;
                }
            }

            offsetX *= Render.getScaledWidth() * 0.5;
            offsetY *= Render.getScaledHeight() * 0.5;

            mat.translate(Render.getScaledWidth() * 0.5, Render.getScaledHeight() * 0.5, 0.0);
            mat.translate(offsetX, -offsetY, 0.0);
            mat.rotate(angle, 0.0f, 0.0f, 1.0f);
            mat.translate(-Render.getScaledWidth() * 0.5, -Render.getScaledHeight() * 0.5, 0.0);
        }
    }

    public void postRenderCrossHair(MatrixStack mat, float partialTicks) {
        mat.pop();
    }
}
