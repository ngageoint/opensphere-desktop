package io.opensphere.mantle.util.compiler;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * The Class StringJavaFileObject.
 */
public class StringJavaFileObject extends SimpleJavaFileObject
{
    /** The my source. */
    private final String mySource;

    /**
     * Instantiates a new string java file object.
     *
     * @param name the name
     * @param source the source
     */
    public StringJavaFileObject(String name, String source)
    {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        mySource = source;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
    {
        return mySource;
    }
}
