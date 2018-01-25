package io.opensphere.kml.tree.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.awt.image.FilteredImageSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.GroundOverlay;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Model;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import de.micromata.opengis.kml.v_2_2_0.PhotoOverlay;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.ScreenOverlay;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleState;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.image.ColorizeFilter;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.tree.NodeImageObserver;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLDataSource.FailureReason;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.kml.common.util.KMLToolboxUtils;

/**
 * Tree cell renderer for JAK 2.2.0 objects.
 */
public class KMLTreeCellRenderer extends DefaultTreeCellRenderer
{
    /** Foreground color for features that support balloons. */
    private static final Color BALLOON_FEATURE_FOREGROUND_COLOR = new Color(150, 150, 255);

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(KMLTreeCellRenderer.class);

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The data registry used to look up icons. */
    private final DataRegistry myDataRegistry;

    /** The default font. */
    private Font myDefaultFont;

    /** The icon map. */
    private final Map<String, ImageIcon> myIconMap;

    /** The link font. */
    private Font myLinkFont;

    /**
     * Gets the tooltip text from the data source.
     *
     * @param kmlFeature The KML user object
     * @return The tooltip text
     */
    private static String getToolTipText(KMLFeature kmlFeature)
    {
        String toolTipText = null;
        KMLDataSource dataSource = kmlFeature.getDataSource();
        Feature feature = kmlFeature.getFeature();
        if (dataSource != null && dataSource.loadError())
        {
            if (dataSource.getFailureReason() == FailureReason.INVALID_BASIC_AUTH
                    || dataSource.getFailureReason() == FailureReason.INVALID_CERTIFICATE
                    || dataSource.getFailureReason() == FailureReason.INVALID_EITHER)
            {
                toolTipText = "Authentication failed.";
            }
        }
        else
        {
            if (feature.getTimePrimitive() != null)
            {
                TimeSpan timeSpan = KMLSpatialTemporalUtils.timeSpanFromTimePrimitive(feature.getTimePrimitive());
                toolTipText = timeSpan.toDisplayString();
            }
        }
        return toolTipText;
    }

