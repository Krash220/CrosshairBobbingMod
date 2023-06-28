package ik.ffm1.game.api;

import net.minecraftforge.forgespi.Environment;
import net.minecraftforge.versions.mcp.MCPVersion;

public class Loader {

    public static String getPlatform() {
        return "Forge";
    }

    public static String getVersion() {
        return MCPVersion.getMCVersion();
    }

    public static boolean isClient() {
        return Environment.get().getDist().isClient();
    }
}
