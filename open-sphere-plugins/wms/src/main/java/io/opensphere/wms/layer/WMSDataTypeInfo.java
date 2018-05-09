package io.opensphere.wms.layer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import javax.swing.JDialog;

import org.apache.log4j.Logger;

import gnu.trove.procedure.TObjectIntProcedure;
import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.ParentTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.order.ParticipantOrderChangeEvent;
import io.opensphere.core.order.ParticipantOrderChangeEvent.ParticipantChangeType;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.TimeExtents;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMapTileVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultTileLevelController;
import io.opensphere.mantle.data.impl.DefaultTimeExtents;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.services.AbstractServerDataTypeInfo;
import io.opensphere.server.services.ServerDataTypeSync;
import io.opensphere.server.services.ServerDataTypeSync.ServerSyncChangeEvent.SyncChangeType;
import io.opensphere.server.services.ServerMapVisualizationInfo;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig.LayerType;
import io.opensphere.wms.config.v1.WMSLayerConfigurationSet;
import io.opensphere.wms.display.WMSLayerConfigPanel;

/**
 * The Class WMSDataTypeInfo.
 */
@SuppressWarnings("PMD.GodClass")
public class WMSDataTypeInfo extends AbstractServerDataTypeInfo implements WMSDataType
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(WMSDataTypeInfo.class);

    /**
     * My Enabled flag. Determines whether a layer is configured to appear
     * anywhere outside the WMS Plugin. This is different from
     * DefaultDataTypeInfo:isVisible which just turns a layer's tiles on/off
     * visually on the map.
     */
    private boolean myEnabled;

    /** The Mantle toolbox used for access to the Z-Order manager. */
    private final MantleToolbox myMantleTb;

    /** Listener for changes to the order of WMS layers. */
    private final OrderChangeListener myOrderChangeListener = new OrderChangeListener()
    {
        @Override
        public void orderChanged(ParticipantOrderChangeEvent event)
        {
            if (event.getChangeType() == ParticipantChangeType.ORDER_CHANGED)
            {
                event.getChangedParticipants().forEachEntry(new TObjectIntProcedure<OrderParticipantKey>()
                {
                    @Override
                    public boolean execute(OrderParticipantKey participant, int order)
                    {
                        if (participant.equals(getOrderKey()) && getMapVisualizationInfo() != null)
                        {
                            getMapVisualizationInfo().setZOrder(order, null);
                        }
                        return true;
                    }
                });
            }
        }
    };

    /** The WMS preferences. */
    private final transient Preferences myPrefs;

    /** My WMS Layer Config. */
    private transient WMSLayerConfigurationSet myWmsConfig;

    /** The WMS config panel. */
    private WMSLayerConfigPanel myWMSConfigPanel;

    /**
     * Constructor.
     *
     * @param tb the Core Toolbox
     * @param prefs the WMS preferences
     * @param serverName the server name
     * @param wmsConfig the WMS Layer config
     * @param layerKey the full path to the layer
     * @param displayName the Layer's display name (a.k.a. Title)
     */
    public WMSDataTypeInfo(Toolbox tb, Preferences prefs, String serverName, WMSLayerConfigurationSet wmsConfig, String layerKey,
            String displayName)
    {
        super(tb, serverName, layerKey, wmsConfig.getLayerConfig().getLayerName(), displayName, false);
        myMantleTb = tb.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        myWmsConfig = wmsConfig;
        myPrefs = prefs;
        initialize(wmsConfig.getLayerConfig());
        setAssistant(new WMSDataTypeInfoAssistant(prefs));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultDataTypeInfo other = (DefaultDataTypeInfo)obj;
        return Objects.equals(getTypeKey(), other.getTypeKey());
    }

    @Override
    public Collection<String> getNamesForComparison()
    {
        Set<String> nameSet = New.set();
        nameSet.add(ServerToolboxUtils.formatNameForComparison(getTypeName()));
        nameSet.add(ServerToolboxUtils.formatNameForComparison(getDisplayName()));
        return nameSet;
    }

    @Override
    public Preferences getPrefs()
    {
        return myPrefs;
    }

    @Override
    public WMSLayerConfigurationSet getWmsConfig()
    {
        return myWmsConfig;
    }

    @Override
    public void handleSyncChangeEvent(SyncChangeType type, Object source)
    {
        if (Utilities.notSameInstance(this, source))
        {
            switch (type)
            {
                case SYNC_HAS_DATA:
                    if (getTypeSync().isHasData())
                    {
                        if (!getBasicVisualizationInfo().getLoadsTo().equals(LoadsTo.TIMELINE))
                        {
                            getBasicVisualizationInfo().setLoadsTo(LoadsTo.STATIC, source);
                        }
                        reinitializeTileRenderProperties(source);
                    }
                    break;
                case SYNC_TIME_EXTENT:
                    TimeExtents timeExtent = getTypeSync().getTimeExtents();
                    if (timeExtent != null && CollectionUtilities.hasContent(timeExtent.getTimespans())
                            && timeExtent.getTimespans().get(0) != TimeSpan.TIMELESS)
                    {
                        setTimeExtents(timeExtent, false);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (getTypeKey() == null ? 0 : getTypeKey().hashCode());
        return result;
    }

    /**
     * Checks if this layer is enabled.
     *
     * @return true, if is enabled
     */
    public boolean isEnabled()
    {
        return myEnabled;
    }

    /**
     * Sets the enabled flag.
     *
     * @param enabled the new enabled flag
     */
    public void setEnabled(boolean enabled)
    {
        myEnabled = enabled;
    }

    @Override
    public void setTypeSync(ServerDataTypeSync typeSync)
    {
        super.setTypeSync(typeSync);
        typeSync.setHasMapTiles(true, this);
        if (getBasicVisualizationInfo().getDefaultTypeColor() != DEFAULT_TYPE_COLOR)
        {
            typeSync.setDefaultColor(getBasicVisualizationInfo().getDefaultTypeColor(), this);
        }
        if (typeSync.isHasData())
        {
            if (!getBasicVisualizationInfo().getLoadsTo().equals(LoadsTo.TIMELINE))
            {
                getBasicVisualizationInfo().setLoadsTo(LoadsTo.STATIC, this);
            }
            reinitializeTileRenderProperties(this);
        }
        if (getTimeExtents() != null && CollectionUtilities.hasContent(getTimeExtents().getTimespans())
                && getTimeExtents().getTimespans().get(0) != TimeSpan.TIMELESS)
        {
            typeSync.setTimeExtents(getTimeExtents(), this);
        }
        else
        {
            TimeExtents timeExtent = getTypeSync().getTimeExtents();
            if (timeExtent != null && CollectionUtilities.hasContent(timeExtent.getTimespans())
                    && timeExtent.getTimespans().get(0) != TimeSpan.TIMELESS)
            {
                setTimeExtents(timeExtent, false);
            }
        }
    }

    /**
     * Set the wmsConfig. In addition to the changed parameters in the config
     * set, also set some DataTypeInfo specific things, as appropriate.
     *
     * @param wmsConfigSet the {@link WMSLayerConfigurationSet} to set
     */
    public void setWmsConfig(WMSLayerConfigurationSet wmsConfigSet)
    {
        myWmsConfig = wmsConfigSet;

        // Set the MapVisualization type
        WMSLayerConfig wmsConfig = wmsConfigSet.getLayerConfig();

        if (getOrderKey() != null)
        {
            removeZOrderParticipant(getOrderKey());
        }

        MapVisualizationType mvt = getVisualizationType(wmsConfig.getLayerType());
        OrderParticipantKey orderKey;
        if (mvt == MapVisualizationType.TERRAIN_TILE)
        {
            orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY,
                    DefaultOrderCategory.EARTH_ELEVATION_CATEGORY, getTypeKey());
        }
        else
        {
            if (wmsConfig.getTimeExtent() == TimeSpan.TIMELESS)
            {
                orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                        DefaultOrderCategory.IMAGE_BASE_MAP_CATEGORY, getTypeKey());
            }
            else
            {
                orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                        DefaultOrderCategory.IMAGE_DATA_CATEGORY, getTypeKey());
            }
        }

        setOrderKey(orderKey);
        int zorder = addZOrderParticipant(getOrderKey());

        TileRenderProperties props = getMapVisualizationInfo().getTileRenderProperties();

        props.setOpacity(getOpacityForLayerFromPrefs());
        ServerMapVisualizationInfo mapInfo = new ServerMapVisualizationInfo(mvt, props);
        mapInfo.setDataTypeInfo(this);
        mapInfo.setTileLevelController(new DefaultTileLevelController());
        setMapVisualizationInfo(mapInfo);

        mapInfo.setZOrder(zorder, this);
    }

    /**
     * Show configuration panel.
     *
     * @param parent The component to use for setting the relative position of
     *            the configuration dialog.
     */
    public void showConfig(Window parent)
    {
        JDialog di = new JDialog(parent);
        di.setTitle(StringUtilities.concat(myWmsConfig.getLayerConfig().getLayerTitle(), " Settings"));
        di.setLocationRelativeTo(parent);

        if (myWMSConfigPanel == null)
        {
            myWMSConfigPanel = new WMSLayerConfigPanel(getToolbox(), di, myWmsConfig, myPrefs);
            di.setLayout(new BorderLayout());
            di.setContentPane(myWMSConfigPanel);

            Dimension size = new Dimension(450, 320);
            di.setSize(size);
            di.setPreferredSize(size);
            di.setMinimumSize(size);
            di.setResizable(false);
            di.setVisible(true);

            // These 2 event listeners will handle cleaning up the config panel
            // if the cancel button is pressed or the 'X' button on the dialog
            // is pressed.
            di.addComponentListener(new ComponentAdapter()
            {
                @Override
                public void componentHidden(ComponentEvent componentevent)
                {
                    myWMSConfigPanel = null;
                }
            });
            myWMSConfigPanel.addDialogClosedListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    if (Utilities.sameInstance(evt.getSource(), myWMSConfigPanel))
                    {
                        myWMSConfigPanel = null;
                    }
                }
            });
        }
    }

    /**
     * Adds this layer as a participant in the z-order {@link OrderManager} and
     * return its corresponding z-order. Also, adds a change listener to the
     * {@link OrderManager}.
     *
     * @param layerKey the key that uniquely identifies this layer to the
     *            {@link OrderManager}
     * @return the z-order for the newly added layer
     */
    private int addZOrderParticipant(OrderParticipantKey layerKey)
    {
        OrderManager manager = getToolbox().getOrderManagerRegistry().getOrderManager(layerKey);
        int zorder = manager.activateParticipant(layerKey);
        manager.addParticipantChangeListener(myOrderChangeListener);
        return zorder;
    }

    /**
     * Attempts to translate a WMS style into a color.
     *
     * @param style the style from the WMS Capabilities
     * @return the color as interpreted from the style
     */
    private Color getColorFromStyle(String style)
    {
        Color hexColor = null;
        if (style != null && !style.isEmpty())
        {
            int indexOfX = style.indexOf('x');
            if (indexOfX == 1)
            {
                String hexString = style.substring(2).trim();
                try
                {
                    hexColor = new Color((int)Long.parseLong(hexString, 16));
                }
                catch (NumberFormatException e)
                {
                    LOGGER.warn("Encountered invalid hex color : " + hexString);
                    // if there was an error determining the color, use red.
                    hexColor = new Color(255, 0, 0);
                }
            }
        }
        return hexColor;
    }

    /**
     * Gets the opacity for layer from prefs.
     *
     * @return the opacity for layer from prefs
     */
    private float getOpacityForLayerFromPrefs()
    {
        final int maxOpacity = 255;
        int opacity = MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataTypeInfoPreferenceAssistant()
                .getOpacityPreference(getTypeKey(), maxOpacity);
        return (float)opacity / (float)maxOpacity;
    }

    /**
     * Populate this class from a WMSLayerConfig.
     *
     * @param wmsConfig the WMS config
     */
    private void initialize(WMSLayerConfig wmsConfig)
    {
        MapVisualizationType mvt = getVisualizationType(wmsConfig.getLayerType());
        LoadsTo layerType;
        OrderParticipantKey orderKey;

        if (mvt == MapVisualizationType.TERRAIN_TILE)
        {
            layerType = LoadsTo.BASE;
            orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY,
                    DefaultOrderCategory.EARTH_ELEVATION_CATEGORY, getTypeKey());
        }
        else
        {
            if (wmsConfig.getTimeExtent() == TimeSpan.TIMELESS)
            {
                layerType = LoadsTo.BASE;
                orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                        DefaultOrderCategory.IMAGE_BASE_MAP_CATEGORY, getTypeKey());
            }
            else
            {
                layerType = LoadsTo.TIMELINE;
                orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                        DefaultOrderCategory.IMAGE_DATA_CATEGORY, getTypeKey());
            }
        }

        setOrderKey(orderKey);
        Color defaultColor = getColorFromStyle(wmsConfig.getGetMapConfig().getDefaultStyle());
        defaultColor = defaultColor != null ? defaultColor : DEFAULT_TYPE_COLOR;
        BasicVisualizationInfo basicInfo = new DefaultBasicVisualizationInfo(layerType, defaultColor, false);
        setBasicVisualizationInfo(basicInfo);

        // Set the keywords in the DataTypeInfo to the names of directories in
        // this type's hierarchy. Ignore server name and layer name in path.
        if (myWmsConfig.getInheritedLayerConfig() != null)
        {
            Set<String> keywords = New.set(myWmsConfig.getInheritedLayerConfig().getDirectoryPath());
            keywords.remove(myWmsConfig.getServerConfig().getServerTitle());
            keywords.remove(wmsConfig.getLayerTitle());
            for (String key : keywords)
            {
                addTag(key, this);
            }
        }

        TileRenderProperties props;
        String layerKey = getTypeKey();
        if (layerKey != null && !layerKey.isEmpty() && myMantleTb != null)
        {
            int zorder = addZOrderParticipant(getOrderKey());
            props = new DefaultTileRenderProperties(zorder, true, false);
        }
        else
        {
            props = new DefaultTileRenderProperties(0, true, false);
        }
        props.setOpacity(getOpacityForLayerFromPrefs());

        ServerMapVisualizationInfo mapInfo = new ServerMapVisualizationInfo(mvt, new ParentTileRenderProperties(props));
        mapInfo.setDataTypeInfo(this);
        setMapVisualizationInfo(mapInfo);

        mapInfo.setTileLevelController(new DefaultTileLevelController());

        setTimeExtents(new DefaultTimeExtents(wmsConfig.getTimeExtent()), this);
    }

    /**
     * Reinitialize tile render properties.
     *
     * @param source the source
     */
    private void reinitializeTileRenderProperties(Object source)
    {
        TileRenderProperties props;
        String layerKey = getTypeKey();
        if (layerKey != null && !layerKey.isEmpty() && getToolbox() != null)
        {
            removeZOrderParticipant(getOrderKey());
            OrderParticipantKey orderKey;
            if (getMapVisualizationInfo().getVisualizationType() == MapVisualizationType.TERRAIN_TILE)
            {
                orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY,
                        DefaultOrderCategory.EARTH_ELEVATION_CATEGORY, getTypeKey());
            }
            else
            {
                if (getBasicVisualizationInfo().getLoadsTo() == LoadsTo.BASE)
                {
                    orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                            DefaultOrderCategory.IMAGE_BASE_MAP_CATEGORY, getTypeKey());
                }
                else
                {
                    orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                            DefaultOrderCategory.IMAGE_DATA_CATEGORY, getTypeKey());
                }
            }
            setOrderKey(orderKey);
            int zorder = addZOrderParticipant(getOrderKey());

            props = new DefaultTileRenderProperties(zorder, true, false);
        }
        else
        {
            props = new DefaultTileRenderProperties(0, true, false);
        }
        props.setOpacity(getOpacityForLayerFromPrefs());
        ((DefaultMapTileVisualizationInfo)getMapVisualizationInfo())
                .setTileRenderProperties(new ParentTileRenderProperties(props), source);
    }

    /**
     * Removes this layer as a participant in the z-order {@link OrderManager}.
     * Also, removes the change listener from the {@link OrderManager}.
     *
     * @param layerKey the key that uniquely identifies this layer to the
     *            {@link OrderManager}
     */
    private void removeZOrderParticipant(OrderParticipantKey layerKey)
    {
        OrderManager manager = getToolbox().getOrderManagerRegistry().getOrderManager(layerKey);
        manager.deactivateParticipant(layerKey);
        manager.removeParticipantChangeListener(myOrderChangeListener);
    }

    /**
     * Gets the MapVisualizationType for the LayerType.
     *
     * @param layerType the LayerType
     * @return the MapVisualizationType
     */
    private static MapVisualizationType getVisualizationType(LayerType layerType)
    {
        return layerType != null ? layerType.getMapVisualizationType() : MapVisualizationType.IMAGE_TILE;
    }
}
