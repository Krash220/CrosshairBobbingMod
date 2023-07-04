package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {

    @Accessor("tick")
    int getTick();

    @Invoker("bobHurt")
    void bobHurt(MatrixStack mat, float partialTicks);

    @Invoker("bobView")
    void bobView(MatrixStack mat, float partialTicks);
}
