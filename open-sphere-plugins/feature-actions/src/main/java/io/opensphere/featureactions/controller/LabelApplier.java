package io.opensphere.featureactions.controller;

import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.AbstractFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.AbstractPathVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PointFeatureVisualizationStyle;

/**
 * Applies a label style to a created feature action style.
 */
@Deprecated
public class LabelApplier
{
    /**
     * Applies the label style to the passed in style.
     *
     * @param style The style to apply label styles to.
     * @param containsLabelAction Does this style already contain a label
     *            feature action.
     * @param defaultStyleCopy The default style to copy label styles from.
     */
    public void applyLabel(FeatureVisualizationStyle style, boolean containsLabelAction,
            FeatureVisualizationStyle defaultStyleCopy)
    {
        if (style != null && !containsLabelAction && defaultStyleCopy instanceof AbstractPathVisualizationStyle)
        {
            AbstractPathVisualizationStyle toCopy = (AbstractPathVisualizationStyle)defaultStyleCopy;
            style.setParameter(AbstractPathVisualizationStyle.LABEL_ENABLED_PROPERTY_KEY,
                    Boolean.valueOf(toCopy.isLabelEnabled()), this);
            style.setParameter(AbstractPathVisualizationStyle.LABEL_COLOR_PROPERTY_KEY, toCopy.getLabelColor(), this);
            style.setParameter(AbstractPathVisualizationStyle.LABEL_COLUMN_KEY_PROPERTY_KEY, toCopy.getLabelColumnPropertyName(),
                    this);
            style.setParameter(AbstractPathVisualizationStyle.LABEL_SIZE_PROPERTY_KEY, Integer.valueOf(toCopy.getLabelSize()),
                    this);
        }
        else if (style != null && !containsLabelAction && defaultStyleCopy instanceof AbstractFeatureVisualizationStyle)
        {
            AbstractFeatureVisualizationStyle toCopy = (AbstractFeatureVisualizationStyle)defaultStyleCopy;
            style.setParameter(PointFeatureVisualizationStyle.LABEL_ENABLED_PROPERTY_KEY,
                    Boolean.valueOf(toCopy.isLabelEnabled()), this);
            style.setParameter(PointFeatureVisualizationStyle.LABEL_COLOR_PROPERTY_KEY, toCopy.getLabelColor(), this);
            style.setParameter(PointFeatureVisualizationStyle.LABEL_COLUMN_KEY_PROPERTY_KEY, toCopy.getLabelColumnPropertyName(),
                    this);
            style.setParameter(PointFeatureVisualizationStyle.LABEL_SIZE_PROPERTY_KEY, Integer.valueOf(toCopy.getLabelSize()),
                    this);
        }
    }
}
