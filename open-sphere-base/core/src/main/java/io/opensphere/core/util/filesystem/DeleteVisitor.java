package io.opensphere.core.util.filesystem;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/** A file visitor that deletes the file. */
public final class DeleteVisitor extends SimpleFileVisitor<Path>
{
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException
    {
        Files.delete(path);
        return super.visitFile(path, attrs);
    }
}