    /**
     * Constructor.
     *
     * @param dataRegistry The data registry
     */
    public KMLTreeCellRenderer(DataRegistry dataRegistry)
    {
        myDataRegistry = dataRegistry;
        myIconMap = new HashMap<>();

        initIcons();

        // Make the non selected cell transparent
        setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));

        // Make the selected cell somewhat transparent
        Color c = getBackgroundSelectionColor();
        setBackgroundSelectionColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 191));
    }

    @Override
    public Component getTreeCellRendererComponent(final JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
            int row, boolean focus)
    {
        // Set up
        KMLFeature kmlFeature = null;
        String labelText = value.toString();
        if (value instanceof DefaultMutableTreeNode)
        {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
            if (treeNode.getUserObject() instanceof KMLFeature)
            {
                kmlFeature = (KMLFeature)treeNode.getUserObject();

                updateKMLFeature(kmlFeature, expanded);
                labelText = kmlFeature.getLabelText();

                manageImageObserver(tree, treeNode, kmlFeature);
            }
        }

        // Call super
        super.getTreeCellRendererComponent(tree, labelText, sel, expanded, leaf, row, focus);

        // --- "Override" method calls starting here ---

        if (kmlFeature != null)
        {
            // Set foreground
            if (kmlFeature.getColor() != null)
            {
                setForeground(kmlFeature.getColor());
            }
            else if (kmlFeature.isShowBalloon())
            {
                setForeground(BALLOON_FEATURE_FOREGROUND_COLOR);
            }

            // Set icon
            if (kmlFeature.getIcon() != null)
            {
                setIcon(kmlFeature.getIcon());
            }
        }

        // --- Method calls that need to be called each time starting here ---

        // Init the fonts
        if (myDefaultFont == null)
        {
            myDefaultFont = getFont();

            @SuppressWarnings("unchecked")
            Map<TextAttribute, Object> attr = (Map<TextAttribute, Object>)myDefaultFont.getAttributes();
            attr.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            myLinkFont = getFont().deriveFont(attr);
        }

        boolean showBalloon = false;
        String toolTipText = null;
        if (kmlFeature != null)
        {
            showBalloon = kmlFeature.isShowBalloon();
            toolTipText = kmlFeature.getToolTipText();
        }

        // Set font
        setFont(showBalloon ? myLinkFont : myDefaultFont);

        // Set tooltip
        setToolTipText(toolTipText);

        return this;
    }

    /**
     * Cache a colorized icon.
     *
     * @param iconKey The icon key.
     * @param iconColor The icon color.
     * @param icon The icon to be colorized.
     * @return The colorized icon.
     */
    protected ImageIcon cacheColorizedIcon(String iconKey, String iconColor, ImageIcon icon)
    {
        if (icon != null && iconColor != null)
        {
            Color color = KMLSpatialTemporalUtils.convertColor(iconColor);
            Image image = Toolkit.getDefaultToolkit()
                    .createImage(new FilteredImageSource(icon.getImage().getSource(), new ColorizeFilter(color.getRGB())));
            ImageIcon colorized = new ImageIcon(image);
            myIconMap.put(iconKey + "##" + iconColor, colorized);
            return colorized;
        }
        else
        {
            return icon;
        }
    }

    /**
     * Retrieves an icon from the KMZ cache and caches it locally. Ideally this
     * should not be done in the renderer.
     *
     * @param dataSource The data source
     * @param iconKey The icon key
     * @return The Icon, or null
     */
    private ImageIcon cacheKmzIcon(KMLDataSource dataSource, String iconKey)
    {
        ImageIcon icon = null;
        try (InputStream inputStream = KMLDataRegistryHelper.queryAndReturn(myDataRegistry, dataSource, iconKey))
        {
            if (inputStream != null)
            {
                ByteBuffer imageData = new StreamReader(inputStream).readStreamIntoBuffer();
                icon = new ImageIcon(
                        Toolkit.getDefaultToolkit().createImage(imageData.array(), imageData.arrayOffset(), imageData.limit()),
                        iconKey);
                icon = ImageUtil.scaleImageIcon(icon, 16, false);
                myIconMap.put(iconKey, icon);
            }
        }
        catch (IOException e)
        {
            LOGGER.warn(e.getMessage());
        }
        return icon;
    }

    /**
     * Gets the icon.
     *
     * @param expanded The expanded state
     * @param kmlFeature The KML user object
     * @return The icon, or null
     */
    private Icon getIcon(boolean expanded, KMLFeature kmlFeature)
    {
        String iconKey = null;
        Feature feature = kmlFeature.getFeature();
        String iconColor = null;
        if (kmlFeature.getResultingDataSource() != null && kmlFeature.getResultingDataSource().isBusy())
        {
            iconKey = "busy";
        }
        else if (feature instanceof Placemark)
        {
            Placemark placemark = (Placemark)feature;

            Style style = KMLToolboxUtils.getKmlToolbox().getStyleCache().getStyle(kmlFeature, StyleState.NORMAL);
            if (style != null && style.getIconStyle() != null && style.getIconStyle().getIcon() != null)
            {
                iconColor = style.getIconStyle().getColor();
                iconKey = StringUtilities.trim(style.getIconStyle().getIcon().getHref());
            }
            else if (placemark.getGeometry() != null)
            {
                iconKey = placemark.getGeometry().getClass().getName();
            }
        }
        else if (feature instanceof NetworkLink)
        {
            boolean isLoadError = kmlFeature.getDataSource().loadError();
            iconKey = new StringBuilder(feature.getClass().getName()).append('.').append(expanded).append('.')
                    .append(!isLoadError).toString();
        }
        else if (feature instanceof Folder)
        {
            iconKey = new StringBuilder(feature.getClass().getName()).append('.').append(expanded).toString();
        }
        else
        {
            iconKey = feature.getClass().getName();
        }

        ImageIcon icon = null;
        if (iconKey != null)
        {
            if (iconColor != null)
            {
                icon = myIconMap.get(iconKey + "##" + iconColor);
            }
            if (icon == null)
            {
                icon = myIconMap.get(iconKey);
                icon = cacheColorizedIcon(iconKey, iconColor, icon);
            }
            if (icon == null)
            {
                icon = cacheKmzIcon(kmlFeature.getDataSource(), iconKey);
                icon = cacheColorizedIcon(iconKey, iconColor, icon);
            }
            if (icon == null && feature instanceof Placemark && ((Placemark)feature).getGeometry() != null)
            {
                icon = myIconMap.get(((Placemark)feature).getGeometry().getClass().getName());
                icon = cacheColorizedIcon(iconKey, iconColor, icon);
            }
        }
        return icon;
    }

    /** Initializes icons. */
    private void initIcons()
    {
        // Read the icons from the classpath
        ImageIcon documentIcon = new ImageIcon(KMLTreeCellRenderer.class.getResource("/images/16x16-icon-earth.png"));
        ImageIcon pointIcon = new ImageIcon(KMLTreeCellRenderer.class.getResource("/images/points.png"));
        ImageIcon polygonIcon = new ImageIcon(KMLTreeCellRenderer.class.getResource("/images/polygon.png"));
        ImageIcon lineIcon = new ImageIcon(KMLTreeCellRenderer.class.getResource("/images/lines.png"));
        ImageIcon multiGeomIcon = new ImageIcon(KMLTreeCellRenderer.class.getResource("/images/multi_geometry.png"));
        ImageIcon groundOverlayIcon = new ImageIcon(KMLTreeCellRenderer.class.getResource("/images/ground_overlay.png"));
        ImageIcon closedFolderIcon = new ImageIcon(KMLTreeCellRenderer.class.getResource("/images/closedfolder.png"));
        ImageIcon openFolderIcon = new ImageIcon(KMLTreeCellRenderer.class.getResource("/images/openfolder.png"));
        ImageIcon closedLinkConnectedIcon = new ImageIcon(
                KMLTreeCellRenderer.class.getResource("/images/ni_closed_connected.png"));
        ImageIcon openLinkConnectedIcon = new ImageIcon(KMLTreeCellRenderer.class.getResource("/images/ni_open_connected.png"));
        ImageIcon closedLinkDisconnectedIcon = new ImageIcon(
                KMLTreeCellRenderer.class.getResource("/images/ni_closed_disconnected.png"));
        ImageIcon openLinkDisconnectedIcon = new ImageIcon(
                KMLTreeCellRenderer.class.getResource("/images/ni_open_disconnected.png"));
        ImageIcon modelIcon = new ImageIcon(KMLTreeCellRenderer.class.getResource("/images/model.png"));
        ImageIcon unsupportedIcon = new ImageIcon(KMLTreeCellRenderer.class.getResource("/images/question_mark.png"));
        ImageIcon busyIcon = new ImageIcon(KMLTreeCellRenderer.class.getResource("/images/busy.gif"));

        // Create the icon map
        String closedFolderKey = new StringBuilder(Folder.class.getName()).append('.').append(false).toString();
        String openFolderKey = new StringBuilder(Folder.class.getName()).append('.').append(true).toString();
        String closedLinkConnectedKey = new StringBuilder(NetworkLink.class.getName()).append('.').append(false).append('.')
                .append(true).toString();
        String openLinkConnectedKey = new StringBuilder(NetworkLink.class.getName()).append('.').append(true).append('.')
                .append(true).toString();
        String closedLinkDisconnectedKey = new StringBuilder(NetworkLink.class.getName()).append('.').append(false).append('.')
                .append(false).toString();
        String openLinkDisconnectedKey = new StringBuilder(NetworkLink.class.getName()).append('.').append(true).append('.')
                .append(false).toString();
        myIconMap.put(Document.class.getName(), documentIcon);
        myIconMap.put(Point.class.getName(), pointIcon);
        myIconMap.put(Polygon.class.getName(), polygonIcon);
        myIconMap.put(LineString.class.getName(), lineIcon);
        myIconMap.put(LinearRing.class.getName(), polygonIcon);
        myIconMap.put(MultiGeometry.class.getName(), multiGeomIcon);
        myIconMap.put(GroundOverlay.class.getName(), groundOverlayIcon);
        myIconMap.put(ScreenOverlay.class.getName(), groundOverlayIcon);
        myIconMap.put(PhotoOverlay.class.getName(), unsupportedIcon);
        myIconMap.put(closedFolderKey, closedFolderIcon);
        myIconMap.put(openFolderKey, openFolderIcon);
        myIconMap.put(closedLinkConnectedKey, closedLinkConnectedIcon);
        myIconMap.put(openLinkConnectedKey, openLinkConnectedIcon);
        myIconMap.put(closedLinkDisconnectedKey, closedLinkDisconnectedIcon);
        myIconMap.put(openLinkDisconnectedKey, openLinkDisconnectedIcon);
        myIconMap.put(Model.class.getName(), modelIcon);
        myIconMap.put("busy", busyIcon);
    }

    /**
     * Manages the image observer, adding or removing paths as necessary.
     *
     * @param tree The tree
     * @param treeNode The tree node
     * @param kmlFeature The KML user object
     */
    private void manageImageObserver(final JTree tree, DefaultMutableTreeNode treeNode, KMLFeature kmlFeature)
    {
        Icon icon = kmlFeature.getIcon();
        if (icon instanceof ImageIcon)
        {
            ImageIcon iicon = (ImageIcon)icon;

            NodeImageObserver obs;
            if (iicon.getImageObserver() instanceof NodeImageObserver)
            {
                obs = (NodeImageObserver)iicon.getImageObserver();
            }
            else
            {
                obs = new NodeImageObserver(tree);
                iicon.setImageObserver(obs);
            }

            obs.addPath(new TreePath(((DefaultTreeModel)tree.getModel()).getPathToRoot(treeNode)));
        }
    }

    /**
     * Updates the feature kmlFeature.
     *
     * @param kmlFeature The KML feature kmlFeature
     * @param expanded Whether the node is expanded
     */
    private void updateKMLFeature(KMLFeature kmlFeature, boolean expanded)
    {
        // Do these every time, as the data source might change
        kmlFeature.setToolTipText(getToolTipText(kmlFeature));
        kmlFeature.setIcon(getIcon(expanded, kmlFeature));
    }
}
