package io.opensphere.core.hud.border;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.PolygonMeshGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.hud.framework.Border;
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.hud.framework.Panel;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.hud.widget.ClassicHUDPalette;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.Utilities;

/**
 * A border with no size and no geometries.
 */
public class TitleBarBorder extends Renderable implements Border
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(TitleBarBorder.class);

    /** Height of the titlebar. */
    private static final int ourHeight = 20;

    /** Geometry which can be dragged to move the window. */
    private Geometry myDragGeometry;

    /** Geometries which will be drawn on the border. */
    private final Set<Geometry> myGeometries = new HashSet<>();

    /** True when the window has a kill 'X' in the title bar. */
    private final boolean myHasKill;

    /** Drawn geometries that make up the 'X' to close the window. */
    private final Set<Geometry> myKillBox = new HashSet<>();

    /** Geometry which can be clicked to close the window. */
    private Geometry myKillGeometry;

    /** Support class for events from the control context. */
    private final ControlEventSupport myMouseSupport;

    /** Title text to display. */
    private final String myTitle;

    /**
     * Construct a HUDSimpleLineBorder.
     *
     * @param parent Parent panel.
     * @param title Title text to display.
     * @param hasKill True when the window has a kill 'X' in the title bar.
     */
    public TitleBarBorder(Panel<?, ?> parent, String title, boolean hasKill)
    {
        super(parent);
        myHasKill = hasKill;
        myTitle = title;
        myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
    }

    @Override
    public int getBottomInset()
    {
        return 0;
    }

    @Override
    public final ScreenBoundingBox getDrawBounds()
    {
        return getParent().getDrawBounds();
    }

    @Override
    public Set<Geometry> getGeometries()
    {
        return myGeometries;
    }

    @Override
    public int getLeftInset()
    {
        return 0;
    }

    @Override
    public int getRightInset()
    {
        return 0;
    }

    @Override
    public int getTopInset()
    {
        return ourHeight;
    }

    @Override
    public void handleCleanupListeners()
    {
        myMouseSupport.cleanupListeners();
    }

    @Override
    public final void init()
    {
        super.init();
        ScreenBoundingBox bbox = getDrawBounds();
        if (bbox.getHeight() < ourHeight)
        {
            LOGGER.warn("Panel is too small for the title bar.");
        }

        setupMesh();
        setupBorderLines();
        setupKill(Color.WHITE.darker());
        setupTitle();
    }

    @Override
    public void mouseClicked(Geometry geom, MouseEvent evt)
    {
        if (Utilities.sameInstance(geom, myKillGeometry))
        {
            closeWindow();
        }
    }

    @Override
    public void mouseDragged(Geometry geom, Point dragStart, MouseEvent evt)
    {
        if (Utilities.sameInstance(geom, myDragGeometry))
        {
            Point end = evt.getPoint();
            moveWindow(new ScreenPosition((int)(end.getX() - dragStart.getX()), (int)(end.getY() - dragStart.getY())));
        }
    }

    @Override
    public void mouseEntered(Geometry geom, Point location)
    {
        if (Utilities.sameInstance(geom, myKillGeometry))
        {
            Set<Geometry> oldKills = new HashSet<>(myKillBox);
            getGeometries().removeAll(oldKills);
            setupKill(Color.RED.darker());
            updateGeometries(myKillBox, oldKills);
        }
    }

    @Override
    public void mouseExited(Geometry geom, Point location)
    {
        if (Utilities.sameInstance(geom, myKillGeometry))
        {
            Set<Geometry> oldKills = new HashSet<>(myKillBox);
            getGeometries().removeAll(oldKills);
            setupKill(Color.WHITE.darker());
            updateGeometries(myKillBox, oldKills);
        }
    }

    /**
     * Create the builder for the kill geometry.
     *
     * @return the newly created builder
     */
    private PolylineGeometry.Builder<ScreenPosition> createKillBuilder()
    {
        PolylineGeometry.Builder<ScreenPosition> polyBuilder = new PolylineGeometry.Builder<ScreenPosition>();
        polyBuilder.setLineSmoothing(false);
        return polyBuilder;
    }

    /** Put lines on the bottom. */
    private void setupBorderLines()
    {
        ScreenBoundingBox bbox = getDrawBounds();
        ScreenPosition ul = bbox.getUpperLeft();
        ScreenPosition lr = bbox.getLowerRight();

        PolylineGeometry.Builder<ScreenPosition> polyBuilder = new PolylineGeometry.Builder<ScreenPosition>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(4, true, false);
        props.setColor(ClassicHUDPalette.ourBottomDecorLineColor);

        List<ScreenPosition> positions = new ArrayList<>();
        positions.add(new ScreenPosition(ul.getX(), ourHeight - 1));
        positions.add(new ScreenPosition(lr.getX(), ourHeight - 1));
        polyBuilder.setVertices(positions);

        PolylineGeometry line = new PolylineGeometry(polyBuilder, props, null);
        getGeometries().add(line);

        positions.clear();
        positions.add(new ScreenPosition(ul.getX(), ourHeight - 2));
        positions.add(new ScreenPosition(lr.getX(), ourHeight - 2));
        PolylineRenderProperties props2 = new DefaultPolylineRenderProperties(4, true, false);
        props2.setColor(ClassicHUDPalette.ourTopDecorLineColor);
        polyBuilder.setVertices(positions);

        line = new PolylineGeometry(polyBuilder, props2, null);
        getGeometries().add(line);
    }

    /**
     * Add the kill box if required.
     *
     * @param color Color for the lines of the box.
     */
    private void setupKill(Color color)
    {
        if (!myHasKill)
        {
            return;
        }

        ScreenBoundingBox bbox = getDrawBounds();
        myKillBox.clear();

        PolylineGeometry.Builder<ScreenPosition> polyBuilder = createKillBuilder();

        double posX = bbox.getUpperLeft().getX();
        double posY = bbox.getUpperLeft().getY();
        double width = (float)bbox.getWidth();

        // surrounding box.
        List<ScreenPosition> positions = new ArrayList<>();
        positions.add(new ScreenPosition(posX + width - 16, posY + 3));
        positions.add(new ScreenPosition(posX + width - 16, posY + 15));
        positions.add(new ScreenPosition(posX + width - 3, posY + 15));
        positions.add(new ScreenPosition(posX + width - 3, posY + 3));
        positions.add(new ScreenPosition(posX + width - 16, posY + 3));

        polyBuilder.setVertices(positions);
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(7, true, false);
        props.setColor(color);
        PolylineGeometry line = new PolylineGeometry(polyBuilder, props, null);
        myKillBox.add(line);
        getGeometries().add(line);

        // Close X - \
        positions.clear();
        positions.add(new ScreenPosition(posX + width - 14, posY + 6));
        positions.add(new ScreenPosition(posX + width - 6, posY + 14));
        positions.add(new ScreenPosition(posX + width - 5, posY + 13));
        positions.add(new ScreenPosition(posX + width - 13, posY + 5));
        positions.add(new ScreenPosition(posX + width - 14, posY + 6));

        polyBuilder.setVertices(positions);
        line = new PolylineGeometry(polyBuilder, props, null);
        myKillBox.add(line);
        getGeometries().add(line);

        // Close X - /
        positions.clear();
        positions.add(new ScreenPosition(posX + width - 14, posY + 13));
        positions.add(new ScreenPosition(posX + width - 6, posY + 5));
        positions.add(new ScreenPosition(posX + width - 5, posY + 6));
        positions.add(new ScreenPosition(posX + width - 13, posY + 14));
        positions.add(new ScreenPosition(posX + width - 14, posY + 13));

        polyBuilder.setVertices(positions);
        line = new PolylineGeometry(polyBuilder, props, null);
        myKillBox.add(line);
        getGeometries().add(line);

        if (myKillGeometry == null)
        {
            ScreenBoundingBox pickBox = new ScreenBoundingBox(new ScreenPosition(posX + width - 16, posY + 3),
                    new ScreenPosition(posX + width - 3, posY + 15));
            TileGeometry pickTile = getPickTile(pickBox, 20);
            myKillGeometry = pickTile;
            myMouseSupport.setActionGeometry(pickTile);
            getGeometries().add(pickTile);
        }
    }

    /** Generate the positions in the mesh. */
    private void setupMesh()
    {
        ScreenBoundingBox bbox = getDrawBounds();
        double boxTop = bbox.getUpperLeft().getY();
        double boxLeft = bbox.getUpperLeft().getX();

        PolygonMeshGeometry.Builder<ScreenPosition> polyBuilder = new PolygonMeshGeometry.Builder<ScreenPosition>();
        PolygonMeshRenderProperties props = new DefaultPolygonMeshRenderProperties(2, true, true, true);
        props.setColor(ClassicHUDPalette.ourTitleBarColor);
        props.setLighting(LightingModelConfigGL.getDefaultLight());

        List<ScreenPosition> positions = new ArrayList<>();
        List<Vector3d> normals = new ArrayList<>();

        double width = bbox.getWidth();

        // TODO enzio - this should all be configurable.
        double radius = ourHeight / 2.0;
        final double horizontalSections = 20;
        final float numberOfAngleFaces = 20;
        final double widthPerSection = width / horizontalSections;

        for (int i = 0; i < horizontalSections; i++)
        {
            double xOffset = i * widthPerSection + boxLeft;
            for (int j = 0; j < numberOfAngleFaces; j++)
            {
                double oldAngle = Math.PI * j / numberOfAngleFaces;
                double oldAngleCosForNormal = Math.cos(Math.PI - oldAngle);

                double newAngle = Math.PI * (j + 1) / numberOfAngleFaces;
                double newAngleCosForNormal = Math.cos(Math.PI - newAngle);

                double oldCos = -Math.cos(oldAngle) * radius + radius;
                double newCos = -Math.cos(newAngle) * radius + radius;

                double origSinO = Math.sin(oldAngle);
                double origSinN = Math.sin(newAngle);

                normals.add(new Vector3d(0, oldAngleCosForNormal, origSinO));
                normals.add(new Vector3d(0, oldAngleCosForNormal, origSinO));
                normals.add(new Vector3d(0, newAngleCosForNormal, origSinN));
                normals.add(new Vector3d(0, newAngleCosForNormal, origSinN));

                positions.add(new ScreenPosition((int)xOffset, (int)(ourHeight - oldCos) + boxTop));
                positions.add(new ScreenPosition((int)(xOffset + widthPerSection), (int)(ourHeight - oldCos) + boxTop));
                positions.add(new ScreenPosition((int)(xOffset + widthPerSection), (int)(ourHeight - newCos) + boxTop));
                positions.add(new ScreenPosition((int)xOffset, (int)(ourHeight - newCos) + boxTop));
            }
        }

        polyBuilder.setPolygonVertexCount(4);
        polyBuilder.setPositions(positions);
        polyBuilder.setNormals(normals);

        PolygonMeshGeometry bar = new PolygonMeshGeometry(polyBuilder, props, null);
        myMouseSupport.setActionGeometry(bar);
        myDragGeometry = bar;
        getGeometries().add(bar);
    }

    /** Setup the title text. */
    private void setupTitle()
    {
        LabelGeometry.Builder<ScreenPosition> labelBuilder = new LabelGeometry.Builder<ScreenPosition>();
        labelBuilder.setFont(Font.SANS_SERIF + " PLAIN 12");
        labelBuilder.setText(myTitle);

        LabelRenderProperties props = new DefaultLabelRenderProperties(5, true, false);
        props.setColor(Color.BLACK);
        labelBuilder.setPosition(new ScreenPosition(12, ourHeight - 5));
        LabelGeometry geom = new LabelGeometry(labelBuilder, props, null);
        getGeometries().add(geom);

        LabelRenderProperties props2 = new DefaultLabelRenderProperties(6, true, false);
        props2.setColor(Color.BLACK);
        labelBuilder.setPosition(new ScreenPosition(10, ourHeight - 7));
        geom = new LabelGeometry(labelBuilder, props2, null);
        getGeometries().add(geom);
    }
}
