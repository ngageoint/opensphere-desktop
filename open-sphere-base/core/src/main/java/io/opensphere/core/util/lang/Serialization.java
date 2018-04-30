package io.opensphere.core.util.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;

/**
 * Serialization utilities.
 */
public final class Serialization
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(Serialization.class);

    /** Executor for asynchronous serialization. */
    private static final ExecutorService SERIALIZATION_EXECUTOR = Executors
            .newCachedThreadPool(new NamedThreadFactory("Serialization"));

    /**
     * De-serialize an object from a byte array.
     *
     * @param arr The byte array.
     * @return The object.
     * @throws IOException If the object could not be de-serialized.
     * @throws ClassNotFoundException If the class was not found.
     */
    public static Serializable deserialize(byte[] arr) throws IOException, ClassNotFoundException
    {
        return (Serializable)new ObjectInputStream(new ByteArrayInputStream(arr)).readObject();
    }

    /**
     * Serialize a single object and return the byte array. A <code>null</code>
     * is okay.
     *
     * @param object The object.
     * @return The byte array.
     * @throws IOException If the object could not be serialized due to a stream
     *             error.
     * @throws NotSerializableException If a member of the object does not
     *             implement the {@link Serializable} interface.
     */
    public static byte[] serialize(Serializable object) throws IOException
    {
        ByteArrayOutputStream baos = Utilities.getByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        return baos.toByteArray();
    }

    /**
     * Get an input stream from which the serialized representation of an object
     * can be read. This will serialize the object on a thread provided by an
     * internal executor.
     *
     * @param object The object.
     * @return The input stream
     * @throws IOException If the object could not be serialized due to a stream
     *             error.
     * @throws NotSerializableException If a member of the object does not
     *             implement the {@link Serializable} interface.
     */
    public static InputStream serializeToStream(final Serializable object) throws IOException
    {
        return serializeToStream(object, SERIALIZATION_EXECUTOR);
    }

    /**
     * Get an input stream from which the serialized representation of an object
     * can be read. Serialize the object on a thread provided by the given
     * executor.
     *
     * @param object The object.
     * @param executor The executor that will be used to do the serialization.
     * @return The input stream
     * @throws IOException If the object could not be serialized due to a stream
     *             error.
     * @throws NotSerializableException If a member of the object does not
     *             implement the {@link Serializable} interface.
     */
    public static InputStream serializeToStream(final Serializable object, Executor executor) throws IOException
    {
        PipedInputStream is = new PipedInputStream();
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try (ObjectOutputStream oos = new ObjectOutputStream(new PipedOutputStream(is)))
                {
                    oos.writeObject(object);
                }
                catch (IOException e)
                {
                    if (e.getMessage().contains("Pipe closed"))
                    {
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("Failed to write object: " + e, e);
                        }
                    }
                    else
                    {
                        LOGGER.error("Failed to write object: " + e, e);
                    }
                }
                catch (RuntimeException e)
                {
                    // Serialization can fail in unexpected ways if the input
                    // stream is closed prematurely.
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Caught runtime exception during serialize: " + e);
                    }
                }
            }
        });
        return is;
    }

    /** Disallow instantiation. */
    private Serialization()
    {
    }
}
