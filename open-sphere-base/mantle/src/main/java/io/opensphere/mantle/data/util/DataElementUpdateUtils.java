package io.opensphere.mantle.data.util;

import java.awt.Color;
import java.util.List;
import java.util.Set;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.rangeset.RangedLongSet;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.element.event.DataElementAltitudeChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementColorChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementLOBVsibilityChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementSelectionChangeEvent;

/**
 * The Interface DataElementUpdateUtils.
 */
public interface DataElementUpdateUtils
{
    /**
     * Uses a {@link VisualizationStateAdjustmentVisitor} to make changes to the
     * visualization state for a DataElement specified with a registry id.
     * Provided the VisualizationState exists. Publishes events corresponding to
     * the changes as determined in the adjustor.
     *
     * @param vsHint the {@link VisualizationState} to adjust ( or if null it
     *            will be looked up by the id )
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     * @param adjustor the adjustor to make the changes
     * @param id the id of the data element in the registry
     * @param source the source the originator of the change
     */
    void adjustVisualizationState(VisualizationState vsHint, String dataTypeKeyHint, VisualizationStateAdjustmentVisitor adjustor,
            long id, Object source);

    /**
     * Adjust visualization states of the specified DataElements using a.
     *
     * @param adjustor the adjustor to make the adjustment
     * @param ids the ids to be adjusted
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     *            assumes if provided all types are the same.
     * @param source the originator of the change.
     *            {@link VisualizationStateAdjustmentVisitor}.
     */
    void adjustVisualizationStates(VisualizationStateAdjustmentVisitor adjustor, List<Long> ids, String dataTypeKeyHint,
            Object source);

    /**
     * Sets the data altitude adjust ( if it exists ). Fires a
     *
     * @param alt the alt
     * @param id the id
     * @param vsHint the {@link VisualizationState} to adjust ( or if null it
     *            will be looked up by the id )
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     * @param source the source {@link DataElementAltitudeChangeEvent} via the
     *            {@link EventManager} if a changed occurred.
     */
    void setDataElementAltitudeAdjust(float alt, long id, VisualizationState vsHint, String dataTypeKeyHint, Object source);

    /**
     * Sets the data element color ( if it exists ). Fires a
     *
     * @param aColor the aColor
     * @param id the id
     * @param vsHint the {@link VisualizationState} to adjust ( or if null it
     *            will be looked up by the id )
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     *            assumes if set all are same type.
     * @param source the source {@link DataElementColorChangeEvent} via the
     *            {@link EventManager} if a changed occurred.
     */
    void setDataElementColor(Color aColor, long id, VisualizationState vsHint, String dataTypeKeyHint, Object source);

    /**
     * Sets the data element lob visible flag ( if it exists ). Fires a
     *
     * @param lobVisible the lobVisible
     * @param id the id
     * @param vsHint the {@link VisualizationState} to adjust ( or if null it
     *            will be looked up by the id )
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     * @param source the source {@link DataElementLOBVsibilityChangeEvent} via
     *            the {@link EventManager} if a changed occurred.
     */
    void setDataElementLOBVisible(boolean lobVisible, long id, VisualizationState vsHint, String dataTypeKeyHint, Object source);

    /**
     * Sets the altitude adjust of multiple {@link DataElement}. Dispatches all
     * events for the elements that existed and were changed.
     *
     * @param alt the alt
     * @param ids the ids to change
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     *            assumes if provided all types are the same.
     * @param source the source the originator of the change request
     */
    void setDataElementsAltitudeAdjust(float alt, List<Long> ids, String dataTypeKeyHint, Object source);

    /**
     * Sets the altitude adjust of multiple {@link DataElement}. Dispatches all
     * events for the elements that existed and were changed.
     *
     * @param alt the alt
     * @param ids the ids to change
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     * @param source the source the originator of the change request
     */
    void setDataElementsAltitudeAdjust(float alt, RangedLongSet ids, String dataTypeKeyHint, Object source);

    /**
     * Sets the color of multiple {@link DataElement}. Dispatches all events for
     * the elements that existed and were changed.
     *
     * @param aColor the aColor
     * @param ids the ids to change
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     * @param source the source the originator of the change request
     */
    void setDataElementsColor(Color aColor, List<Long> ids, String dataTypeKeyHint, Object source);

    /**
     * Sets the color of multiple {@link DataElement}. Dispatches all events for
     * the elements that existed and were changed.
     *
     * @param aColor the aColor
     * @param ids the ids to change
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     * @param source the source the originator of the change request
     */
    void setDataElementsColor(Color aColor, RangedLongSet ids, String dataTypeKeyHint, Object source);

    /**
     * Sets the color of multiple {@link DataElement}. Dispatches all events for
     * the elements that existed and were changed.
     *
     * @param idToColorMap the data element registry id to color map.
     * @param defaultColor the default color for dots that are not in the map
     * @param ids the ids to change
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     * @param source the source the originator of the change request
     */
    void setDataElementsColors(TLongObjectHashMap<Color> idToColorMap, Color defaultColor, List<Long> ids, String dataTypeKeyHint,
            Object source);

    /**
     * Sets the data element selection ( if it exists ). Fires a
     *
     * @param selected the selected
     * @param id the id
     * @param vsHint the {@link VisualizationState} to adjust ( or if null it
     *            will be looked up by the id )
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     * @param source the source {@link DataElementSelectionChangeEvent} via the
     *            {@link EventManager} if a changed occurred.
     */
    void setDataElementSelected(boolean selected, long id, VisualizationState vsHint, String dataTypeKeyHint, Object source);

