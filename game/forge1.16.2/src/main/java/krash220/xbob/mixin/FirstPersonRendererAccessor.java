package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.item.ItemStack;

@Mixin(FirstPersonRenderer.class)
public interface FirstPersonRendererAccessor {

    @Accessor("mainHandHeight")
    float getMainHandHeight();

    @Accessor("oMainHandHeight")
    float getPrevMainHandHeight();

    @Accessor("mainHandItem")
    ItemStack getMainHandItem();
}
