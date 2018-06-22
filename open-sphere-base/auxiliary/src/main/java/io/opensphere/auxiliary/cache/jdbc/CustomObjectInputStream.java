package io.opensphere.auxiliary.cache.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import edu.umd.cs.findbugs.annotations.Nullable;

import org.apache.log4j.Logger;

import io.opensphere.core.cache.ClassProvider;

/**
 * A custom {@link ObjectInputStream} that uses a provided list of
 * {@link ClassProvider} to find deserialized classes that are not found by the
 * system class loader.
 */
public class CustomObjectInputStream extends ObjectInputStream
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(CustomObjectInputStream.class);

    /**
     * The class providers.
     */
    @Nullable
    private final ClassProvider myClassProvider;

    /**
     * Constructs a new {@link CustomObjectInputStream}.
     *
     * @param classProvider The class providers to use when a class is not
     *            found.
     * @param in The serialized object data.
     * @throws IOException If an I/O error occurs while reading stream header.
     */
    public CustomObjectInputStream(@Nullable ClassProvider classProvider, InputStream in) throws IOException
    {
        super(in);
        myClassProvider = classProvider;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
    {
        Class<?> resolvedClass = null;

        try
        {
            resolvedClass = super.resolveClass(desc);
        }
        catch (ClassNotFoundException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e.getMessage(), e);
            }
        }

        if (resolvedClass == null)
        {
            if (myClassProvider != null)
            {
                resolvedClass = myClassProvider.getClass(desc.getName());
            }

            if (resolvedClass == null)
            {
                throw new ClassNotFoundException("Could not find class " + desc.getName());
            }
        }

        return resolvedClass;
    }
}
