package ik.ffm1.loader.fabric;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.transformer.ext.IClassGenerator;

public class ModCoreLoader implements IClassGenerator {

    private final FileSystem fs;

    public ModCoreLoader() {
        FileSystem fs = null;

        try {
            URI uri = ModCoreLoader.class.getResource("ModCoreLoader.class").toURI();
            fs = FileSystems.newFileSystem(uri, new HashMap<>());
            fs = FileSystems.newFileSystem(fs.getPath("/META-INF/core/fabric.jar"), this.getClass().getClassLoader()); // choose a version here
        } catch (URISyntaxException | IOException e) {}

        this.fs = fs;
    }

    @Override
    public String getName() {
        return "${MOD_ID} Core";
    }

    @Override
    public boolean generate(String name, ClassNode classNode) {
        String file = name.replace('.', '/') + ".class";
        Path path = fs.getPath("/", file);

        if (!Files.exists(path)) {
            return false;
        }

        try {
            ClassReader cr = new ClassReader(Files.newInputStream(path));

            cr.accept(classNode, 0);
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
