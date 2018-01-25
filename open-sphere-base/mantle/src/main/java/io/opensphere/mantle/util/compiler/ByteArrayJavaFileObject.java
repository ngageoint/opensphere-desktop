package io.opensphere.mantle.util.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * The Class ByteArrayJavaFileObject.
 */
public class ByteArrayJavaFileObject extends SimpleJavaFileObject
{
    /** The ByteArrayOutputStream. */
    private final ByteArrayOutputStream myBOS = new ByteArrayOutputStream();

    /**
     * Instantiates a new byte array java file object.
     *
     * @param name the name
     * @param aKind the kind
     */
    public ByteArrayJavaFileObject(String name, Kind aKind)
    {
        super(URI.create("string:///" + name.replace('.', '/') + aKind.extension), aKind);
    }

    /**
     * Gets the class bytes.
     *
     * @return the class bytes
     */
    public byte[] getClassBytes()
    {
        return myBOS.toByteArray();
    }

    @Override
    public OutputStream openOutputStream() throws IOException
    {
        return myBOS;
    }
}
