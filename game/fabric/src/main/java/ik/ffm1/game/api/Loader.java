package ik.ffm1.game.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.FabricLoaderImpl;

public class Loader {

    public static String getPlatform() {
        return "Fabric";
    }

    public static String getVersion() {
        return FabricLoaderImpl.INSTANCE.getGameProvider().getRawGameVersion();
    }

    public static boolean isClient() {
        return FabricLoaderImpl.INSTANCE.getEnvironmentType() == EnvType.CLIENT;
    }
}
