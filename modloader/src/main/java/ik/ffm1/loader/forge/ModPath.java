package ik.ffm1.loader.forge;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

class ModPath implements Path {

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
    public boolean isAbsolute() {
        throw new UnsupportedOperationException();
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
    public Path getParent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNameCount() {
        return this.mod.getNameCount();
    }

    @Override
    public Path getName(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean startsWith(Path other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean endsWith(Path other) {
        throw new UnsupportedOperationException();
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

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Path other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean startsWith(String other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean endsWith(String other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path resolveSibling(Path other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path resolveSibling(String other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Path> iterator() {
        throw new UnsupportedOperationException();
    }
}
