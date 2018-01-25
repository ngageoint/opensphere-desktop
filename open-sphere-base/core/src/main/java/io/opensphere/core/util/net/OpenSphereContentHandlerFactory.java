package io.opensphere.core.util.net;

import java.net.ContentHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import io.opensphere.core.util.MimeType;

/**
 * A factory that creates content handlers.
 */
public final class OpenSphereContentHandlerFactory implements java.net.ContentHandlerFactory
{
    /** Singleton instance. */
    private static final OpenSphereContentHandlerFactory INSTANCE = new OpenSphereContentHandlerFactory();

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(OpenSphereContentHandlerFactory.class);

    /** Map of content types to handler classes. */
    private final Map<String, Class<? extends ContentHandler>> myMap = new ConcurrentHashMap<>();

    /**
     * Get the singleton instance.
     *
     * @return The content handler factory.
     */
    public static OpenSphereContentHandlerFactory getInstance()
    {
        return INSTANCE;
    }

    /**
     * Construct the factory.
     */
    private OpenSphereContentHandlerFactory()
    {
        registerContentHandler(MimeType.PNG.getMimeType(), ImageContentHandler.class);
        registerContentHandler(MimeType.DDS.getMimeType(), ImageContentHandler.class);
    }

    @Override
    public ContentHandler createContentHandler(String mimetype)
    {
        Class<? extends ContentHandler> cl = myMap.get(mimetype);
        if (cl == null)
        {
            LOGGER.warn("No content handler registered for mimetype [" + mimetype + "]");
            return null;
        }
        try
        {
            return cl.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            LOGGER.error("Failed to instantiate content handler: " + e, e);
        }
        return null;
    }

    /**
     * Register a content handler.
     *
     * @param mimetype The content type for the handler.
     * @param contentHandler The handler.
     */
    public void registerContentHandler(String mimetype, Class<? extends ContentHandler> contentHandler)
    {
        myMap.put(mimetype, contentHandler);
    }
}
