package krash220.xbob.game.api.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import krash220.xbob.mixin.LivingEntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public class PlayerBus {

    private static List<Consumer<Float>> attack = new ArrayList<>();

    public static void registerAttack(Consumer<Float> attack) {
        PlayerBus.attack.add(attack);
    }

    @SuppressWarnings("resource")
    public static void onAttack(LivingEntity entity, DamageSource source, float damage) {
        if (entity.world.isClient) {
            if (!entity.isInvulnerableTo(source) && !entity.isDead()) {
                if (source.getAttacker() == MinecraftClient.getInstance().player) {
                    damage = (source.getSource() == source.getAttacker() ? damage : 1.0f) * (((LivingEntityAccessor) entity).blockedByShield(source) ?  0 : 1);

                    for (Consumer<Float> handler : attack) {
                        handler.accept(damage);
                    }
                }
            }
        }
    }

    public static void onBreakBlock() {
        for (Consumer<Float> handler : attack) {
            handler.accept(0.6f);
        }
    }
}
