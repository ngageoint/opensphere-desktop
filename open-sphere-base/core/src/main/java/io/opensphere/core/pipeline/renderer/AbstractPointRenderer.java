package io.opensphere.core.pipeline.renderer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRoundnessRenderProperty;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * Abstract base class containing behavior common to all point renderers.
 *
 * @param <T> The geometry type.
 */
public abstract class AbstractPointRenderer<T extends AbstractRenderableGeometry> extends AbstractRenderer<T>
{
    /**
     * Construct the renderer.
     *
     * @param cache The geometry cache.
     */
    public AbstractPointRenderer(CacheProvider cache)
    {
        super(cache);
    }

    /**
     * Group points based on their render properties.
     *
     * @param input Geometries to be grouped.
     * @return Grouped geometries.
     * @throws ClassCastException If a geometry's render properties cannot be
     *             cast to {@link PointRenderProperties}.
     */
    protected Map<RenderKey, List<T>> groupPoints(Collection<? extends T> input) throws ClassCastException
    {
        Map<RenderKey, List<T>> sorted = new TreeMap<>();

        for (T geom : input)
        {
            PointRenderProperties props = (PointRenderProperties)geom.getRenderProperties();
            RenderKey key = new RenderKey(props.getRenderingOrder(), props.getSizeProperty(), props.getRoundnessRenderProperty(),
                    props.isObscurant());
            CollectionUtilities.multiMapAdd(sorted, key, geom, false);
        }

        return sorted;
    }

    /** Render key for point groups. */
    protected static class RenderKey implements Comparable<RenderKey>
    {
        /**
         * When true, the geometries may obscure other geometries based on depth
         * from the viewer, otherwise depth is ignored.
         */
        private final boolean myObscurant;

        /** The render order is used to determine the key's natural ordering. */
        private final int myRenderOrder;

        /** The roundness property. */
        private final PointRoundnessRenderProperty myRoundnessProperty;

        /** The size property. */
        private final PointSizeRenderProperty mySizeProperty;

        /**
         * Constructor.
         *
         * @param renderOrder The rendering order for geometries associated with
         *            this key.
         * @param sizeProperty The size property.
         * @param roundnessProperty The roundness property.
         * @param obscurant When true, the geometries may obscure other
         *            geometries based on depth from the viewer, otherwise depth
         *            is ignored.
         */
        public RenderKey(int renderOrder, PointSizeRenderProperty sizeProperty, PointRoundnessRenderProperty roundnessProperty,
                boolean obscurant)
        {
            myRenderOrder = renderOrder;
            mySizeProperty = sizeProperty;
            myRoundnessProperty = roundnessProperty;
            myObscurant = obscurant;
        }

        @Override
        public int compareTo(RenderKey o)
        {
            if (myRenderOrder != o.myRenderOrder)
            {
                return myRenderOrder < o.myRenderOrder ? -1 : 1;
            }

            if (!mySizeProperty.equals(o.mySizeProperty))
            {
                return mySizeProperty.compareTo(o.mySizeProperty);
            }

            if (!myRoundnessProperty.equals(o.myRoundnessProperty))
            {
                return myRoundnessProperty.compareTo(o.myRoundnessProperty);
            }

            return myObscurant == o.myObscurant ? 0 : myObscurant ? 1 : -1;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            RenderKey other = (RenderKey)obj;
            if (myRenderOrder != other.myRenderOrder || myObscurant != other.myObscurant)
            {
                return false;
            }
            return EqualsHelper.equals(myRoundnessProperty, other.myRoundnessProperty, mySizeProperty, other.mySizeProperty);
        }

        /**
         * Accessor for the roundnessProperty.
         *
         * @return The roundnessProperty.
         */
        public PointRoundnessRenderProperty getRoundnessProperty()
        {
            return myRoundnessProperty;
        }

        /**
         * Accessor for the sizeProperty.
         *
         * @return The sizeProperty.
         */
        public PointSizeRenderProperty getSizeProperty()
        {
            return mySizeProperty;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (myObscurant ? 1231 : 1237);
            result = prime * result + myRenderOrder;
            result = prime * result + (myRoundnessProperty == null ? 0 : myRoundnessProperty.hashCode());
            result = prime * result + (mySizeProperty == null ? 0 : mySizeProperty.hashCode());
            return result;
        }

        /**
         * Get whether the geometries are obscurant.
         *
         * @return true when the geometries are obscurant
         */
        public boolean isObscurant()
        {
            return myObscurant;
        }
    }
}
