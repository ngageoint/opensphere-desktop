package io.opensphere.wms.envoy;

import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.wms.capabilities.WMS130Capabilities;
import io.opensphere.wms.capabilities.WMSServerCapabilities;
import io.opensphere.wms.capabilities.WMSTimeUtils;
import io.opensphere.wms.config.v1.WMSBoundingBoxConfig;
import io.opensphere.wms.config.v1.WMSInheritedLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerDisplayConfig;
import io.opensphere.wms.config.v1.WMSLayerGetMapConfig;
import io.opensphere.wms.config.v1.WMSServerConfig;
import net.opengis.wms_130.BoundingBox;
import net.opengis.wms_130.Dimension;
import net.opengis.wms_130.Keyword;
import net.opengis.wms_130.Layer;
import net.opengis.wms_130.Style;

/**
 * Builder for the configuration tree for WMS layers.
 */
public class WMS130LayerTreeBuilder extends AbstractWMSLayerTreeBuilder
{
    /** The LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(WMS130LayerTreeBuilder.class);

    /**
     * Instantiates a new WMS layer tree builder.
     *
     * @param toolbox the toolbox
     * @param conn the server connection information
     * @param prefs the WMS preferences
     */
    public WMS130LayerTreeBuilder(Toolbox toolbox, ServerConnectionParams conn, Preferences prefs)
    {
        super(toolbox, conn, prefs);
    }

    /**
     * Construct the tree based on the capabilities document.
     *
     * @param serverConfig Server configuration.
     * @param caps capabilities document.
     * @param activeListener A listener for DataGroupInfo activation events
     * @return layer hierarchy
     */
    public DataGroupInfo build130Nodes(WMSServerConfig serverConfig, WMSServerCapabilities caps,
            ActivationListener activeListener)
    {
        // Get the top level (root) layer from the Capabilities
        Layer layer = ((WMS130Capabilities)caps).getCapabilities().getCapability().getLayer();
        DataGroupInfo root = null;

        // Make sure that there is actually a layer to select
        if (layer != null && (layer.getName() != null || layer.getLayer() != null && layer.getLayer().size() > 0))
        {
            WMSInheritedLayerConfig topData = buildInheritedConfig(caps, null, null);
            root = buildNode(serverConfig, null, layer, caps, topData, activeListener);
        }

        return root;
    }

    /**
     * Build the node for the given layer.
     *
     * @param serverConfig Stored layer configuration for this server.
     * @param parent The parent node
     * @param layer layer from the capabilities document.
     * @param caps My server's capabilities document.
     * @param parentData Parameters inherited from parent layers
     * @param activeListener A listener for DataGroupInfo activation events
     * @return Newly created node
     */
    private DataGroupInfo buildNode(WMSServerConfig serverConfig, DataGroupInfo parent, Layer layer, WMSServerCapabilities caps,
            WMSInheritedLayerConfig parentData, ActivationListener activeListener)
    {
        WMSLayerConfig defaultConfig = generateDefault130LayerConfig(caps, layer, serverConfig.getServerId(), parentData);

        NodeBuilder builder = new NodeBuilder(layer.getTitle(), layer.getName(), layer.getAbstract(), defaultConfig,
                layer.getCRS(), getKeywords(layer), getStyles(layer));
        builder.setLeaf(layer.getLayer().isEmpty());
        Pair<DataGroupInfo, WMSInheritedLayerConfig> group = buildNode(serverConfig, parent, caps, parentData, activeListener,
                builder);

        // Recursively add child layers
        for (Layer child : layer.getLayer())
        {
            group.getFirstObject().addChild(
                    buildNode(serverConfig, group.getFirstObject(), child, caps, group.getSecondObject(), activeListener), this);
        }
        return group.getFirstObject();
    }

