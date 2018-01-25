package io.opensphere.core.pipeline.cache;

/**
 * A single cache association, along with its reported size.
 */
class CacheNode
{
    /** The cached object. */
    private final Object myObject;

    /** The size of the cached object in bytes. */
    private final long mySizeGPU;

    /** The size of the cached object in bytes. */
    private final long mySizeVM;

    /**
     * Construct a node.
     *
     * @param object the cached object
     * @param sizeVM the amount of VM memory used by the cached object in bytes
     * @param sizeGPU the amount of video card memory used by the cached object
     *            in bytes
     */
    public CacheNode(Object object, long sizeVM, long sizeGPU)
    {
        myObject = object;
        mySizeVM = sizeVM;
        mySizeGPU = sizeGPU;
    }

    /**
     * Accessor for the cached object.
     *
     * @return the object
     */
    public Object getObject()
    {
        return myObject;
    }

    /**
     * Get the sizeGL.
     *
     * @return the sizeGL
     */
    public long getSizeGPU()
    {
        return mySizeGPU;
    }

    /**
     * Accessor for the size.
     *
     * @return the size in bytes
     */
    public long getSizeVM()
    {
        return mySizeVM;
    }

    @Override
    public String toString()
    {
        return myObject.toString();
    }
}
