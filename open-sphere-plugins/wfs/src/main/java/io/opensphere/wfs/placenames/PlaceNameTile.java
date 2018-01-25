package io.opensphere.wfs.placenames;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.math.Plane;
import io.opensphere.core.math.Sphere;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.viewer.Viewer;

/** A group of place names over a particular region. */
public class PlaceNameTile
{
    /** The bounding sphere for this tile in model coordinates. */
    private final Sphere myBoundingSphere;

    /** Children tiles or null if at the bottom level. */
    private List<PlaceNameTile> myChildren;

    /** True when I am waiting to receive data. */
    private boolean myDataRequested;

    /** Split depth in the quad-split tree. */
    private final int myDepth;

    /** Geometries which will display for this tile. */
    private final List<LabelGeometry> myGeometries = Collections.synchronizedList(new ArrayList<LabelGeometry>());

    /** True when I am visible. */
    private boolean myIsVisible;

    /** Key which identifies this tile. */
    private final PlaceNameKey myKey;

    /** Layer to which this tile belongs. */
    private final PlaceNameLayer myLayer;

    /** Lock for synchronization. */
    private final Object myLock = new Object();

    /** The center of this tile in model coordinates. */
    private final Vector3d myModelCenter;

    /** Parent tile or null if at the top level. */
    private final PlaceNameTile myParent;

    /** Place names to display for this tile when visible. */
    private PlaceNameData myPlaceNames;

    /** The server name this tile is associated with. */
    private String myServerName;

    /**
     * Constructor.
     *
     * @param key Key which identifies this tile.
     * @param layer Layer to which this tile belongs.
     * @param parent Parent tile.
     * @param serverName The server name for this tile.
     */
    public PlaceNameTile(PlaceNameKey key, PlaceNameLayer layer, PlaceNameTile parent, String serverName)
    {
        myKey = key;
        myLayer = layer;
        myParent = parent;
        if (myParent != null)
        {
            myDepth = myParent.getDepth() + 1;
        }
        else
        {
            myDepth = 0;
        }
        myServerName = serverName;

        Projection proj = myLayer.getToolbox().getMapManager().getProjection();
        myModelCenter = proj.convertToModel(myKey.getBounds().getCenter(), Vector3d.ORIGIN);
        myBoundingSphere = proj.getBoundingSphere(myKey.getBounds(), Vector3d.ORIGIN, false);
    }

    /**
     * Build a list of the visible tiles from my level down.
     *
     * @return A list of the visible tiles from my level down.
     */
    public List<PlaceNameTile> buildVisible()
    {
        List<PlaceNameTile> visibleTiles = new ArrayList<>();
        Viewer viewer = myLayer.getToolbox().getMapManager().getStandardViewer();
        synchronized (myLock)
        {
            // only consider tiles whose centers are within the max view
            // distance.
            double maxViewDist = myLayer.getConfiguration().getMaxDisplayDistance();
            double minViewDist = myLayer.getConfiguration().getMinDisplayDistance();
            double distance = viewer.getPosition().getLocation().distance(myModelCenter);
            Plane plane = new Plane(Vector3d.ORIGIN, viewer.getPosition().getLocation());
            // For checking to see if the tile is on the opposite side of the
            // globe, it would be better to use the tile width as the tolerance.
            double boundingRadius = myBoundingSphere.getRadius();
            double tolerance = boundingRadius;
            if (myKey.getBounds().getWidth() < 23)
            {
                tolerance = 0.;
            }
            if (distance > maxViewDist + boundingRadius || distance < minViewDist - boundingRadius
                    || !viewer.isInView(myBoundingSphere.getCenter(), boundingRadius)
                    || !plane.isInFront(myModelCenter, tolerance))
            {
                clearGeometries();
                myChildren = null;
                return visibleTiles;
            }

            if (myDepth >= myLayer.getSplitDepth())
            {
                visibleTiles.add(this);
            }
            else
            {
                if (myChildren == null)
                {
                    myChildren = new ArrayList<>();
                    GeographicBoundingBox bbox = myKey.getBounds();
                    GeographicPosition center = bbox.getCenter();
                    GeographicPosition leftCenter = bbox.getLeftCenter();
                    GeographicPosition upperCenter = bbox.getUpperCenter();
                    GeographicPosition lowerCenter = bbox.getLowerCenter();
                    GeographicPosition rightCenter = bbox.getRightCenter();
                    PlaceNameKey ulkey = new PlaceNameKey(new GeographicBoundingBox(leftCenter, upperCenter));
                    PlaceNameKey llkey = new PlaceNameKey(new GeographicBoundingBox(myKey.getBounds().getLowerLeft(), center));
                    PlaceNameKey lrkey = new PlaceNameKey(new GeographicBoundingBox(lowerCenter, rightCenter));
                    PlaceNameKey urkey = new PlaceNameKey(new GeographicBoundingBox(center, myKey.getBounds().getUpperRight()));

                    myChildren.add(new PlaceNameTile(ulkey, myLayer, this, myServerName));
                    myChildren.add(new PlaceNameTile(llkey, myLayer, this, myServerName));
                    myChildren.add(new PlaceNameTile(lrkey, myLayer, this, myServerName));
                    myChildren.add(new PlaceNameTile(urkey, myLayer, this, myServerName));
                }

                for (PlaceNameTile child : myChildren)
                {
                    visibleTiles.addAll(child.buildVisible());
                }
            }
        }

        return visibleTiles;
    }

