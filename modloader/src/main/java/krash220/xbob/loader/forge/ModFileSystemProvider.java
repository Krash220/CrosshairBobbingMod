package krash220.xbob.loader.forge;

import java.io.IOException;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import krash220.xbob.loader.fs.AbstractFileSystemProvider;

class ModFileSystemProvider extends AbstractFileSystemProvider {

    static final ModFileSystemProvider INSTANCE = new ModFileSystemProvider();

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
        ModPath p = (ModPath) dir;
        Path mod = p.mod;

        return Files.newDirectoryStream(mod, filter);
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {}

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        ModPath p = (ModPath) path;
        Path mod = p.mod;

        return Files.readAttributes(mod, type, options);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        ModPath p = (ModPath) path;
        Path mod = p.mod;

        return Files.readAttributes(mod, attributes, options);
    }
}
