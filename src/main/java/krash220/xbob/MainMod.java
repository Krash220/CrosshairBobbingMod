package krash220.xbob;

import java.util.function.Function;

import krash220.xbob.game.api.Config;
import krash220.xbob.game.api.Loader;
import krash220.xbob.game.api.Logger;
import krash220.xbob.game.api.Player;
import krash220.xbob.game.api.Player.ItemType;
import krash220.xbob.game.api.Render;
import krash220.xbob.game.api.bus.GuiBus;
import krash220.xbob.game.api.bus.PlayerBus;
import krash220.xbob.game.api.math.MatrixStack;

public class MainMod {

    private static final float MIN_ATTACK_SCALE = 1.4f;
    private static final float MAX_ATTACK_SCALE = 2.0f;
    private static final float MIN_ATTACK_DEGREES = 15f;
    private static final float MAX_ATTACK_DEGREES = 25f;
    private static final int ATTACK_ANIM_LEN = 200;

    private static final int CRIT_ANIM_LEN = 300;
    private static final float CRIT_SCALE = 2.0f;

    private static final String CONFIG_BOB = "bob";
    private static final String CONFIG_SNEAK = "sneak";
    private static final String CONFIG_ITEM = "item";
    private static final String CONFIG_AIM = "aim";
    private static final String CONFIG_SPEAR = "spear";
    private static final String CONFIG_XBOW = "xbow";
    private static final String CONFIG_EAT = "eat";
    private static final String CONFIG_ATK = "atk";

    private MatrixStack matrix;

    private boolean isCrit;
    private int critDir = 1;
    private long attackAnim;
    private float attackAnimDegree;
    private float attackAnimScale;
    private float attackDamage;

    public MainMod() {
        Logger.info("Hello, ${MOD_NAME}!");
        Logger.info("Platform: {}, Minecraft: {}, isClient: {}", Loader.getPlatform(), Loader.getVersion(), Loader.isClient());

        this.matrix = new MatrixStack();
        GuiBus.registerRenderCrosshair(this::preRenderCrossHair, this::postRenderCrossHair);
        PlayerBus.registerAttack(this::onAttack);

        Config.define(CONFIG_BOB, true);
        Config.define(CONFIG_SNEAK, true);
        Config.define(CONFIG_ITEM, true);
        Config.define(CONFIG_AIM, true);
        Config.define(CONFIG_SPEAR, true);
        Config.define(CONFIG_XBOW, true);
        Config.define(CONFIG_EAT, true);
        Config.define(CONFIG_ATK, true);
        Config.load();
        Config.registerGui();
    }

