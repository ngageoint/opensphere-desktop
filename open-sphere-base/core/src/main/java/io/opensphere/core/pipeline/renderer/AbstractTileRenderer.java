package io.opensphere.core.pipeline.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import com.jogamp.opengl.util.awt.TextRenderer;

import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.core.viewer.impl.PositionConverter;

/**
 * Abstract base class containing behavior common to all tile renderers.
 */
public abstract class AbstractTileRenderer extends AbstractRenderer<TileGeometry>
{
    /** Debug key for tessellation lines. */
    public static final String DEBUG_TESSELLATION_LINES = "TessellationLines";

    /** Debug key for tile borders. */
    public static final String DEBUG_TILE_BORDERS = "TileBorders";

    /** Debug key for tile ellipsoid axes. */
    public static final String DEBUG_TILE_ELLIPSOID_AXES = "TileEllipsoidAxes";

    /** Debug key for tile ellipsoids. */
    public static final String DEBUG_TILE_ELLIPSOIDS = "TileEllipsoids";

    /** Debug key for tile labels. */
    public static final String DEBUG_TILE_LABELS = "TileLabels";

    /**
     * Debug key for rendering a solid color for tiles based on the projection
     * with which they were produced.
     */
    public static final String DEBUG_TILE_PROJECTION_COLOR = "TileProjectionColor";

    /** Debug key for tile textures. */
    public static final String DEBUG_TILE_TEXTURES = "TileTextures";

    /** How long to wait for tile retrieval during rendering. */
    protected static final long DATA_RETRIEVAL_TIME_BUDGET_MILLIS = 0L;

    /** Text renderer for rendering the tile labels. */
    private TextRenderer myDebugTextRenderer;

