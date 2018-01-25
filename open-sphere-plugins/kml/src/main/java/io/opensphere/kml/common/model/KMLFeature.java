package io.opensphere.kml.common.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.concurrent.GuardedBy;
import javax.swing.Icon;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.AbstractView;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.GroundOverlay;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Region;
import de.micromata.opengis.kml.v_2_2_0.ScreenOverlay;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;
import de.micromata.opengis.kml.v_2_2_0.TimePrimitive;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.BooleanUtilities;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.LoadsTo;

/**
 * KML wrapper for a JAK Feature.
 */
@SuppressWarnings("PMD.GodClass")
public class KMLFeature
{
    /** A pattern to get a color from HTML tags. */
    private static final Pattern HTML_COLOR_PATTERN = Pattern.compile(".*?color=\"(#\\w{6})\".*");

    /** The JAK KML Feature. */
    private final Feature myFeature;

    /** The data source that caused this feature to come to be. */
    private final KMLDataSource myCreatingDataSource;

    /** The (overridden) name of the feature. */
    private final String myName;

    /** The label text. */
    private final String myLabelText;

    /** The color. */
    private final int myColor;

    /** The geometry color. */
    private volatile Color myGeometryColor;

    /** The parent KMLFeature. */
    private volatile KMLFeature myParent;

    /** The children KMLFeatures. */
    @GuardedBy("this")
    private Collection<KMLFeature> myChildren;

    /** The internal data source that was created from this feature, or null. */
    private volatile KMLDataSource myResultingDataSource;

    /** Whether the feature is visible (overridden). */
    @GuardedBy("this")
    private byte myIsVisible;

    /** Whether the feature is open (overridden). */
    @GuardedBy("this")
    private byte myIsOpen;

    /** Geographic bounding box for ground overlays (overridden). */
    @GuardedBy("this")
    private GeographicBoundingBox myGeoBoundingBox;

    /** Whether the region for this feature is active (or true if no region). */
    private boolean myIsRegionActive = true;

    /** The mantle id. */
    private volatile long myId;

    /** The state of the feature (true = added, false = existing). */
    private boolean myAdded = true;

    /** The geometries associated with this feature. */
    @GuardedBy("this")
    private Map<Object, List<Geometry>> myGeometries;

    /** The tooltip text. */
    private String myToolTipText;

    /** The icon. */
    private Icon myIcon;

    /** Whether to show the balloon. */
    private boolean myShowBalloon;

    /**
     * Constructor.
     *
     * @param feature The JAK KML Feature
     * @param dataSource The data source
     */
    public KMLFeature(Feature feature, KMLDataSource dataSource)
    {
        myFeature = feature;
        myCreatingDataSource = dataSource;

        // Set name
        String name = "[no name]";
        if (myFeature.getName() != null)
        {
            name = myFeature.getName();
        }
        else if (myCreatingDataSource.getCreatingKMLFeature() != null)
        {
            name = myCreatingDataSource.getCreatingKMLFeature().getName();
        }
        myName = name.trim();

        // Set label text and color
        String labelText = myName;
        Color color = null;
        if (StringUtilities.containsHTML(labelText))
        {
            Matcher m = HTML_COLOR_PATTERN.matcher(labelText);
            if (m.matches())
            {
                String colorString = m.group(1);
                color = Color.decode(colorString);
            }

            labelText = StringUtilities.removeHTML(labelText);
        }
        if (StringUtils.isBlank(labelText))
        {
            labelText = "[no name]";
        }
        myLabelText = labelText;
        myColor = color != null ? color.getRGB() : -1;
    }

