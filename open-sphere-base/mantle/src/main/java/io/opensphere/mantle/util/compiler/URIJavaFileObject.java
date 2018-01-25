package io.opensphere.mantle.util.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

/**
 * A {@link JavaFileObject} that points to a URI.
 */
public class URIJavaFileObject implements JavaFileObject
{
    /** The binary name of the file. */
    private final String myBinaryName;

    /** The user-friendly name of the file. */
    private final String myName;

    /** The URI of the file. */
    private final URI myURI;

    /**
     * Constructor.
     *
     * @param binaryName The binary name of the class.
     * @param uri The URI of the file.
     */
    public URIJavaFileObject(String binaryName, URI uri)
    {
        myBinaryName = binaryName;
        myName = uri.getPath() == null ? uri.getSchemeSpecificPart() : uri.getPath();
        myURI = uri;
    }

    @Override
    public boolean delete()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Modifier getAccessLevel()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the binary name of the class.
     *
     * @return The binary name.
     */
    public String getBinaryName()
    {
        return myBinaryName;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Kind getKind()
    {
        return Kind.CLASS;
    }

    @Override
    public long getLastModified()
    {
        return 0;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public NestingKind getNestingKind()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNameCompatible(String simpleName, Kind kind)
    {
        String baseName = simpleName + kind.extension;
        return kind.equals(getKind()) && (baseName.equals(getName()) || getName().endsWith("/" + baseName));
    }

    @Override
    public InputStream openInputStream() throws IOException
    {
        return myURI.toURL().openStream();
    }

    @Override
    public OutputStream openOutputStream() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Writer openWriter() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI toUri()
    {
        return myURI;
    }
}
