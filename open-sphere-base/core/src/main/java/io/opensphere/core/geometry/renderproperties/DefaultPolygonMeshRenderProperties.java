package io.opensphere.core.geometry.renderproperties;

import java.util.Objects;

import javax.annotation.Nullable;

import io.opensphere.core.math.Matrix4d;

/** Standard implementation of {@link PolygonMeshRenderProperties}. */
public class DefaultPolygonMeshRenderProperties extends DefaultColorRenderProperties implements PolygonMeshRenderProperties
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The transform. */
    @Nullable
    private volatile Matrix4d myTransform;

    /**
     * Constructor.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     * @param obscurant When true, geometries will obscure other geometries
     *            based on distance from the viewer.
     */
    public DefaultPolygonMeshRenderProperties(int zOrder, boolean drawable, boolean pickable, boolean obscurant)
    {
        super(zOrder, drawable, pickable, obscurant);
    }

    /**
     * Constructor that takes initial colors.
     *
     * @param zOrder The z-order of the associated geometry.
     * @param drawable When true the geometry can be drawn.
     * @param pickable When true the geometry should be pickable.
     * @param obscurant When true, geometries will obscure other geometries
     *            based on distance from the viewer.
     * @param color The color of the geometry, in ARGB bytes.
     * @param highlightColor The highlight color of the geometry, in ARGB bytes.
     */
    public DefaultPolygonMeshRenderProperties(int zOrder, boolean drawable, boolean pickable, boolean obscurant, int color,
            int highlightColor)
    {
        super(zOrder, drawable, pickable, obscurant, color, highlightColor);
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
        return Objects.equals(myTransform, ((DefaultPolygonMeshRenderProperties)obj).myTransform);
    }

    @Override
    @Nullable
    public Matrix4d getTransform()
    {
        return myTransform;
    }

    @Override
    public int hashCode()
    {
        return 31 * super.hashCode() + (myTransform == null ? 0 : myTransform.hashCode());
    }

    @Override
    public void setTransform(@Nullable Matrix4d transform)
    {
        myTransform = transform;
        notifyChanged();
    }
}
