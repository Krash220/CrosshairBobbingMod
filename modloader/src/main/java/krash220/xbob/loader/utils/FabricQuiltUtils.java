package krash220.xbob.loader.utils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.loader.impl.gui.QuiltGuiEntry;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.gui.FabricGuiEntry;

public class FabricQuiltUtils {

    private static final boolean isQuilt;

    static {
        boolean q = false;

        try {
            Class.forName("org.quiltmc.loader.api.QuiltLoader");
            q = true;
        } catch (ClassNotFoundException e2) {}

        isQuilt = q;
    }

    public static String getPlatform() {
        return isQuilt ? "Quilt" : "Fabric";
    }

    public static String getVersion() {
        if (isQuilt) {
            return QuiltLoaderImpl.INSTANCE.getGameProvider().getRawGameVersion();
        } else {
            return FabricLoaderImpl.INSTANCE.getGameProvider().getRawGameVersion();
        }
    }

    public static void displayError(String main, String err, boolean exitAfter) {
        if (isQuilt) {
            QuiltGuiEntry.displayError(main, new RuntimeException(err), true, exitAfter);
        } else {
            FabricGuiEntry.displayError(main, new RuntimeException(err), exitAfter);
        }

        throw new RuntimeException(err);
    }

    public static Object getGameTransformer() {
        if (isQuilt) {
            return QuiltLoaderImpl.INSTANCE.getGameProvider().getEntrypointTransformer();
        } else {
            return FabricLoaderImpl.INSTANCE.getGameProvider().getEntrypointTransformer();
        }
    }

    public static EnvType getEnvironmentType() {
        if (isQuilt) {
            return QuiltLoaderImpl.INSTANCE.getEnvironmentType();
        } else {
            return FabricLoaderImpl.INSTANCE.getEnvironmentType();
        }
    }

    public static FileSystem getFileSystem(URI uri) throws IOException {
        if (isQuilt) {
            return Paths.get(uri).getFileSystem();
        } else {
            return FileSystems.newFileSystem(uri, new HashMap<>());
        }
    }
}
