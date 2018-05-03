package io.opensphere.wfs.layer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.TimeExtents;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupActivator;
import io.opensphere.mantle.data.impl.specialkey.EndTimeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.mantle.util.TextViewDialog;
import io.opensphere.server.services.AbstractServerDataTypeInfo;
import io.opensphere.server.services.ServerBasicVisualizationInfo;
import io.opensphere.server.services.ServerDataTypeSync;
import io.opensphere.server.services.ServerDataTypeSync.ServerSyncChangeEvent.SyncChangeType;
import io.opensphere.server.toolbox.LayerConfiguration;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.util.OGCOutputFormat;
import io.opensphere.wfs.layer.SingleLayerRequeryEvent.RequeryType;
import io.opensphere.wfs.layer.TimeColumnChooserDialog.TimeColumns;
import io.opensphere.wfs.util.WFSConstants;

/**
 * Model for a WFS type.
 */
@SuppressWarnings("PMD.GodClass")
public class WFSDataType extends AbstractServerDataTypeInfo
{
    /** The Constant PROPERTY_DESCRIPTOR. */
    public static final PropertyDescriptor<WFSDataType> WFS_PROPERTY_DESCRIPTOR = new PropertyDescriptor<>("value",
            WFSDataType.class);

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(WFSDataType.class);

    /**
     * An activation listener, which (if not null) is added to the parent when the parent is established.
     */
    private ActivationListener myActivationListener;

    /**
     * This flag indicates that a source should not be changed during animation.
     */
    private boolean myIsAnimationSensitive;

    /**
     * Flag that's true if position order in points has latitude before longitude.
     */
    private boolean myIsLatBeforeLon;

    /** Whether the time column(s) have been chosen. */
    private boolean myIsTimeColumnChosen;

    /** Flag that's true when time column is set for Whole-world WFS layers. */
    private boolean myIsWFSOnlyWithTime;

    /** Server output format. */
    private OGCOutputFormat myOutputFormat;

    /** The server type. */
    private final LayerConfiguration myStateConfiguration;

    /** The WFS config dialog. */
    private WFSColumnConfigPanel myWFSConfigDialog;

    /** The WFS version. */
    private String myWFSVersion;

    /**
     * Construct a WFS type. By default, all WFS data types can be filtered.
     *
     * @param tb the Core Toolbox
     * @param serverName the server name
     * @param layerKey a full path to the layer that uniquely identifies it across all servers.
     * @param name The typename that uniquely identifies this layer on its host server.
     * @param title the displayable name for this layer
     * @param properties The properties list (i.e. column list) for this type
     * @param stateConfiguration The datatype's state-related configuration.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public WFSDataType(Toolbox tb, String serverName, String layerKey, String name, String title, MetaDataInfo properties,
            LayerConfiguration stateConfiguration)
    {
        this(tb, serverName, layerKey, name, title, stateConfiguration, true);
        if (properties == null)
        {
            throw new IllegalArgumentException("properties cannot be null");
        }
        setMantleProps(properties);
    }

    /**
     * Construct a WFS type, configured with the supplied parameters. This constructor implementation permits extended subclasses
     * to create a WFS datatype that can not be filtered.
     *
     * @param pToolbox the Core Toolbox through which application interaction occurs.
     * @param pServerName the server name
     * @param pLayerKey a full path to the layer that uniquely identifies it across all servers.
     * @param pLayerTypeName The typename that uniquely identifies this layer on its host server.
     * @param pDisplayTitle the user-facing name for this layer.
     * @param pStateConfiguration The datatype's state-related configuration.
     * @param pFiltersData True if the data type can be filtered, false otherwise.
     */
    protected WFSDataType(Toolbox pToolbox, String pServerName, String pLayerKey, String pLayerTypeName, String pDisplayTitle,
            LayerConfiguration pStateConfiguration, boolean pFiltersData)
    {
        super(pToolbox, pServerName, pLayerKey, pLayerTypeName, pDisplayTitle, pFiltersData);
        if (pLayerTypeName == null)
        {
            throw new IllegalArgumentException("name cannot be null");
        }
        myStateConfiguration = pStateConfiguration;
    }