    /**
     * Returns whether the given object is nominally equal to this one.
     *
     * @param other The other feature
     * @return Whether they are nominally equal
     */
    public boolean equalsNominally(KMLFeature other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null)
        {
            return false;
        }
        if (!EqualsHelper.equals(myName, other.myName))
        {
            return false;
        }
        String path = getResultingDataSource() != null ? getResultingDataSource().getPath() : null;
        String otherPath = other.getResultingDataSource() != null ? other.getResultingDataSource().getPath() : null;
        return EqualsHelper.equals(path, otherPath);
    }

    /**
     * Returns whether the given object is spatially equal to this one.
     *
     * @param other The other feature
     * @return Whether they are spatially equal
     */
    public boolean equalsSpatially(KMLFeature other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null)
        {
            return false;
        }
        if (myFeature instanceof Placemark && other.myFeature instanceof Placemark)
        {
            if (!EqualsHelper.equals(((Placemark)myFeature).getGeometry(), ((Placemark)other.myFeature).getGeometry()))
            {
                return false;
            }
        }
        else if (myFeature instanceof GroundOverlay)
        {
            if (!EqualsHelper.equals(((GroundOverlay)myFeature).getLatLonBox(), ((GroundOverlay)other.myFeature).getLatLonBox()))
            {
                return false;
            }
        }
        else if (myFeature instanceof ScreenOverlay)
        {
            if (!EqualsHelper.equals(((ScreenOverlay)myFeature).getOverlayXY(), ((ScreenOverlay)other.myFeature).getOverlayXY()))
            {
                return false;
            }
            if (!EqualsHelper.equals(((ScreenOverlay)myFeature).getScreenXY(), ((ScreenOverlay)other.myFeature).getScreenXY()))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the abstract view.
     *
     * @return the abstract view
     */
    public AbstractView getAbstractView()
    {
        return myFeature.getAbstractView();
    }

    /**
     * Gets all the features (recursively).
     *
     * @return all the features
     */
    public synchronized Collection<KMLFeature> getAllFeatures()
    {
        Collection<KMLFeature> features = new ArrayList<>(myResultingDataSource != null || myChildren != null ? 10 : 1);
        features.add(this);
        if (myResultingDataSource != null)
        {
            features.addAll(myResultingDataSource.getAllFeatures());
        }
        else if (myChildren != null)
        {
            for (KMLFeature child : myChildren)
            {
                features.addAll(child.getAllFeatures());
            }
        }
        return features;
    }

    /**
     * Gets the children.
     *
     * @return the children
     */
    public synchronized Collection<KMLFeature> getChildren()
    {
        return myChildren == null ? Collections.emptyList() : New.list(myChildren);
    }

    /**
     * Gets the children.
     *
     * @return the children
     */
    public synchronized Collection<KMLFeature> getChildrenUnsafe()
    {
        return myChildren;
    }

    /**
     * Adds a child feature.
     *
     * @param child the child
     */
    public synchronized void addChild(KMLFeature child)
    {
        if (myChildren == null)
        {
            myChildren = New.list();
        }
        myChildren.add(child);
        child.setParent(this);
    }

    /**
     * Getter for color.
     *
     * @return the color
     */
    public Color getColor()
    {
        return myColor == -1 ? null : new Color(myColor, true);
    }

    /**
     * Getter for creatingDataSource.
     *
     * @return the creatingDataSource
     */
    public KMLDataSource getCreatingDataSource()
    {
        return myCreatingDataSource;
    }

    /**
     * Temporary method to get the data source until callers can choose
     * specifically.
     *
     * @return the preferred data source
     */
    public KMLDataSource getDataSource()
    {
        return myResultingDataSource != null ? myResultingDataSource : myCreatingDataSource;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription()
    {
        return myFeature.getDescription();
    }

    /**
     * Gets the extended data.
     *
     * @return the extended data
     */
    public ExtendedData getExtendedData()
    {
        return myFeature.getExtendedData();
    }

    /**
     * Getter for feature.
     *
     * @return the feature
     */
    public Feature getFeature()
    {
        return myFeature;
    }

    /**
     * Getter for geoBoundingBox.
     *
     * @return the geoBoundingBox
     */
    public synchronized GeographicBoundingBox getGeoBoundingBox()
    {
        return myGeoBoundingBox;
    }

    /**
     * Getter for icon.
     *
     * @return the icon
     */
    public Icon getIcon()
    {
        return myIcon;
    }

    /**
     * Getter for id.
     *
     * @return the id
     */
    public long getId()
    {
        return myId;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public LabelGeometry getLabel()
    {
        return (LabelGeometry)getGeometry("label");
    }

    /**
     * Getter for labelText.
     *
     * @return the labelText
     */
    public String getLabelText()
    {
        return myLabelText;
    }

    /**
     * Getter for loadsTo.
     *
     * @return the loadsTo
     */
    public LoadsTo getLoadsTo()
    {
        return myFeature.getTimePrimitive() != null && myCreatingDataSource.isIncludeInTimeline() ? LoadsTo.TIMELINE
                : LoadsTo.STATIC;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    public KMLFeature getParent()
    {
        return myParent;
    }

    /**
     * Walks up the feature inheritance tree to find the first Region.
     *
     * @return The first Region in the inheritance tree.
     */
    public Region getRegion()
    {
        KMLFeature feature = this;
        while (feature.getParent() != null && feature.getFeature().getRegion() == null)
        {
            feature = feature.getParent();
        }
        return feature.getFeature().getRegion();
    }

    /**
     * Gets the resulting data source.
     *
     * @return the resulting data source
     */
    public KMLDataSource getResultingDataSource()
    {
        return myResultingDataSource;
    }

    /**
     * Getter for state (true = added, false = existing).
     *
     * @return whether the feature is added
     */
    public boolean isAdded()
    {
        return myAdded;
    }

    /**
     * Gets the style selector.
     *
     * @return the style selector
     */
    public List<StyleSelector> getStyleSelector()
    {
        return myFeature.getStyleSelector();
    }

    /**
     * Gets the style url.
     *
     * @return the style url
     */
    public String getStyleUrl()
    {
        return myFeature.getStyleUrl();
    }

    /**
     * Get the tile geometry.
     *
     * @return The geometry.
     */
    public TileGeometry getTile()
    {
        return (TileGeometry)getGeometry("tile");
    }

    /**
     * Gets the time primitive.
     *
     * @return the time primitive
     */
    public TimePrimitive getTimePrimitive()
    {
        return myFeature.getTimePrimitive();
    }

    /**
     * Getter for toolTipText.
     *
     * @return the toolTipText
     */
    public String getToolTipText()
    {
        return myToolTipText;
    }

    /**
     * Checks if is open.
     *
     * @return the boolean
     */
    public synchronized Boolean isOpen()
    {
        Boolean isOpen = BooleanUtilities.fromByte(myIsOpen);
        return isOpen == null ? myFeature.isOpen() == null ? Boolean.FALSE : myFeature.isOpen() : isOpen;
    }

    /**
     * Getter for isRegionActive.
     *
     * @return the isRegionActive
     */
    public boolean isRegionActive()
    {
        return myIsRegionActive;
    }

    /**
     * Getter for showBalloon.
     *
     * @return the showBalloon
     */
    public boolean isShowBalloon()
    {
        return myShowBalloon;
    }

    /**
     * Checks if is visibility.
     *
     * @return the boolean
     */
    public synchronized Boolean isVisibility()
    {
        Boolean isVisible = BooleanUtilities.fromByte(myIsVisible);
        return isVisible == null ? myFeature.isVisibility() == null ? Boolean.TRUE : myFeature.isVisibility() : isVisible;
    }

    /**
     * Sets this equal to the other KMLFeature.
     *
     * @param other The other KMLFeature
     * @param equalsSpatially Indicates if features are in the same location.
     */
    public void setEqualTo(KMLFeature other, boolean equalsSpatially)
    {
        if (other != null)
        {
            setVisibility(other.isVisibility());
            setOpen(other.isOpen());
            setResultingDataSource(other.getResultingDataSource());

            if (equalsSpatially)
            {
                myId = other.myId;
                setLabel(other.getLabel());
                setTile(other.getTile());
                setGeoBoundingBox(other.getGeoBoundingBox());
            }
        }
    }

    /**
     * Setter for geoBoundingBox.
     *
     * @param geoBoundingBox the geoBoundingBox
     */
    public synchronized void setGeoBoundingBox(GeographicBoundingBox geoBoundingBox)
    {
        myGeoBoundingBox = geoBoundingBox;
    }

    /**
     * Setter for icon.
     *
     * @param icon the icon
     */
    public void setIcon(Icon icon)
    {
        myIcon = icon;
    }

    /**
     * Setter for id.
     *
     * @param id the id
     */
    public void setId(long id)
    {
        myId = id;
    }

    /**
     * Sets the label.
     *
     * @param geom the label geometry
     */
    public void setLabel(LabelGeometry geom)
    {
        setGeometry("label", geom);
    }

    /**
     * Sets the open.
     *
     * @param isOpen the new open
     */
    public synchronized void setOpen(Boolean isOpen)
    {
        myIsOpen = BooleanUtilities.toByte(isOpen);
    }

    /**
     * Sets the parent.
     *
     * @param parent the new parent
     */
    public void setParent(KMLFeature parent)
    {
        myParent = parent;
    }

    /**
     * Setter for isRegionActive.
     *
     * @param isRegionActive the isRegionActive
     */
    public void setRegionActive(boolean isRegionActive)
    {
        myIsRegionActive = isRegionActive;
    }

    /**
     * Sets the resulting data source.
     *
     * @param resultingDataSource the new resulting data source
     */
    public void setResultingDataSource(KMLDataSource resultingDataSource)
    {
        myResultingDataSource = resultingDataSource;
    }

    /**
     * Setter for showBalloon.
     *
     * @param showBalloon the showBalloon
     */
    public void setShowBalloon(boolean showBalloon)
    {
        myShowBalloon = showBalloon;
    }

    /**
     * Setter for state.
     *
     * @param added whether the feature is added
     */
    public void setAdded(boolean added)
    {
        myAdded = added;
    }

    /**
     * Set the tile geometry.
     *
     * @param geom The geometry.
     */
    public void setTile(TileGeometry geom)
    {
        setGeometry("tile", geom);
    }

    /**
     * Setter for toolTipText.
     *
     * @param toolTipText the toolTipText
     */
    public void setToolTipText(String toolTipText)
    {
        myToolTipText = toolTipText;
    }

    /**
     * Sets the visibility.
     *
     * @param isVisible the new visibility
     */
    public synchronized void setVisibility(Boolean isVisible)
    {
        myIsVisible = BooleanUtilities.toByte(isVisible);
    }

    /**
     * Sets the geometries for the key.
     *
     * @param key the key
     * @param geoms the geometries
     */
    public synchronized void setGeometries(Object key, Collection<? extends Geometry> geoms)
    {
        if (myGeometries == null)
        {
            myGeometries = New.map();
        }
        myGeometries.put(key, New.list(geoms));
    }

    /**
     * Clears the geometries for the key.
     *
     * @param key the key
     */
    public synchronized void clearGeometries(Object key)
    {
        if (myGeometries != null)
        {
            myGeometries.remove(key);
        }
    }

    /**
     * Gets the geometries for the key.
     *
     * @param key the key
     * @return the geometries
     */
    public synchronized Collection<Geometry> getGeometries(Object key)
    {
        Collection<Geometry> geoms = Collections.emptyList();
        if (myGeometries != null)
        {
            List<Geometry> tmp = myGeometries.get(key);
            if (tmp != null)
            {
                geoms = New.list(tmp);
            }
        }
        return geoms;
    }

    /**
     * Gets the geometry color.
     *
     * @return the geometry color
     */
    public Color getGeometryColor()
    {
        return myGeometryColor;
    }

    /**
     * Sets the geometry color.
     *
     * @param geometryColor the geometry color
     */
    public void setGeometryColor(Color geometryColor)
    {
        myGeometryColor = geometryColor;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (myCreatingDataSource != null && myCreatingDataSource.getName() != null)
        {
            sb.append(myCreatingDataSource.getName());
        }
        if (myFeature != null)
        {
            sb.append('/').append(getName());
        }
        return sb.toString();
    }

    /**
     * Sets the single geometry for the given key.
     *
     * @param key the key
     * @param geom the geometry
     */
    private synchronized void setGeometry(Object key, Geometry geom)
    {
        if (myGeometries == null)
        {
            myGeometries = New.map();
        }

        List<Geometry> geoms = myGeometries.get(key);
        if (geoms == null)
        {
            if (geom != null)
            {
                geoms = New.list(1);
                geoms.add(geom);
                myGeometries.put(key, geoms);
            }
        }
        else
        {
            geoms.clear();
            if (geom != null)
            {
                geoms.add(geom);
            }
        }
    }

    /**
     * Gets the single geometry for the given key.
     *
     * @param key the key
     * @return the geometry, or null
     */
    private synchronized Geometry getGeometry(Object key)
    {
        Geometry geom = null;
        if (myGeometries != null)
        {
            List<Geometry> geoms = myGeometries.get(key);
            if (geoms != null)
            {
                geom = CollectionUtilities.getItemOrNull(geoms, 0);
            }
        }
        return geom;
    }
}
