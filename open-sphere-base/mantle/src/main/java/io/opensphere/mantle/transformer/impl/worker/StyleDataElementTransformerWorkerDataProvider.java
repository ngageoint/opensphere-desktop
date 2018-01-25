package io.opensphere.mantle.transformer.impl.worker;

import gnu.trove.map.hash.TLongLongHashMap;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;

/**
 * The Interface DataElementTransformerWorkerDataProvider.
 */
public interface StyleDataElementTransformerWorkerDataProvider extends DataElementTransformerWorkerDataProvider
{
    /**
     * Any style applies to all elements.
     *
     * @return true, if successful
     */
    boolean anyStyleAppliesToAllElements();

    /**
     * Gets the combined id given a mgs type id and a data model id.
     *
     * @param mgsTypeId the mgs type id
     * @param dataModelId the data model id
     * @return the combined id
     */
    long getCombinedId(long mgsTypeId, long dataModelId);

    /**
     * Given a geometry id, extracts the portion of the Id that is from the data
     * model.
     *
     * @param geomId the geometry id
     * @return data model id
     */
    long getDataModelIdFromGeometryId(long geomId);

    /**
     * Gets the element ids for geometry ids.
     *
     * @param geometryIds the geometry ids
     * @return the element ids for geometry ids
     */
    TLongLongHashMap getElementIdsForGeometryIds(long[] geometryIds);

    /**
     * Gets the mgs type id given an mgs.
     *
     * @param mgs the {@link MapGeometrySupport}
     * @return the style id
     */
    long getMGSTypeId(MapGeometrySupport mgs);

    /**
     * Gets the style id from geometry id.
     *
     * @param geomId the geom id
     * @return the style id from geometry id
     */
    long getMGSTypeIdFromGeometryId(long geomId);

    /**
     * Gets the style by the style id.
     *
     * @param mgsTypeId the {@link MapGeometrySupport} type id.
     * @param elementId the data element ID
     * @return the FeatureVisualizationStyle.
     */
    FeatureVisualizationStyle getStyle(long mgsTypeId, long elementId);

    /**
     * Gets the {@link FeatureVisualizationStyle} for a
     * {@link MapGeometrySupport}.
     *
     * @param mgs the {@link MapGeometrySupport} for which to retrieve the
     *            style.
     * @param elementId the data element ID
     * @return the {@link FeatureVisualizationStyle}.
     */
    FeatureVisualizationStyle getStyle(MapGeometrySupport mgs, long elementId);

    /**
     * Checks to see if a particular geometry id is managed by this transformer.
     *
     *
     * Note: This is not an efficient calls as it may have to do exhaustive
     * searching, do not call repeatedly or when time is critical.
     *
     * @param geomId the geometry id to check.
     * @return true, if this transformer manages this geometry id.
     */
    boolean hasGeometryId(long geomId);

    /**
     * Styles require meta data.
     *
     * @return true, if successful
     */
    boolean stylesRequireMetaData();
}
