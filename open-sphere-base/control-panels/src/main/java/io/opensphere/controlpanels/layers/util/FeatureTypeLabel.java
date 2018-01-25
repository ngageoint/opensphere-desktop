package io.opensphere.controlpanels.layers.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.iconlegend.IconLegendRegistry;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;

/**
 * The Class FeatureTypeLabel.
 */
@SuppressWarnings("serial")
public class FeatureTypeLabel extends JLabel
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(FeatureTypeLabel.class);

    /**
     * The icon used for annotation feature types.
     */
    private static ImageIcon ourAnnotationFeaturesDarkOutlineIcon;

    /**
     * The icon used for annotation feature types.
     */
    private static ImageIcon ourAnnotationFeaturesIcon;

    /**
     * The icon used for annotation feature types.
     */
    private static ImageIcon ourAnnotationFeaturesLightOutlineIcon;

    /**
     * The icon used for annotation region types.
     */
    private static ImageIcon ourAnnotationRegionsDarkOutlineIcon;

    /**
     * The icon used for annotation region types.
     */
    private static ImageIcon ourAnnotationRegionsIcon;

    /**
     * The icon used for annotation region types.
     */
    private static ImageIcon ourAnnotationRegionsLightOutlineIcon;

    /** The our features dark outline icon. */
    private static ImageIcon ourFeaturesDarkOutlineIcon;

    /** The points feature icon. */
    private static ImageIcon ourFeaturesIcon;

    /** The our features light outline icon. */
    private static ImageIcon ourFeaturesLightOutlineIcon;

    /** The imagery icon. */
    private static ImageIcon ourImageryIcon;

    /**
     * The motion imagery icon.
     */
    private static ImageIcon ourMotionImageryIcon;

    /**
     * The motion imagery meta data icon.
     */
    private static ImageIcon ourMotionImageryDataIcon;

    /** The terrain icon. */
    private static ImageIcon ourTerrainIcon;

    /** The our tiles dark outline icon. */
    private static ImageIcon ourTilesDarkOutlineIcon;

    /** The tiles icon. */
    private static ImageIcon ourTilesIcon;

    /** The our tiles light outline icon. */
    private static ImageIcon ourTilesLightOutlineIcon;

    /** The tracks icon. */
    private static ImageIcon ourTracksIcon;

    /** The dark outline of the tracks icon. */
    private static ImageIcon ourTracksDarkOutlineIcon;

    /** The light outline of the tracks icon. */
    private static ImageIcon ourTracksLightOutlineIcon;

    /** The heatmaps icon. */
    private static ImageIcon ourHeatmapsIcon;

    /** The light outline of the heatmaps icon. */
    private static ImageIcon ourHeatmapsLightOutlineIcon;

    /** The dark outline of the heatmaps icon. */
    private static ImageIcon ourHeatmapsDarkOutlineIcon;

    /** The unknown type icon. */
    private static ImageIcon ourUnknownTypeIcon;

    /** The Base icon to render icon map. */
    private final Map<ImageIcon, ImageIcon> myBaseIconToRenderIconMap = New.map();

    /** The our outline icon. */
    private ImageIcon myOutlineIcon;

    /** The type. */
    private String myType;

    static
    {
        try
        {
            ourFeaturesIcon = new ImageIcon(ImageIO.read(FeatureTypeLabel.class.getResource("/images/features-base.png")));
            ourFeaturesLightOutlineIcon = new ImageIcon(
                    ImageIO.read(FeatureTypeLabel.class.getResource("/images/features-base-lightoutline.png")));
            ourFeaturesDarkOutlineIcon = new ImageIcon(
                    ImageIO.read(FeatureTypeLabel.class.getResource("/images/features-base-darkoutline.png")));
            ourTilesIcon = new ImageIcon(ImageIO.read(FeatureTypeLabel.class.getResource("/images/tiles-base.png")));
            ourTilesLightOutlineIcon = new ImageIcon(
                    ImageIO.read(FeatureTypeLabel.class.getResource("/images/tiles-base-lightoutline.png")));
            ourTilesDarkOutlineIcon = new ImageIcon(
                    ImageIO.read(FeatureTypeLabel.class.getResource("/images/tiles-base-darkoutline.png")));

            ourTracksIcon = new ImageIcon(ImageIO.read(FeatureTypeLabel.class.getResource("/images/path.png")));
            ourTracksLightOutlineIcon = new ImageIcon(
                    ImageIO.read(FeatureTypeLabel.class.getResource("/images/path-lightoutline.png")));
            ourTracksDarkOutlineIcon = new ImageIcon(
                    ImageIO.read(FeatureTypeLabel.class.getResource("/images/path-darkoutline.png")));

            ourHeatmapsIcon = new ImageIcon(ImageIO.read(FeatureTypeLabel.class.getResource("/images/heatmaps.png")));
            ourHeatmapsLightOutlineIcon = new ImageIcon(
                    ImageIO.read(FeatureTypeLabel.class.getResource("/images/heatmaps-lightoutline.png")));
            ourHeatmapsDarkOutlineIcon = new ImageIcon(
                    ImageIO.read(FeatureTypeLabel.class.getResource("/images/heatmaps-darkoutline.png")));

            ourImageryIcon = new ImageIcon(ImageIO.read(FeatureTypeLabel.class.getResource("/images/imagery.png")));
            ourTerrainIcon = new ImageIcon(ImageIO.read(FeatureTypeLabel.class.getResource("/images/terrain.png")));
            ourAnnotationFeaturesIcon = new ImageIcon(
                    ImageIO.read(FeatureTypeLabel.class.getResource("/images/location16x16.png")));
            ourAnnotationFeaturesLightOutlineIcon = new ImageIcon(
                    ImageIO.read(FeatureTypeLabel.class.getResource("/images/location_lightoutline16x16.png")));
            ourAnnotationFeaturesDarkOutlineIcon = new ImageIcon(
                    ImageIO.read(FeatureTypeLabel.class.getResource("/images/location_darkoutline16x16.png")));
            ourAnnotationRegionsIcon = new ImageIcon(ImageIO.read(FeatureTypeLabel.class.getResource("/images/polygon.png")));
            ourAnnotationRegionsLightOutlineIcon = new ImageIcon(
                    ImageIO.read(FeatureTypeLabel.class.getResource("/images/polygon_lightoutline.png")));
            ourAnnotationRegionsDarkOutlineIcon = new ImageIcon(
                    ImageIO.read(FeatureTypeLabel.class.getResource("/images/polygon_darkoutline.png")));
            ourMotionImageryIcon = new ImageIcon(ImageIO.read(FeatureTypeLabel.class.getResource("/images/film.png")));
            ourMotionImageryDataIcon = new ImageIcon(ImageIO.read(FeatureTypeLabel.class.getResource("/images/mi_metadata.png")));
            ourUnknownTypeIcon = new ImageIcon(ImageIO.read(FeatureTypeLabel.class.getResource("/images/unknownType.png")));
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to load image icons for AddDataLeafNodePanel. " + e);
        }
    }

    /**
     * Instantiates a new feature type label.
     */
    public FeatureTypeLabel()
    {
        super(ourUnknownTypeIcon);
        Dimension size = new Dimension(17, 17);
        setMinimumSize(size);
        setMaximumSize(size);
        setPreferredSize(size);
    }

    /**
     * Instantiates a new feature type label.
     *
     * @param toolbox the toolbox
     */
    public FeatureTypeLabel(Toolbox toolbox)
    {
        this();
        IconLegendRegistry iconReg = toolbox.getUIRegistry().getIconLegendRegistry();
        iconReg.addIconToLegend(IconUtil.getNormalIcon(ourFeaturesIcon), "Feature Layer",
                "Feature layers are highly interactive, allowing users to manipulate and select individual records on the map.");
        iconReg.addIconToLegend(IconUtil.getNormalIcon(ourTilesIcon), "Tile Layer",
                "Tile layers summarize large amounts of geospatial data as non-interactive "
                        + "snapshot images, and they have no additional data associated to them.");
        iconReg.addIconToLegend(IconUtil.getNormalIcon(ourHeatmapsIcon), "Heatmap Layer",
                "Heatmap layers provide a high-level indication of activity concentrations.");
        iconReg.addIconToLegend(IconUtil.getNormalIcon(ourTracksIcon), "Track",
                "Tracks can be created manually, by using existing meta data points, or by using a set of points in the list tool."
                        + "The controls for creating a track are very similar to the ArcLength tool.");
        iconReg.addIconToLegend(IconUtil.getNormalIcon(ourImageryIcon), "Imagery Layer",
                "Imagery layers are images that have been georectified and overlaid on the map at thier corresponding location..");
        iconReg.addIconToLegend(ourTerrainIcon, "Terrain Layer",
                "A terrain layer provides a vertical offset associated with a point "
                        + "on the earth which gives the appearance of height to layers that are overlaid on terrain layers.");
        iconReg.addIconToLegend(IconUtil.getNormalIcon(ourAnnotationFeaturesIcon), "Annotation Features",
                "An annotation feature is a user added point on the map that is highly customizable. "
                        + "These annotations show up in the 'Layers' panel under the 'My Places' category");
        iconReg.addIconToLegend(IconUtil.getNormalIcon(ourAnnotationRegionsIcon), "Annotation Regions",
                "An annotation region is a user created polygon or circle on the map. "
                        + "It can be used for querying data, purging data, and zooming. "
                        + "These annotations show up in the 'Layers' panel under the 'My Places' category");
        iconReg.addIconToLegend(IconUtil.getNormalIcon(ourUnknownTypeIcon), "Unknown Type",
                "This application attempts to identify layer types based on many criteria. "
                        + "If a layer type does not fit any of the know criteria, the layer will be assigned the 'Unknown Type'");
    }

    /**
     * Gets the feature type.
     *
     * @return the feature type
     */
    public String getType()
    {
        return myType;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (myOutlineIcon != null)
        {
            g.drawImage(myOutlineIcon.getImage(), 0, 0, null);
        }
    }

    /**
     * Sets the icon by type.
     *
     * @param typeColor the type color
     * @param type the new icon by type
     */
    public void setIconByType(Color typeColor, MapVisualizationType type)
    {
        ImageIcon anIcon = null;
        boolean isLightOutline = typeColor == null || ColorUtilities.getBrightness(typeColor) < 130;
        myOutlineIcon = null;
        switch (type)
        {
            case POINT_ELEMENTS:
            case ELLIPSE_ELEMENTS:
            case CIRCLE_ELEMENTS:
            case POLYGON_ELEMENTS:
            case ICON_ELEMENTS:
            case MIXED_ELEMENTS:
            case TRACK_ELEMENTS:
            case POLYLINE_ELEMENTS:
            case COMPOUND_FEATURE_ELEMENTS:
            case LOB_ELEMENTS:
            case PLACE_NAME_ELEMENTS:
            case PROCESS_RESULT_ELEMENTS:
                anIcon = ourFeaturesIcon;
                myType = "Feature";
                myOutlineIcon = isLightOutline ? ourFeaturesLightOutlineIcon : ourFeaturesDarkOutlineIcon;
                break;
            case TERRAIN_TILE:
                anIcon = ourTerrainIcon;
                myType = "Terrain";
                break;
            case IMAGE:
                anIcon = ourImageryIcon;
                myType = "Imagery";
                break;
            case IMAGE_TILE:
                anIcon = ourTilesIcon;
                myType = "Tile";
                myOutlineIcon = isLightOutline ? ourTilesLightOutlineIcon : ourTilesDarkOutlineIcon;
                break;
            case INTERPOLATED_IMAGE_TILES:
                anIcon = ourHeatmapsIcon;
                myType = "Heatmap";
                myOutlineIcon = isLightOutline ? ourHeatmapsLightOutlineIcon : ourHeatmapsDarkOutlineIcon;
                break;
            case USER_TRACK_ELEMENTS:
                anIcon = ourTracksIcon;
                myType = "Track";
                myOutlineIcon = isLightOutline ? ourTracksLightOutlineIcon : ourTracksDarkOutlineIcon;
                break;
            case ANNOTATION_POINTS:
                anIcon = ourAnnotationFeaturesIcon;
                myType = "Annotation";
                myOutlineIcon = isLightOutline ? ourAnnotationFeaturesLightOutlineIcon : ourAnnotationFeaturesDarkOutlineIcon;
                break;
            case ANNOTATION_REGIONS:
                anIcon = ourAnnotationRegionsIcon;
                myType = "Annotation";
                myOutlineIcon = isLightOutline ? ourAnnotationRegionsLightOutlineIcon : ourAnnotationRegionsDarkOutlineIcon;
                break;
            case MOTION_IMAGERY:
                anIcon = ourMotionImageryIcon;
                myType = "Motion Imagery";
                break;
            case MOTION_IMAGERY_DATA:
                anIcon = ourMotionImageryDataIcon;
                myType = "Motion Imagery Data";
                break;
            default:
                anIcon = ourUnknownTypeIcon;
                myType = "Unknown";
                break;
        }
        if (typeColor != null && !Color.white.equals(typeColor))
        {
            anIcon = mixColorWithIcon(anIcon, typeColor);
        }
        setIcon(anIcon);
    }

    /**
     * Sets the icon by type.
     *
     * @param dti the new icon by type
     */
    public void setIconByType(DataTypeInfo dti)
    {
        if (dti != null && dti.getMapVisualizationInfo() != null)
        {
            MapVisualizationType type = dti.getMapVisualizationInfo().getVisualizationType();
            setIconByType(dti.getBasicVisualizationInfo().getTypeColor(), type);
        }
        else
        {
            myOutlineIcon = null;
            setIcon(ourUnknownTypeIcon);
            myType = "Unknown";
        }
    }

    /**
     * Mix color with icon.
     *
     * @param anIcon the an icon
     * @param typeColor the type color
     * @return the image icon
     */
    private ImageIcon mixColorWithIcon(ImageIcon anIcon, Color typeColor)
    {
        ImageIcon renderIcon = myBaseIconToRenderIconMap.get(anIcon);
        if (renderIcon == null)
        {
            BufferedImage bi = new BufferedImage(anIcon.getIconWidth(), anIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            renderIcon = new ImageIcon();
            renderIcon.setImage(bi);
            myBaseIconToRenderIconMap.put(anIcon, renderIcon);
        }
        Graphics g = renderIcon.getImage().getGraphics();
        g.setColor(Color.black);
        ((Graphics2D)g).setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, renderIcon.getIconWidth(), renderIcon.getIconHeight());
        g.setColor(typeColor);
        ((Graphics2D)g).setComposite(AlphaComposite.SrcOver);
        g.fillRect(0, 0, renderIcon.getIconWidth(), renderIcon.getIconHeight());
        ((Graphics2D)g).setComposite(AlphaComposite.DstIn);
        g.drawImage(anIcon.getImage(), 0, 0, null);
        return renderIcon;
    }
}
