package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("autoSpinAttackTicks")
    int getAutoSpinAttackTicks();

    @Invoker("isDamageSourceBlocked")
    boolean isDamageSourceBlocked(DamageSource source);
}
