package io.opensphere.mantle.data.geom.style;

import java.awt.Color;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;

/**
 * The Interface FeatureVisualizationStyle.
 */
public interface FeatureVisualizationStyle extends VisualizationStyle
{
    /**
     * All changes require rebuild of geometry.
     *
     * @return true, if all property changes will require a geometry rebuild.
     */
    boolean allChangesRequireRebuild();

    /**
     * Takes all of the provided {@link MapGeometrySupport} and converts them
     * into a combined {@link AbstractRenderableGeometry} using the style
     * information for this style and the remainder of the provided information.
     * Note: that the lists for mgs, mdpList, vsStateList will be in the same
     * order and size as the array of element ids.
     *
     * Note: that mdpList will be null unless requiresMetaData() returns true
     * for this style type).
     *
     * @param setToAddTo the set to add the resultant {@link Geometry}
     * @param builderData the builder data
     * @param renderPropertyPool the {@link RenderPropertyPool} for the
     *            transformation
     * @throws IllegalArgumentException the illegal argument exception
     */
    void createCombinedGeometry(Set<Geometry> setToAddTo, FeatureCombinedGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool) throws IllegalArgumentException;

    /**
     * Converts the provided {@link MapGeometrySupport} to a.
     *
     * @param setToAddTo the set to add the resultant {@link Geometry}
     * @param builderData the builder data
     * @param renderPropertyPool the render property pool
     * @throws IllegalArgumentException the illegal argument exception if input
     *             is unacceptable for the necessary transformation.
     *             {@link Geometry} using the style information for this style,
     *             and the remainder of the provided information.
     */
    void createIndividualGeometry(Set<Geometry> setToAddTo, FeatureIndividualGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool) throws IllegalArgumentException;

    /**
     * Derive geometry from render property change.
     *
     * This is only called if the visualization style parameters changed in the
     * style all apply only to render properties and geometries do not need to
     * be fully rebuilt.
     *
     * @param changedParameterKeyToParameterMap the changed parameter key to
     *            parameter map
     * @param rpp the {@link RenderPropertyPool}
     * @param geom the {@link AbstractRenderableGeometry}
     * @param dti the dti
     * @param vs the vs
     * @param defaultVS the default vs
     * @param mdp the mdp
     * @return the derived {@link AbstractRenderableGeometry}
     */
    AbstractRenderableGeometry deriveGeometryFromRenderPropertyChange(
            Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap, RenderPropertyPool rpp,
            AbstractRenderableGeometry geom, DataTypeInfo dti, VisualizationState vs, VisualizationState defaultVS,
            MetaDataProvider mdp);

    /**
     * Give the set of changed style parameters that only impact render
     * properties, create a new altered render property with the changes.
     *
     * @param changedParameterKeyToParameterMap the changed parameter key to
     *            parameter map
     * @param dti the dti
     * @param vs the vs
     * @param defaultVS the default vs
     * @param mdp the mdp
     * @param orig the orig
     * @return the altered render property
     */
    BaseRenderProperties getAlteredRenderProperty(Map<String, VisualizationStyleParameter> changedParameterKeyToParameterMap,
            DataTypeInfo dti, VisualizationState vs, VisualizationState defaultVS, MetaDataProvider mdp,
            BaseRenderProperties orig);

    /**
     * Gets the applies to.
     *
     * @return the applies to
     */
    AppliesTo getAppliesTo();

    /**
     * Gets the color for the feature vis type.
     *
     * @return the color
     */
    Color getColor();

    /**
     * If this style will only work for particular {@link MapVisualizationType}
     * 's, this set will define which types those are. If it is generic the set
     * will be empty.
     *
     * @return the required map vis types
     */
    Set<MapVisualizationType> getRequiredMapVisTypes();

    /**
     * Get if this style is sensitive to the selection state of the elements.
     *
     * @return {@code true} if this style is selection-sensitive.
     */
    boolean isSelectionSensitiveStyle();

    /**
     * Requires meta data.
     *
     * @return true, if successful
     */
    boolean requiresMetaData();

    /**
     * Supports labels. ( true if the style supports labels, false if not).
     *
     * @return true ( supports labels )
     */
    boolean supportsLabels();

    /**
     * The Enum AppliesTo.
     */
    enum AppliesTo
    {
        /** The ALL_ELEMENTS. */
        ALL_ELEMENTS,

        /** The INDIVIDUAL_ELEMENT. */
        INDIVIDUAL_ELEMENT;

        /**
         * Checks if is all elements.
         *
         * @return true, if is all elements
         */
        public boolean isAllElements()
        {
            return this == ALL_ELEMENTS;
        }

        /**
         * Checks if is individual elements.
         *
         * @return true, if is individual elements
         */
        public boolean isIndividualElements()
        {
            return this == INDIVIDUAL_ELEMENT;
        }
    }
}
