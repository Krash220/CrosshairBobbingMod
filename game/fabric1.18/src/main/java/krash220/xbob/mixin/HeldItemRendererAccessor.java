package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.item.HeldItemRenderer;

@Mixin(HeldItemRenderer.class)
public interface HeldItemRendererAccessor {

    @Accessor("equipProgressMainHand")
    float getEquipProgressMainHand();

    @Accessor("prevEquipProgressMainHand")
    float getPrevEquipProgressMainHand();
}
