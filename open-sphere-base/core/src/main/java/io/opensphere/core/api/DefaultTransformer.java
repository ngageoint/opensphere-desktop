package io.opensphere.core.api;

import java.awt.Font;
import java.util.Collection;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.messaging.DefaultGenericPublisher;
import io.opensphere.core.messaging.GenericSubscriber;

/**
 * Partial implementation of {@link Transformer} that provides a geometry
 * publisher and convenience methods.
 */
public class DefaultTransformer implements Transformer
{
    /** The Constant ourFont. */
    public static final Font ourFont;

    static
    {
        if (System.getProperty("os.name").contains("Windows"))
        {
            ourFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
        else
        {
            ourFont = new Font(Font.SERIF, Font.PLAIN, 12);
        }
    }

    /** The data registry. */
    private final DataRegistry myDataRegistry;

    /** The geometry sender. */
    private final DefaultGenericPublisher<Geometry> myGeometrySender = new DefaultGenericPublisher<>();

    /** Flag indicating if the transformer is currently open. */
    private volatile boolean myOpen;

    /**
     * Constructor.
     *
     * @param dataRegistry The data registry (may be <code>null</code> if
     *            unneeded).
     */
    public DefaultTransformer(DataRegistry dataRegistry)
    {
        myDataRegistry = dataRegistry;
    }

    @Override
    public void addSubscriber(GenericSubscriber<Geometry> receiver)
    {
        myGeometrySender.addSubscriber(receiver);
    }

    @Override
    public void close()
    {
        myOpen = false;
    }

    /**
     * Get the data registry.
     *
     * @return The data registry.
     */
    public DataRegistry getDataRegistry()
    {
        return myDataRegistry;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    /**
     * Get if this transformer is open.
     *
     * @return Indicates if the transformer is open.
     */
    public boolean isOpen()
    {
        return myOpen;
    }

    @Override
    public void open()
    {
        myOpen = true;
    }

    @Override
    public final void publishGeometries(Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
    {
        if (!isOpen())
        {
            throw new IllegalStateException("Geometries cannot be published before the " + getClass().getSimpleName()
                    + " transformer is opened or after it has been closed.");
        }
        myGeometrySender.sendObjects(this, adds, removes);
    }

    @Override
    public void removeSubscriber(GenericSubscriber<Geometry> receiver)
    {
        myGeometrySender.removeSubscriber(receiver);
    }
}
