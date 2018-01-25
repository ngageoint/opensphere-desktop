package io.opensphere.imagery;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import gnu.trove.procedure.TObjectIntProcedure;
import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.order.OrderChangeListener;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.order.ParticipantOrderChangeEvent;
import io.opensphere.core.order.ParticipantOrderChangeEvent.ParticipantChangeType;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultMapTileVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultTileLevelController;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceCreator;
import io.opensphere.mantle.datasources.impl.AbstractDataSourceController;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class ImageryFileSourceController.
 */
@SuppressWarnings("PMD.GodClass")
public class ImageryFileSourceController extends AbstractDataSourceController
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ImageryFileSourceController.class);

    /** Config preferences key. */
    private static final String PREFERENCES_KEY = "config";

    /** The Assistant. */
    private final ImageryDataGroupInfoAssistant myAssistant;

    /** The Config. */
    private final ImageryFilesConfig myConfig;

    /** The File handler. */
    private final ImageryFileHandler myFileHandler;

    /** The Importer. */
    private final ImageryFileImporter myImporter;

    /** Listener for changes to the order of imagery sources. */
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

    /** Manager which determines the z-order of the imagery sources. */
    private final OrderManager myOrderManager;

    /** The Root group. */
    private final DefaultDataGroupInfo myRootGroup;

    /** The Source type. */
    private final DataSourceType mySourceType = DataSourceType.FILE;

    /** Listener for changes to the activation state of a data group. */
    private final ActivationListener myActivationListener = new AbstractActivationListener()
    {
        @Override
        public void handleDeactivating(DataGroupInfo dgi)
        {
            LOGGER.info("Deactivate  " + dgi.getId());
            if (dgi.hasMembers(false) && dgi instanceof ImageryDataGroupInfo)
            {
                myFileHandler.removeDataSource(((ImageryDataGroupInfo)dgi).getImagerySourceGroup());
            }
        }

        @Override
        public boolean handleActivating(DataGroupInfo dgi, PhasedTaskCanceller canceller)
        {
            LOGGER.info("Activate  " + dgi.getId());
            if (dgi.hasMembers(false) && dgi instanceof ImageryDataGroupInfo)
            {
                return myFileHandler.addDataSource(((ImageryDataGroupInfo)dgi).getImagerySourceGroup());
            }
            else
            {
                return true;
            }
        }
    };

    /**
     * Instantiates a new advanced image file source controller.
     *
     * @param tb the {@link Toolbox}
     */
    public ImageryFileSourceController(Toolbox tb)
    {
        super(tb);
        myConfig = tb.getPreferencesRegistry().getPreferences(ImageryPlugin.class).getJAXBObject(ImageryFilesConfig.class,
                PREFERENCES_KEY, new ImageryFilesConfig());
        myRootGroup = new DefaultDataGroupInfo(true, tb, "Imagery", "ImageryPluginRootGroup", "Imagery");
        myFileHandler = new ImageryFileHandler(this);
        myImporter = new ImageryFileImporter(this);
        myAssistant = new ImageryDataGroupInfoAssistant(this);
        tb.getImporterRegistry().addImporter(myImporter);

        myOrderManager = getToolbox().getOrderManagerRegistry().getOrderManager(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY);
        myOrderManager.addParticipantChangeListener(myOrderChangeListener);
    }

    @Override
    public void activateSource(IDataSource source)
    {
        myFileHandler.addDataSource(source);
    }

    @Override
    public void addSource(IDataSource source)
    {
        if (source instanceof ImagerySourceGroup)
        {
            myConfig.addSource(source);
            if (!source.isTransient())
            {
                saveConfigState();
            }
            ImagerySourceGroup group = (ImagerySourceGroup)source;
            createGroupForSource(group);

            if (source.isActive())
            {
                group.getDataGroupInfo().activationProperty().setActive(true);
            }
        }
    }

    /** Perform any required cleanup before being discarded. */
    public void close()
    {
        myOrderManager.removeParticipantChangeListener(myOrderChangeListener);
    }

    @SuppressWarnings("unused")
    @Override
    public void createSource(Container parent, DataSourceType type, List<File> chosenFiles, Set<IDataSource> sourcesInUse,
            IDataSourceCreator caller)
    {
        if (chosenFiles == null || chosenFiles.isEmpty())
        {
            throw new IllegalArgumentException("Image File Sources may only have one contributing file");
        }

        new ImagerySourceWizardPanel(parent, getToolbox(), chosenFiles, sourcesInUse, caller);
    }

    @Override
    public void deactivateSource(IDataSource source)
    {
        myFileHandler.removeDataSource(source);
    }

    /**
     * Gets the file importer.
     *
     * @return the file importer
     */
    public ImageryFileImporter getFileImporter()
    {
        return myImporter;
    }

    @Override
    public List<Class<? extends IDataSource>> getSourceClasses()
    {
        return Collections.<Class<? extends IDataSource>>singletonList(ImagerySourceGroup.class);
    }

    @Override
    public List<IDataSource> getSourceList()
    {
        return myConfig.getSourceList();
    }

    @Override
    public DataSourceType getSourceType()
    {
        return mySourceType;
    }

    @Override
    public String[] getTypeExtensions()
    {
        return new String[] { "png", "gif", "jpg", "jpeg", "tif", "nitf", "ntf", "tiff", "geoTIFF", "WorldFile" };
    }

    @Override
    public String getTypeName()
    {
        return "Image";
    }

    @Override
    public void initialize()
    {
        MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataGroupController().addRootDataGroupInfo(myRootGroup, this);
        for (ImagerySourceGroup groupSource : myConfig.getImageSourceGroups())
        {
            createGroupForSource(groupSource);
        }
    }

    /**
     * Checks if is multi frame or animated gif.
     *
     * @param filePath the file path
     * @return true, if is multi frame or animated gif
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public boolean isMultiFrameOrAnimatedGif(String filePath) throws IOException
    {
        if (!filePath.toLowerCase().endsWith(".gif"))
        {
            return false;
        }
        Object input = new File(filePath);
        ImageInputStream stream = ImageIO.createImageInputStream(input);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
        if (!readers.hasNext())
        {
            throw new IOException("no image reader found");
        }

        ImageReader reader = readers.next();
        reader.setInput(stream);
        // don't use false!
        final int numImages = reader.getNumImages(true);

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("numImages = " + numImages);
            for (int i = 0; i < numImages; i++)
            {
                BufferedImage image = reader.read(i);
                LOGGER.trace("image[" + i + "] = " + image);
            }
        }
        stream.close();
        return numImages > 1;
    }

    @Override
    public boolean removeSource(IDataSource source, boolean cleanup, Component parent)
    {
        ImagerySourceGroup groupSource = (ImagerySourceGroup)source;
        for (ImageryFileSource imageSource : groupSource.getImageSources())
        {
            OrderParticipantKey key = imageSource.getDataTypeInfo().getOrderKey();
            if (key != null)
            {
                myOrderKeyMap.remove(key);
                myOrderManager.deactivateParticipant(key);
            }
        }

        boolean removed = myConfig.removeSource(source);
        saveConfigState();
        ImageryEnvoy envoy = groupSource.getImageryEnvoy();
        if (cleanup && envoy != null)
        {
            String message = "Would you like to clear the cache for this imagery group?"
                    + "\n\nSelecting yes will not remove the source images from disk.";
            int result = JOptionPane.showConfirmDialog(SwingUtilities.getRootPane(parent), message, "Clean Image Tile Cache",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION)
            {
                envoy.clearImageCache();
                LOGGER.info("Cache clean is complete.");
            }
        }

        if (source.isActive())
        {
            deactivateSource(source);
        }

        if (cleanup)
        {
            myRootGroup.removeChild(groupSource.getDataGroupInfo(), this);
            MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataGroupController()
                    .cleanUpGroup(groupSource.getDataGroupInfo());
        }

        return removed;
    }

    @Override
    public void setExecutorService(ExecutorService execService)
    {
        myFileHandler.setExecutorService(execService);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append("Common ");
        sb.append(getTypeName());
        sb.append(" Types [ ");
        for (String str : getTypeExtensions())
        {
            sb.append("*.").append(str).append(", ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" ]");
        return sb.toString();
    }

    @Override
    public void updateSource(IDataSource source)
    {
        myConfig.updateSource(source);
        if (!source.isTransient())
        {
            saveConfigState();
        }
    }

    /**
     * Save config state.
     */
    protected void saveConfigState()
    {
        getToolbox().getPreferencesRegistry().getPreferences(ImageryPlugin.class).putJAXBObject(PREFERENCES_KEY, myConfig, false,
                this);
    }

    /**
     * Creates the group for source.
     *
     * @param groupSource the group source
     */
    private void createGroupForSource(ImagerySourceGroup groupSource)
    {
        ImageryDataGroupInfo groupDGI = new ImageryDataGroupInfo(false, getToolbox(),
                getClass().getName() + "::" + groupSource.getName(), groupSource.getName(), groupSource);
        groupSource.setDataGroupInfo(groupDGI);
        groupDGI.setAssistant(myAssistant);
        groupDGI.activationProperty().addListener(myActivationListener);

        for (ImageryFileSource source : groupSource.getImageSources())
        {
            if (source.isEnabled())
            {
                ImageryDataTypeInfo idti = new ImageryDataTypeInfo(getToolbox(), source, groupSource);
                source.setDataTypeInfo(idti);
                BasicVisualizationInfo bvi = new DefaultBasicVisualizationInfo(LoadsTo.BASE, Color.black, false);
                idti.setBasicVisualizationInfo(bvi);

                DefaultOrderParticipantKey participant = new DefaultOrderParticipantKey(
                        DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY, DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY,
                        idti.getTypeKey());
                idti.setOrderKey(participant);
                myOrderKeyMap.put(participant, idti);
                int zOrder = myOrderManager.activateParticipant(participant);

                TileRenderProperties props = new DefaultTileRenderProperties(zOrder, true, true);
                float opacityFromPrefs = getOpacityForLayerFromPrefs(idti);
                props.setOpacity(opacityFromPrefs);
                props.setHighlightColor(ColorUtilities.opacitizeColor(Color.BLACK, opacityFromPrefs));

                DefaultMapTileVisualizationInfo mvi = new DefaultMapTileVisualizationInfo(MapVisualizationType.IMAGE, props,
                        true);
                mvi.setTileLevelController(new DefaultTileLevelController());
                idti.setMapVisualizationInfo(mvi);
                idti.setEnabled(true);
                idti.setDisplayable(true, this);
                idti.addBoundingBox(source.getBoundingBox());
                idti.setUrl(source.getPath());
                groupDGI.addMember(idti, this);
            }
        }

        myRootGroup.addChild(groupDGI, this);
    }

    /**
     * Gets the opacity for layer from preferences.
     *
     * @param dti the DataTypeInfo
     * @return the opacity for layer from preferences
     */
    private float getOpacityForLayerFromPrefs(DataTypeInfo dti)
    {
        final int maxOpacity = 255;
        int opacity = MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataTypeInfoPreferenceAssistant()
                .getOpacityPreference(dti.getTypeKey(), maxOpacity);
        return (float)opacity / (float)maxOpacity;
    }
}
