package io.opensphere.mantle.data.util;

import java.awt.Component;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.VisualizationState;

/**
 * Utilities package for actions that can be taken with a DataElement.
 */
public interface DataElementActionUtils
{
    /**
     * Requests the map go to ( re-center ) on the selected feature.
     *
     * @param deId the data element cache id.
     * @param flyTo - true to fly to the feature, false to center on.
     * @return true, if successful, false if could not re-center for feature.
     */
    boolean gotoSelectedFeature(long deId, boolean flyTo);

    /**
     * Requests the map go to ( re-center ) on the selected feature.
     *
     * @param deIds the data element cache ids.
     * @param flyTo - true to fly to the feature, false to center on.
     * @return true, if successful, false if could not re-center for feature.
     */
    boolean gotoSelectedFeature(long[] deIds, boolean flyTo);

    /**
     * Purges the data elements from the Mantle.
     *
     * @param dtiHint the DataTypeInfo if this is a single type being removed.
     * @param idsToPurge the ids to purge
     * @param confirmDialogParentComponent the confirm dialog parent component
     *            (may be null, if so uses application main frame as parent)
     */
    void purgeDataElements(DataTypeInfo dtiHint, long[] idsToPurge, Component confirmDialogParentComponent);

    /**
     * Purge data elements from the application with basic criteria used
     * generally by the list tool.
     *
     * @param vsFilter the {@link VisualizationState} {@link Predicate} to
     *            select data elements
     * @param dtiSet the set of DataTypeInfo from which elements are to be
     *            selected and purged, if null or empty all types are considered
     * @param tsOfInterest the set of timespans to be purged from, or no time
     *            constraint if empty or null
     * @param confirmDialogParentComponent the confirm dialog parent component
     *            (may be null, if so uses application main frame as parent)
     * @return the number of elements purged
     */
    int purgeDataElements(final Predicate<? super VisualizationState> vsFilter, Set<DataTypeInfo> dtiSet,
            Set<TimeSpan> tsOfInterest, Component confirmDialogParentComponent);

    /**
     * Query point radius from data element centers.
     *
     * @param dataElementIds the data element ids
     * @param radiusMeters the radius meters
     * @param source the source of the query
     */
    void queryPointRadiusFromDataElmentCenters(List<Long> dataElementIds, double radiusMeters, Object source);

    /**
     * Request geometry rebuild.
     *
     * @param dti the {@link DataTypeInfo}
     * @param source the source of the request
     */
    void requestGeometryRebuild(DataTypeInfo dti, Object source);
}
