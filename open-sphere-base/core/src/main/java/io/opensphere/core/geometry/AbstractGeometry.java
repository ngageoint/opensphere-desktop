package io.opensphere.core.geometry;

import java.util.List;
import java.util.Set;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * Top-level partial implementation of the {@link Geometry} interface. This
 * provides constraints, picking information, and Z ordering.
 */
public abstract class AbstractGeometry implements Geometry
{
    /** Set of geometries with debug enabled. */
    private static volatile Set<AbstractGeometry> ourDebugGeometries;

    /**
     * The id of the data model from which I was created. A value of -1
     * indicates that there is no backing data model.
     */
    private final long myDataModelId;

    /** Indicates if this geometry is likely to change often. */
    // TODO Move to ImageManager
    private final boolean myRapidUpdate;

    /** Properties which affect how the geometry is rendered. */
    private ZOrderRenderProperties myRenderProperties;

    /**
     * Turn debug off for this geometry.
     *
     * @param geom The geometry.
     */
    public static synchronized void turnDebugOff(AbstractGeometry geom)
    {
        if (ourDebugGeometries == null)
        {
            return;
        }
        ourDebugGeometries.remove(geom);
    }

    /**
     * Turn debug on for this geometry.
     *
     * @param geom The geometry.
     */
    public static synchronized void turnDebugOn(AbstractGeometry geom)
    {
        if (ourDebugGeometries == null)
        {
            ourDebugGeometries = New.set();
        }
        ourDebugGeometries.add(geom);
    }

    /**
     * Construct the geometry.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     */
    protected AbstractGeometry(AbstractGeometry.Builder builder, ZOrderRenderProperties renderProperties)
    {
        Utilities.checkNull(renderProperties, "renderProperties");
        myRenderProperties = renderProperties;
        myRapidUpdate = builder.isRapidUpdate();
        myDataModelId = builder.getDataModelId();
    }

    @Override
    public AbstractGeometry clone()
    {
        try
        {
            AbstractGeometry clone = (AbstractGeometry)super.clone();
            clone.myRenderProperties = myRenderProperties.clone();
            return clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    @Override
    public long getDataModelId()
    {
        return myDataModelId;
    }

    @Override
    public GeometryOrderKey getGeometryOrderKey()
    {
        return null;
    }

    @Override
    public ZOrderRenderProperties getRenderProperties()
    {
        return myRenderProperties;
    }

    /**
     * Determine if debug is on for this geometry.
     *
     * @return <code>true</code> if debug is on for this geometry
     */
    public final boolean isDebugOn()
    {
        return ourDebugGeometries != null && ourDebugGeometries.contains(this);
    }

    @Override
    public boolean isRapidUpdate()
    {
        return myRapidUpdate;
    }

    @Override
    public boolean jtsIntersectionTests(JTSIntersectionTests test, List<Polygon> polygons, GeometryFactory geomFactory)
    {
        return false;
    }

    /** Turn debug off for this geometry. */
    public final void turnDebugOff()
    {
        turnDebugOff(this);
    }

    /** Turn debug on for this geometry. */
    public final void turnDebugOn()
    {
        turnDebugOn(this);
    }

    /**
     * Create a raw builder. This is used by {@link #createBuilder()} to create
     * the correct type of builder.
     *
     * @return The builder instance.
     */
    protected abstract Builder createRawBuilder();

    /**
     * Create a builder populated with my properties. This is a protected helper
     * method for {@link Geometry#createBuilder()}. This class does not
     * implement {@link Geometry#createBuilder()} directly in an effort to
     * remind subclass developers to do so.
     *
     * @return The builder.
     */
    @OverridingMethodsMustInvokeSuper
    protected Builder doCreateBuilder()
    {
        Builder builder = createRawBuilder();
        builder.setDataModelId(getDataModelId());
        builder.setRapidUpdate(isRapidUpdate());
        return builder;
    }

    /**
     * Builder for the geometry.
     */
    public static class Builder implements Geometry.Builder
    {
        /**
         * The id of the data model from which I was created. A value of -1
         * indicates that there is no backing data model.
         */
        private long myDataModelId = -1;

        /** Indicates if this geometry is likely to change often. */
        private boolean myRapidUpdate;

        /**
         * Get the dataModelId.
         *
         * @return the dataModelId
         */
        public long getDataModelId()
        {
            return myDataModelId;
        }

        /**
         * Indicates if this geometry is likely to change frequently.
         *
         * @return The rapid update flag.
         */
        public boolean isRapidUpdate()
        {
            return myRapidUpdate;
        }

        /**
         * Set the dataModelId.
         *
         * @param dataModelId the dataModelId to set
         */
        public void setDataModelId(long dataModelId)
        {
            myDataModelId = dataModelId;
        }

        /**
         * Indicates if this geometry is likely to change frequently (several
         * times a second). This is used as a hint to the rendering engine that
         * it needs to handle this geometry specially.
         *
         * @param rapidUpdate The rapidUpdate flag.
         */
        public void setRapidUpdate(boolean rapidUpdate)
        {
            myRapidUpdate = rapidUpdate;
        }
    }

    /** Rendering mode. */
    public enum RenderMode
    {
        /** Normal drawing mode. */
        DRAW,

        /** Drawing using pick colors. */
        PICK,
    }
}