    /**
     * Sets the lob visible flag of multiple {@link DataElement}. Dispatches all
     * events for the elements that existed and were changed.
     *
     * @param lobVisible the lobVisible flag
     * @param ids the ids to change
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     *            assumes if set all are same type.
     * @param source the source the originator of the change request
     */
    void setDataElementsLOBVisible(boolean lobVisible, List<Long> ids, String dataTypeKeyHint, Object source);

    /**
     * Sets the lob visible flag of multiple {@link DataElement}. Dispatches all
     * events for the elements that existed and were changed.
     *
     * @param lobVisible the lobVisible flag
     * @param ids the ids to change
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     *            assumes if set all are same type.
     * @param source the source the originator of the change request
     */
    void setDataElementsLOBVisible(boolean lobVisible, RangedLongSet ids, String dataTypeKeyHint, Object source);

    /**
     * Sets the data elements opacity of multiple {@link DataElement}.
     * Dispatches all events for the elements that existed and were changed.
     *
     * @param opacity the opacity 0 to 255
     * @param ids the ids to change
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     * @param source the source the originator of the change request
     */
    void setDataElementsOpacity(int opacity, List<Long> ids, String dataTypeKeyHint, Object source);

    /**
     * Sets the selection of multiple {@link DataElement}. Dispatches all events
     * for the elements that existed and were changed.
     *
     * @param selected the selected flag
     * @param ids the ids to change
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     *            assumes if set all are same type.
     * @param source the source the originator of the change request
     */
    void setDataElementsSelected(boolean selected, List<Long> ids, String dataTypeKeyHint, Object source);

    /**
     * Sets the selection of multiple {@link DataElement}. Dispatches all events
     * for the elements that existed and were changed.
     *
     * @param selected the selected flag
     * @param ids the ids to change
     * @param vsHint the {@link VisualizationState} to adjust ( or if null it
     *            will be looked up by the id )
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     * @param source the source the originator of the change request
     */
    void setDataElementsSelected(boolean selected, RangedLongSet ids, VisualizationState vsHint, String dataTypeKeyHint,
            Object source);

    /**
     * Sets the selection of multiple {@link DataElement}. Those that are to be
     * selected are determined by the contents of the select set, all others
     * will be deselected Dispatches all events for the elements that existed
     * and were changed.
     *
     * @param selectedSet the set of ids in the idsToModify list that are to be
     *            selected
     * @param idsToModify the ids to change ( selected if in selected set,
     *            deselect otherwise)
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     *            assumes if set all are same type.
     * @param source the source the originator of the change request
     */
    void setDataElementsSelectionState(Set<Long> selectedSet, List<Long> idsToModify, String dataTypeKeyHint, Object source);

    /**
     * Sets the visibility of multiple {@link DataElement}. Dispatches all
     * events for the elements that existed and were changed.
     *
     * @param visible the visible flag
     * @param ids the ids to change
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     *            assumes if set all are same type.
     * @param source the source the originator of the change request
     */
    void setDataElementsVisibility(boolean visible, List<Long> ids, String dataTypeKeyHint, Object source);

    /**
     * Sets the visibility of multiple {@link DataElement}. Dispatches all
     * events for the elements that existed and were changed.
     *
     * @param visible the visible flag
     * @param ids the ids to change
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     *            assumes if set all are same type.
     * @param source the source the originator of the change request
     */
    void setDataElementsVisibility(boolean visible, RangedLongSet ids, String dataTypeKeyHint, Object source);

    /**
     * Sets the data element visibility ( if it exists ). Fires a
     * DataElementVisibilityChangeEvent via the {@link EventManager} if a
     * changed occurred.
     *
     * @param visible the visible
     * @param id the id
     * @param vsHint the {@link VisualizationState} to adjust ( or if null it
     *            will be looked up by the id )
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     * @param source the source
     */
    void setDataElementVisibility(boolean visible, long id, VisualizationState vsHint, String dataTypeKeyHint, Object source);

    /**
     * Sets the data element visualization state. Fires events for changes made
     * on the update.
     *
     * @param adjustor to make the changes and matching state
     * @param id the id - the DataElement registry id
     * @param vsHint the {@link VisualizationState} to adjust ( or if null it
     *            will be looked up by the id )
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     * @param source the source - the source of the change
     */
    void setDataElementVisualizationState(AdjustToMatchSelectedFields adjustor, long id, VisualizationState vsHint,
            String dataTypeKeyHint, Object source);

    /**
     * Adjust the visualization states for all specified data elements.
     *
     * @param adjustor to make the changes and matching state
     * @param ids the ids - the DataElement registry ids
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     *            assumes if set all are same type.
     * @param source the source - the source of the change
     */
    void setDataElementVisualizationStates(AdjustToMatchSelectedFields adjustor, List<Long> ids, String dataTypeKeyHint,
            Object source);

    /**
     * Adjust the visualization states for all specified data elements.
     *
     * @param adjustor to make the changes and matching state
     * @param ids the ids - the DataElement registry ids
     * @param dataTypeKeyHint the data type key ( or if null will be looked up )
     *            assumes if set all are same type.
     * @param source the source - the source of the change
     */
    void setDataElementVisualizationStates(AdjustToMatchSelectedFields adjustor, RangedLongSet ids, String dataTypeKeyHint,
            Object source);
}