    /** Remove all geometries from the pipeline. */
    public void clearGeometries()
    {
        synchronized (myLock)
        {
            myIsVisible = false;
            if (myChildren != null)
            {
                for (PlaceNameTile child : myChildren)
                {
                    child.clearGeometries();
                }
            }
            if (!myGeometries.isEmpty())
            {
                List<LabelGeometry> removes = new ArrayList<>(myGeometries);
                PlaceNamesEvent evt = new PlaceNamesEvent(Collections.<LabelGeometry>emptySet(), removes, myServerName,
                        myLayer.getConfiguration().getDataSetName(), myLayer.isActive());
                myLayer.getToolbox().getEventManager().publishEvent(evt);
            }

            myPlaceNames = null;
            myGeometries.clear();
        }
    }

    /**
     * Get the depth.
     *
     * @return the depth
     */
    public int getDepth()
    {
        return myDepth;
    }

    /**
     * Get the publishedGeometries.
     *
     * @return the publishedGeometries
     */
    public List<LabelGeometry> getGeometries()
    {
        return myGeometries;
    }

    /**
     * Get the key.
     *
     * @return the key
     */
    public PlaceNameKey getKey()
    {
        return myKey;
    }

    /**
     * Get a string representation of my key.
     *
     * @return A string representation of my key.
     */
    public String getKeyString()
    {
        return myKey.toString();
    }

    /**
     * Get the layer.
     *
     * @return the layer
     */
    public PlaceNameLayer getLayer()
    {
        return myLayer;
    }

    /**
     * Get the parent.
     *
     * @return the parent
     */
    public PlaceNameTile getParent()
    {
        return myParent;
    }

    /**
     * Get the placeNames.
     *
     * @return the placeNames
     */
    public PlaceNameData getPlaceNames()
    {
        return myPlaceNames;
    }

    /**
     * Get the server name.
     *
     * @return The server name.
     */
    public String getServerName()
    {
        return myServerName;
    }

    /**
     * Get the isVisible.
     *
     * @return the isVisible
     */
    public boolean isIsVisible()
    {
        return myIsVisible;
    }

    /** Publish geometries to the pipeline. */
    public void publishGeometries()
    {
        synchronized (myLock)
        {
            myIsVisible = true;

            if (myPlaceNames == null)
            {
                if (!myDataRequested)
                {
                    myDataRequested = true;
                    PlaceNamesRequestEvent event = new PlaceNamesRequestEvent(this);
                    myLayer.getToolbox().getEventManager().publishEvent(event);
                }
                return;
            }

            if (!myPlaceNames.getPlaceNames().isEmpty() && (myGeometries == null || myGeometries.isEmpty()))
            {
                generateGeometries();
            }

            if (!myGeometries.isEmpty())
            {
                List<LabelGeometry> adds = new ArrayList<>(myGeometries);
                PlaceNamesEvent evt = new PlaceNamesEvent(adds, Collections.<LabelGeometry>emptySet(), myServerName,
                        myLayer.getConfiguration().getDataSetName(), myLayer.isActive());
                myLayer.getToolbox().getEventManager().publishEvent(evt);
            }
        }
    }

    /**
     * Receive data which has been retrieved from the server.
     *
     * @param placeNameData Data which has been retrieved from the server.
     */
    public void receiveData(PlaceNameData placeNameData)
    {
        synchronized (myLock)
        {
            myDataRequested = false;
            if (myIsVisible)
            {
                myPlaceNames = placeNameData;
                publishGeometries();
            }
        }
    }

    /**
     * Set the isVisible.
     *
     * @param isVisible the isVisible to set
     */
    public void setIsVisible(boolean isVisible)
    {
        myIsVisible = isVisible;
    }

    /**
     * Set the server name.
     *
     * @param serverName The server name.
     */
    public void setServerName(String serverName)
    {
        myServerName = serverName;
    }

    /** Generate geometries to publish to the pipeline. */
    private void generateGeometries()
    {
        synchronized (myLock)
        {
            if (myPlaceNames != null)
            {
                myGeometries.clear();
                LabelGeometry.Builder<GeographicPosition> builder = new LabelGeometry.Builder<GeographicPosition>();
                builder.setOutlined(true);
                Color color = new Color(Integer.decode(myLayer.getConfiguration().getFontColor()).intValue());
                // TODO This should be configurable in the Z-Order manager.
                LabelRenderProperties props = new DefaultLabelRenderProperties(100, true, false);
                props.setColor(color);
                props.setShadowOffset(1f, 1f);
                builder.setFont(myLayer.getConfiguration().getFontColor());
                builder.setHorizontalAlignment(0.5f);
                builder.setVerticalAlignment(0.5f);
                for (PlaceNameData.PlaceName place : myPlaceNames.getPlaceNames())
                {
                    if (place.getName() != null)
                    {
                        builder.setPosition(place.getLocation());
                        builder.setText(place.getName());
                        LabelGeometry label = new LabelGeometry(builder, props, null);
                        myGeometries.add(label);
                    }
                }
            }
        }
    }
}
