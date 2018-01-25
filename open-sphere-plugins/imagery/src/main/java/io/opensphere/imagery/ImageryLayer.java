package io.opensphere.imagery;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.AbstractModel;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.control.DiscreteEventListener;
import io.opensphere.core.control.PickListener;
import io.opensphere.core.control.PickListener.PickEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.Utilities;
import io.opensphere.imagery.util.ImageryMetaGeometryUtil;
import io.opensphere.mantle.controller.event.DataTypeInfoFocusEvent;
import io.opensphere.mantle.data.TypeFocusEvent.FocusType;

/**
 * Model for a ImageryLayer layer.
 */
@SuppressWarnings("PMD.GodClass")
public class ImageryLayer extends AbstractModel implements ImageProvider<ImageryImageKey>
{
    /** Property descriptor used for the data registry. */
    public static final PropertyDescriptor<ImageryLayer> PROPERTY_DESCRIPTOR = new PropertyDescriptor<ImageryLayer>("value",
            ImageryLayer.class);

    /** The Constant ourIdCounter. */
    private static final AtomicLong ourIdCounter = new AtomicLong(30000);

    /** Width of the world in degrees. */
    private static final double WORLD_HEIGHT = 180.;

    /** Height of the world in degrees. */
    private static final double WORLD_WIDTH = 360.;

    /** The DataTypeInfo for the layer. */
    private final ImageryDataTypeInfo myDataTypeInfo;

    /** The DGI focus listener. */
    private final EventListener<DataTypeInfoFocusEvent> myDTIFocusListener;

    /** The Focus border. */
    private final PolygonGeometry myFocusBorder;

    /** The image provider. */
    private ImageProvider<ImageryImageKey> myImageProvider;

    /** True if my action geometry is currently picked. */
    private PickEvent myLastPickEvent;

    /** The cell dimensions for each level of detail, starting with level 0. */
    private final List<Vector2d> myLayerCellDimensions;

    /** The Id. */
    private final long myLayerId = ourIdCounter.incrementAndGet();

    /** The event adapter. */
    private final DiscreteEventListener myMouseClickListener;

    /** The Pick listener. */
    private final PickListener myPickListener;

    /** The Select border. */
    private final PolygonGeometry mySelectBorder;

    /** The Select border showing. */
    private boolean mySelectBorderShowing;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Create a list of cell dimensions, one for each level of detail, each
     * one-quarter the size of the previous level.
     *
     * @param numLevels The number of levels.
     * @param levelZeroSizeD The size of level zero in degrees.
     * @return The list of cell dimensions.
     */
    public static List<Vector2d> createQuadSplitLayerCellDimensions(int numLevels, double levelZeroSizeD)
    {
        List<Vector2d> layerCellDimensions = new ArrayList<>(numLevels);
        for (int i = 0; i < numLevels; i++)
        {
            double cellSize = levelZeroSizeD / (1 << i);
            layerCellDimensions.add(new Vector2d(cellSize, cellSize));
        }
        return layerCellDimensions;
    }

