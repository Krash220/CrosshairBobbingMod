package krash220.xbob.game.api.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import krash220.xbob.game.api.math.MatrixStack;

public class GuiBus {
    
    public static float partialTicks;

    private static List<BiConsumer<MatrixStack, Float>> pre = new ArrayList<>();
    private static List<BiConsumer<MatrixStack, Float>> post = new ArrayList<>();

    public static void registerRenderCrosshair(BiConsumer<MatrixStack, Float> pre, BiConsumer<MatrixStack, Float> post) {
        GuiBus.pre.add(pre);
        GuiBus.post.add(post);
    }

    public static void pre(net.minecraft.client.util.math.MatrixStack matrix) {
        MatrixStack mat = new MatrixStack(matrix);

        for (BiConsumer<MatrixStack, Float> handler : pre) {
            handler.accept(mat, partialTicks);
        }
    }

    public static void post(net.minecraft.client.util.math.MatrixStack matrix) {
        MatrixStack mat = new MatrixStack(matrix);

        for (BiConsumer<MatrixStack, Float> handler : post) {
            handler.accept(mat, partialTicks);
        }
    }
}
