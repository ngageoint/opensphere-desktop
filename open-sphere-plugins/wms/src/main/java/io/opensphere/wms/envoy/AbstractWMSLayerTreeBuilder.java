package io.opensphere.wms.envoy;

import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.server.control.DefaultServerDataGroupInfo;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.wms.capabilities.WMSServerCapabilities;
import io.opensphere.wms.config.v1.WMSInheritedLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig.LayerType;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.config.v1.WMSLayerGetMapConfig;
import io.opensphere.wms.config.v1.WMSServerConfig;
import io.opensphere.wms.layer.WMSDataTypeInfo;

/**
 * Builder for the configuration tree for WMS layers.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractWMSLayerTreeBuilder
{
    /** Server connection parameters. */
    private final ServerConnectionParams myConnectionParams;

    /** The WMS preferences. */
    private final Preferences myPrefs;

    /** Toolbox of core utilities. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new WMS layer tree builder.
     *
     * @param toolbox the toolbox
     * @param conn the server connection information
     * @param prefs the WMS preferences
     */
    public AbstractWMSLayerTreeBuilder(Toolbox toolbox, ServerConnectionParams conn, Preferences prefs)
    {
        myToolbox = toolbox;
        myConnectionParams = conn;
        myPrefs = prefs;
    }

    /**
     * Get the connectionParams.
     *
     * @return the connectionParams.
     */
    public ServerConnectionParams getConnectionParams()
    {
        return myConnectionParams;
    }

    /**
     * Get the prefs.
     *
     * @return the prefs.
     */
    public Preferences getPrefs()
    {
        return myPrefs;
    }

    /**
     * Get the toolbox.
     *
     * @return the toolbox.
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Builds an inherited config.
     *
     * @param caps the capabilities document
     * @param parentData the parent data to clone, if available
     * @param displayName the display name
     * @return the new WMS inherited layer config
     */
    protected WMSInheritedLayerConfig buildInheritedConfig(WMSServerCapabilities caps, WMSInheritedLayerConfig parentData,
            String displayName)
    {
        WMSInheritedLayerConfig childData;
        if (parentData != null)
        {
            try
            {
                childData = parentData.clone();
                childData.addLevelToPath(displayName);
            }
            catch (CloneNotSupportedException e)
            {
                childData = buildInheritedConfig(caps, null, displayName);
            }
        }
        else
        {
            childData = new WMSInheritedLayerConfig();
            childData.setExceptionFormats(caps.getExceptionFormats());
            childData.setGetMapFormats(caps.getGetMapFormats());
            childData.addLevelToPath(displayName);
        }
        return childData;
    }

    /**
     * Build the node for the given layer.
     *
     * @param serverConfig Stored layer configuration for this server.
     * @param parent The parent node
     * @param caps My server's capabilities document.
     * @param parentData Parameters inherited from parent layers
     * @param activeListener A listener for DataGroupInfo activation events
     * @param builder The builder which holds the layer specific information.
     * @return Newly created node
     */
    protected Pair<DataGroupInfo, WMSInheritedLayerConfig> buildNode(WMSServerConfig serverConfig, DataGroupInfo parent,
            WMSServerCapabilities caps, WMSInheritedLayerConfig parentData, ActivationListener activeListener,
            NodeBuilder builder)
    {
        WMSInheritedLayerConfig treeData = buildInheritedConfig(caps, parentData, builder.getTitle());
        String layerPath = StringUtilities.join(WMSLayerKey.LAYERNAME_SEPARATOR, treeData.getDirectoryPath());

        DataGroupInfo group = generateDataGroup(parent, layerPath, builder.getTitle());
        WMSLayerConfig defaultConfig = builder.getLayerConfig();
        treeData.setBoundingBox(defaultConfig.getBoundingBoxConfig().getGeographicBoundingBox());
        for (String srs : builder.getCRS())
        {
            treeData.addSrsOption(srs);
        }

        if (!isNull(builder.getName()))
        {
            // If the layer configuration has been saved, use that saved config,
            // but store the default for reference when saving modified
            // configurations.
            WMSLayerConfig config = serverConfig.getLayer(builder.getName());
            if (config == null)
            {
                config = defaultConfig;
            }
            else
            {
                config.setTimeExtent(defaultConfig.getTimeExtent());
                config.setBoundingBoxConfig(defaultConfig.getBoundingBoxConfig());
            }

            if (builder.getKeywords() != null)
            {
                for (String keyword : builder.getKeywords())
                {
                    if ("dted".equalsIgnoreCase(keyword) || "terrain".equalsIgnoreCase(keyword))
                    {
                        config.setLayerType(LayerType.SRTM);
                        config.getGetMapConfig().setImageFormat(MimeType.BIL.getMimeType());
                        break;
                    }
                }
            }

            serverConfig.addDefaultLayer(defaultConfig);
            updateGetMapOverride(config.getGetMapConfig());

            String layerKey = WMSLayerKey.createKey(serverConfig.getServerId(), builder.getName());
            treeData.getStyles().addAll(builder.getStyles());

            DefaultDataTypeInfo wmsInfo = new WMSDataTypeInfo(getToolbox(), getPrefs(), serverConfig.getServerTitle(),
                    new WMSLayerConfigurationSet(serverConfig, config, treeData), layerKey, builder.getTitle());

            if (!isNull(builder.getAbstract()))
            {
                wmsInfo.setDescription(builder.getAbstract());
            }
            wmsInfo.setUrl(serverConfig.getServerId());

            // If this is a folder, add the associated layer as a child,
            // otherwise add layer to the current tree level.
            if (builder.isLeaf())
            {
                group.addMember(wmsInfo, this);
                group.activationProperty().addListener(activeListener);
            }
            else
            {
                DataGroupInfo childGroup = generateDataGroup(group, layerPath, builder.getTitle());
                childGroup.addMember(wmsInfo, this);
                childGroup.activationProperty().addListener(activeListener);
                group.addChild(childGroup, this);
            }
        }
        return new Pair<DataGroupInfo, WMSInheritedLayerConfig>(group, treeData);
    }

    /**
     * Generate a data group info for a layer folder.
     *
     * @param parent the parent data group info
     * @param id the hierarchical path that uniquely identifies the layer/folder
     * @param title the name of the layer/folder for display purposes
     * @return the new created data group info
     */
    protected DataGroupInfo generateDataGroup(DataGroupInfo parent, String id, String title)
    {
        DataGroupInfo group = new DefaultServerDataGroupInfo(false, myToolbox, id);
        group.setDisplayName(title, this);
        if (parent != null)
        {
            group.setParent(parent);
        }
        return group;
    }

    /**
     * Get the SRS from the configuration.
     *
     * @param layerCRS The available CRS options for the layer.
     * @param inheritedData parameters inherited from parent layer
     * @return the SRS String
     */
    protected String getDefaultCRS(List<String> layerCRS, WMSInheritedLayerConfig inheritedData)
    {
        TreeSet<String> srsOptions = new TreeSet<>();
        srsOptions.addAll(layerCRS);
        srsOptions.addAll(inheritedData.getSRSOptions());

        boolean first = true;
        String returnSrs = null;
        for (String option : srsOptions)
        {
            if (first || "EPSG:4326".equals(option))
            {
                returnSrs = option;
            }
            first = false;
        }
        return returnSrs;
    }

    /**
     * Get the image format in the configuration.
     *
     * @param wmsCaps The capabilities.
     * @return The image format.
     */
    protected String getImageFormat(WMSServerCapabilities wmsCaps)
    {
        boolean first = true;
        String format = null;
        for (String fmt : wmsCaps.getGetMapFormats())
        {
            if (first || MimeType.PNG.getMimeType().equals(fmt))
            {
                format = fmt;
            }
            first = false;
        }
        return format;
    }

    /**
     * Checks if a string is null, empty, "null" or "NULL".
     *
     * @param input the input string to check
     * @return true, if string is null
     */
    protected boolean isNull(String input)
    {
        return input == null || input.isEmpty() || "NULL".equalsIgnoreCase(input);
    }

    /**
     * Update the WMS GetMap Override URL.
     *
     * @param getMapConfig the GetMap config
     */
    protected void updateGetMapOverride(WMSLayerGetMapConfig getMapConfig)
    {
        if (getMapConfig == null)
        {
            throw new IllegalArgumentException("Attempted to set URL information in null GetMapConfig");
        }
        if (StringUtils.isEmpty(getMapConfig.getGetMapURLOverride())
                && StringUtils.isNotEmpty(myConnectionParams.getWmsGetMapOverride()))
        {
            getMapConfig.setGetMapURLOverride(myConnectionParams.getWmsGetMapOverride());
        }
    }

    /**
     * A holder for information which is specific to a layer and is required to
     * build a node. This builder allows generic building of a node without the
     * need to know the WMS version of the layer which the node represents.
     */
    protected static class NodeBuilder
    {
        /** The layer abstract. */
        private final String myAbstract;

        /** The available CRS options. */
        private final List<String> myCRS;

        /** The keywords present in the layer. */
        private final List<String> myKeywords;

        /** The layer configuration. */
        private final WMSLayerConfig myLayerConfig;

        /** True when the layer is a leaf. */
        private boolean myLeaf;

        /** The name of the layer. */
        private final String myName;

        /** The available style options. */
        private final List<String> myStyles;

        /** The title of the layer. */
        private final String myTitle;

        /**
         * Constructor.
         *
         * @param title The title of the layer.
         * @param name The name of the layer.
         * @param abst The layer abstract.
         * @param layerConfig The layer configuration.
         * @param crs The available CRS options.
         * @param keywords The keywords present in the layer.
         * @param styles The available style options.
         */
        public NodeBuilder(String title, String name, String abst, WMSLayerConfig layerConfig, List<String> crs,
                List<String> keywords, List<String> styles)
        {
            myTitle = title;
            myName = name;
            myAbstract = abst;
            myLayerConfig = layerConfig;

            myCRS = New.unmodifiableList(crs);
            myKeywords = New.unmodifiableList(keywords);
            myStyles = New.unmodifiableList(styles);
        }

        /**
         * Get the abstract.
         *
         * @return the abstract.
         */
        public String getAbstract()
        {
            return myAbstract;
        }

        /**
         * Get the cRS.
         *
         * @return the cRS.
         */
        public List<String> getCRS()
        {
            return myCRS;
        }

        /**
         * Get the keywords.
         *
         * @return the keywords.
         */
        public List<String> getKeywords()
        {
            return myKeywords;
        }

        /**
         * Get the layerConfig.
         *
         * @return the layerConfig.
         */
        public WMSLayerConfig getLayerConfig()
        {
            return myLayerConfig;
        }

        /**
         * Get the name.
         *
         * @return the name.
         */
        public String getName()
        {
            return myName;
        }

        /**
         * Get the styles.
         *
         * @return the styles.
         */
        public List<String> getStyles()
        {
            return myStyles;
        }

        /**
         * Get the title.
         *
         * @return the title.
         */
        public String getTitle()
        {
            return myTitle;
        }

        /**
         * Tell whether the layer is a leaf.
         *
         * @return true when the layer is a leaf.
         */
        public boolean isLeaf()
        {
            return myLeaf;
        }

        /**
         * Set the leaf.
         *
         * @param leaf the leaf to set
         */
        public void setLeaf(boolean leaf)
        {
            myLeaf = leaf;
        }
    }
}
