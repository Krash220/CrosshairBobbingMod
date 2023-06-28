package ik.ffm1.loader.forge;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.collect.ImmutableList;

import net.minecraftforge.fml.loading.moddiscovery.ExplodedDirectoryLocator;

public class ModLocator extends ExplodedDirectoryLocator {

    @Override
    public String name() {
        return "${MOD_ID} modloader";
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {
        HashMap<String, Object> map = new HashMap<>(arguments);
        String[] ver = ((String) arguments.get("mcVersion")).split("\\.");
        int v = Integer.parseInt(ver[0]) * 100 + Integer.parseInt(ver[1]);

        URI uri = null;
        Path loader = null;

        try {
            uri = ModLocator.class.getProtectionDomain().getCodeSource().getLocation().toURI();

            String path = uri.getPath();

            if (uri.getScheme().equals("union")) {
                if (path.contains("#")) {
                    path = path.substring(0, path.lastIndexOf("#"));
                }
            }

            loader = new File(path).toPath();
            uri = URI.create("jar:" + new File(path).toURI().toURL());

            try {
                FileSystems.newFileSystem(uri, new HashMap<>());
            } catch (FileSystemAlreadyExistsException ex) {}
        } catch (URISyntaxException | IOException e) {
            return;
        }

        FileSystem fs = FileSystems.getFileSystem(uri);

        Path mod = fs.getPath("/mod.jar");
        Path core = fs.getPath("/META-INF/core/forge" + ver[0] + "." + ver[1] + ".jar");

        try {
            mod = new ModPath(FileSystems.newFileSystem(mod, this.getClass().getClassLoader()).getPath("/"), loader);
            core = new ModPath(FileSystems.newFileSystem(core, this.getClass().getClassLoader()).getPath("/"), loader);
        } catch (IOException e) {}

        if (v > 116) {
            map.put("explodedTargets", ImmutableList.of(new ExplodedMod("yourmodid", ImmutableList.of(mod, core))));
        } else {
            map.put("explodedTargets", ImmutableList.of(ImmutablePair.of(mod, ImmutableList.of(mod, core))));
        }

        super.initArguments(map);
    }
}
