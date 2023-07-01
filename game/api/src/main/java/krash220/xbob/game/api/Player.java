package krash220.xbob.game.api;

public class Player {

    public static float spinProgress(float partialTicks) {throw new UnsupportedOperationException();}

    public static float sneakProgress(float partialTicks) {throw new UnsupportedOperationException();}

    public static float swingProgress(float partialTicks) {throw new UnsupportedOperationException();}

    public static float usingItemProgress(float partialTicks) {throw new UnsupportedOperationException();}

    public static float getItemShake(float partialTicks) {throw new UnsupportedOperationException();}

    public static ItemType getUsingType() {throw new UnsupportedOperationException();}

    public static float changeItemProgress(float partialTicks) {throw new UnsupportedOperationException();}

    public enum ItemType {
        NONE,
        EATING,
        BOW,
        SPEAR,
        CROSSBOW
    }
}
