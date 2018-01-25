package io.opensphere.core.pipeline.util;

import java.awt.Point;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.media.opengl.GL;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.util.BufferUtilities;
import io.opensphere.core.util.Utilities;

/**
 * Keeps track of pick colors and the geometries using them. This is designed to
 * be used in a single thread.
 */
public abstract class PickManager
{
    /** Bit position of alpha in an RGBA integer. */
    protected static final int ALPHA_SHIFT = 24;

    /** Bit position of blue in an RGBA integer. */
    protected static final int BLUE_SHIFT = 0;

    /** Bit position of green in an RGBA integer. */
    protected static final int GREEN_SHIFT = 8;

    /** Bit position of red in an RGBA integer. */
    protected static final int RED_SHIFT = 16;

    /** The map of colors to objects. */
    private final TIntObjectMap<Geometry> myColorToGeometryMap = new TIntObjectHashMap<>();

    /** The control registry. */
    private final ControlRegistry myControlRegistry;

    /** The map of objects to RGBA colors. */
    private final TObjectIntMap<Geometry> myGeometryToColorMap = new TObjectIntHashMap<>();

    /** The last geometry for which the pick determination changed. */
    private Geometry myLastPickedGeometry;

    /** The next color to be assigned. */
    private int myNextColor = 1;

    /** The set of currently picked geometries. */
    private final Set<Geometry> myPickedGeometries = new HashSet<>();

    /** Unmodifiable view of the picked geometries set. */
    private final Set<Geometry> myUnmodifiablePickedGeometries = Collections.unmodifiableSet(myPickedGeometries);

    /**
     * Constructor.
     *
     * @param controlRegistry The control registry.
     */
    public PickManager(ControlRegistry controlRegistry)
    {
        myControlRegistry = controlRegistry;
    }

    /**
     * Query GL for the current pick color and determine if there's a matching
     * geometry.
     *
     * @param gl The OpenGL interface.
     * @param x The x coordinate of the pick point (origin at left).
     * @param y The y coordinate of the pick point (origin at bottom).
     */
    public void determinePicks(GL gl, int x, int y)
    {
        boolean change;
        Geometry pickedGeom;
        synchronized (this)
        {
            myPickedGeometries.clear();

            if (myColorToGeometryMap.isEmpty())
            {
                pickedGeom = null;
                change = false;
            }
            else
            {
                java.nio.ByteBuffer pixel = BufferUtilities.newByteBuffer(3);
                gl.glReadPixels(x, y, 1, 1, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, pixel);
                int red = pixel.get() & 0xff;
                int green = pixel.get() & 0xff;
                int blue = pixel.get() & 0xff;
                int color = (red << RED_SHIFT) + (green << GREEN_SHIFT) + (blue << BLUE_SHIFT);

                pickedGeom = myColorToGeometryMap.get(color);

                if (pickedGeom != null)
                {
                    myPickedGeometries.add(pickedGeom);
                }

                if (Utilities.sameInstance(myLastPickedGeometry, pickedGeom))
                {
                    change = false;
                }
                else
                {
                    myLastPickedGeometry = pickedGeom;
                    change = true;
                }
            }
        }

        if (change)
        {
            ControlContext cctx = myControlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT);
            cctx.notifyPicked(pickedGeom, new Point(x, y));
        }
    }

    /**
     * Get the pick color for a geometry. If no pick color has been assigned,
     * assign one and return it. NOTE: If the geometry is not in a processor,
     * then {@link PickManager#removeGeometries(Collection)} must be called to
     * clean out pick entries for the geometry.
     *
     * @param key The geometry of interest.
     * @return The pick color as an RGBA integer.
     */
    public synchronized int getPickColor(Geometry key)
    {
        int color = myGeometryToColorMap.get(key);
        if (color == myGeometryToColorMap.getNoEntryValue())
        {
            color = getNextColor();
            myGeometryToColorMap.put(key, color);
            myColorToGeometryMap.put(color, key);
        }
        return color;
    }

    /**
     * Get the pick color for a geometry. If no pick color has been assigned,
     * assign one. Add the bytes to the buffer in RGBA order. NOTE: If the
     * geometry is not in a processor, then
     * {@link PickManager#removeGeometries(Collection)} must be called to clean
     * out pick entries for the geometry.
     *
     * @param key The geometry of interest.
     * @param buf The output byte buffer.
     */
    public void getPickColor(Geometry key, ByteBuffer buf)
    {
        int pickColor = getPickColor(key);
        buf.put((byte)(pickColor >> RED_SHIFT));
        buf.put((byte)(pickColor >> GREEN_SHIFT));
        buf.put((byte)(pickColor >> BLUE_SHIFT));
        buf.put((byte)-1);
    }

    /**
     * Get the currently picked geometries.
     *
     * @return The currently picked geometries.
     */
    public Set<Geometry> getPickedGeometries()
    {
        return myUnmodifiablePickedGeometries;
    }

    /**
     * Set the GL state to use the pick color for the specified geometry. NOTE:
     * If the geometry is not in a processor, then
     * {@link PickManager#removeGeometries(Collection)} must be called to clean
     * out pick entries for the geometry.
     *
     * @param gl The OpenGL interface.
     * @param key The geometry being drawn.
     * @return The pick color.
     */
    public abstract int glColor(GL gl, Geometry key);

    /**
     * Set the GL texture environment color to use the pick color for the
     * specified geometry. NOTE: If the geometry is not in a processor, then
     * {@link PickManager#removeGeometries(Collection)} must be called to clean
     * out pick entries for the geometry.
     *
     * @param gl The OpenGL interface.
     * @param key The geometry being drawn.
     */
    public abstract void glTexEnvColor(GL gl, Geometry key);

    /**
     * Remove pick entries for the given geometries.
     *
     * @param removes The geometries for which the pick entries should be
     *            removed.
     */
    public synchronized void removeGeometries(Collection<? extends Geometry> removes)
    {
        for (Geometry geom : removes)
        {
            if (geom.getRenderProperties() instanceof BaseRenderProperties
                    && ((BaseRenderProperties)geom.getRenderProperties()).isPickable() && myGeometryToColorMap.containsKey(geom))
            {
                int color = myGeometryToColorMap.remove(geom);
                myColorToGeometryMap.remove(color);
            }
        }
    }

    /** Forget all pick colors and start over. */
    public synchronized void reset()
    {
        myGeometryToColorMap.clear();
        myColorToGeometryMap.clear();
        myNextColor = 1;
    }

    /**
     * Get the next available pick color.
     *
     * @return The next color as an RGBA integer.
     */
    private synchronized int getNextColor()
    {
        if (myNextColor >= 1 << ALPHA_SHIFT)
        {
            myNextColor = 1;
        }
        return myNextColor++;
    }
}
