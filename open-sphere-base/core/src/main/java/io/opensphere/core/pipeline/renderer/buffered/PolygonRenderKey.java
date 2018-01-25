package io.opensphere.core.pipeline.renderer.buffered;

import java.util.Objects;

import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.util.lang.EqualsHelper;

/** Render key for polygon groups. */
class PolygonRenderKey implements Comparable<PolygonRenderKey>
{
    /** The lighting model for the geometries. */
    private final LightingModelConfigGL myLighting;

    /** The line smoothing. */
    private final boolean myLineSmoothing;

    /**
     * When true, the geometries may obscure other geometries based on depth
     * from the viewer, otherwise depth is ignored.
     */
    private final boolean myObscurant;

    /** The render order is used to determine the key's natural ordering. */
    private final int myRenderOrder;

    /** The stipple of the geometries. */
    private final StippleModelConfig myStipple;

    /** The number of vertices per tessera. */
    private final int myTesseraVertexCount;

    /** The width of the geometries. */
    private final float myWidth;

    /**
     * Constructor.
     *
     * @param renderOrder The rendering order for geometries associated with
     *            this key.
     * @param width The width of the geometries.
     * @param lighting The lighting model config.
     * @param stipple The line stipple.
     * @param obscurant When true, the geometries may obscure other geometries
     *            based on depth from the viewer, otherwise depth is ignored.
     * @param lineSmoothing The line smoothing.
     * @param tesseraVertexCount The tessera vertex count.
     */
    public PolygonRenderKey(int renderOrder, float width, LightingModelConfigGL lighting, StippleModelConfig stipple,
            boolean obscurant, boolean lineSmoothing, int tesseraVertexCount)
    {
        myRenderOrder = renderOrder;
        myWidth = width;
        myLighting = lighting;
        myStipple = stipple;
        myObscurant = obscurant;
        myLineSmoothing = lineSmoothing;
        myTesseraVertexCount = tesseraVertexCount;
    }

    /**
     * Compare to.
     *
     * @param o The o.
     * @return the int
     */
    @Override
    public int compareTo(PolygonRenderKey o)
    {
        if (myRenderOrder != o.myRenderOrder)
        {
            return myRenderOrder < o.myRenderOrder ? -1 : 1;
        }
        else if (!Objects.equals(myLighting, o.myLighting))
        {
            return Integer.compare(System.identityHashCode(myLighting), System.identityHashCode(o.myLighting));
        }
        else if (myLineSmoothing != o.myLineSmoothing)
        {
            return myLineSmoothing ? 1 : -1;
        }
        else if (!Objects.equals(myStipple, o.myStipple))
        {
            return Integer.compare(System.identityHashCode(myStipple), System.identityHashCode(o.myStipple));
        }
        else if (myWidth != o.myWidth)
        {
            return Float.compare(myWidth, o.myWidth);
        }
        else if (myTesseraVertexCount != o.myTesseraVertexCount)
        {
            return Integer.compare(myTesseraVertexCount, o.myTesseraVertexCount);
        }
        else
        {
            return myObscurant == o.myObscurant ? 0 : myObscurant ? 1 : -1;
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
        PolygonRenderKey other = (PolygonRenderKey)obj;
        return myLineSmoothing == other.myLineSmoothing && myObscurant == other.myObscurant
                && myRenderOrder == other.myRenderOrder && myWidth == other.myWidth
                && myTesseraVertexCount == other.myTesseraVertexCount
                && EqualsHelper.equals(myLighting, other.myLighting, myStipple, other.myStipple);
    }

    /**
     * Get the lighting.
     *
     * @return The lighting.
     */
    public LightingModelConfigGL getLighting()
    {
        return myLighting;
    }

    /**
     * Get the render order.
     *
     * @return The render order.
     */
    public int getRenderOrder()
    {
        return myRenderOrder;
    }

    /**
     * Get the stipple.
     *
     * @return The stipple.
     */
    public StippleModelConfig getStipple()
    {
        return myStipple;
    }

    /**
     * Get the tessera vertex count.
     *
     * @return The tessera vertex count.
     */
    public int getTesseraVertexCount()
    {
        return myTesseraVertexCount;
    }

    /**
     * Get the width.
     *
     * @return The width.
     */
    public float getWidth()
    {
        return myWidth;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myLighting == null ? 0 : myLighting.hashCode());
        result = prime * result + (myLineSmoothing ? 1231 : 1237);
        result = prime * result + (myObscurant ? 1231 : 1237);
        result = prime * result + myRenderOrder;
        result = prime * result + (myStipple == null ? 0 : myStipple.hashCode());
        result = prime * result + Float.floatToIntBits(myWidth);
        result = prime * result + myTesseraVertexCount;
        return result;
    }

    /**
     * Checks if is line smoothing.
     *
     * @return true, if is line smoothing
     */
    public boolean isLineSmoothing()
    {
        return myLineSmoothing;
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
