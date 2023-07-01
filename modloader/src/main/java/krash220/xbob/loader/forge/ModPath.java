package krash220.xbob.loader.forge;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import krash220.xbob.loader.fs.AbstractPath;

class ModPath extends AbstractPath {

    final Path mod;
    final Path loader;

    ModPath(Path mod, Path loader) {
        this.mod = mod;
        this.loader = loader;
    }

    @Override
    public FileSystem getFileSystem() {
        return ModFileSystem.INSTANCE;
    }

    @Override
    public Path getRoot() {
        return this.mod;
    }

    @Override
    public Path getFileName() {
        return this.loader.getFileName();
    }

    @Override
    public int getNameCount() {
        return this.mod.getNameCount();
    }

    @Override
    public Path normalize() {
        return this;
    }

    @Override
    public Path resolve(Path other) {
        return this.mod.resolve(other.toString());
    }

    @Override
    public Path resolve(String other) {
        return this.mod.resolve(other);
    }

    @Override
    public Path relativize(Path other) {
        return this.mod.relativize(other);
    }

    @Override
    public URI toUri() {
        return this.loader.toUri();
    }

    @Override
    public Path toAbsolutePath() {
        return this;
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return this.mod;
    }
}