    /**
     * Construct the layer.
     *
     * @param builder The builder.
     * @param tb the {@link Toolbox}
     */
    public ImageryLayer(Builder builder, Toolbox tb)
    {
        checkConfig(builder);
        myToolbox = tb;
        myDataTypeInfo = builder.getDataTypeInfo();
        mySelectBorder = ImageryMetaGeometryUtil.createGeometry(myDataTypeInfo.getImageryFileSource().getBoundingBox(), Color.red,
                1, null);
        myFocusBorder = ImageryMetaGeometryUtil.createGeometry(myDataTypeInfo.getImageryFileSource().getBoundingBox(),
                Color.white, 1, null);
        ArrayList<Vector2d> dimensions = new ArrayList<>(builder.getLayerCellDimensions());
        // Comparator that orders coordinates such that smaller ones come first.
        Collections.sort(dimensions, Vector2d.LENGTH_COMPARATOR);
        myLayerCellDimensions = Collections.unmodifiableList(dimensions);
        myImageProvider = builder.getImageProvider();
        myPickListener = createPickListener();
        myMouseClickListener = createMouseClickListener();
        myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT).addPickListener(myPickListener);
        myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT).addListener(myMouseClickListener,
                new DefaultMouseBinding(MouseEvent.MOUSE_CLICKED));
        myDTIFocusListener = createDGIFocusListener();
        myToolbox.getEventManager().subscribe(DataTypeInfoFocusEvent.class, myDTIFocusListener);
    }

    /**
     * Deactivate.
     */
    public void deactivate()
    {
        clearSelectBorder();
        clearFocusBorder();
        myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT).removePickListener(myPickListener);
        myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT)
                .removeListener(myMouseClickListener);
        myToolbox.getEventManager().unsubscribe(DataTypeInfoFocusEvent.class, myDTIFocusListener);
    }

    /**
     * Generate a grid of bounding boxes at the specified level of detail.
     *
     * @param level The level of detail.
     * @return A collection of bounding boxes.
     */
    public Collection<GeographicBoundingBox> generateGrid(int level)
    {
        Vector2d cellDimensions = myLayerCellDimensions.get(level);

        double height;
        double width;
        final double minLatitude;
        final double minLongitude;
        final double maxLatitude;
        final double maxLongitude;
        GeographicBoundingBox bbox = myDataTypeInfo.getImageryFileSource().getBoundingBox();
        if (bbox == null || bbox.getDeltaLonD() > WORLD_WIDTH || bbox.getDeltaLatD() > WORLD_HEIGHT)
        {
            height = WORLD_HEIGHT;
            width = WORLD_WIDTH;
            minLatitude = -height * 0.5;
            minLongitude = -width * 0.5;
            maxLatitude = height * 0.5;
            maxLongitude = width * 0.5;
        }
        else
        {
            height = bbox.getDeltaLatD();
            width = bbox.getDeltaLonD();
            minLatitude = bbox.getLowerLeft().getLatLonAlt().getLatD();
            minLongitude = bbox.getLowerLeft().getLatLonAlt().getLonD();
            maxLatitude = bbox.getUpperRight().getLatLonAlt().getLatD();
            maxLongitude = bbox.getUpperRight().getLatLonAlt().getLonD();
        }

        int rows = (int)Math.ceil(height / cellDimensions.getY());
        int cols = (int)Math.ceil(width / cellDimensions.getX());
        List<GeographicBoundingBox> results = new ArrayList<>(rows * cols);
        double lat1 = minLatitude;
        for (int row = 0; row < rows;)
        {
            double lat2 = minLatitude + cellDimensions.getY() * ++row;
            if (lat2 > maxLatitude)
            {
                lat2 = maxLatitude;
            }
            double lon1 = minLongitude;
            for (int col = 0; col < cols;)
            {
                double lon2 = minLongitude + cellDimensions.getX() * ++col;
                if (lon2 > maxLongitude)
                {
                    lon2 = maxLongitude;
                }
                LatLonAlt lowerLeft = LatLonAlt.createFromDegrees(lat1, lon1);
                LatLonAlt upperRight = LatLonAlt.createFromDegrees(lat2, lon2);
                results.add(new GeographicBoundingBox(lowerLeft, upperRight));
                lon1 = lon2;
            }
            lat1 = lat2;
        }

        return results;
    }

    /**
     * Get the key used to cache this layer.
     *
     * @return The key.
     */
    public String getCacheKey()
    {
        return myImageProvider.toString();
    }

    @Override
    public Image getImage(ImageryImageKey key)
    {
        if (myImageProvider == null)
        {
            throw new IllegalStateException("getImage called before image provider has been set.");
        }
        return myImageProvider.getImage(key);
    }

    /**
     * Get the image provider for this layer.
     *
     * @return The image provider.
     */
    public ImageProvider<ImageryImageKey> getImageProvider()
    {
        return myImageProvider;
    }

    /**
     * Gets the layer id.
     *
     * @return the layer id
     */
    public long getLayerId()
    {
        return myLayerId;
    }

    /**
     * Get the size of the grid for the highest level of detail.
     *
     * @return The size of the smallest grid square.
     */
    public Vector2d getMinimumGridSize()
    {
        return myLayerCellDimensions.get(myLayerCellDimensions.size() - 1);
    }

    /**
     * Get the title of the layer.
     *
     * @return The title of the layer.
     */
    public String getTitle()
    {
        return myDataTypeInfo.getDisplayName();
    }

    /**
     * Gets the type info.
     *
     * @return the type info
     */
    public ImageryDataTypeInfo getTypeInfo()
    {
        return myDataTypeInfo;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(getTitle().length() + 19);
        sb.append(ImageryLayer.class.getSimpleName()).append(" [myTitle=").append(getTitle()).append(']');
        return sb.toString();
    }

    /**
     * Check to make sure that the configuration has everything set.
     *
     * @param builder The builder for the layer.
     */
    protected final void checkConfig(Builder builder)
    {
        if (builder.myDataTypeInfo.getImageryFileSource() == null)
        {
            throw new IllegalArgumentException("imagery file source configuration cannot be null");
        }
        if (builder.getLayerCellDimensions() == null)
        {
            throw new IllegalArgumentException("layerCellDimensions cannot be null");
        }
    }

    /**
     * Get the overall bounding box for this layer.
     *
     * @return The bounding box.
     */
    protected GeographicBoundingBox getBoundingBox()
    {
        return myDataTypeInfo.getImageryFileSource().getBoundingBox();
    }

    /**
     * Get the list containing the cell dimensions of each level of detail,
     * starting with level 0.
     *
     * @return The cell dimensions.
     */
    protected List<Vector2d> getLayerCellDimensions()
    {
        return myLayerCellDimensions;
    }

    /**
     * Set the image provider for the layer.
     *
     * @param imageProvider The image provider.
     */
    protected final void setImageProvider(ImageProvider<ImageryImageKey> imageProvider)
    {
        myImageProvider = imageProvider;
    }

    /**
     * Clear focus border.
     */
    private void clearFocusBorder()
    {
        myToolbox.getGeometryRegistry().removeGeometriesForSource(this, Collections.singletonList(myFocusBorder));
    }

    /**
     * Clear select border.
     */
    private void clearSelectBorder()
    {
        mySelectBorderShowing = false;
        myToolbox.getGeometryRegistry().removeGeometriesForSource(this, Collections.singletonList(mySelectBorder));
        myFocusBorder.getRenderProperties().setHidden(false);
    }

    /**
     * Creates the dgi focus listener.
     *
     * @return the event listener
     */
    private EventListener<DataTypeInfoFocusEvent> createDGIFocusListener()
    {
        EventListener<DataTypeInfoFocusEvent> listener = new EventListener<DataTypeInfoFocusEvent>()
        {
            @Override
            public void notify(DataTypeInfoFocusEvent event)
            {
                if (!Utilities.sameInstance(event.getSource(), ImageryLayer.this) && event.getFocusType() == FocusType.CLICK)
                {
                    if (event.getTypes().contains(myDataTypeInfo))
                    {
                        showSelectBorder();
                    }
                    else
                    {
                        clearSelectBorder();
                    }
                }
            }
        };
        return listener;
    }

    /**
     * Creates the mouse click listener.
     *
     * @return the discrete event listener
     */
    private DiscreteEventListener createMouseClickListener()
    {
        DiscreteEventAdapter eventAdapter = new DiscreteEventAdapter("DataElement", "DataElementClickListener",
                "Monitors for clicks on Data Elements")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                if (event instanceof MouseEvent)
                {
                    MouseEvent mouseEvent = (MouseEvent)event;
                    if (myLastPickEvent != null && mouseEvent.getID() == MouseEvent.MOUSE_CLICKED
                            && mouseEvent.getButton() == MouseEvent.BUTTON1)
                    {
                        toggleSelectBorder();
                        myToolbox.getEventManager()
                                .publishEvent(new DataTypeInfoFocusEvent(myDataTypeInfo, ImageryLayer.this, FocusType.CLICK));
                    }
                }
            }
        };
        eventAdapter.setReassignable(false);
        return eventAdapter;
    }

    /**
     * Creates the pick listener.
     *
     * @return the pick listener
     */
    private PickListener createPickListener()
    {
        return new PickListener()
        {
            @Override
            public void handlePickEvent(PickEvent evt)
            {
                Geometry picked = evt.getPickedGeometry();
                long geomId = picked == null ? 0L : picked.getDataModelId();
                if (picked != null && geomId == myLayerId)
                {
                    showFocusBorder();
                    myLastPickEvent = evt;
                }
                else
                {
                    clearFocusBorder();
                    myLastPickEvent = null;
                }
            }
        };
    }

    /**
     * Show focus border.
     */
    private void showFocusBorder()
    {
        myToolbox.getGeometryRegistry().addGeometriesForSource(this, Collections.singletonList(myFocusBorder));
    }

    /**
     * Show select border.
     */
    private void showSelectBorder()
    {
        mySelectBorderShowing = true;
        myToolbox.getGeometryRegistry().addGeometriesForSource(this, Collections.singletonList(mySelectBorder));
        myFocusBorder.getRenderProperties().setHidden(true);
    }

    /**
     * Toggle select border.
     */
    private void toggleSelectBorder()
    {
        if (mySelectBorderShowing)
        {
            clearSelectBorder();
        }
        else
        {
            showSelectBorder();
        }
    }

    /** Builder class to aid in construction of WMSLayer. */
    public static class Builder
    {
        /** DataTypeInfo for this layer. */
        private final ImageryDataTypeInfo myDataTypeInfo;

        /** The image provider. */
        private ImageProvider<ImageryImageKey> myImageProvider;

        /**
         * The cell dimensions for each level of detail, starting with level 0.
         */
        private List<Vector2d> myLayerCellDimensions;

        /**
         * Construct me.
         *
         * @param info The layer's DataTypeInfo.
         */
        public Builder(ImageryDataTypeInfo info)
        {
            myDataTypeInfo = info;
        }

        /**
         * Accessor for the DataTypeInfo.
         *
         * @return The DataTypeInfo.
         */
        public ImageryDataTypeInfo getDataTypeInfo()
        {
            return myDataTypeInfo;
        }

        /**
         * Accessor for the imageProvider.
         *
         * @return The imageProvider.
         */
        public ImageProvider<ImageryImageKey> getImageProvider()
        {
            return myImageProvider;
        }

        /**
         * Accessor for the layerCellDimensions.
         *
         * @return The layerCellDimensions.
         */
        public List<Vector2d> getLayerCellDimensions()
        {
            return myLayerCellDimensions;
        }

        /**
         * Mutator for the image provider.
         *
         * @param imageProvider The imageProvider to set.
         */
        public void setImageProvider(ImageProvider<ImageryImageKey> imageProvider)
        {
            myImageProvider = imageProvider;
        }

        /**
         * Mutator for the layerCellDimensions.
         *
         * @param layerCellDimensions The layerCellDimensions to set.
         */
        public void setLayerCellDimensions(List<Vector2d> layerCellDimensions)
        {
            myLayerCellDimensions = layerCellDimensions;
        }
    }
}
