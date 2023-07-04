package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {

    @Accessor("ticks")
    int getTicks();

    @Invoker("bobViewWhenHurt")
    void bobViewWhenHurt(MatrixStack mat, float partialTicks);

    @Invoker("bobView")
    void bobView(MatrixStack mat, float partialTicks);
}