    public void preRenderCrossHair(MatrixStack mat, float partialTicks) {
        mat.push();

        if (!Render.isDebugCrosshair()) {
            this.matrix.identity();

            Render.updateCameraMatrix(this.matrix, partialTicks);

            if (Config.check(CONFIG_BOB)) {
                Render.bobView(this.matrix, partialTicks);
                Render.distortion(this.matrix, partialTicks);
                Render.camOverhaul(this.matrix);
            }

            float item = 1.0f - Player.changeItemProgress(partialTicks);
            float swing = Player.swingProgress(partialTicks);

            long now = System.currentTimeMillis();

            if (!this.isCrit && now < this.attackAnim + ATTACK_ANIM_LEN || this.isCrit && now < this.attackAnim + CRIT_ANIM_LEN || !Config.check(CONFIG_ATK)) {
                swing = 1.0f;
            }

            if (Config.check(CONFIG_ITEM)) {
                this.matrix.translate(item * 0.08, item * -0.3, 0.0);
            }

//            if (Config.check(CONFIG_AIM)) {
//                this.matrix.translate(0.0, Player.getItemShake(partialTicks) * 0.01f, 0.0);
//            }

            float dist = Render.getCenterDepth();//Math.max(Render.getCenterDepth(), Render.getReachDistance());
            float[] pos = this.matrix.multiplyVector(0f, 0f, -dist, 1f);
            float[] rot = this.matrix.multiplyVector(0f, 1f, -dist, 1f);

            float offsetX = pos[0] / pos[3];
            float offsetY = pos[1] / pos[3];
            float rotOffX = rot[0] / rot[3] - pos[0] / pos[3];
            float rotOffY = rot[1] / rot[3] - pos[1] / pos[3];

            float angle = (float) Math.toDegrees(Math.atan2(rotOffX, rotOffY));
            float scale = 1.0f + interpolation(Math::sin, swing, 0.5 * Math.PI, Math.PI, false) * 0.2f;
            float scaleBow = 1.0f;

            if (Config.check(CONFIG_SPEAR)) {
                angle += interpolation(Math::sin, Player.spinProgress(partialTicks), 0, 0.5 * Math.PI, false) * 720f;
            }

            if (Config.check(CONFIG_SNEAK)) {
                angle += -Player.sneakProgress(partialTicks) * 70f;
            }

            ItemType using = Player.getUsingType();
            float usingProgress = Player.usingItemProgress(partialTicks);

//            if (using == ItemType.EATING && Config.check(CONFIG_EAT)) {
//                if (usingProgress >= 0.2 && usingProgress < 0.9) {
//                    offsetX += interpolation(Math::sin, (usingProgress - 0.2) / 0.7, 0.0, 6.0 * Math.PI, false) * -0.01;
//                }
//            }

            if (using == ItemType.EATING && Config.check(CONFIG_EAT)) {
                if (usingProgress < 0.2) {
                    angle += interpolation(Math::sin, Math.min(1.0, usingProgress * 10.0), 0.0, 0.5 * Math.PI, false) * 135.0;
                } else if (usingProgress >= 0.2 && usingProgress < 0.9) {
                    angle += interpolation(Math::sin, (usingProgress - 0.2) / 0.7, 0.0, 6.0 * Math.PI, false) * -30.0 + 45.0;
                } else if (usingProgress >= 0.9) {
                    angle += interpolation(Math::sin, Math.min(1.0, (usingProgress - 0.9) * 10.0), 0.0, 0.5 * Math.PI, false) * -135.0 + 45.0;
                }
            } else if (using == ItemType.CROSSBOW && Config.check(CONFIG_XBOW)) {
                if (usingProgress < 0.9) {
                    angle += usingProgress / 0.9 * 360.0;
                } else if (usingProgress >= 0.9) {
                    angle += interpolation(Math::sin, Math.min(1.0, (usingProgress - 0.9) * 10.0), 0, 0.5 * Math.PI, false) * -180.0 + 180.0;
                }
//            } else if ((using == ItemType.BOW || using == ItemType.SPEAR) && Config.check(CONFIG_AIM)) {
//                angle = lerp(angle, -45.0f, interpolation(Math::sin, usingProgress, 0, 0.5 * Math.PI, false));
            }

            if (using == ItemType.CROSSBOW && Config.check(CONFIG_XBOW)) {
                if (usingProgress < 0.9) {
                    scale *= interpolation(MainMod::fract, 1.0 - usingProgress / 0.9, 0, 5, false) * 0.8 + 1.0;
                }
            } else if ((using == ItemType.BOW || using == ItemType.SPEAR) && Config.check(CONFIG_AIM)) {
                scale *= interpolation(Math::sin, usingProgress, 0, 0.5 * Math.PI, false) * 0.5 + 1.0;
                scaleBow *= interpolation(Math::sin, usingProgress, 0, 0.5 * Math.PI, false) * -0.6 + 1.0;
            }

            if (Config.check(CONFIG_ATK)) {
                if (this.isCrit && now < this.attackAnim + CRIT_ANIM_LEN) {
                    float x = lerpTime(this.attackAnim, this.attackAnim + CRIT_ANIM_LEN, now);

                    angle += interpolation(Math::sin, x, 0, 0.5 * Math.PI, false) * 360.0f * critDir;
                    scale *= interpolation(Math::sin, x, 0, Math.PI, false) * (CRIT_SCALE - 1.0f) + 1.0f;
                } else if (now < this.attackAnim + ATTACK_ANIM_LEN) {
                    float x = lerpTime(this.attackAnim, this.attackAnim + ATTACK_ANIM_LEN, now);

                    if (this.attackDamage > 0.01) {
                        angle += interpolation(Math::sin, x, 0.5 * Math.PI, Math.PI, false) * this.attackDamage * this.attackAnimDegree;
                        scale *= interpolation(Math::sin, x, 0.5 * Math.PI, Math.PI, false) * this.attackDamage * (this.attackAnimScale - 1.0f) + 1.0f;
                    } else {
                        offsetX += interpolation(Math::sin, x, 0, 4.0 * Math.PI, false) * 0.01;
                    }
                }
            }

            offsetX *= Render.getScaledWidth() * 0.5;
            offsetY *= Render.getScaledHeight() * 0.5;

            mat.translate(Render.getScaledWidth() * 0.5, Render.getScaledHeight() * 0.5, 0.0);
            mat.translate(offsetX, -offsetY, 0.0);
            mat.rotate(angle, 0.0f, 0.0f, 1.0f);
            mat.scale(scale, scale, scale);
//            mat.rotate(45.0f, 0.0f, 0.0f, 1.0f);
            mat.scale(1.0f, scaleBow, 1.0f);
//            mat.rotate(-45.0f, 0.0f, 0.0f, 1.0f);
            mat.translate(-Render.getScaledWidth() * 0.5, -Render.getScaledHeight() * 0.5, 0.0);
        }
    }

    public void postRenderCrossHair(MatrixStack mat, float partialTicks) {
        mat.pop();
    }

    public void onAttack(float damage) {
        long now = System.currentTimeMillis();
        boolean crit = damage > 1.0f;

        if (crit || this.isCrit && now > this.attackAnim + CRIT_ANIM_LEN || !this.isCrit) {
            this.attackDamage = Math.min(1.0f, damage);
            this.attackAnim = now;

            float r = (float) Math.random();

            this.attackAnimDegree = r * (MAX_ATTACK_DEGREES - MIN_ATTACK_DEGREES) + MIN_ATTACK_DEGREES;
            this.attackAnimScale = r * (MAX_ATTACK_SCALE - MIN_ATTACK_SCALE) + MIN_ATTACK_SCALE;

            this.isCrit = crit;
            this.critDir *= -1;
        }
    }

    private float interpolation(Function<Double, Double> func, double x, double begin, double end, boolean beginZeroY) {
        x = Math.max(Math.min(1f, x), 0f);

        float y = func.apply((double) ((end - begin) * x + begin)).floatValue();

        if (beginZeroY) {
            y -= func.apply((double) begin).floatValue();
        }

        return y;
    }

    private float lerp(float a, float b, float x) {
        return (1 - x) * a + x * b;
    }

    private float lerpTime(long begin, long end, long now) {
        return (float) (now - begin) / (float) (end - begin);
    }

    private static double fract(double v) {
        return v - Math.floor(v);
    }
}
