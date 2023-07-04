package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import krash220.xbob.game.api.bus.PlayerBus;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.util.math.BlockPos;

@Mixin(PlayerController.class)
public class PlayerControllerMixin {

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    public void onDestroyBlock(BlockPos pos, CallbackInfoReturnable<?> ci) {
        if (ci.getReturnValueZ()) {
            PlayerBus.onBreakBlock();
        }
    }
}
