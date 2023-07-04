package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererAccessor {

    @Accessor("mainHandHeight")
    float getMainHandHeight();

    @Accessor("oMainHandHeight")
    float getPrevMainHandHeight();

    @Accessor("mainHandItem")
    ItemStack getMainHandItem();
}
