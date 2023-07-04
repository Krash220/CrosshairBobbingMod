package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("autoSpinAttackTicks")
    int getAutoSpinAttackTicks();

    @Invoker("isDamageSourceBlocked")
    boolean isDamageSourceBlocked(DamageSource source);
}
