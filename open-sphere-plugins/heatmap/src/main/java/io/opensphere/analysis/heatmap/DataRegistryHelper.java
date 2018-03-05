package io.opensphere.analysis.heatmap;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.DataRegistryListener;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.mantle.data.DataTypeInfo;

/** Abstracts the data registry for heat map use. */
public class DataRegistryHelper
{
    /** The data model category. */
    private static final DataModelCategory CATEGORY = new DataModelCategory(DataRegistryHelper.class.getName(), "Heatmap", null);

    /** The property descriptor. */
    private static final PropertyDescriptor<HeatmapImageInfo> IMAGE_PROPERTY = new PropertyDescriptor<>("image",
            HeatmapImageInfo.class);

    /** The data registry. */
    private final DataRegistry myDataRegistry;

    /**
     * Constructor.
     *
     * @param dataRegistry The data registry
     */
    public DataRegistryHelper(DataRegistry dataRegistry)
    {
        myDataRegistry = dataRegistry;
    }

    /**
     * Adds an image to the data registry.
     *
     * @param imageInfo the image info
     */
    public void addImage(HeatmapImageInfo imageInfo)
    {
        Collection<HeatmapImageInfo> input = Collections.singleton(imageInfo);
        CacheDeposit<HeatmapImageInfo> deposit = new SimpleSessionOnlyCacheDeposit<>(
                CATEGORY.withCategory(imageInfo.getDataType().getTypeKey()), IMAGE_PROPERTY, input);
        myDataRegistry.addModels(deposit);
    }

    /**
     * Removes an image from the data registry.
     *
     * @param typeKey The layer type key
     */
    public void removeImage(String typeKey)
    {
        myDataRegistry.removeModels(CATEGORY.withCategory(typeKey), false);
    }

    /**
     * Gets the image info for the type key.
     *
     * @param typeKey The layer type key
     * @return the image info, or null
     */
    public HeatmapImageInfo queryImage(String typeKey)
    {
        SimpleQuery<HeatmapImageInfo> query = new SimpleQuery<>(CATEGORY.withCategory(typeKey), IMAGE_PROPERTY);
        myDataRegistry.performLocalQuery(query);
        List<HeatmapImageInfo> results = query.getResults();
        return !results.isEmpty() ? results.get(0) : null;
    }

    /**
     * Adds a listener for images.
     *
     * @param listener the listener
     */
    public void addListener(DataRegistryListener<HeatmapImageInfo> listener)
    {
        myDataRegistry.addChangeListener(listener, CATEGORY, IMAGE_PROPERTY);
    }

    /** Heat map image and other info. */
    public static class HeatmapImageInfo
    {
        /** The image. */
        private final BufferedImage myImage;

        /** The model in which the heatmap parameters are described. */
        private HeatmapModel myModel;

        /** The bounding box. */
        private final GeographicBoundingBox myBbox;

        /** The data type. */
        private final DataTypeInfo myDataType;

        /**
         * Constructor.
         *
         * @param image The image
         * @param model the model that backs the info.
         * @param dataType The data type
         */
        public HeatmapImageInfo(BufferedImage image, HeatmapModel model, DataTypeInfo dataType)
        {
            myImage = image;
            myModel = model;
            myBbox = myModel.getBbox();
            myDataType = dataType;
        }

        /**
         * Gets the image.
         *
         * @return the image
         */
        public BufferedImage getImage()
        {
            return myImage;
        }

        /**
         * Gets the bbox.
         *
         * @return the bbox
         */
        public GeographicBoundingBox getBbox()
        {
            return myBbox;
        }

        /**
         * Gets the dataType.
         *
         * @return the dataType
         */
        public DataTypeInfo getDataType()
        {
            return myDataType;
        }

        /**
         * Gets the value of the {@link #myModel} field.
         *
         * @return the value stored in the {@link #myModel} field.
         */
        public HeatmapModel getModel()
        {
            return myModel;
        }
    }
}
