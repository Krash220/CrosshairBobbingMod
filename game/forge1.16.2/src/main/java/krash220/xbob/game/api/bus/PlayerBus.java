package krash220.xbob.game.api.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import krash220.xbob.mixin.LivingEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerBus {

    private static boolean registered = false;
    private static List<Consumer<Float>> attack = new ArrayList<>();

    public static void registerAttack(Consumer<Float> attack) {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(PlayerBus.class);
        }

        PlayerBus.attack.add(attack);
    }

    @SuppressWarnings("resource")
    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        LivingEntity entity = event.getEntityLiving();
        DamageSource source = event.getSource();

        if (entity.level.isClientSide) {
            if (!entity.isInvulnerableTo(source) && !entity.isDeadOrDying()) {
                if (source.getEntity() == Minecraft.getInstance().player) {
                    float damage = (source.getDirectEntity() == source.getEntity() ? event.getAmount() : 1.0f) * (((LivingEntityAccessor) entity).isDamageSourceBlocked(source) ?  0 : 1);

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
