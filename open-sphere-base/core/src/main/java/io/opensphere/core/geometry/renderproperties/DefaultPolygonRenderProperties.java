package io.opensphere.core.geometry.renderproperties;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import edu.umd.cs.findbugs.annotations.Nullable;

import io.opensphere.core.util.Utilities;

/** Standard implementation of {@link PolygonRenderProperties}. */
public class DefaultPolygonRenderProperties extends DefaultPolylineRenderProperties implements PolygonRenderProperties
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The fill color render properties. */
    @Nullable
    private final ColorRenderProperties myFillColorRenderProperties;

    /**
     * Constructor for use with empty polygons.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     */
    public DefaultPolygonRenderProperties(int zOrder, boolean drawable, boolean pickable)
    {
        super(zOrder, drawable, pickable);
        myFillColorRenderProperties = null;
    }

    /**
     * Constructor for use with filled polygons.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     * @param fillColorRenderProperties The color render properties for the
     *            fill.
     */
    public DefaultPolygonRenderProperties(int zOrder, boolean drawable, boolean pickable,
            ColorRenderProperties fillColorRenderProperties)
    {
        super(zOrder, drawable, pickable);
        myFillColorRenderProperties = fillColorRenderProperties;
    }

    @Override
    public DefaultPolygonRenderProperties clone()
    {
        return (DefaultPolygonRenderProperties)super.clone();
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
        DefaultPolygonRenderProperties other = (DefaultPolygonRenderProperties)obj;
        return Utilities.sameInstance(myFillColorRenderProperties, other.myFillColorRenderProperties);
    }

    @Override
    public ColorRenderProperties getFillColorRenderProperties()
    {
        return myFillColorRenderProperties;
    }

    @Override
    public Collection<? extends AbstractRenderProperties> getThisPlusDescendants()
    {
        if (myFillColorRenderProperties == null)
        {
            return Collections.singleton(this);
        }
        else
        {
            return Arrays.asList((AbstractRenderProperties)this, (AbstractRenderProperties)myFillColorRenderProperties);
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myFillColorRenderProperties == null ? 0 : myFillColorRenderProperties.hashCode());
        return result;
    }
}
