package io.opensphere.search;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.callout.Callout;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.FourTuple;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.mappoint.impl.MapPointTransformer;
import io.opensphere.mantle.mappoint.impl.MapPointTransformerHelper;
import io.opensphere.mantle.mp.event.impl.CreateMapAnnotationPointEvent;
import io.opensphere.mantle.mp.impl.DefaultMapAnnotationPoint;

/**
 * Simple transformer to draw the current "Goto" location and coordinates on the
 * map.
 */
public class SearchTransformer extends DefaultTransformer
{
    /** The Geometries. */
    private final List<FourTuple<PointGeometry, PointGeometry, TileGeometry, PolylineGeometry>> myGeometries = New.list();

    /** The Geometry context menu provider. */
    @SuppressWarnings("PMD.SingularField")
    private final ContextMenuProvider<GeometryContextKey> myGeometryContextMenuProvider = new ContextMenuProvider<GeometryContextKey>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, GeometryContextKey key)
        {
            final Geometry geom = key.getGeometry();
            List<JMenuItem> menuItems = null;

            final FourTuple<PointGeometry, PointGeometry, TileGeometry, PolylineGeometry> point = getMatchingGeomsForPoint(geom);

            if (point != null)
            {
                menuItems = New.list();

                JMenuItem createMapPoint = new JMenuItem("Create map point");
                createMapPoint.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        GeographicPosition pos = (GeographicPosition)point.getFirstObject().getPosition();
                        if (pos != null)
                        {
                            myToolbox.getEventManager().publishEvent(new CreateMapAnnotationPointEvent(this, pos));
                        }
                    }
                });
                menuItems.add(createMapPoint);

                JMenuItem mi = new JMenuItem("Remove point");
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        removeGotoGeometry(point);
                    }
                });
                menuItems.add(mi);
            }
            return menuItems;
        }

        @Override
        public int getPriority()
        {
            return 20000;
        }
    };

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Creates a point geometry for the given position.
     *
     * @param pos The geographic position of the point.
     * @param color The point color
     * @param size The point size
     * @param zDelta The zOrder delta from TOP_Z
     * @return the point geometry
     */
    private static PointGeometry getPointGeometry(GeographicPosition pos, Color color, int size, int zDelta)
    {
        PointGeometry.Builder<GeographicPosition> pointBuilder = new PointGeometry.Builder<GeographicPosition>();
        pointBuilder.setPosition(pos);

        PointRenderProperties pointProps = new DefaultPointRenderProperties(ZOrderRenderProperties.TOP_Z - zDelta, true, true,
                true);
        pointProps.setColor(color);
        pointProps.setSize(size);

        PointGeometry gotoGeom = new PointGeometry(pointBuilder, pointProps, null);
        return gotoGeom;
    }

    /**
     * Constructor.
     *
     * @param toolbox The data model repository.
     */
    public SearchTransformer(Toolbox toolbox)
    {
        super(toolbox.getDataRegistry());
        myToolbox = toolbox;
        ContextActionManager actionMgr = myToolbox.getUIRegistry().getContextActionManager();
        actionMgr.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT, GeometryContextKey.class,
                myGeometryContextMenuProvider);
    }

    /**
     * Add the appropriate geometries to show a point and the coordinates for
     * that point.
     *
     * @param pos The geographic position of the point.
     * @param label The string coordinates for the point.
     */
    public void addGeometries(GeographicPosition pos, String label)
    {
        // Create the geometries
        PointGeometry mainPoint = getPointGeometry(pos, Color.GREEN, 5, 998);
        PointGeometry outlinePoint = getPointGeometry(pos, Color.DARK_GRAY.darker(), 10, 999);
        Pair<TileGeometry, PolylineGeometry> labelGeomPair = getLabelGeometries(pos, label);

        // Publish the geometries
        List<Geometry> geometries = New.list(4);
        geometries.add(mainPoint);
        geometries.add(outlinePoint);
        geometries.add(labelGeomPair.getFirstObject());
        geometries.add(labelGeomPair.getSecondObject());
        publishGeometries(geometries, Collections.<Geometry>emptySet());

        // Keep track of stuff
        synchronized (myGeometries)
        {
            myGeometries.add(new FourTuple<PointGeometry, PointGeometry, TileGeometry, PolylineGeometry>(mainPoint, outlinePoint,
                    labelGeomPair.getFirstObject(), labelGeomPair.getSecondObject()));
        }
    }

    /**
     * Removes all geometries.
     */
    public void removeAllGeometries()
    {
        synchronized (myGeometries)
        {
            List<Geometry> geometries = New.list();
            for (FourTuple<PointGeometry, PointGeometry, TileGeometry, PolylineGeometry> geom : myGeometries)
            {
                geometries.add(geom.getFirstObject());
                geometries.add(geom.getSecondObject());
                geometries.add(geom.getThirdObject());
                geometries.add(geom.getFourthObject());
            }
            publishGeometries(Collections.<Geometry>emptySet(), geometries);
            myGeometries.clear();
        }
    }

    /**
     * Creates label geometries for the given position and label.
     *
     * @param pos The geographic position of the point.
     * @param label The string coordinates for the point.
     * @return the label geometries
     */
    private Pair<TileGeometry, PolylineGeometry> getLabelGeometries(GeographicPosition pos, String label)
    {
        DefaultMapAnnotationPoint point = new DefaultMapAnnotationPoint();
        point.setTitle(label, this);
        point.setLat(pos.getLatLonAlt().getLatD(), this);
        point.setLon(pos.getLatLonAlt().getLonD(), this);
        point.setxOffset(10, this);
        point.setyOffset(-4, this);
        point.setColor(Color.GREEN, this);
        point.setBackgroundColor(Color.DARK_GRAY, this);
        Callout callOut = MapPointTransformer.createCallOut(point);
        callOut.setPickable(false);
        Pair<TileGeometry, PolylineGeometry> geomPair = MapPointTransformerHelper.createGeometryPair(callOut, null);
        return geomPair;
    }

    /**
     * Get the geometries which contains the given geometry.
     *
     * @param geom the geometry for which the matching geometries are desired
     *            (expected to be a PointGeometry).
     * @return the geometries or {@code null} if there is no match.
     */
    private FourTuple<PointGeometry, PointGeometry, TileGeometry, PolylineGeometry> getMatchingGeomsForPoint(Geometry geom)
    {
        for (FourTuple<PointGeometry, PointGeometry, TileGeometry, PolylineGeometry> point : myGeometries)
        {
            if (Utilities.sameInstance(geom, point.getFirstObject()) || Utilities.sameInstance(geom, point.getSecondObject()))
            {
                return point;
            }
        }
        return null;
    }

    /**
     * Removes the goto geometry.
     *
     * @param point the geom
     */
    private void removeGotoGeometry(FourTuple<PointGeometry, PointGeometry, TileGeometry, PolylineGeometry> point)
    {
        synchronized (myGeometries)
        {
            myGeometries.remove(point);

            List<Geometry> geometries = New.list(4);
            geometries.add(point.getFirstObject());
            geometries.add(point.getSecondObject());
            geometries.add(point.getThirdObject());
            geometries.add(point.getFourthObject());
            publishGeometries(Collections.<Geometry>emptySet(), geometries);
        }
    }
}
