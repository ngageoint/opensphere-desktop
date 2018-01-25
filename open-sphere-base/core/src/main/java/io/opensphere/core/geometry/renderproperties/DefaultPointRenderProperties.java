package io.opensphere.core.geometry.renderproperties;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/** Standard implementation of {@link PointRenderProperties}. */
@SuppressWarnings("PMD.GodClass")
public class DefaultPointRenderProperties implements PointRenderProperties
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The base render properties. */
    private BaseAltitudeRenderProperties myBaseRenderProperties;

    /** If the point is round or square. */
    private final PointRoundnessRenderProperty myRoundnessProperty;

    /** Size to render the geometry. */
    private PointSizeRenderProperty mySizeProperty;

    /**
     * Constructor that takes a {@link BaseAltitudeRenderProperties} and a
     * {@link DefaultPointSizeRenderProperty}. This should be used if this
     * properties object can share a {@link BaseAltitudeRenderProperties} or a
     * {@link DefaultPointSizeRenderProperty} with other objects. For example,
     * if a number of points need to have different colors, they need to have
     * different {@link PointRenderProperties} objects, but if they're all the
     * same size, share the same lighting model, and can all be shown/hidden
     * together, they can share a {@link BaseAltitudeRenderProperties} and a
     * {@link PointSizeRenderProperty}.
     * <p>
     * This constructor will use the default immutable roundness property
     * {@link ImmutablePointRoundnessRenderProperty#ROUND}.
     *
     * @param baseRenderProperties The shared base render properties.
     * @param sizeProperty The shared size property.
     */
    public DefaultPointRenderProperties(BaseAltitudeRenderProperties baseRenderProperties, PointSizeRenderProperty sizeProperty)
    {
        myBaseRenderProperties = baseRenderProperties;
        mySizeProperty = sizeProperty;
        myRoundnessProperty = ImmutablePointRoundnessRenderProperty.ROUND;
    }

    /**
     * Constructor that takes a {@link BaseAltitudeRenderProperties}, a
     * {@link PointSizeRenderProperty}, and a
     * {@link PointRoundnessRenderProperty}. This should be used if this
     * properties object needs to have a mutable
     * {@link PointRoundnessRenderProperty} and can share a
     * {@link BaseAltitudeRenderProperties} or a {@link PointSizeRenderProperty}
     * with other objects.
     *
     * @param baseRenderProperties The shared base render properties.
     * @param sizeProperty The shared size property.
     * @param roundnessProperty The shared roundness property.
     */
    public DefaultPointRenderProperties(BaseAltitudeRenderProperties baseRenderProperties, PointSizeRenderProperty sizeProperty,
            PointRoundnessRenderProperty roundnessProperty)
    {
        myBaseRenderProperties = baseRenderProperties;
        mySizeProperty = sizeProperty;
        myRoundnessProperty = Utilities.checkNull(roundnessProperty, "roundnessProperty");
    }

    /**
     * Default constructor that creates unique
     * {@link BaseAltitudeRenderProperties} and
     * {@link DefaultPointSizeRenderProperty} objects dedicated to this object.
     * <p>
     * <b>WARNING: This constructor should only be used if these properties
     * cannot share base render properties or a size property with any other
     * properties. The number of render properties in the system must be
     * minimized to maximize graphics performance.</b>
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     * @param obscurant When true, geometries will obscure other geometries
     *            based on distance from the viewer.
     *
     * @see #DefaultPointRenderProperties(BaseAltitudeRenderProperties,
     *      PointSizeRenderProperty)
     */
    public DefaultPointRenderProperties(int zOrder, boolean drawable, boolean pickable, boolean obscurant)
    {
        this(new DefaultBaseAltitudeRenderProperties(zOrder, drawable, pickable, obscurant),
                new DefaultPointSizeRenderProperty());
    }

    /**
     * Default constructor that creates unique
     * {@link BaseAltitudeRenderProperties} and
     * {@link DefaultPointSizeRenderProperty} objects dedicated to this object.
     * <p>
     * <b>WARNING: This constructor should only be used if these properties
     * cannot share base render properties or a size property with any other
     * properties. The number of render properties in the system must be
     * minimized to maximize graphics performance.</b>
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     * @param obscurant When true, geometries will obscure other geometries
     *            based on distance from the viewer.
     * @param roundness The roundness property.
     *
     * @see #DefaultPointRenderProperties(BaseAltitudeRenderProperties,
     *      PointSizeRenderProperty)
     */
    public DefaultPointRenderProperties(int zOrder, boolean drawable, boolean pickable, boolean obscurant,
            PointRoundnessRenderProperty roundness)
    {
        this(new DefaultBaseAltitudeRenderProperties(zOrder, drawable, pickable, obscurant), new DefaultPointSizeRenderProperty(),
                roundness);
    }

    @Override
    public void addListener(RenderPropertyChangeListener listen)
    {
        // I don't have any properties of my own so I don't need to notify
        // listeners.
    }

    @Override
    public DefaultPointRenderProperties clone()
    {
        try
        {
            DefaultPointRenderProperties props;
            props = (DefaultPointRenderProperties)super.clone();
            props.setSizeProperty(getSizeProperty().clone());
            props.setBaseRenderProperties(myBaseRenderProperties.clone());
            return props;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultPointRenderProperties other = (DefaultPointRenderProperties)obj;
        return EqualsHelper.equals(myBaseRenderProperties, other.myBaseRenderProperties)
                && EqualsHelper.equals(mySizeProperty, other.mySizeProperty)
                && EqualsHelper.equals(myRoundnessProperty, other.myRoundnessProperty);
    }

    @Override
    public float getBaseAltitude()
    {
        return myBaseRenderProperties.getBaseAltitude();
    }

    @Override
    public BaseAltitudeRenderProperties getBaseRenderProperties()
    {
        return myBaseRenderProperties;
    }

    @Override
    public BlendingConfigGL getBlending()
    {
        return myBaseRenderProperties.getBlending();
    }

    @Override
    public Color getColor()
    {
        return myBaseRenderProperties.getColor();
    }

    @Override
    public int getColorARGB()
    {
        return myBaseRenderProperties.getColorARGB();
    }

    @Override
    public Color getHighlightColor()
    {
        return myBaseRenderProperties.getHighlightColor();
    }

    @Override
    public int getHighlightColorARGB()
    {
        return myBaseRenderProperties.getHighlightColorARGB();
    }

    @Override
    public float getHighlightSize()
    {
        return mySizeProperty.getHighlightSize();
    }

    @Override
    public LightingModelConfigGL getLighting()
    {
        return myBaseRenderProperties.getLighting();
    }

    @Override
    public int getRenderingOrder()
    {
        return myBaseRenderProperties.getRenderingOrder();
    }

    @Override
    public PointRoundnessRenderProperty getRoundnessRenderProperty()
    {
        return myRoundnessProperty;
    }

    @Override
    public float getSize()
    {
        return mySizeProperty.getSize();
    }

    @Override
    public PointSizeRenderProperty getSizeProperty()
    {
        return mySizeProperty;
    }

    @Override
    public Collection<? extends RenderProperties> getThisPlusDescendants()
    {
        return Arrays.asList(this, myBaseRenderProperties, mySizeProperty, myRoundnessProperty);
    }

    @Override
    public int getZOrder()
    {
        return myBaseRenderProperties.getZOrder();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myBaseRenderProperties == null ? 0 : myBaseRenderProperties.hashCode());
        result = prime * result + (myRoundnessProperty == null ? 0 : myRoundnessProperty.hashCode());
        result = prime * result + (mySizeProperty == null ? 0 : mySizeProperty.hashCode());
        return result;
    }

    @Override
    public boolean isDrawable()
    {
        return myBaseRenderProperties.isDrawable();
    }

    @Override
    public boolean isHidden()
    {
        return myBaseRenderProperties.isHidden();
    }

    @Override
    public boolean isObscurant()
    {
        return myBaseRenderProperties.isObscurant();
    }

    @Override
    public boolean isPickable()
    {
        return myBaseRenderProperties.isPickable();
    }

    @Override
    public boolean isRound()
    {
        return getRoundnessRenderProperty().isRound();
    }

    @Override
    public void opacitizeColor(float opacity)
    {
        myBaseRenderProperties.opacitizeColor(opacity);
    }

    @Override
    public void removeListener(RenderPropertyChangeListener listen)
    {
        // I don't have any properties of my own so I don't need to notify
        // listeners.
    }

    @Override
    public void setBaseAltitude(float baseAlt)
    {
        myBaseRenderProperties.setBaseAltitude(baseAlt);
    }

    /**
     * Use a different base render property object. Typically, this should be
     * set during construction and the point properties should share a single
     * base render properties object, but setting this may be required when the
     * point render properties no longer wish to share their base render
     * properties.
     *
     * @param baseRenderProperties The new base render properties.
     */
    public void setBaseRenderProperties(BaseAltitudeRenderProperties baseRenderProperties)
    {
        myBaseRenderProperties = baseRenderProperties;
    }

    @Override
    public void setBlending(BlendingConfigGL blend)
    {
        myBaseRenderProperties.setBlending(blend);
    }

    @Override
    public void setColor(Color color)
    {
        myBaseRenderProperties.setColor(color);
    }

    @Override
    public void setColorARGB(int color)
    {
        myBaseRenderProperties.setColorARGB(color);
    }

    @Override
    public void setHidden(boolean hidden)
    {
        myBaseRenderProperties.setHidden(hidden);
    }

    @Override
    public void setHighlightColor(Color color)
    {
        myBaseRenderProperties.setHighlightColor(color);
    }

    @Override
    public void setHighlightColorARGB(int color)
    {
        myBaseRenderProperties.setHighlightColorARGB(color);
    }

    @Override
    public void setHighlightSize(float size)
    {
        mySizeProperty.setHighlightSize(size);
    }

    @Override
    public void setLighting(LightingModelConfigGL lighting)
    {
        myBaseRenderProperties.setLighting(lighting);
    }

    @Override
    public void setObscurant(boolean obscurant)
    {
        myBaseRenderProperties.setObscurant(obscurant);
    }

    @Override
    public void setRenderingOrder(int order)
    {
        myBaseRenderProperties.setRenderingOrder(order);
    }

    @Override
    public void setRound(boolean round)
    {
        getRoundnessRenderProperty().setRound(round);
    }

    @Override
    public void setSize(float size)
    {
        mySizeProperty.setSize(size);
    }

    /**
     * Use a different point size property object. Typically, this should be set
     * during construction and the point properties should share a single size
     * properties object, but setting this may be required when the point render
     * properties no longer wish to share their size properties.
     *
     * @param sizeProperty The new size properties.
     */
    public void setSizeProperty(PointSizeRenderProperty sizeProperty)
    {
        mySizeProperty = sizeProperty;
    }
}
