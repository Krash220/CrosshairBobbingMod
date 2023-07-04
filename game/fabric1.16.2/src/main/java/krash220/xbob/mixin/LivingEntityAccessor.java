package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("riptideTicks")
    int getRiptideTicks();

    @Invoker("blockedByShield")
    boolean blockedByShield(DamageSource source);
}
