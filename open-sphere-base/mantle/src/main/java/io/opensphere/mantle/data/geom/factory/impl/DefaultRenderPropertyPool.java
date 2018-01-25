package io.opensphere.mantle.data.geom.factory.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.geometry.AbstractGeometryGroup;
import io.opensphere.core.geometry.AbstractGroupHeightGeometry;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.RenderProperties;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;

/**
 * The Class RenderPropertyPoolImpl.
 */
public class DefaultRenderPropertyPool implements RenderPropertyPool
{
    /** The Data type info. */
    private final DataTypeInfo myDataTypeInfo;

    /** The Render property pool. */
    private final Map<RenderProperties, RenderProperties> myRenderPropertyPool;

    /**
     * Creates the pool.
     *
     * @param dti the {@link DataTypeInfo}
     * @param geomCol the {@link AbstractRenderableGeometry} collection list.
     * @return the render property pool
     */
    @SafeVarargs
    public static RenderPropertyPool createPool(DataTypeInfo dti, Set<Geometry>... geomCol)
    {
        DefaultRenderPropertyPool pool = new DefaultRenderPropertyPool(dti);
        if (geomCol != null && geomCol.length > 0)
        {
            for (Collection<Geometry> col : geomCol)
            {
                pool.addAllFromGeometry(col);
            }
        }
        return pool;
    }

    /**
     * Instantiates a new render property pool impl.
     *
     * @param type the {@link DataTypeInfo} of the pool.
     */
    public DefaultRenderPropertyPool(DataTypeInfo type)
    {
        super();
        myDataTypeInfo = type;
        myRenderPropertyPool = new HashMap<>();
    }

    @Override
    public void addAllFromGeometry(Collection<Geometry> geomCollection)
    {
        if (geomCollection != null && !geomCollection.isEmpty())
        {
            for (Geometry g : geomCollection)
            {
                if (g instanceof AbstractRenderableGeometry)
                {
                    getPoolInstance(((AbstractRenderableGeometry)g).getRenderProperties());
                }
                else if (g instanceof AbstractGeometryGroup)
                {
                    AbstractGeometryGroup agg = (AbstractGeometryGroup)g;
                    if (agg instanceof AbstractGroupHeightGeometry)
                    {
                        getPoolInstance(((AbstractGroupHeightGeometry)agg).getRenderProperties());
                    }
                    if (agg.getGeometryRegistry() != null)
                    {
                        addAllFromGeometry(agg.getGeometryRegistry().getGeometries());
                    }
                }
            }
        }
    }

    @Override
    public void addFromGeometry(Geometry geom)
    {
        if (geom != null)
        {
            if (geom instanceof AbstractRenderableGeometry)
            {
                getPoolInstance(((AbstractRenderableGeometry)geom).getRenderProperties());
            }
            else if (geom instanceof AbstractGeometryGroup)
            {
                AbstractGeometryGroup agg = (AbstractGeometryGroup)geom;
                if (agg instanceof AbstractGroupHeightGeometry)
                {
                    getPoolInstance(((AbstractGroupHeightGeometry)geom).getRenderProperties());
                }
                if (agg.getGeometryRegistry() != null)
                {
                    addAllFromGeometry(((AbstractGeometryGroup)geom).getGeometryRegistry().getGeometries());
                }
            }
        }
    }

    @Override
    public void clearPool()
    {
        myRenderPropertyPool.clear();
    }

    @Override
    public DataTypeInfo getDataType()
    {
        return myDataTypeInfo;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RenderProperties> T getPoolInstance(T prop)
    {
        RenderProperties rp = myRenderPropertyPool.get(prop);
        if (rp == null)
        {
            rp = prop;
            myRenderPropertyPool.put(prop, prop);
        }
        return (T)rp;
    }

    @Override
    public void removePoolInstance(RenderProperties prop)
    {
        myRenderPropertyPool.remove(prop);
    }

    @Override
    public int size()
    {
        return myRenderPropertyPool.size();
    }

    @Override
    public Set<RenderProperties> values()
    {
        return New.set(myRenderPropertyPool.keySet());
    }
}
