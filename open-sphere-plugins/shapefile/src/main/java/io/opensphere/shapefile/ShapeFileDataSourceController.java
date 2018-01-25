package io.opensphere.shapefile;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import gnu.trove.procedure.TObjectIntProcedure;
import io.opensphere.core.Toolbox;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.order.ParticipantOrderChangeEvent;
import io.opensphere.core.order.ParticipantOrderChangeEvent.ParticipantChangeType;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.filesystem.FileUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.event.DataTypeInfoLoadsToChangeEvent;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceCreator;
import io.opensphere.mantle.datasources.impl.AbstractDataSourceController;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.shapefile.config.v1.ShapeFileSource;
import io.opensphere.shapefile.config.v1.ShapeFilesConfig;

// TODO: Auto-generated Javadoc
/**
 * The Class ShapeFileDataSourceController.
 */
@SuppressWarnings("PMD.GodClass")
public class ShapeFileDataSourceController extends AbstractDataSourceController
{
    /** The Constant FAILED_TO_LOAD_SHAPE_FILE. */
    private static final String FAILED_TO_LOAD_SHAPE_FILE = "Failed to load shape file ";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ShapeFileDataSourceController.class);

    /** Config preferences key. */
    private static final String PREFERENCES_KEY = "config";

    /** The Constant SHAPE_FILE. */
    private static final String SHAPE_FILE = "Shape File";

    /** The config. */
    private final ShapeFilesConfig myConfig;

    /** The data group info assistant. */
    private final ShapeFileDataGroupInfoAssistant myDataGroupInfoAssistant;

    /** The file importer. */
    private final ShapeFileImporter myFileImporter;

    /** The base data group info. */
    private final DefaultDataGroupInfo myMasterGroup;

    /** Listener for changes to the order of shape files. */
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
                        DataTypeInfo dti = myOrderKeyMap.get(participant);
                        if (dti != null)
                        {
                            dti.getMapVisualizationInfo().setZOrder(order, null);
                        }
                        return true;
                    }
                });
            }
        }
    };

    /** A map of the order key to the data type info being ordered. */
    private final Map<OrderParticipantKey, DataTypeInfo> myOrderKeyMap = New.map();

    /** Manager which determines the z-order of the shape file sources. */
    private final OrderManager myOrderManager;

    /**
     * A map of data sources to the keys for participation in order management.
     */
    private final Map<IDataSource, DefaultOrderParticipantKey> myOrderParticipants = New.map();

    /** The shape file handler. */
    private final ShapeFileHandler myShapeFileHandler;

    /** The source type. */
    private final DataSourceType mySourceType = DataSourceType.FILE;

    /** The service manager. */
    private final EventListenerService myServiceManager;

    /** The activation listener. */
    private final ActivationListener myActivationListener = new AbstractActivationListener()
    {
        @Override
        public boolean handleActivating(DataGroupInfo dgi, io.opensphere.core.util.lang.PhasedTaskCanceller canceller)
            throws io.opensphere.mantle.data.DataGroupActivationException
        {
            if (dgi.hasMembers(false))
            {
                for (DataTypeInfo dti : dgi.getMembers(false))
                {
                    if (dti instanceof ShapeFileDataTypeInfo)
                    {
                        ShapeFileSource fileSource = ((ShapeFileDataTypeInfo)dti).getFileSource();
                        LOGGER.info("Activate  " + dgi.getId());
                        if (fileSource.isParticipating())
                        {
                            if (!myShapeFileHandler.updateDataSource(fileSource))
                            {
                                return false;
                            }
                        }
                        else
                        {
                            if (!myShapeFileHandler.addDataSource(fileSource))
                            {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        }

        @Override
        public void handleDeactivating(DataGroupInfo dgi)
        {
            if (dgi.hasMembers(false))
            {
                for (DataTypeInfo dti : dgi.getMembers(false))
                {
                    if (dti instanceof ShapeFileDataTypeInfo)
                    {
                        ShapeFileSource fileSource = ((ShapeFileDataTypeInfo)dti).getFileSource();
                        LOGGER.info("Deactivate  " + dgi.getId());
                        if (fileSource.isParticipating())
                        {
                            myShapeFileHandler.removeDataSource(fileSource);
                        }
                        else
                        {
                            // Set us to not busy in this case since
                            // we will have been set to busy before.
                            fileSource.setBusy(false, this);
                        }
                    }
                }
            }
        }
    };

    /**
     * Instantiates a new shape file data source controller.
     *
     * @param tb tuberculosis
     */
    public ShapeFileDataSourceController(Toolbox tb)
    {
        super(tb);
        myMasterGroup = new DefaultDataGroupInfo(true, tb, SHAPE_FILE, ShapeFileEnvoy.class.getName(), "Shape Files");
        myFileImporter = new ShapeFileImporter(this);
        myDataGroupInfoAssistant = new ShapeFileDataGroupInfoAssistant(this);
        myShapeFileHandler = new ShapeFileHandler(this);
        myConfig = tb.getPreferencesRegistry().getPreferences(ShapeFilePlugin.class).getJAXBObject(ShapeFilesConfig.class,
                PREFERENCES_KEY, new ShapeFilesConfig());

        tb.getImporterRegistry().addImporter(myFileImporter);

        myOrderManager = tb.getOrderManagerRegistry().getOrderManager(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY,
                DefaultOrderCategory.FEATURE_CATEGORY);
        myOrderManager.addParticipantChangeListener(myOrderChangeListener);

        myServiceManager = new EventListenerService(tb.getEventManager(), 1);
        myServiceManager.bindEvent(ApplicationLifecycleEvent.class, this::handleApplicationLifecycleEvent);
    }

    /**
     * Activate source.
     *
     * @param source The source.
     */
    @Override
    public void activateSource(IDataSource source)
    {
        ShapeFileSource aSource = (ShapeFileSource)source;
        if (aSource.isParticipating())
        {
            myShapeFileHandler.updateDataSource(source);
        }
        else
        {
            myShapeFileHandler.addDataSource(source);
        }
    }

    /**
     * Adds the source.
     *
     * @param source The source.
     */
    @Override
    public void addSource(IDataSource source)
    {
        if (source instanceof ShapeFileSource)
        {
            ShapeFileSource shapeFileSource = (ShapeFileSource)source;

            initializeSource(shapeFileSource, DataGroupInfo.NO_EVENT_SOURCE);

            if (!source.loadError())
            {
                myConfig.addSource(shapeFileSource);
                saveConfigState();
            }

            if (source.isActive())
            {
                shapeFileSource.getDataGroupInfo().activationProperty().setActive(true);
            }
        }
    }

    /** Perform any required cleanup before being discarded. */
    public void close()
    {
        myServiceManager.close();
        myOrderManager.removeParticipantChangeListener(myOrderChangeListener);
    }

    /**
     * Creates the source.
     *
     * @param parent The parent.
     * @param type The type.
     * @param chosenFiles The chosen files.
     * @param sourcesInUse The sources in use.
     * @param caller The caller.
     */
    @SuppressWarnings("unused")
    @Override
    public void createSource(Container parent, DataSourceType type, List<File> chosenFiles, Set<IDataSource> sourcesInUse,
            IDataSourceCreator caller)
    {
        if (chosenFiles == null || chosenFiles.isEmpty() || chosenFiles.size() > 1)
        {
            throw new IllegalArgumentException("Shape File Sources may only have one contributing file");
        }

        if (fileAlreadyLoaded(parent, chosenFiles.get(0), sourcesInUse))
        {
            caller.sourceCreated(false, null);
            return;
        }

        Set<String> namesInUse = getDataSourceNames(sourcesInUse);

        String name = FileUtilities.getBasename(chosenFiles.get(0));

        ESRIShapefile shapefile = ShapeFileReadUtilities.readFile(chosenFiles.get(0).getAbsolutePath(),
                getToolbox().getServerProviderRegistry().getProvider(HttpServer.class));
        List<String> header = ShapeFileReadUtilities.getHeader(shapefile);
        if (header == null)
        {
            String result = (String)JOptionPane.showInputDialog(parent, "Please input a name for the shape file.",
                    "Shape File Name", JOptionPane.PLAIN_MESSAGE, null, null, name);

            if (result != null)
            {
                while (result != null && (namesInUse.contains(result) || result.isEmpty()))
                {
                    if (namesInUse.contains(result))
                    {
                        result = JOptionPane.showInputDialog(parent, "The name \"" + result
                                + "\" is already in use.\n\nPlease choose another name for the shape file.");
                    }
                    else if (result.isEmpty())
                    {
                        result = JOptionPane.showInputDialog(parent,
                                "The name cannot be empty.\n\nPlease choose another name for the shape file.");
                    }
                }
            }

            if (result == null)
            {
                caller.sourceCreated(false, null);
            }
            else
            {
                ShapeFileSource source = new ShapeFileSource();
                source.setName(result);
                source.setPath(chosenFiles.get(0).getAbsolutePath());
                caller.sourceCreated(true, source);
            }
        }
        else
        {
            ShapeFileSource source = new ShapeFileSource();

            Object selection = namesInUse.isEmpty() ? null : promptForImportSettingsReuse(parent, namesInUse);
            if (selection != null)
            {
                ShapeFileSource other = myConfig.getShapeFileSource(selection.toString());
                source.setEqualTo(other);
                source.setName(name);
                source.setPath(chosenFiles.get(0).getAbsolutePath());
                caller.sourceCreated(true, source);
            }
            else
            {
                source.setName(name);
                source.setPath(chosenFiles.get(0).getAbsolutePath());
                new LegacyShapeFileImportWizard(parent, getToolbox(), source, namesInUse, caller);
            }
        }
    }

    /**
     * Prompt the user to determine if the import settings of another file
     * should be reused.
     *
     * @param parent The dialog parent.
     * @param namesInUse The existing data sources names.
     * @return The selected data source name, or {@code null}.
     */
    private Object promptForImportSettingsReuse(Container parent, Set<String> namesInUse)
    {
        JPanel p = new JPanel(new BorderLayout());
        JTextArea jta = new JTextArea("Do you want to use the same import settings\nas one of these other files?");
        jta.setBackground(p.getBackground());
        jta.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        jta.setEditable(false);
        p.add(jta, BorderLayout.NORTH);
        JComboBox<String> cb = new JComboBox<>(New.array(namesInUse, String.class));
        p.add(cb, BorderLayout.CENTER);

        int value = JOptionPane.showConfirmDialog(parent, p, "Reuse Import Settings", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null);

        return value == JOptionPane.YES_OPTION ? cb.getSelectedItem() : null;
    }

    /**
     * Get the names of the data sources.
     *
     * @param dataSources The data sources.
     * @return The names.
     */
    private Set<String> getDataSourceNames(Collection<? extends IDataSource> dataSources)
    {
        Set<String> namesInUse = new HashSet<>(dataSources.size());
        for (IDataSource source : dataSources)
        {
            namesInUse.add(source.getName());
        }
        return namesInUse;
    }

    /**
     * Deactivate source.
     *
     * @param source The source.
     */
    @Override
    public void deactivateSource(IDataSource source)
    {
        ShapeFileSource aSource = (ShapeFileSource)source;
        if (aSource.isParticipating())
        {
            myShapeFileHandler.removeDataSource(source);
        }
        else
        {
            // We need to do this because the handler was never called to remove
            // the source and we are probably in a "busy" state.
            aSource.setBusy(false, this);
        }
    }

    /**
     * File importer.
     *
     * @return the cSV file importer
     */
    public ShapeFileImporter getFileImporter()
    {
        return myFileImporter;
    }

    /**
     * Gets the source by name.
     *
     * @param sourceName the source name
     * @return the source
     */
    public ShapeFileSource getSource(String sourceName)
    {
        return myConfig.getShapeFileSource(sourceName);
    }

    /**
     * Gets the source classes.
     *
     * @return the source classes
     */
    @Override
    public List<Class<? extends IDataSource>> getSourceClasses()
    {
        return Collections.<Class<? extends IDataSource>>singletonList(ShapeFileSource.class);
    }

    /**
     * Get the source list.
     *
     * @return The source list.
     */
    @Override
    public List<IDataSource> getSourceList()
    {
        return myConfig.getSourceList();
    }

    /**
     * Get the source type.
     *
     * @return The source type.
     */
    @Override
    public DataSourceType getSourceType()
    {
        return mySourceType;
    }

    /**
     * Get the type extensions.
     *
     * @return The type extensions.
     */
    @Override
    public String[] getTypeExtensions()
    {
        return new String[] { "shp", "SHP" };
    }

    /**
     * Get the type name.
     *
     * @return The type name.
     */
    @Override
    public String getTypeName()
    {
        return "ESRI Shapefile";
    }

    /**
     * Initialize.
     */
    @Override
    public void initialize()
    {
        myServiceManager.open();
    }

    /**
     * Removes the source.
     *
     * @param source The source.
     * @param cleanup The cleanup.
     * @param parent The parent.
     * @return true, if successful
     */
    @Override
    public boolean removeSource(IDataSource source, boolean cleanup, Component parent)
    {
        DefaultOrderParticipantKey participant = myOrderParticipants.remove(source);
        if (participant != null)
        {
            myOrderKeyMap.remove(participant);
            myOrderManager.deactivateParticipant(participant);
        }
        boolean removed = myConfig.removeSource(source);
        saveConfigState();
        if (source.isActive())
        {
            deactivateSource(source);
        }

        if (cleanup)
        {
            ShapeFileSource shpSource = (ShapeFileSource)source;
            String typeKey = shpSource.generateTypeKey();
            DefaultMetaDataInfo.clearPreferencesRegistryEntryForNumericCache(getToolbox(), typeKey, this);
            MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataTypeInfoPreferenceAssistant().removePreferences(typeKey);

            DataGroupInfo dgi = shpSource.getDataGroupInfo();
            if (dgi != null)
            {
                myMasterGroup.removeChild(dgi, this);
                MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataGroupController().cleanUpGroup(dgi);
            }
        }

        return removed;
    }

    /**
     * Set the executor service.
     *
     * @param execService The executor service.
     */
    @Override
    public void setExecutorService(ExecutorService execService)
    {
        myShapeFileHandler.setExecutorService(execService);
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getTypeName()).append(" Files [ ");
        for (String str : getTypeExtensions())
        {
            sb.append("*.").append(str).append(", ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * Update source.
     *
     * @param source The source.
     */
    @Override
    public void updateSource(IDataSource source)
    {
        myConfig.updateSource(source);
        saveConfigState();
    }

    /**
     * Handle loads to changed.
     *
     * @param event The event.
     */
    @Override
    protected void handleLoadsToChanged(DataTypeInfoLoadsToChangeEvent event)
    {
        if (event.getDataTypeInfo() instanceof ShapeFileDataTypeInfo)
        {
            ((ShapeFileDataTypeInfo)event.getDataTypeInfo()).getFileSource().setLoadsTo(event.getLoadsTo());
            updateSource(((ShapeFileDataTypeInfo)event.getDataTypeInfo()).getFileSource());
        }
    }

    /**
     * Save config state.
     */
    private void saveConfigState()
    {
        getToolbox().getPreferencesRegistry().getPreferences(ShapeFilePlugin.class).putJAXBObject(PREFERENCES_KEY, myConfig,
                false, this);
    }

    /**
     * Check that the data sources in use do not already contain the chosen
     * file.
     *
     * @param parent The dialog parent.
     * @param chosenFile The chosen file.
     * @param sourcesInUse The data sources in use
     * @return {@code true} if the chosen file is already in use.
     */
    private boolean fileAlreadyLoaded(Container parent, File chosenFile, Set<IDataSource> sourcesInUse)
    {
        Set<String> filesInUse = new HashSet<>();
        for (IDataSource source : sourcesInUse)
        {
            filesInUse.add(((ShapeFileSource)source).getPath());
        }

        if (filesInUse.contains(chosenFile.getAbsolutePath()))
        {
            String foundName = null;
            for (IDataSource source : sourcesInUse)
            {
                if (((ShapeFileSource)source).getPath().equals(chosenFile.getAbsolutePath()))
                {
                    foundName = source.getName();
                }
            }

            JOptionPane
                    .showMessageDialog(parent,
                            "The file you selected is already loaded under the name:\n\"" + foundName
                                    + "\"\nYou can not load the same file twice.",
                            "Duplicate File Error", JOptionPane.ERROR_MESSAGE);
            return true;
        }

        return false;
    }

    /**
     * Handles a {@link ApplicationLifecycleEvent}.
     *
     * @param event the event
     */
    private void handleApplicationLifecycleEvent(ApplicationLifecycleEvent event)
    {
        if (event.getStage() == ApplicationLifecycleEvent.Stage.PLUGINS_INITIALIZED)
        {
            initializeSources();
        }
    }

    /**
     * Initializes the data sources in the configuration.
     */
    private void initializeSources()
    {
        MantleToolbox mantleToolbox = MantleToolboxUtils.getMantleToolbox(getToolbox());
        mantleToolbox.getDataGroupController().addRootDataGroupInfo(myMasterGroup, this);
        for (ShapeFileSource source : myConfig.getShapeFileSources())
        {
            source.setVisible(mantleToolbox.getDataTypeInfoPreferenceAssistant().isVisiblePreference(source.generateTypeKey()));
            initializeSource(source, this);
        }
    }

    /**
     * Initializes the source by creating mantle types and adding it to the
     * z-order manager.
     *
     * @param source The data source
     * @param eventSource The event source
     */
    private void initializeSource(ShapeFileSource source, Object eventSource)
    {
        boolean isSuccess = false;
        try
        {
            DataTypeInfo dti = ShapeMantleUtilities.generateDataTypeInfo(getToolbox(), source);
            source.setDataTypeInfo(dti);
            String category = source.getName();
            DefaultDataGroupInfo dgi = new DefaultDataGroupInfo(false, getToolbox(), SHAPE_FILE, dti.getTypeKey(), category);
            source.setDataGroupInfo(dgi);
            dgi.setAssistant(myDataGroupInfoAssistant);
            dgi.activationProperty().addListener(myActivationListener);
            dgi.addMember(dti, this);
            dti.setVisible(source.isVisible(), this);

            DefaultOrderParticipantKey participant = new DefaultOrderParticipantKey(
                    DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY, DefaultOrderCategory.FEATURE_CATEGORY, dti.getTypeKey());
            dti.setOrderKey(participant);
            myOrderKeyMap.put(participant, dti);
            myOrderParticipants.put(source, participant);
            int zOrder = myOrderManager.activateParticipant(participant);
            dti.getMapVisualizationInfo().setZOrder(zOrder, null);

            myMasterGroup.addChild(dgi, eventSource);
            isSuccess = dti.getMapVisualizationInfo().getVisualizationType() != MapVisualizationType.UNKNOWN;
        }
        catch (IOException e)
        {
            LOGGER.error(FAILED_TO_LOAD_SHAPE_FILE + source.getPath(), e);
            UserMessageEvent.error(getToolbox().getEventManager(), FAILED_TO_LOAD_SHAPE_FILE + source.getPath(), true, null, e);
        }
        source.setLoadError(!isSuccess, this);
    }
}
