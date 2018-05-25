package io.opensphere.heatmap;

import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.data.DataRegistryListener;
import io.opensphere.core.data.DataRegistryListenerAdapter;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.MutableConstraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.heatmap.DataRegistryHelper.HeatmapImageInfo;
import io.opensphere.mantle.crust.MiniMantle;

/** Heat map transformer. */
public class HeatmapTransformer extends DefaultTransformer
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The data registry helper. */
    private final DataRegistryHelper myRegistryHelper;

    /** Connects core geometries to mantle layer. */
    private final MiniMantle myMiniMantle;

    /** The data registry listener. */
    private final DataRegistryListener<HeatmapImageInfo> myListener = new DataRegistryListenerAdapter<HeatmapImageInfo>()
    {
        @Override
        public void valuesAdded(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends HeatmapImageInfo> newValues,
                Object source)
        {
            for (HeatmapImageInfo value : newValues)
            {
                publishImage(value);
            }
        }

        @Override
        public void valuesRemoved(DataModelCategory dataModelCategory, long[] ids,
                Iterable<? extends HeatmapImageInfo> removedValues, Object source)
        {
            for (HeatmapImageInfo value : removedValues)
            {
                unpublishImage(value);
            }
        }

        @Override
        public boolean isIdArrayNeeded()
        {
            return false;
        }
    };

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public HeatmapTransformer(Toolbox toolbox)
    {
        super(toolbox.getDataRegistry());
        myToolbox = toolbox;
        myRegistryHelper = new DataRegistryHelper(toolbox.getDataRegistry());
        myMiniMantle = new MiniMantle(toolbox.getEventManager());
    }

    @Override
    public void open()
    {
        myMiniMantle.open();
        myRegistryHelper.addListener(myListener);
        super.open();
    }

    @Override
    public void close()
    {
        super.close();
        myMiniMantle.close();
    }

    /**
     * Publishes an image to the registry.
     *
     * @param imageInfo the image info
     */
    void publishImage(HeatmapImageInfo imageInfo)
    {
        Collection<TileGeometry> geoms = Collections.singleton(createGeometry(imageInfo));
        publishGeometries(geoms, Collections.emptyList());
        myMiniMantle.addGeometries(imageInfo.getDataType().getTypeKey(), geoms);
    }

    /**
     * Un-publishes an image from the registry.
     *
     * @param imageInfo the image info
     */
    void unpublishImage(HeatmapImageInfo imageInfo)
    {
        Collection<Geometry> geoms = myMiniMantle.removeGeometries(imageInfo.getDataType().getTypeKey());
        if (CollectionUtilities.hasContent(geoms))
        {
            publishGeometries(Collections.emptyList(), geoms);
        }
    }

    /**
     * Creates the tile geometry.
     *
     * @param imageInfo the image info
     * @return the tile geometry
     */
    private TileGeometry createGeometry(HeatmapImageInfo imageInfo)
    {
        TileGeometry.Builder<GeographicPosition> builder = new TileGeometry.Builder<>();
        builder.setBounds(imageInfo.getBbox());

        ImageProvider<String> imageProvider = new ImageProvider<String>()
        {
            @Override
            public Image getImage(String key)
            {
                return new ImageIOImage(imageInfo.getImage());
            }
        };
        builder.setImageManager(new ImageManager("dontMatter", imageProvider));
        OrderParticipantKey orderKey = imageInfo.getDataType().getOrderKey();
        int zOrder = myToolbox.getOrderManagerRegistry().getOrderManager(orderKey).getOrder(orderKey);
        TileRenderProperties renderProperties = new DefaultTileRenderProperties(zOrder, true, false);
        Constraints constraints;
        if (imageInfo.getDataType().getTimeExtents().getTimespans().stream().allMatch(t -> t.isTimeless()))
        {
            constraints = null;
        }
        else
        {
            constraints = new MutableConstraints(
                    TimeConstraint.getTimeConstraint(imageInfo.getDataType().getTimeExtents().getExtent()), null);
        }

        TileGeometry geom = new TileGeometry(builder, renderProperties, constraints, imageInfo.getDataType().getTypeKey());
        return geom;
    }
}
