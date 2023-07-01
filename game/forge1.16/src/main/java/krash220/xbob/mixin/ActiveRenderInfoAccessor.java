package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.ActiveRenderInfo;

@Mixin(ActiveRenderInfo.class)
public interface ActiveRenderInfoAccessor {

    @Accessor("eyeHeight")
    float getEyeHeight();

    @Accessor("eyeHeightOld")
    float getEyeHeightOld();
}