    /**
     * Get a menu that allows toggling debug features.
     *
     * @param mapContext The map context.
     * @return The menu.
     */
    public static JMenu getDebugMenu(final MapContext<?> mapContext)
    {
        JMenu menu = new JMenu("Tile Rendering");

        ActionListener listener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                toggleFeature(e.getActionCommand());
                mapContext.getProjectionChangeSupport().notifyProjectionChangeListeners(
                        new ProjectionChangedEvent(mapContext.getProjection(), mapContext.getProjection().getSnapshot(), true),
                        null);
            }
        };
        JCheckBoxMenuItem menuItem;
        menuItem = new JCheckBoxMenuItem(DEBUG_TESSELLATION_LINES);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menuItem = new JCheckBoxMenuItem(DEBUG_TILE_BORDERS);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menuItem = new JCheckBoxMenuItem(DEBUG_TILE_PROJECTION_COLOR);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menuItem = new JCheckBoxMenuItem(DEBUG_TILE_ELLIPSOID_AXES);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menuItem = new JCheckBoxMenuItem(DEBUG_TILE_ELLIPSOIDS);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menuItem = new JCheckBoxMenuItem(DEBUG_TILE_LABELS);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menuItem = new JCheckBoxMenuItem(DEBUG_TILE_TEXTURES, true);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        return menu;
    }

    /**
     * Toggle the tessellation lines debug feature.
     */
    public static void toggleTessellationLines()
    {
        toggleFeature(DEBUG_TESSELLATION_LINES);
    }

    /**
     * Toggle the tile borders debug feature.
     */
    public static void toggleTileBorders()
    {
        toggleFeature(DEBUG_TILE_BORDERS);
    }

    /**
     * Toggle the tile labels debug feature.
     */
    public static void toggleTileLabels()
    {
        toggleFeature(DEBUG_TILE_LABELS);
        toggleFeature(DEBUG_TILE_ELLIPSOID_AXES);
        toggleFeature(DEBUG_TILE_ELLIPSOIDS);
    }

    /** Toggle the projection color feature. */
    public static void toggleTileProjectionColor()
    {
        toggleFeature(DEBUG_TILE_PROJECTION_COLOR);
    }

    /**
     * Toggle the tile textures debug feature.
     */
    public static void toggleTileTextures()
    {
        toggleFeature(DEBUG_TILE_TEXTURES);
    }

    /**
     * Construct the renderer.
     *
     * @param cache The geometry cache.
     */
    public AbstractTileRenderer(CacheProvider cache)
    {
        super(cache);
    }

    /**
     * Determine if tessellation lines should be drawn.
     *
     * @return <code>true</code> if tessellation lines should be drawn.
     */
    public boolean isTessellationLinesOn()
    {
        return isDebugFeatureOn(DEBUG_TESSELLATION_LINES);
    }

    /**
     * Determine if tile borders should be drawn.
     *
     * @return <code>true</code> if tile borders should be drawn.
     */
    public boolean isTileBordersOn()
    {
        return isDebugFeatureOn(DEBUG_TILE_BORDERS);
    }

    /**
     * Determine if tile ellipsoid axes should be drawn.
     *
     * @return <code>true</code> if tile ellipsoid axes should be drawn.
     */
    public boolean isTileEllipsoidAxesOn()
    {
        return isDebugFeatureOn(DEBUG_TILE_ELLIPSOID_AXES);
    }

    /**
     * Determine if tile ellipsoids should be drawn.
     *
     * @return <code>true</code> if tile ellipsoids should be drawn.
     */
    public boolean isTileEllipsoidsOn()
    {
        return isDebugFeatureOn(DEBUG_TILE_ELLIPSOIDS);
    }

    /**
     * Determine if tile labels should be drawn.
     *
     * @return <code>true</code> if tile labels should be drawn.
     */
    public boolean isTileLabelsOn()
    {
        return isDebugFeatureOn(DEBUG_TILE_LABELS);
    }

    /**
     * Determine if the projection color feature is active.
     *
     * @return <code>true</code> if tiles should be rendered in a solid color to
     *         indicate the projection from which they were produced.
     */
    public boolean isTileProjectionColorOn()
    {
        return isDebugFeatureOn(DEBUG_TILE_PROJECTION_COLOR);
    }

    /**
     * Determine if tile textures should be hidden.
     *
     * @return <code>true</code> if tile textures should not be drawn.
     */
    public boolean isTileTexturesOff()
    {
        return isDebugFeatureOn(DEBUG_TILE_TEXTURES);
    }

    /**
     * Draw an ellipsoid.
     *
     * @param rc The render context.
     * @param ell The ellipsoid.
     */
    protected void drawEllipsoid(RenderContext rc, Ellipsoid ell)
    {
        GL gl = rc.getGL();
        final int circleCount = 20;
        final int circleSections = 40;

        for (double localX = -1.; localX < 1.; localX += 2. / circleCount)
        {
            final float topRed = .9f;
            final float topGreen = .8f;
            final float topBlue = 0f;
            gl.getGL2().glColor3f(topRed, topGreen, topBlue);
            gl.getGL2().glBegin(GL.GL_LINE_STRIP);
            for (int section = 0; section <= circleSections / 2; ++section)
            {
                double theta = section * MathUtil.TWO_PI / circleSections;
                double radius = Math.sqrt(1 - localX * localX);
                Vector3d local = new Vector3d(localX, radius * Math.cos(theta), radius * Math.sin(theta));
                Vector3d model = ell.localToModel(local);
                gl.getGL2().glVertex3d((float)model.getX(), (float)model.getY(), (float)model.getZ());
            }
            gl.getGL2().glEnd();

            final float bottomRed = .1f;
            final float bottomGreen = 0f;
            final float bottomBlue = .9f;
            gl.getGL2().glColor3f(bottomRed, bottomGreen, bottomBlue);
            gl.getGL2().glBegin(GL.GL_LINE_STRIP);
            for (int section = circleSections / 2; section <= circleSections; ++section)
            {
                double theta = section * MathUtil.TWO_PI / circleSections;
                double radius = Math.sqrt(1 - localX * localX);
                Vector3d local = new Vector3d(localX, radius * Math.cos(theta), radius * Math.sin(theta));
                Vector3d model = ell.localToModel(local);
                gl.getGL2().glVertex3d((float)model.getX(), (float)model.getY(), (float)model.getZ());
            }
            gl.getGL2().glEnd();
        }
    }

    /**
     * Debugging method that will draw the axes of the bounding ellipsoids of
     * the tiles.
     * <p>
     * NOTE: calling this method in a display list may cause rendering problems
     * because it pushes/pops GL attributes.
     *
     * @param rc The render context.
     * @param onscreen The tiles to draw.
     * @param mapContext The map context.
     */
    protected void drawEllipsoidAxes(RenderContext rc, Collection<? extends TileGeometry> onscreen, MapContext<?> mapContext)
    {
        GL gl = rc.getGL();
        gl.getGL2().glPushAttrib(GL2.GL_CURRENT_BIT);
        try
        {
            final float lineWidth = 3f;
            gl.glLineWidth(lineWidth);
            for (TileGeometry tileGeometry : onscreen)
            {
                Class<? extends Position> positionType = tileGeometry.getPositionType();
                if (positionType.equals(GeographicPosition.class))
                {
                    Ellipsoid ell = mapContext.getProjection().getBoundingEllipsoid(
                            (GeographicBoundingBox)tileGeometry.getBounds(), rc.getCurrentModelCenter(), true);

                    drawEllipsoidAxis(rc, ell);
                }
            }
        }
        finally
        {
            gl.getGL2().glPopAttrib();
        }
    }

    /**
     * Draw the axes of an ellipsoid.
     *
     * @param rc The render context.
     * @param ell The ellipsoid.
     */
    protected void drawEllipsoidAxis(RenderContext rc, Ellipsoid ell)
    {
        GL gl = rc.getGL();
        gl.getGL2().glColor4f(1f, 0f, 0f, 1f);
        gl.getGL2().glBegin(GL.GL_LINES);
        gl.getGL2().glVertex3f((float)ell.getCenter().getX(), (float)ell.getCenter().getY(), (float)ell.getCenter().getZ());
        Vector3d xAxis = ell.getXAxis();
        gl.getGL2().glVertex3f((float)(ell.getCenter().getX() + xAxis.getX()), (float)(ell.getCenter().getY() + xAxis.getY()),
                (float)(ell.getCenter().getZ() + xAxis.getZ()));
        gl.getGL2().glEnd();
        gl.getGL2().glColor4f(0f, 1f, 0f, 1f);
        gl.getGL2().glBegin(GL.GL_LINES);
        gl.getGL2().glVertex3f((float)ell.getCenter().getX(), (float)ell.getCenter().getY(), (float)ell.getCenter().getZ());
        gl.getGL2().glVertex3f((float)(ell.getCenter().getX() + ell.getYAxis().getX()),
                (float)(ell.getCenter().getY() + ell.getYAxis().getY()), (float)(ell.getCenter().getZ() + ell.getYAxis().getZ()));
        gl.getGL2().glEnd();
        gl.getGL2().glColor4f(0f, 0f, 1f, 1f);
        gl.getGL2().glBegin(GL.GL_LINES);
        gl.getGL2().glVertex3f((float)ell.getCenter().getX(), (float)ell.getCenter().getY(), (float)ell.getCenter().getZ());
        gl.getGL2().glVertex3f((float)(ell.getCenter().getX() + ell.getZAxis().getX()),
                (float)(ell.getCenter().getY() + ell.getZAxis().getY()), (float)(ell.getCenter().getZ() + ell.getZAxis().getZ()));
        gl.getGL2().glEnd();
    }

    /**
     * Debugging method that will draw the bounding ellipsoids of the tiles.
     * <p>
     * NOTE: calling this method in a display list may cause rendering problems
     * because it pushes/pops GL attributes.
     *
     * @param rc The render context.
     * @param onscreen The tiles to draw.
     * @param mapContext The map context.
     */
    protected void drawEllipsoids(RenderContext rc, Collection<? extends TileGeometry> onscreen, MapContext<?> mapContext)
    {
        GL gl = rc.getGL();
        gl.getGL2().glPushAttrib(GL2.GL_CURRENT_BIT);
        try
        {
            final float lineWidth = 3f;
            gl.glLineWidth(lineWidth);
            for (TileGeometry tileGeometry : onscreen)
            {
                Class<? extends Position> positionType = tileGeometry.getPositionType();
                if (positionType.equals(GeographicPosition.class))
                {
                    Ellipsoid ell = mapContext.getProjection().getBoundingEllipsoid(
                            (GeographicBoundingBox)tileGeometry.getBounds(), rc.getCurrentModelCenter(), true);

                    drawEllipsoid(rc, ell);
                }
            }
        }
        finally
        {
            gl.getGL2().glPopAttrib();
        }
    }

    /**
     * Debugging method that will draw labels for the tiles.
     * <p>
     * NOTE: calling this method in a display list may cause rendering problems
     * because it pushes/pops GL attributes.
     *
     * @param rc The render context.
     * @param onscreen The tiles to draw.
     * @param mapContext The map context.
     */
    protected void drawTileLabels(RenderContext rc, Collection<? extends TileGeometry> onscreen, MapContext<?> mapContext)
    {
        GL gl = rc.getGL();
        gl.getGL2().glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_CURRENT_BIT);
        try
        {
            gl.glEnable(GL.GL_BLEND);
            gl.glDisable(GL.GL_DEPTH_TEST);

            PositionConverter positionConverter = new PositionConverter(mapContext);
            if (myDebugTextRenderer == null)
            {
                Font drawFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
                myDebugTextRenderer = new TextRenderer(drawFont);
                myDebugTextRenderer.setUseVertexArrays(false);
                myDebugTextRenderer.setSmoothing(false);
                myDebugTextRenderer.setColor(Color.WHITE);
            }
            int width = mapContext.getScreenViewer().getViewportWidth();
            int height = mapContext.getScreenViewer().getViewportHeight();

            myDebugTextRenderer.beginRendering(width, height);
            for (TileGeometry tile : onscreen)
            {
                for (Position orig : tile.getBounds().getVertices())
                {
                    if (orig instanceof GeographicPosition)
                    {
                        GeographicPosition geo = (GeographicPosition)orig;
                        Vector3d window = positionConverter.convertPositionToWindow(orig, null);
                        StringBuilder sb = new StringBuilder();
                        sb.append('[').append(geo.getLatLonAlt().getLatD());
                        sb.append(',').append(geo.getLatLonAlt().getLonD()).append(']');
                        myDebugTextRenderer.draw(sb.toString(), (int)window.getX(), (int)window.getY());
                    }
                }
            }
            myDebugTextRenderer.endRendering();
        }
        finally
        {
            gl.getGL2().glPopAttrib();
        }
    }
}
