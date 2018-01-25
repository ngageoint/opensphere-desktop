package io.opensphere.core.order.impl;

import java.util.Objects;

import org.apache.commons.lang.math.IntRange;

import io.opensphere.core.order.OrderCategory;
import io.opensphere.core.order.impl.config.v1.OrderCategoryConfig;

/** The default implementation for an order category. */
public class DefaultOrderCategory implements OrderCategory
{
    /** The family for elevation participants. */
    public static final String DEFAULT_ELEVATION_FAMILY = "Elevation Family";

    /** The family for feature layer participants. */
    public static final String DEFAULT_FEATURE_LAYER_FAMILY = "Feature Layer Family";

    /** The family for image layer participants. */
    public static final String DEFAULT_IMAGE_LAYER_FAMILY = "Image Layer Family";

    /**
     * The category of participants who provide elevation data for earth
     * terrain.
     */
    public static final OrderCategory EARTH_ELEVATION_CATEGORY = new DefaultOrderCategory("Earth Terrain Layers",
            new IntRange(10000, 20000));

    /** The category for ordered features. */
    public static final OrderCategory FEATURE_CATEGORY = new DefaultOrderCategory("Feature Layers", new IntRange(40000, 45000));

    /** The category for image layers which are base maps. */
    public static final OrderCategory IMAGE_BASE_MAP_CATEGORY = new DefaultOrderCategory("Reference Layers",
            new IntRange(10000, 15000));

    /** The category for image layers which are data tiles. */
    public static final OrderCategory IMAGE_DATA_CATEGORY = new DefaultOrderCategory("Tile Layers", new IntRange(20000, 25000));

    /** The category for image layers which are overlay images. */
    public static final OrderCategory IMAGE_OVERLAY_CATEGORY = new DefaultOrderCategory("Image Overlay Layers",
            new IntRange(30000, 35000));

    /** The category for HUD and other screen coordinate based images. */
    public static final OrderCategory IMAGE_SCREEN_CATEGORY = new DefaultOrderCategory("Image Screen Layers",
            new IntRange(36000, 37000));

    /** The id of this category. */
    private final String myCategoryId;

    /** The allowable order range for this category. */
    private final IntRange myOrderRange;

    /**
     * Construct using a configuration.
     *
     * @param config The configuration.
     */
    public DefaultOrderCategory(OrderCategoryConfig config)
    {
        this(config.getCategoryId(), new IntRange(config.getRangeMin(), config.getRangeMax()));
    }

    /**
     * Constructor.
     *
     * @param categoryId The unique id of this category.
     * @param orderRange The valid range of order values for this category.
     */
    public DefaultOrderCategory(String categoryId, IntRange orderRange)
    {
        myCategoryId = categoryId;
        myOrderRange = new IntRange(orderRange.getMinimumInteger(), orderRange.getMaximumInteger());
    }

    /**
     * Categories are considered to be equal when the categoryId's are the same
     * regardless of other factors.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof DefaultOrderCategory))
        {
            return false;
        }
        DefaultOrderCategory other = (DefaultOrderCategory)obj;
        return Objects.equals(myCategoryId, other.myCategoryId);
    }

    @Override
    public String getCategoryId()
    {
        return myCategoryId;
    }

    @Override
    public int getOrderMax()
    {
        return myOrderRange.getMaximumInteger();
    }

    @Override
    public int getOrderMin()
    {
        return myOrderRange.getMinimumInteger();
    }

    @Override
    public IntRange getOrderRange()
    {
        return myOrderRange;
    }

    /**
     * Categories are considered to be equal when the categoryId's are the same
     * regardless of other factors, so do not include anything else in the hash
     * code.
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myCategoryId == null ? 0 : myCategoryId.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append("Order Category Id[").append(myCategoryId).append("] OrderRange[").append(myOrderRange).append("])");
        return sb.toString();
    }
}
