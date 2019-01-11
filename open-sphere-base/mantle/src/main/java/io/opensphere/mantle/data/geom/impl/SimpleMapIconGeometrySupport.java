package io.opensphere.mantle.data.geom.impl;

import java.util.Objects;
import java.util.function.Function;

import io.opensphere.core.image.processor.ImageProcessor;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.units.length.Kilometers;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapIconGeometrySupport;

/**
 * The Class SimpleMapIconGeometrySupport.
 *
 * Does not support children, callouts, or tooltips.
 */
public class SimpleMapIconGeometrySupport extends AbstractSimpleLocationGeometrySupport implements MapIconGeometrySupport
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Icon size. */
    private float myIconSize = DEFAULT_ICON_SIZE;

    /** The Icon url. */
    private String myIconURL;

    /** The optional ImageProcessor. */
    private transient ImageProcessor myImageProcessor;

    /** Optional scaling function. */
    private transient Function<Kilometers, Float> myScaleFunction;

    /** Default constructor. */
    public SimpleMapIconGeometrySupport()
    {
        super();
    }

    /**
     * Copy constructor.
     *
     * @param source the object from which to copy data.
     */
    public SimpleMapIconGeometrySupport(SimpleMapIconGeometrySupport source)
    {
        super(source);

        myIconSize = source.myIconSize;
        myIconURL = source.myIconURL;
        myImageProcessor = source.myImageProcessor;
        myScaleFunction = source.myScaleFunction;
    }

    /**
     * Constructor with {@link LatLonAlt}.
     *
     * @param loc - the location
     * @param iconURL the icon url
     * @param iconSize the icon size
     */
    public SimpleMapIconGeometrySupport(LatLonAlt loc, String iconURL, float iconSize)
    {
        super(loc);
        myIconSize = iconSize;
        myIconURL = iconURL;
    }

    @Override
    public float getIconSize()
    {
        return myIconSize;
    }

    @Override
    public String getIconURL()
    {
        return myIconURL;
    }

    @Override
    public ImageProcessor getImageProcessor()
    {
        return myImageProcessor;
    }

    @Override
    public Function<Kilometers, Float> getScaleFunction()
    {
        return myScaleFunction;
    }

    @Override
    public MapVisualizationType getVisualizationType()
    {
        return MapVisualizationType.ICON_ELEMENTS;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        SimpleMapIconGeometrySupport other = (SimpleMapIconGeometrySupport)obj;
        return Objects.equals(myIconURL, other.myIconURL);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myIconURL == null ? 0 : myIconURL.hashCode());
        return result;
    }

    /**
     * Sets the icon size.
     *
     * @param size the new icon size
     */
    public void setIconSize(float size)
    {
        myIconSize = size;
    }

    /**
     * Sets the icon url.
     *
     * @param iconURL the new icon url
     */
    public void setIconURL(String iconURL)
    {
        myIconURL = iconURL;
    }

    /**
     * Setter for imageProcessor.
     *
     * @param imageProcessor the imageProcessor
     */
    public void setImageProcessor(ImageProcessor imageProcessor)
    {
        myImageProcessor = imageProcessor;
    }

    /**
     * Sets the scale function.
     *
     * @param scaleFunction the scale function
     */
    public void setScaleFunction(Function<Kilometers, Float> scaleFunction)
    {
        myScaleFunction = scaleFunction;
    }

    @Override
    public float getIconHighlightSize()
    {
        return myIconSize;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.MapGeometrySupport#createCopy()
     */
    @Override
    public SimpleMapIconGeometrySupport createCopy()
    {
        return new SimpleMapIconGeometrySupport(this);
    }
}
