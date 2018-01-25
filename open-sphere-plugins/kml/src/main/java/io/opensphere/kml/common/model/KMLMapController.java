package io.opensphere.kml.common.model;

import java.util.Collection;

/**
 * An interface for a KML map controller.
 */
public interface KMLMapController extends KMLController
{
    /**
     * Adds features to the controller.
     *
     * @param features The list of features
     */
    void addFeatures(Collection<? extends KMLFeature> features);

    /**
     * Sets the selection state of the features.
     *
     * @param features The list of features
     * @param isSelected Whether the features are to be selected
     */
    void setFeaturesSelected(Collection<? extends KMLFeature> features, boolean isSelected);

    /**
     * Shows the feature details, i.e. in a popup.
     *
     * @param feature The feature
     */
    void showFeatureDetails(KMLFeature feature);

    /**
     * Updates the visibility of the features.
     *
     * @param features The list of features
     */
    void updateFeatureVisibility(Collection<? extends KMLFeature> features);
}
