package io.opensphere.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;

import com.vividsolutions.jts.geom.LineString;

import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.SingletonImageProvider;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.image.Image;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.model.GeographicBoxAnchor;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import io.opensphere.core.util.jts.core.JTSLineStringUtilities;
import io.opensphere.core.util.swing.SwingImageHelper;

/**
 * Displays a region.
 */
public class SelectionRegionTransformer extends DefaultTransformer
{
    /** Font to use for labels. */
    private static final String LABEL_FONT = Font.SANS_SERIF + " PLAIN 11";

    /** Foreground color of the text for the label. */
    private static final Color ourForegroundColor = Color.WHITE;

    /** The Decorator geometries. */
    private Collection<Geometry> myDecoratorGeometries = Collections.emptySet();

    /** The geometries for the region. */
    private List<? extends Geometry> myGeometries = Collections.emptyList();

    /** The Label geometries. */
    private Collection<TileGeometry> myLabelGeometries = Collections.emptySet();

    /**
     * Constructor.
     */
    public SelectionRegionTransformer()
    {
        super(null);
    }

    /**
     * Remove all geometries from the data registry.
     */
    public void clearRegion()
    {
        setRegion(null, null, null, null, null);
    }

    /**
     * Get the geometries that have been published.
     *
     * @return The geometries.
     */
    public synchronized List<? extends Geometry> getGeometries()
    {
        return myGeometries;
    }

    /**
     * Set the region to be displayed. Generate and publish the new geometries,
     * and remove any old geometries.
     *
     * @param region The region.
     * @param boundaryColor The color for the boundary.
     * @param decorators Decorator geometries to be published, or {@code null}.
     * @param labelPos The position for the label, or {@code null} if there's no
     *            label.
     * @param label The label text, or {@code null} if no label.
     */
    public synchronized void setRegion(com.vividsolutions.jts.geom.Geometry region, Color boundaryColor,
            Collection<io.opensphere.core.geometry.Geometry> decorators, LatLonAlt labelPos, String label)
    {
        if (decorators == null || decorators.isEmpty())
        {
            if (!myDecoratorGeometries.isEmpty())
            {
                publishGeometries(Collections.<Geometry>emptySet(), myDecoratorGeometries);
                myDecoratorGeometries = Collections.<Geometry>emptySet();
            }
        }
        else
        {
            Collection<Geometry> oldGeometries = myDecoratorGeometries;
            myDecoratorGeometries = decorators;
            publishGeometries(decorators, oldGeometries);
        }

        if (region == null)
        {
            if (!myGeometries.isEmpty())
            {
                publishGeometries(Collections.<Geometry>emptySet(), myGeometries);
                myGeometries = Collections.<Geometry>emptyList();
            }
        }
        else
        {
            if (!(region instanceof LineString))
            {
                Collection<? extends Geometry> oldGeometries = myGeometries;
                myGeometries = JTSCoreGeometryUtilities.buildSelectionPolygonSet(region, boundaryColor);
                publishGeometries(myGeometries, oldGeometries);
            }
            else
            {
                Collection<? extends Geometry> oldGeometries = myGeometries;
                myGeometries = JTSLineStringUtilities.buildLineSet(region, boundaryColor);
                publishGeometries(myGeometries, oldGeometries);
            }
        }

        if (labelPos == null || label == null)
        {
            if (!myLabelGeometries.isEmpty())
            {
                publishGeometries(Collections.<Geometry>emptySet(), myLabelGeometries);
                myLabelGeometries = Collections.<TileGeometry>emptySet();
            }
        }
        else
        {
            Collection<TileGeometry> oldGeometries = myLabelGeometries;
            myLabelGeometries = Collections.singleton(createLabelTile(labelPos, label));
            publishGeometries(myLabelGeometries, oldGeometries);
        }
    }

    /**
     * Create the tile for the label.
     *
     * @param labelPos the label pos
     * @param label the label
     * @return The newly created tile.
     */
    protected TileGeometry createLabelTile(LatLonAlt labelPos, String label)
    {
        List<String> lines = Arrays.asList(label.split("\n"));
        // Do not let the background color be completely transparent, otherwise
        // it would not be pickable except where there is text.
        Color background = new Color(84, 84, 107, 140);
        BufferedImage image = SwingImageHelper.textToImage(true, lines, background, ourForegroundColor,
                BorderFactory.createEmptyBorder(), Font.decode(LABEL_FONT));
        GeographicPosition attachment = new GeographicPosition(labelPos);

        ScreenPosition upperLeft = new ScreenPosition(0., 0.);
        ScreenPosition lowerRight = new ScreenPosition(image.getWidth(), image.getHeight());
        Vector2i v2i = new Vector2i(10, 0);
        GeoScreenBoundingBox gsbb = new GeoScreenBoundingBox(upperLeft, lowerRight,
                new GeographicBoxAnchor(attachment, v2i, 0f, 0f));

        TileGeometry.Builder<ScreenPosition> tileBuilder = new TileGeometry.Builder<>();
        TileRenderProperties props = new DefaultTileRenderProperties(0, true, true);
        tileBuilder.setBounds(gsbb);
        SingletonImageProvider imageProvider = new SingletonImageProvider(image, Image.CompressionType.D3DFMT_A8R8G8B8);
        tileBuilder.setImageManager(new ImageManager(null, imageProvider));

        return new TileGeometry(tileBuilder, props, null);
    }
}
