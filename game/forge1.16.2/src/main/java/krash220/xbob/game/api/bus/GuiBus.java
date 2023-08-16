package krash220.xbob.game.api.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import krash220.xbob.game.api.math.MatrixStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuiBus {

    private static boolean registered = false;

    private static List<BiConsumer<MatrixStack, Float>> pre = new ArrayList<>();
    private static List<BiConsumer<MatrixStack, Float>> post = new ArrayList<>();

    public static void registerRenderCrosshair(BiConsumer<MatrixStack, Float> pre, BiConsumer<MatrixStack, Float> post) {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(GuiBus.class);
        }

        GuiBus.pre.add(pre);
        GuiBus.post.add(post);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void pre(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == ElementType.CROSSHAIRS && !event.isCanceled()) {
            MatrixStack mat = new MatrixStack(event.getMatrixStack());

            for (BiConsumer<MatrixStack, Float> handler : pre) {
                handler.accept(mat, event.getPartialTicks());
            }
        }
    }

    @SubscribeEvent
    public static void post(RenderGameOverlayEvent.Post event) {
        if (event.getType() == ElementType.CROSSHAIRS) {
            MatrixStack mat = new MatrixStack(event.getMatrixStack());

            for (BiConsumer<MatrixStack, Float> handler : post) {
                handler.accept(mat, event.getPartialTicks());
            }
        }
    }
}
