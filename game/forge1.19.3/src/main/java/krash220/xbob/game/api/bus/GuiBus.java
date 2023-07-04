package krash220.xbob.game.api.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import krash220.xbob.game.api.math.MatrixStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
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

    @SubscribeEvent
    public static void pre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type()) {
            MatrixStack mat = new MatrixStack(event.getPoseStack());

            for (BiConsumer<MatrixStack, Float> handler : pre) {
                handler.accept(mat, event.getPartialTick());
            }
        }
    }

    @SubscribeEvent
    public static void post(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type()) {
            MatrixStack mat = new MatrixStack(event.getPoseStack());

            for (BiConsumer<MatrixStack, Float> handler : post) {
                handler.accept(mat, event.getPartialTick());
            }
        }
    }
}
