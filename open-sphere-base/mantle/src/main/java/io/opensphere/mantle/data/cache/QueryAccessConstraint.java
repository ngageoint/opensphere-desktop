package io.opensphere.mantle.data.cache;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.element.VisualizationState;

/**
 * A configuration of a constraint on a CacheQuery that gives hints to the query
 * engine as to which types of information from elements will be required for
 * the query to execute.
 *
 * This helps the query know what data is needed by the query to more
 * effectively optimize the query against a persistent store. If a data type is
 * not specified it may or may not be available in the {@link CacheEntryView}
 * presented to the CacheQuery in its methods. Any types not identified as
 * available in the constraint are always available such as {@link TimeSpan}
 * etc.
 *
 * Generally DTIKey and {@link VisualizationState} are always available even if
 * not specified, however this may change in the future and developers should
 * plan accordingly.
 */
public class QueryAccessConstraint
{
    /** The my dti key. */
    private final boolean myDTIKey;

    /** The my map geometry support. */
    private final boolean myMapGeometrySupport;

    /** The my meta data provider. */
    private final boolean myMetaDataProvider;

    /** The my origin id. */
    private final boolean myOriginId;

    /** The my vis state. */
    private final boolean myVisState;

    /**
     * Constraint with all.
     *
     * @return the query element access constraint
     */
    public static QueryAccessConstraint constraintWithAll()
    {
        return new QueryAccessConstraint(true, true, true, true, true);
    }

    /**
     * Constraint with all but map geometry support.
     *
     * @return the query element access constraint
     */
    public static QueryAccessConstraint constraintWithAllButMapGeometrySupport()
    {
        return new QueryAccessConstraint(true, true, true, true, false);
    }

    /**
     * Instantiates a new query element access constraint.
     *
     * @param dataTypeInfoRequired the data type info required
     * @param visualizationStateRequired the visualization state required
     * @param originIdRequired the origin id required
     * @param metaDataProviderRequired the meta data provider required
     * @param mapGeomSupport the map geom support
     */
    public QueryAccessConstraint(boolean dataTypeInfoRequired, boolean visualizationStateRequired, boolean originIdRequired,
            boolean metaDataProviderRequired, boolean mapGeomSupport)
    {
        myDTIKey = dataTypeInfoRequired;
        myVisState = visualizationStateRequired;
        myOriginId = originIdRequired;
        myMetaDataProvider = metaDataProviderRequired;
        myMapGeometrySupport = mapGeomSupport;
    }

    /**
     * Checks if is data type info key required.
     *
     * @return true, if is data type info key required
     */
    public boolean isDataTypeInfoKeyRequired()
    {
        return myDTIKey;
    }

    /**
     * Checks if is map geometry support required.
     *
     * @return true, if is map geometry support required
     */
    public boolean isMapGeometrySupportRequired()
    {
        return myMapGeometrySupport;
    }

    /**
     * Checks if is meta data provider required.
     *
     * @return true, if is meta data provider required
     */
    public boolean isMetaDataProviderRequired()
    {
        return myMetaDataProvider;
    }

    /**
     * Checks if is origin id required.
     *
     * @return true, if is origin id required
     */
    public boolean isOriginIdRequired()
    {
        return myOriginId;
    }

    /**
     * Checks if is visualization state required.
     *
     * @return true, if is visualization state required
     */
    public boolean isVisualizationStateRequired()
    {
        return myVisState;
    }

    /**
     * Requires cached content.
     *
     * @return true, if successful
     */
    public boolean requiresCachedContent()
    {
        return myOriginId || myMetaDataProvider || myMapGeometrySupport;
    }
}