    @Override
    public void changeTimeColumns()
    {
        // This is called from the button in layer details
        if (chooseTimeColumns(this))
        {
            MantleToolbox mantleTb = getToolbox().getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
            if (mantleTb.getDataGroupController().isTypeActive(this))
            {
                Integer result = EventQueueUtilities.happyOnEdt(() ->
                {
                    Component parent = getToolbox().getUIRegistry().getMainFrameProvider().get();
                    int option = JOptionPane.showConfirmDialog(parent,
                            "This layer will be reactivated in order make changes. Continue?", "Reactivate Layer?",
                            JOptionPane.YES_NO_OPTION);
                    return Integer.valueOf(option);
                });
                if (result.intValue() == JOptionPane.YES_OPTION)
                {
                    try
                    {
                        new DefaultDataGroupActivator(getToolbox().getEventManager()).reactivateGroup(getParent());
                    }
                    catch (InterruptedException e)
                    {
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug(e, e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Displays a UI to let the user choose time columns for this data type.
     *
     * @param source the object making the change
     * @return Whether time columns were chosen by the user
     */
    public boolean chooseTimeColumns(Object source)
    {
        boolean chosen = false;
        final List<String> dateKeys = getMetaDataInfo() == null ? null : ((WFSMetaDataInfo)getMetaDataInfo()).getDateKeys();
        if (CollectionUtilities.hasContent(dateKeys))
        {
            TimeColumns result = EventQueueUtilities.happyOnEdt(() ->
            {
                TimeColumns timeColumns = guessTimeColumns(dateKeys);
                Component parent = getToolbox().getUIRegistry().getMainFrameProvider().get();
                return TimeColumnChooserDialog.showDialog(dateKeys, timeColumns.getStartTimeColumn(),
                        timeColumns.getEndTimeColumn(), getDisplayName(), parent);
            });

            if (StringUtils.isNotEmpty(result.getStartTimeColumn()))
            {
                Preferences prefs = getToolbox().getPreferencesRegistry().getPreferences(WFSDataType.class);
                prefs.putString(getStartTimeColumnPreferenceKey(), result.getStartTimeColumn(), this);
                if (result.getEndTimeColumn() == null)
                {
                    prefs.remove(getEndTimeColumnPreferenceKey(), this);
                }
                else
                {
                    prefs.putString(getEndTimeColumnPreferenceKey(), result.getEndTimeColumn(), this);
                }
                setTimeColumns(result, source);
                chosen = true;
            }
        }
        return chosen;
    }

    @Override
    @SuppressWarnings("PMD.UselessOverridingMethod")
    public boolean equals(Object obj)
    {
        // Local variables do not affect equality in this case.
        return super.equals(obj);
    }

    @Override
    public Collection<String> getNamesForComparison()
    {
        Collection<String> names = New.set();
        names.add(ServerToolboxUtils.formatNameForComparison(getTypeName()));
        names.add(ServerToolboxUtils.formatNameForComparison(getDisplayName()));
        return names;
    }

    @Override
    public OrderParticipantKey getOrderKey()
    {
        if (super.getOrderKey() == null)
        {
            setOrderKey(new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY,
                    DefaultOrderCategory.FEATURE_CATEGORY, getTypeKey()));
        }
        return super.getOrderKey();
    }

    /**
     * Gets the proper output format for requests to the server.
     *
     * @return the server output format
     */
    public OGCOutputFormat getOutputFormat()
    {
        return myOutputFormat;
    }

    /**
     * Accessor for the properties list.
     *
     * @return The properties
     */
    public Map<String, Class<?>> getProperties()
    {
        if (getMetaDataInfo() != null)
        {
            return getMetaDataInfo().getKeyClassTypeMap();
        }
        return Collections.emptyMap();
    }

    /**
     * Gets the value of the {@link #myStateConfiguration} field.
     *
     * @return the value stored in the {@link #myStateConfiguration} field.
     */
    public LayerConfiguration getStateConfiguration()
    {
        return myStateConfiguration;
    }

    /**
     * Gets the wFS version.
     *
     * @return the wFS version
     */
    public String getWFSVersion()
    {
        return myWFSVersion;
    }

    @Override
    public void handleSyncChangeEvent(SyncChangeType type, Object source)
    {
        if (Utilities.notSameInstance(this, source))
        {
            switch (type)
            {
                case SYNC_COLOR:
                    // Just sync RGB color, Opacity is handled independently
                    setDefaultTypeColor(ColorUtilities.opacitizeColor(getTypeSync().getDefaultColor(), 1.0f));
                    break;
                case SYNC_TIME_EXTENT:
                    TimeExtents timeExtent = getTypeSync().getTimeExtents();
                    if (timeExtent != null && CollectionUtilities.hasContent(timeExtent.getTimespans())
                            && timeExtent.getTimespans().get(0) != TimeSpan.TIMELESS)
                    {
                        setTimeExtents(timeExtent, false);
                        if (getBasicVisualizationInfo().getLoadsTo() != LoadsTo.TIMELINE)
                        {
                            getBasicVisualizationInfo().setLoadsTo(LoadsTo.TIMELINE, source);
                            getBasicVisualizationInfo()
                                    .setSupportedLoadsToTypes(DefaultBasicVisualizationInfo.LOADS_TO_TIMELINE_ONLY);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    @SuppressWarnings("PMD.UselessOverridingMethod")
    public int hashCode()
    {
        // Local variables should not be factored into this hashCode
        return super.hashCode();
    }

    /**
     * Checks if is animation sensitive.
     *
     * @return true, if is animation sensitive
     */
    public boolean isAnimationSensitive()
    {
        return myIsAnimationSensitive;
    }

    @Override
    // from "Model" interface
    public boolean isDisplayable()
    {
        return isVisible();
    }

    /**
     * Gets the position ordering in points returned from the server.
     *
     * @return true if position order is Lat/Lon, false if Lon/Lat.
     */
    public boolean isLatBeforeLon()
    {
        return myIsLatBeforeLon;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.impl.DefaultDataTypeInfo#isTimeColumnChangeable()
     */
    @Override
    public boolean isTimeColumnChangeable()
    {
        int timeColumnCount = getMetaDataInfo() != null ? ((WFSMetaDataInfo)getMetaDataInfo()).getDateKeys().size() : 0;

        return isQueryable() && timeColumnCount >= 2 && getStateConfiguration().isTimeColumnChangeable();
    }

    /**
     * Returns whether the time column(s) have been chosen.
     *
     * @return Whether the time column(s) have been chosen
     */
    public boolean isTimeColumnChosen()
    {
        return myIsTimeColumnChosen;
    }

    /**
     * Checks whether this layer is timeless.
     *
     * @return true, if is timeless
     */
    public boolean isTimeless()
    {
        return !getBasicVisualizationInfo().getLoadsTo().isTimelineEnabled();
    }

    /**
     * Set the activation listener.
     *
     * @param activationListener The activation listener.
     */
    public void setActivationListener(ActivationListener activationListener)
    {
        if (myActivationListener != null || getParent() != null)
        {
            throw new IllegalStateException();
        }
        myActivationListener = activationListener;
    }

    /**
     * Sets the animation sensitive.
     *
     * @param isAnimationSensitive the new animation sensitive
     */
    public void setAnimationSensitive(boolean isAnimationSensitive)
    {
        myIsAnimationSensitive = isAnimationSensitive;
    }

    @Override
    // from "Model" interface
    public void setDisplayable(boolean displayable, Object source)
    {
        setVisible(displayable, source);
    }

    /**
     * Sets the position ordering in points to "lat before lon" if true.
     *
     * @param isLatBeforeLon the new lat/lon ordering flag
     */
    public void setLatBeforeLon(boolean isLatBeforeLon)
    {
        myIsLatBeforeLon = isLatBeforeLon;
    }

    /**
     * Sets the output format to use for requests the server for this layer.
     *
     * @param format the preferred output format
     */
    public void setOutputFormat(OGCOutputFormat format)
    {
        myOutputFormat = format;
    }

    @Override
    public void setParent(DataGroupInfo parent)
    {
        super.setParent(parent);
        if (myActivationListener != null && parent != null)
        {
            parent.activationProperty().addListener(myActivationListener);
        }
    }

    /**
     * Check to see if the time columns have been set previously, and if so, set them again.
     *
     * @param source The source of the change.
     * @return {@code true} if the time columns were set.
     */
    public boolean setPreferredTimeColumns(Object source)
    {
        final List<String> dateKeys = getMetaDataInfo() == null ? null : ((WFSMetaDataInfo)getMetaDataInfo()).getDateKeys();

        if (dateKeys != null)
        {
            Preferences prefs = getToolbox().getPreferencesRegistry().getPreferences(WFSDataType.class);
            String startTimeColumn = prefs.getString(getStartTimeColumnPreferenceKey(), null);
            String endTimeColumn = prefs.getString(getEndTimeColumnPreferenceKey(), null);
            if (startTimeColumn != null && dateKeys.contains(startTimeColumn)
                    && (endTimeColumn == null || dateKeys.contains(endTimeColumn)))
            {
                setTimeColumns(new TimeColumns(startTimeColumn, endTimeColumn), source);
                return true;
            }
        }
        return false;
    }

    /**
     * Set the time columns for this type.
     *
     * @param columns The time columns.
     * @param source The source of the change.
     */
    public void setTimeColumns(TimeColumns columns, Object source)
    {
        if (!isTimeColumnChangeable())
        {
            throw new IllegalStateException("Cannot set time columns: time columns are not changeable.");
        }
        getMetaDataInfo().removeSpecialKey(TimeKey.DEFAULT, source);
        getMetaDataInfo().removeSpecialKey(EndTimeKey.DEFAULT, source);

        getMetaDataInfo().setSpecialKey(columns.getStartTimeColumn(), TimeKey.DEFAULT, source);
        if (StringUtils.isNotEmpty(columns.getEndTimeColumn()))
        {
            getMetaDataInfo().setSpecialKey(columns.getEndTimeColumn(), EndTimeKey.DEFAULT, source);
        }

        myIsTimeColumnChosen = true;
    }

    @Override
    public void setTimeExtents(TimeExtents extents, boolean syncTime)
    {
        TimeExtents oldExtents = getTimeExtents();
        super.setTimeExtents(extents, syncTime);
        if (extents != null && !extents.getTimespans().isEmpty())
        {
            // If the new extents are just an extension/update of the existing
            // extents, send a re-query event to get updated features.
            if (oldExtents != null && !oldExtents.getTimespans().isEmpty()
                    && extents.getExtent().compareEnd(oldExtents.getExtent().getEnd()) > 0)
            {
                SingleLayerRequeryEvent event = new SingleLayerRequeryEvent(this, RequeryType.FULL_REQUERY);
                getToolbox().getEventManager().publishEvent(event);
            }
        }

        if (getMetaDataInfo() instanceof WFSMetaDataInfo && getMetaDataInfo().getTimeKey() == null)
        {
            WFSMetaDataInfo wfsMDI = (WFSMetaDataInfo)getMetaDataInfo();
            if (wfsMDI.isDynamicTime())
            {
                wfsMDI.addWFSKey(WFSConstants.DEFAULT_TIME_FIELD, TimeSpan.class, this);
                getMetaDataInfo().setSpecialKey(WFSConstants.DEFAULT_TIME_FIELD, TimeKey.DEFAULT, this);
            }
            else if (CollectionUtilities.hasContent(wfsMDI.getDateKeys()))
            {
                String timeKey = wfsMDI.getDateKeys().get(0);
                getMetaDataInfo().setSpecialKey(timeKey, TimeKey.DEFAULT, this);
            }
            /* We need to re-copy the keys to the originals since by this time the main set of columns were already copied.
             * This helps with a problem we see with dynamic columns (user labels) -Pete. */
            wfsMDI.copyKeysToOriginalKeys();
        }
    }

    @Override
    public void setTypeSync(ServerDataTypeSync typeSync)
    {
        super.setTypeSync(typeSync);
        typeSync.setHasData(true, this);
        setQueryable(true);

        // Just sync RGB color, opacity should remain unchanged.
        int opacity = getBasicVisualizationInfo().getTypeOpacity();
        setDefaultTypeColor(ColorUtilities.opacitizeColor(getTypeSync().getDefaultColor(), opacity));

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
                getBasicVisualizationInfo().setLoadsTo(LoadsTo.TIMELINE, this);
                getBasicVisualizationInfo().setSupportedLoadsToTypes(DefaultBasicVisualizationInfo.LOADS_TO_TIMELINE_ONLY);
                setTimeExtents(timeExtent, false);
            }
        }
    }

    @Override
    public void setVisible(boolean visible, Object source)
    {
        boolean wasVisible = isVisible();
        super.setVisible(visible, source);
        if (!isAnimationSensitive() && wasVisible != visible && !isQueryable() && isDisplayable())
        {
            SingleLayerRequeryEvent queryEvent = new SingleLayerRequeryEvent(this, RequeryType.FULL_REQUERY);
            getToolbox().getEventManager().publishEvent(queryEvent);
        }
    }

    /**
     * Sets the wFS vertion.
     *
     * @param wfsVersion the new wFS vertion
     */
    public void setWFSVersion(String wfsVersion)
    {
        myWFSVersion = wfsVersion;
    }

    /**
     * Show configuration panel for layer columns.
     *
     * @param parent the parent window
     */
    public void showConfig(Window parent)
    {
        JDialog configDialog = new JDialog(parent);
        configDialog.setLocationRelativeTo(parent);

        if (myWFSConfigDialog == null)
        {
            myWFSConfigDialog = new WFSColumnConfigPanel(this, configDialog, isInUse());
            configDialog.setLayout(new BorderLayout());
            configDialog.setContentPane(myWFSConfigDialog);

            Dimension size = new Dimension(260, 550);
            configDialog.setSize(size);
            configDialog.setPreferredSize(size);
            configDialog.setMinimumSize(size);
            configDialog.setResizable(false);
            configDialog.setVisible(true);
            LOGGER.info("Setting up " + getTypeName() + " WFS Layer Columns");

            /* These 2 event listeners will handle cleaning up the config panel if the cancel button is pressed or the 'X' button
             * on the dialog is pressed. */
            configDialog.addComponentListener(new ComponentAdapter()
            {
                @Override
                public void componentHidden(ComponentEvent componentevent)
                {
                    myWFSConfigDialog = null;
                }
            });
            myWFSConfigDialog.addDialogClosedListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    if (Utilities.sameInstance(evt.getSource(), myWFSConfigDialog))
                    {
                        myWFSConfigDialog = null;
                    }
                }
            });
        }
    }

    /**
     * Show debug panel with content of this class.
     *
     * @param parent the parent window
     */
    public void showContent(Window parent)
    {
        TextViewDialog dvd = new TextViewDialog(parent, "DataTypeInfo Summary: " + getDisplayName(), toString(), false,
                getToolbox().getPreferencesRegistry());
        dvd.setLocationRelativeTo(parent);
        dvd.setVisible(true);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("WFSDataType:\n" + "  Geometry Column: ").append(getMetaDataInfo().getGeometryColumn())
                .append("\n" + "  OutputFormat   : ").append(myOutputFormat).append("\n" + "  Lat/Lon Order  : ")
                .append(myIsLatBeforeLon ? "Lat,Lon" : "Lon,Lat").append("\n\n");
        sb.append(super.toString());
        return sb.toString();
    }

    @Override
    protected boolean isPreserveVisibility()
    {
        return !myIsWFSOnlyWithTime && super.isPreserveVisibility();
    }

    /**
     * Get the preference key for the end time column.
     *
     * @return The preference key.
     */
    private String getEndTimeColumnPreferenceKey()
    {
        return getTypeKey() + "!!" + EndTimeKey.END_TIME_SPECIAL_KEY_NAME;
    }

    /**
     * Get the preference key for the start time column.
     *
     * @return The preference key.
     */
    private String getStartTimeColumnPreferenceKey()
    {
        return getTypeKey() + "!!" + TimeKey.TIME_SPECIAL_KEY_NAME;
    }

    /**
     * Try to guess the right time columns.
     *
     * @param dateKeys The list of all time columns.
     * @return The guessed time columns.
     */
    private TimeColumns guessTimeColumns(final List<String> dateKeys)
    {
        String startColumn = getMetaDataInfo().getKeyForSpecialType(TimeKey.DEFAULT);
        String endColumn = getMetaDataInfo().getKeyForSpecialType(EndTimeKey.DEFAULT);
        if (!isTimeColumnChosen())
        {
            if (!dateKeys.contains(startColumn))
            {
                startColumn = dateKeys.get(0);
                for (String key : dateKeys)
                {
                    String lowerKey = key.toLowerCase();
                    if (lowerKey.contains("start") || lowerKey.contains("up") || lowerKey.contains("begin"))
                    {
                        startColumn = key;
                        break;
                    }
                }
            }
            if (!dateKeys.contains(endColumn))
            {
                endColumn = null;
                for (String key : dateKeys)
                {
                    String lowerKey = key.toLowerCase();
                    if (!key.equals(startColumn))
                    {
                        if (lowerKey.contains("stop") || lowerKey.contains("down") || lowerKey.contains("end"))
                        {
                            endColumn = key;
                            break;
                        }
                        else if (endColumn == null)
                        {
                            endColumn = key;
                        }
                    }
                }
            }
        }
        return new TimeColumns(startColumn, endColumn);
    }

    /**
     * Auto-Populate the Mantle interface part of this class.
     *
     * @param properties The properties list for this type
     */
    protected void setMantleProps(MetaDataInfo properties)
    {
        ServerBasicVisualizationInfo basicInfo = new ServerBasicVisualizationInfo(LoadsTo.STATIC,
                getBasicVisualizationInfo().getTypeColor(), false);
        setBasicVisualizationInfo(basicInfo);

        OrderManager manager = getToolbox().getOrderManagerRegistry()
                .getOrderManager(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY, DefaultOrderCategory.FEATURE_CATEGORY);
        MapVisualizationInfo mapInfo = new WFSMapVisualizationInfo(MapVisualizationType.UNKNOWN, manager);
        setMapVisualizationInfo(mapInfo);

        setMetaDataInfo(properties);
    }
}
