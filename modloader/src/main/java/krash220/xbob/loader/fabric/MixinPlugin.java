package krash220.xbob.loader.fabric;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.gui.FabricGuiEntry;

public class MixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        String[] ver = FabricLoaderImpl.INSTANCE.getGameProvider().getRawGameVersion().split("\\.");

        if (ver.length < 2) {
            FabricGuiEntry.displayError("Incompatible mod.", new RuntimeException("This mod do not support Minecraft " + ver[0]), false);
            throw new RuntimeException("This mod do not support Minecraft " + ver[0]);
        }

        String coreFile = "/META-INF/core/fabric" + ver[0] + "." + ver[1] + ".jar";
        FileSystem fs = null;

        try {
            URI uri = MixinPlugin.class.getResource("MixinPlugin.class").toURI();
            fs = FileSystems.newFileSystem(uri, new HashMap<>());
        } catch (URISyntaxException | IOException e) {}

        Path core = fs.getPath(coreFile);

        if (!Files.exists(core)) {
            FabricGuiEntry.displayError("Incompatible mod.", new RuntimeException("This mod do not support Minecraft " + FabricLoaderImpl.INSTANCE.getGameProvider().getRawGameVersion()), false);
            throw new RuntimeException("This mod do not support Minecraft " + FabricLoaderImpl.INSTANCE.getGameProvider().getRawGameVersion());
        }

        Map<String, byte[]> patchedClasses = null;

        try {
            GameTransformer transformer = FabricLoaderImpl.INSTANCE.getGameProvider().getEntrypointTransformer();
            Field patched = transformer.getClass().getDeclaredField("patchedClasses");
            patched.setAccessible(true);
            patchedClasses = (Map<String, byte[]>) patched.get(transformer);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {}

        if (patchedClasses == null) {
            FabricGuiEntry.displayError("Incompatible mod.", new RuntimeException("Unable to load this mod."), false);
            new RuntimeException("Unable to load this mod.");
        }

        try {
            ZipInputStream input = new ZipInputStream(Files.newInputStream(core));
            for (ZipEntry entry = input.getNextEntry(); entry != null; input.closeEntry(), entry = input.getNextEntry()) {
                String name = entry.getName();

                if (name.endsWith(".class")) {
                    byte[] buf = new byte[4096];
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int read = 0;

                    while ((read = input.read(buf)) > 0) {
                        bos.write(buf, 0, read);
                    }

                    patchedClasses.put(name.replace("/", ".").substring(0, name.length() - 6), bos.toByteArray());
                    bos.close();
                }
            }

            input.close();
        } catch (IOException e1) {
            FabricGuiEntry.displayError("Incompatible mod.", new RuntimeException("This mod is damaged."), false);
            throw new RuntimeException("This mod is damaged.");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
