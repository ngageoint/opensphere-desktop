package io.opensphere.mantle.data.geom.impl;

import java.util.function.Function;

import io.opensphere.core.image.processor.ImageProcessor;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.geom.MapIconGeometrySupport;

/**
 * A Default Map Icon Geometry Support.
 */
public class DefaultMapIconGeometrySupport extends AbstractLocationGeometrySupport implements MapIconGeometrySupport
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Icon size. */
    private float myIconSize = DEFAULT_ICON_SIZE;

    /** The Icon highlight size. */
    private float myIconHighlightSize = DEFAULT_ICON_SIZE;

    /** The Icon url. */
    private String myIconURL;

    /** The optional ImageProcessor. */
    private transient ImageProcessor myImageProcessor;

    /** Optional scaling function. */
    private transient Function<Kilometers, Float> myScaleFunction;

    /**
     * CTOR.
     */
    public DefaultMapIconGeometrySupport()
    {
        super();
    }

    /**
     * CTOR with {@link LatLonAlt} and an icon url.
     *
     * @param loc - the location
     * @param iconURL the icon url
     * @param highlightSize the icon highlight size
     * @param size the size
     */
    public DefaultMapIconGeometrySupport(LatLonAlt loc, String iconURL, float size, float highlightSize)
    {
        super(loc);
        myIconURL = iconURL;
        myIconSize = size;
        myIconHighlightSize = highlightSize;
    }

    @Override
    public float getIconSize()
    {
        return myIconSize;
    }

    @Override
    public float getIconHighlightSize()
    {
        return myIconHighlightSize;
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
        return hasChildren() ? MapVisualizationType.COMPOUND_FEATURE_ELEMENTS : MapVisualizationType.ICON_ELEMENTS;
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
        DefaultMapIconGeometrySupport other = (DefaultMapIconGeometrySupport)obj;
        return EqualsHelper.equals(myIconURL, other.myIconURL);
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
     * Sets the icon highlight size.
     *
     * @param size the new icon size
     */
    public void setIconHighlightSize(float size)
    {
        myIconHighlightSize = size;
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
}
