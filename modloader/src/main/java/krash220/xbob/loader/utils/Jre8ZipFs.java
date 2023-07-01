package krash220.xbob.loader.utils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;

import krash220.xbob.loader.fs.AbstractFileSystem;
import krash220.xbob.loader.fs.AbstractFileSystemProvider;
import krash220.xbob.loader.fs.AbstractPath;

public class Jre8ZipFs {

    private static FileSystemProvider zipProvider = null;
    private static Constructor<?> constructor = null;

    public static FileSystem newZipFs(Path path) {
        if (zipProvider == null) {
            for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
                if (provider.getClass().getName().equals("com.sun.nio.zipfs.ZipFileSystemProvider")) {
                    zipProvider = provider;
                    break;
                }
            }
        }

        if (zipProvider != null) {
            if (constructor == null) {
                try {
                    Class<?> clazz = Class.forName("com.sun.nio.zipfs.ZipFileSystem");
                    constructor = clazz.getDeclaredConstructors()[0];
                    constructor.setAccessible(true);
                } catch (ClassNotFoundException | IllegalArgumentException e) {}
            }

            if (constructor != null) {
                try {
                    return (FileSystem) constructor.newInstance(zipProvider, new ZipEntryPath(path), Collections.emptyMap());
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
            }
        }

        throw new UnsupportedOperationException("Unable to load jar in jar.");
    }

    private static class ZipEntryPath extends AbstractPath {

        final Path path;

        ZipEntryPath(Path path) {
            this.path = path;
        }

        @Override
        public FileSystem getFileSystem() {
            return SeekableZipEntry.INSTANCE;
        }
    }

    private static class SeekableZipEntry extends AbstractFileSystem {
        private static final SeekableZipEntry INSTANCE = new SeekableZipEntry();

        @Override
        public FileSystemProvider provider() {
            return SeekableZipEntryProvider.INSTANCE;
        }
    }

    private static class SeekableZipEntryProvider extends AbstractFileSystemProvider {
        private static final SeekableZipEntryProvider INSTANCE = new SeekableZipEntryProvider();

        @Override
        public void checkAccess(Path path, AccessMode... modes) throws IOException {}

        @Override
        public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
            SeekableByteChannel channel = Files.newByteChannel(((ZipEntryPath) path).path, options, attrs);

            final long size = channel.size();
            final Buffer buf = ByteBuffer.allocateDirect((int) size);

            channel.read((ByteBuffer) buf);
            buf.flip();

            return new SeekableByteChannel() {
                @Override
                public boolean isOpen() {
                    return true;
                }

                @Override
                public void close() throws IOException {}

                @Override
                public int read(ByteBuffer dst) throws IOException {
                    int srcRem = buf.remaining();
                    int dstRem = dst.remaining();
                    int read = Math.min(srcRem, dstRem);

                    byte[] bytes = new byte[read];

                    ((ByteBuffer) buf).get(bytes);
                    dst.put(bytes);

                    return read;
                }

                @Override
                public int write(ByteBuffer src) throws IOException {
                    throw new UnsupportedOperationException();
                }

                @Override
                public long position() throws IOException {
                    return buf.position();
                }

                @Override
                public SeekableByteChannel position(long newPosition) throws IOException {
                    buf.position((int) newPosition);

                    return this;
                }

                @Override
                public long size() throws IOException {
                    return size;
                }

                @Override
                public SeekableByteChannel truncate(long size) throws IOException {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
