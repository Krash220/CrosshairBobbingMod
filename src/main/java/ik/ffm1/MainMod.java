package ik.ffm1;

import ik.ffm1.game.api.Loader;
import ik.ffm1.game.api.Logger;

public class MainMod {
    public MainMod() {
        Logger.info("Hello, ${MOD_NAME}!");
        Logger.info("Platform: {}, Minecraft: {}, isClient: {}", Loader.getPlatform(), Loader.getVersion(), Loader.isClient());
    }
}