    /**
     * Generate a default configuration for the given layer node.
     *
     * @param wmsCaps The wms capabilities document.
     * @param layer node.
     * @param keyBase the base string used to build the layer key (filename,
     *            url, etc.)
     * @param treeData the parameters inherited from parent layers
     * @return configuration to return
     */
    private WMSLayerConfig generateDefault130LayerConfig(WMSServerCapabilities wmsCaps, Layer layer, String keyBase,
            WMSInheritedLayerConfig treeData)
    {
        WMSLayerConfig cfg = new WMSLayerConfig();
        cfg.setDisplayConfig(new WMSLayerDisplayConfig());
        WMSLayerGetMapConfig getMapConf = new WMSLayerGetMapConfig();
        cfg.setGetMapConfig(getMapConf);

        cfg.setBoundingBoxConfig(getLatLonBoundingBox(layer, treeData));
        cfg.setLayerName(layer.getName());
        cfg.setLayerTitle(layer.getTitle());
        cfg.setLayerKey(keyBase + WMSLayerConfig.LAYERNAME_SEPARATOR + layer.getName());
        cfg.setNoSubsets(layer.isNoSubsets());
        cfg.setQueryable(layer.isQueryable());
        cfg.setTimeExtent(getTimeExtent(layer));
        if (layer.getFixedHeight() != null)
        {
            cfg.setFixedHeight(Integer.valueOf(layer.getFixedHeight().intValue()));
        }
        if (layer.getFixedWidth() != null)
        {
            cfg.setFixedWidth(Integer.valueOf(layer.getFixedWidth().intValue()));
        }

        getMapConf.setExceptions("XML");
        getMapConf.setGetMapURL(wmsCaps.getGetMapURL());
        getMapConf.setImageFormat(getImageFormat(wmsCaps));
        getMapConf.setSRS(getDefaultCRS(layer.getCRS(), treeData));
        getMapConf.setStyleType(WMSLayerGetMapConfig.StyleType.SERVER);
        getMapConf.setStyle("");
        if (!layer.getStyle().isEmpty())
        {
            getMapConf.setDefaultStyle(layer.getStyle().get(0).getName());
        }
        else
        {
            getMapConf.setDefaultStyle("");
        }
        getMapConf.setTextureHeight(Integer.valueOf(512));
        getMapConf.setTextureWidth(Integer.valueOf(512));
        getMapConf.setTransparent(Boolean.TRUE);

        return cfg;
    }

    /**
     * Get the keywords which are present in the layer.
     *
     * @param layer The layer for which the keywords are desired.
     * @return The keywords.
     */
    private List<String> getKeywords(Layer layer)
    {
        List<String> keywords = New.list();
        if (layer.getKeywordList() != null)
        {
            for (Keyword keyword : layer.getKeywordList().getKeyword())
            {
                keywords.add(keyword.getValue());
            }
        }
        return keywords;
    }

    /**
     * Find the bounding box which may be inherited from a parent layer.
     *
     * @param layer layer
     * @param inheritedData parameters inherited from parent layer
     * @return bounding box to return
     */
    private WMSBoundingBoxConfig getLatLonBoundingBox(Layer layer, WMSInheritedLayerConfig inheritedData)
    {
        if (layer == null)
        {
            return null;
        }

        WMSBoundingBoxConfig boxCfg = new WMSBoundingBoxConfig();

        List<BoundingBox> bounds = layer.getBoundingBox();
        if (bounds != null && !bounds.isEmpty() && bounds.get(0) != null)
        {
            BoundingBox box = bounds.get(0);
            if ("EPSG:4326".equals(box.getCRS()))
            {
                // Make x the latitude and y the longitude
                boxCfg.setMinimumLongitude(box.getMiny());
                boxCfg.setMaximumLongitude(box.getMaxy());
                boxCfg.setMinimumLatitude(box.getMinx());
                boxCfg.setMaximumLatitude(box.getMaxx());
            }
            else
            {
                boxCfg.setMinimumLongitude(box.getMinx());
                boxCfg.setMaximumLongitude(box.getMaxx());
                boxCfg.setMinimumLatitude(box.getMiny());
                boxCfg.setMaximumLatitude(box.getMaxy());
            }
        }
        else if (inheritedData.getBoundingBox() != null)
        {
            boxCfg.setGeographicBoundingBox(inheritedData.getBoundingBox());
        }
        else
        {
            boxCfg.setGeographicBoundingBox(GeographicBoundingBox.WHOLE_GLOBE);
        }

        return boxCfg;
    }

    /**
     * Gets the names of each style from the Layer and return them as a list.
     *
     * @param layer the layer whose styles need to be extracted
     * @return the names of the WMS styles from the layer
     */
    private List<String> getStyles(Layer layer)
    {
        List<String> styles = New.list();
        List<Style> layerStyles = layer.getStyle();
        if (CollectionUtilities.hasContent(layerStyles))
        {
            for (Style sty : layerStyles)
            {
                if (sty != null && sty.getName() != null)
                {
                    styles.add(sty.getName());
                }
            }
        }
        return styles;
    }

    /**
     * Gets the time extent.
     *
     * @param layer The layer for which the extent is desired.
     * @return The time extent of the layer.
     */
    private TimeSpan getTimeExtent(Layer layer)
    {
        List<Dimension> dimensions = layer.getDimension();
        for (Dimension dim : dimensions)
        {
            if (dim.getName().equalsIgnoreCase("time"))
            {
                String timeString = dim.getValue();
                try
                {
                    return WMSTimeUtils.parseISOTimeExtent(timeString);
                }
                catch (IllegalArgumentException e)
                {
                    LOGGER.error("Unable to parse time extent for layer " + layer.getName() + " ExtentString[" + timeString + "]",
                            e);
                }
            }
        }
        return TimeSpan.TIMELESS;
    }
}
