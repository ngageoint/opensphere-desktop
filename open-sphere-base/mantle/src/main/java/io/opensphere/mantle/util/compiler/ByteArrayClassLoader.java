package io.opensphere.mantle.util.compiler;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ByteArrayClassLoader.
 */
public class ByteArrayClassLoader extends ClassLoader
{
    /** The cache. */
    private final Map<String, ByteArrayJavaFileObject> myCache = new HashMap<>();

    /**
     * Instantiates a new byte array class loader.
     */
    public ByteArrayClassLoader()
    {
        super(ByteArrayClassLoader.class.getClassLoader());
    }

    /**
     * Put.
     *
     * @param name the name
     * @param obj the obj
     */
    public void put(String name, ByteArrayJavaFileObject obj)
    {
        ByteArrayJavaFileObject co = myCache.get(name);
        if (co == null)
        {
            myCache.put(name, obj);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        Class<?> cls = null;
        try
        {
            ByteArrayJavaFileObject co = myCache.get(name);
            if (co != null)
            {
                byte[] ba = co.getClassBytes();
                cls = defineClass(name, ba, 0, ba.length);
            }
        }
        catch (RuntimeException e)
        {
            throw new ClassNotFoundException("Class name: " + name, e);
        }
        return cls;
    }
}
