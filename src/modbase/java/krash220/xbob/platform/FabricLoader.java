package krash220.xbob.platform;

import java.lang.reflect.InvocationTargetException;

import net.fabricmc.api.ModInitializer;

public class FabricLoader implements ModInitializer {

    @Override
    public void onInitialize() {
        try {
            Class.forName("${MOD_ENTRYPOINT}").getConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
    }
}
