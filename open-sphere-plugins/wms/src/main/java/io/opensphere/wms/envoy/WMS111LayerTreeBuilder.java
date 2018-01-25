package io.opensphere.wms.envoy;

import java.util.Collections;
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
import io.opensphere.wms.capabilities.WMS111Capabilities;
import io.opensphere.wms.capabilities.WMSServerCapabilities;
import io.opensphere.wms.capabilities.WMSTimeUtils;
import io.opensphere.wms.config.v1.WMSBoundingBoxConfig;
import io.opensphere.wms.config.v1.WMSInheritedLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerDisplayConfig;
import io.opensphere.wms.config.v1.WMSLayerGetMapConfig;
import io.opensphere.wms.config.v1.WMSServerConfig;
import net.opengis.wms._111.Extent;
import net.opengis.wms._111.Keyword;
import net.opengis.wms._111.LatLonBoundingBox;
import net.opengis.wms._111.Layer;
import net.opengis.wms._111.SRS;
import net.opengis.wms._111.Style;

/**
 * Builder for the configuration tree for WMS layers.
 */
public class WMS111LayerTreeBuilder extends AbstractWMSLayerTreeBuilder
{
    /** The LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(WMS111LayerTreeBuilder.class);

    /**
     * Instantiates a new WMS layer tree builder.
     *
     * @param toolbox the toolbox
     * @param conn the server connection information
     * @param prefs the WMS preferences
     */
    public WMS111LayerTreeBuilder(Toolbox toolbox, ServerConnectionParams conn, Preferences prefs)
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
    public DataGroupInfo build111Nodes(WMSServerConfig serverConfig, WMSServerCapabilities caps,
            ActivationListener activeListener)
    {
        // Get the top level (root) layer from the Capabilities
        Layer layer = ((WMS111Capabilities)caps).getCapabilities().getCapability().getLayer();
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
        WMSLayerConfig defaultConfig = generateDefault111LayerConfig(caps, layer, serverConfig.getServerId(), parentData);

        NodeBuilder builder = new NodeBuilder(layer.getTitle(), layer.getName(), layer.getAbstract(), defaultConfig,
                getCRS(layer), getKeywords(layer), getStyles(layer));
        builder.setLeaf(layer.getLayer().isEmpty());
        Pair<DataGroupInfo, WMSInheritedLayerConfig> pair = buildNode(serverConfig, parent, caps, parentData, activeListener,
                builder);
        DataGroupInfo group = pair.getFirstObject();

        List<Layer> layers = New.list(layer.getLayer());

        // Special logic for Pixia servers
        if ("HiPER LOOK Map".equals(caps.getTitle()))
        {
            Collections.reverse(layers);
            group.setPreserveChildOrder(true);
            if (group.hasChildren())
            {
                DataGroupInfo child = group.getChildren().iterator().next();
                child.setDisplayName("Composite of " + child.getDisplayName(), this);
            }
        }

        // Recursively add child layers
        for (Layer child : layers)
        {
            group.addChild(buildNode(serverConfig, group, child, caps, pair.getSecondObject(), activeListener), this);
        }
        return group;
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
    private WMSLayerConfig generateDefault111LayerConfig(WMSServerCapabilities wmsCaps, Layer layer, String keyBase,
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
        cfg.setNoSubsets("1".equals(layer.getNoSubsets()));
        cfg.setQueryable("1".equals(layer.getQueryable()));
        cfg.setTimeExtent(getTimeExtent(layer));
        if (layer.getFixedHeight() != null)
        {
            cfg.setFixedHeight(Integer.valueOf(layer.getFixedHeight()));
        }
        if (layer.getFixedWidth() != null)
        {
            cfg.setFixedWidth(Integer.valueOf(layer.getFixedWidth()));
        }

        getMapConf.setExceptions("application/vnd.ogc.se_xml");
        getMapConf.setGetMapURL(wmsCaps.getGetMapURL());
        getMapConf.setImageFormat(getImageFormat(wmsCaps));
        getMapConf.setSRS(getDefaultCRS(getCRS(layer), treeData));
        getMapConf.setStyleType(WMSLayerGetMapConfig.StyleType.SERVER);
        if (!layer.getStyle().isEmpty())
        {
            getMapConf.setStyle(layer.getStyle().get(0).getName());
        }
        else
        {
            getMapConf.setStyle("");
        }
        getMapConf.setTextureHeight(Integer.valueOf(512));
        getMapConf.setTextureWidth(Integer.valueOf(512));
        getMapConf.setTransparent(Boolean.TRUE);

        return cfg;
    }

    /**
     * Get the CRS options which are available for the layer.
     *
     * @param layer The layer for which the CRS options are desired.
     * @return The CRS options.
     */
    private List<String> getCRS(Layer layer)
    {
        List<String> crs = New.list();
        if (layer.getSRS() != null)
        {
            for (SRS srs : layer.getSRS())
            {
                crs.add(srs.getvalue());
            }
        }
        return crs;
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
                keywords.add(keyword.getvalue());
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

        LatLonBoundingBox box = layer.getLatLonBoundingBox();

        WMSBoundingBoxConfig boxCfg = new WMSBoundingBoxConfig();
        if (box != null && !isNull(box.getMinx()) && !isNull(box.getMaxx()) && !isNull(box.getMiny()) && !isNull(box.getMaxy()))
        {
            boxCfg.setMinimumLongitude(Double.parseDouble(box.getMinx()));
            boxCfg.setMaximumLongitude(Double.parseDouble(box.getMaxx()));
            boxCfg.setMinimumLatitude(Double.parseDouble(box.getMiny()));
            boxCfg.setMaximumLatitude(Double.parseDouble(box.getMaxy()));
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
        if (CollectionUtilities.hasContent(layer.getStyle()))
        {
            for (Style sty : layer.getStyle())
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
     * @param layer the layer from the GetCapabilities document
     * @return The time extent of the layer.
     */
    private TimeSpan getTimeExtent(Layer layer)
    {
        List<Extent> extList = layer.getExtent();
        for (Extent ext : extList)
        {
            if (ext.getName().equalsIgnoreCase("time"))
            {
                String timeString = ext.getvalue();
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
