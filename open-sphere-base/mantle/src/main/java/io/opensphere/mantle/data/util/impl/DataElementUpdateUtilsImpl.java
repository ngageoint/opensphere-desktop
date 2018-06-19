package io.opensphere.mantle.data.util.impl;

import java.awt.Color;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.rangeset.RangedLongSet;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.element.event.DataElementAltitudeChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementColorChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementLOBVsibilityChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementSelectionChangeEvent;
import io.opensphere.mantle.data.element.event.DataElementVisibilityChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.AbstractConsolidatedDataElementChangeEvent;
import io.opensphere.mantle.data.element.event.consolidators.AbstractDataElementEventConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementAltitudeChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementColorChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementLOBVisibilityChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementSelectionChangeConsolidator;
import io.opensphere.mantle.data.element.event.consolidators.DataElementVisibilityChangeConsolidator;
import io.opensphere.mantle.data.util.AdjustToMatchSelectedFields;
import io.opensphere.mantle.data.util.DataElementUpdateUtils;
import io.opensphere.mantle.data.util.VisualizationStateAdjustmentVisitor;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class DataElementUpdateUtils.
 */
@SuppressWarnings("PMD.GodClass")
public class DataElementUpdateUtilsImpl implements DataElementUpdateUtils
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DataElementUpdateUtilsImpl.class);

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new data element update utils.
     *
     * @param tb the tb
     */
    public DataElementUpdateUtilsImpl(Toolbox tb)
    {
        myToolbox = tb;
    }

    @Override
    public void adjustVisualizationState(VisualizationState vsHint, String dataTypeKeyHint,
            VisualizationStateAdjustmentVisitor adjustor, long id, Object source)
    {
        VisualizationState vs = vsHint;
        if (vs == null)
        {
            vs = MantleToolboxUtils.getDataElementLookupUtils(myToolbox).getVisualizationState(id);
            if (vs == null)
            {
                return;
            }
        }

        String dtKey = dataTypeKeyHint;
        if (dtKey == null)
        {
            dtKey = MantleToolboxUtils.getDataElementLookupUtils(myToolbox).getDataTypeInfoKey(id);
        }

        adjustor.adjustState(vs, id, dtKey, source);
        if (adjustor.wereAdjustmentsMade())
        {
            List<AbstractConsolidatedDataElementChangeEvent> events = adjustor.getEvents();
            if (!events.isEmpty())
            {
                for (AbstractConsolidatedDataElementChangeEvent evt : events)
                {
                    myToolbox.getEventManager().publishEvent(evt);
                }
            }
        }
    }

    @Override
    public void adjustVisualizationStates(VisualizationStateAdjustmentVisitor adjustor, List<Long> ids, String dataTypeKeyHint,
            Object source)
    {
        List<VisualizationState> vsList = MantleToolboxUtils.getDataElementLookupUtils(myToolbox).getVisualizationStates(ids);
        List<String> dataTypeKeyList = Collections.<String>emptyList();
        if (dataTypeKeyHint == null)
        {
            dataTypeKeyList = MantleToolboxUtils.getDataElementLookupUtils(myToolbox).getDataTypeInfoKeys(ids);
        }
        if (vsList != null && !vsList.isEmpty())
        {
            // Go through and make changes to the visualization states that
            // need to be changed.
            Iterator<String> dtKeyItr = dataTypeKeyList.iterator();
            Iterator<Long> idItr = ids.iterator();
            Long id = null;
            for (VisualizationState vs : vsList)
            {
                id = idItr.next();

                if (vs != null)
                {
                    adjustor.adjustState(vs, id.longValue(), dataTypeKeyHint == null ? dtKeyItr.next() : dataTypeKeyHint, source);
                }
            }

            if (adjustor.wereAdjustmentsMade())
            {
                List<AbstractConsolidatedDataElementChangeEvent> events = adjustor.getEvents();
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Sending out VS Change Events: " + events.get(0).getDescription());
                }
                for (AbstractConsolidatedDataElementChangeEvent evt : events)
                {
                    myToolbox.getEventManager().publishEvent(evt);
                }
            }
        }
    }

    @Override
    public void setDataElementAltitudeAdjust(float alt, long id, VisualizationState vsHint, String dataTypeKeyHint, Object source)
    {
        adjustVisualizationState(vsHint, dataTypeKeyHint, new AdjustAltitude(alt), id, source);
    }

    @Override
    public void setDataElementColor(Color aColor, long id, VisualizationState vsHint, String dataTypeKeyHint, Object source)
    {
        adjustVisualizationState(vsHint, dataTypeKeyHint, new AdjustColor(aColor), id, source);
    }

    @Override
    public void setDataElementLOBVisible(boolean lobVisible, long id, VisualizationState vsHint, String dataTypeKeyHint,
            Object source)
    {
        adjustVisualizationState(vsHint, dataTypeKeyHint, new AdjustLOBVisible(lobVisible), id, source);
    }

    @Override
    public void setDataElementsAltitudeAdjust(float alt, List<Long> ids, String dataTypeKeyHint, Object source)
    {
        adjustVisualizationStates(new AdjustAltitude(alt), ids, dataTypeKeyHint, source);
    }

    @Override
    public void setDataElementsAltitudeAdjust(float alt, RangedLongSet ids, String dataTypeKeyHint, Object source)
    {
        setDataElementsAltitudeAdjust(alt, CollectionUtilities.listView(ids.getValues()), dataTypeKeyHint, source);
    }

    @Override
    public void setDataElementsColor(Color aColor, List<Long> ids, String dataTypeKeyHint, Object source)
    {
        adjustVisualizationStates(new AdjustColor(aColor), ids, dataTypeKeyHint, source);
    }

    @Override
    public void setDataElementsColor(Color aColor, RangedLongSet ids, String dataTypeKeyHint, Object source)
    {
        setDataElementsColor(aColor, CollectionUtilities.listView(ids.getValues()), dataTypeKeyHint, source);
    }

    @Override
    public void setDataElementsColors(TLongObjectHashMap<Color> idToColorMap, Color defaultColor, List<Long> ids,
            String dataTypeKeyHint, Object source)
    {
        adjustVisualizationStates(new AdjustColors(idToColorMap, defaultColor), ids, dataTypeKeyHint, source);
    }

    @Override
    public void setDataElementSelected(boolean selected, long id, VisualizationState vsHint, String dataTypeKeyHint,
            Object source)
    {
        adjustVisualizationState(vsHint, dataTypeKeyHint, new AdjustSelected(selected), id, source);
    }

    @Override
    public void setDataElementsLOBVisible(boolean lobVisible, List<Long> ids, String dataTypeKeyHint, Object source)
    {
        adjustVisualizationStates(new AdjustLOBVisible(lobVisible), ids, dataTypeKeyHint, source);
    }

    @Override
    public void setDataElementsLOBVisible(boolean lobVisible, RangedLongSet ids, String dataTypeKeyHint, Object source)
    {
        setDataElementsLOBVisible(lobVisible, CollectionUtilities.listView(ids.getValues()), dataTypeKeyHint, source);
    }

    @Override
    public void setDataElementsOpacity(int opacity, List<Long> ids, String dataTypeKeyHint, Object source)
    {
        adjustVisualizationStates(new AdjustOpacity(opacity), ids, dataTypeKeyHint, source);
    }

    @Override
    public void setDataElementsSelected(boolean selected, List<Long> ids, String dataTypeKeyHint, Object source)
    {
        adjustVisualizationStates(new AdjustSelected(selected), ids, dataTypeKeyHint, source);
    }

    @Override
    public void setDataElementsSelected(boolean selected, RangedLongSet ids, VisualizationState vsHint, String dataTypeKeyHint,
            Object source)
    {
        setDataElementsSelected(selected, CollectionUtilities.listView(ids.getValues()), dataTypeKeyHint, source);
    }

    @Override
    public void setDataElementsSelectionState(Set<Long> selectedSet, List<Long> idsToModify, String dataTypeKeyHint,
            Object source)
    {
        adjustVisualizationStates(new AdjustSelectDeselect(selectedSet), idsToModify, dataTypeKeyHint, source);
    }

    @Override
    public void setDataElementsVisibility(boolean visible, List<Long> ids, String dataTypeKeyHint, Object source)
    {
        adjustVisualizationStates(new AdjustVisible(visible), ids, dataTypeKeyHint, source);
    }

    @Override
    public void setDataElementsVisibility(boolean visible, RangedLongSet ids, String dataTypeKeyHint, Object source)
    {
        setDataElementsVisibility(visible, CollectionUtilities.listView(ids.getValues()), dataTypeKeyHint, source);
    }

    @Override
    public void setDataElementVisibility(boolean visible, long id, VisualizationState vsHint, String dataTypeKeyHint,
            Object source)
    {
        adjustVisualizationState(vsHint, dataTypeKeyHint, new AdjustVisible(visible), id, source);
    }

    @Override
    public void setDataElementVisualizationState(AdjustToMatchSelectedFields adjustor, long id, VisualizationState vsHint,
            String dataTypeKeyHint, Object source)
    {
        if (adjustor != null)
        {
            adjustVisualizationState(vsHint, dataTypeKeyHint, adjustor, id, source);
        }
    }

    @Override
    public void setDataElementVisualizationStates(AdjustToMatchSelectedFields adjustor, List<Long> ids, String dataTypeKeyHint,
            Object source)
    {
        if (adjustor != null)
        {
            adjustVisualizationStates(adjustor, ids, dataTypeKeyHint, source);
        }
    }

    @Override
    public void setDataElementVisualizationStates(AdjustToMatchSelectedFields adjustor, RangedLongSet ids, String dataTypeKeyHint,
            Object source)
    {
        setDataElementVisualizationStates(adjustor, CollectionUtilities.listView(ids.getValues()), dataTypeKeyHint, source);
    }

    /**
     * Adjusts altitude of a VisualizationState.
     */
    public static class AdjustAltitude extends SingleFieldAdjustmentVisitor
    {
        /** The altitude. */
        private final float myAlt;

        /** The my consolidator. */
        private final DataElementAltitudeChangeConsolidator myConsolidator;

        /**
         * Instantiates a new adjust altitude.
         *
         * @param alt the altitude state
         */
        public AdjustAltitude(float alt)
        {
            myConsolidator = new DataElementAltitudeChangeConsolidator();
            myAlt = alt;
        }

        @Override
        public void adjustState(VisualizationState stateToAdjust, long dataElementId, String dtKey, Object source)
        {
            if (stateToAdjust != null && stateToAdjust.setAltitudeAdjust(myAlt))
            {
                myConsolidator.addEvent(
                        new DataElementAltitudeChangeEvent(dataElementId, dtKey, stateToAdjust.getAltitudeAdjust(), source));
            }
        }

        @Override
        public AbstractDataElementEventConsolidator<?> getConsolidator()
        {
            return myConsolidator;
        }
    }

    /**
     * Adjusts color of a VisualizationState.
     */
    public static class AdjustColor extends SingleFieldAdjustmentVisitor
    {
        /** The color. */
        private final Color myColor;

        /** The my consolidator. */
        private final DataElementColorChangeConsolidator myConsolidator;

        /**
         * Instantiates a new adjust color.
         *
         * @param c the color state
         */
        public AdjustColor(Color c)
        {
            myConsolidator = new DataElementColorChangeConsolidator();
            myColor = c;
        }

        @Override
        public void adjustState(VisualizationState stateToAdjust, long dataElementId, String dtKey, Object source)
        {
            if (stateToAdjust != null && stateToAdjust.setColor(myColor))
            {
                myConsolidator.addEvent(new DataElementColorChangeEvent(dataElementId, dtKey, stateToAdjust.getColor(), source));
            }
        }

        @Override
        public AbstractDataElementEventConsolidator<?> getConsolidator()
        {
            return myConsolidator;
        }
    }

    /**
     * Adjusts color of a VisualizationState.
     */
    public static class AdjustColors extends SingleFieldAdjustmentVisitor
    {
        /** The my consolidator. */
        private final DataElementColorChangeConsolidator myConsolidator;

        /** The my default color. */
        private final Color myDefaultColor;

        /** The color. */
        private final TLongObjectHashMap<Color> myIdToColorMap;

        /**
         * Instantiates a new adjust color.
         *
         * @param idToColorMap the id to color map
         * @param defaultColor the default color
         */
        public AdjustColors(TLongObjectHashMap<Color> idToColorMap, Color defaultColor)
        {
            myConsolidator = new DataElementColorChangeConsolidator();
            myIdToColorMap = idToColorMap;
            myDefaultColor = defaultColor;
        }

        @Override
        public void adjustState(VisualizationState stateToAdjust, long dataElementId, String dtKey, Object source)
        {
            Color c = myIdToColorMap.get(dataElementId);
            if (c == null)
            {
                c = myDefaultColor;
            }

            if (stateToAdjust != null && stateToAdjust.setColor(c))
            {
                myConsolidator.addEvent(new DataElementColorChangeEvent(dataElementId, dtKey, stateToAdjust.getColor(), source));
            }
        }

        @Override
        public AbstractDataElementEventConsolidator<?> getConsolidator()
        {
            return myConsolidator;
        }
    }

    /**
     * Adjusts lob visible of a VisualizationState.
     */
    public static class AdjustLOBVisible extends SingleFieldAdjustmentVisitor
    {
        /** The my consolidator. */
        private final DataElementLOBVisibilityChangeConsolidator myConsolidator;

        /** The selection state. */
        private final boolean myLobVisible;

        /**
         * Instantiates a new adjust lob visible.
         *
         * @param lobVisible the lobVisible state
         */
        public AdjustLOBVisible(boolean lobVisible)
        {
            myConsolidator = new DataElementLOBVisibilityChangeConsolidator();
            myLobVisible = lobVisible;
        }

        @Override
        public void adjustState(VisualizationState stateToAdjust, long dataElementId, String dtKey, Object source)
        {
            if (stateToAdjust != null && stateToAdjust.setLobVisible(myLobVisible))
            {
                myConsolidator.addEvent(
                        new DataElementLOBVsibilityChangeEvent(dataElementId, dtKey, stateToAdjust.isLobVisible(), source));
            }
        }

        @Override
        public AbstractDataElementEventConsolidator<?> getConsolidator()
        {
            return myConsolidator;
        }
    }

    /**
     * Adjusts color of a VisualizationState.
     */
    public static class AdjustOpacity extends SingleFieldAdjustmentVisitor
    {
        /** The consolidator. */
        private final DataElementColorChangeConsolidator myConsolidator;

        /** The color. */
        private final int myOpacity;

        /**
         * Instantiates a new adjust color.
         *
         * @param opacity the opacity ( 0 to 255 ).
         */
        public AdjustOpacity(int opacity)
        {
            myConsolidator = new DataElementColorChangeConsolidator();
            myConsolidator.setOpacityChangeOnly(true);
            myOpacity = opacity;
        }

        @Override
        public void adjustState(VisualizationState stateToAdjust, long dataElementId, String dtKey, Object source)
        {
            if (stateToAdjust != null && stateToAdjust.setOpacity(myOpacity))
            {
                myConsolidator.addEvent(new DataElementColorChangeEvent(dataElementId, dtKey, stateToAdjust.getColor(), source));
            }
        }

        @Override
        public AbstractDataElementEventConsolidator<?> getConsolidator()
        {
            return myConsolidator;
        }
    }

    /**
     * Adjusts selection of a VisualizationState.
     */
    public static class AdjustSelectDeselect extends SingleFieldAdjustmentVisitor
    {
        /** The my consolidator. */
        private final DataElementSelectionChangeConsolidator myConsolidator;

        /** The my select set. */
        private final Set<Long> mySelectSet;

        /**
         * Instantiates a new adjust selection.
         *
         * @param setToSelect the set to select, all others offered to the
         *            adjustor will be deselected.
         */
        public AdjustSelectDeselect(Set<Long> setToSelect)
        {
            myConsolidator = new DataElementSelectionChangeConsolidator();
            mySelectSet = setToSelect;
        }

        @Override
        public void adjustState(VisualizationState stateToAdjust, long dataElementId, String dtKey, Object source)
        {
            if (stateToAdjust != null && stateToAdjust.setSelected(mySelectSet.contains(dataElementId)))
            {
                myConsolidator
                        .addEvent(new DataElementSelectionChangeEvent(dataElementId, dtKey, stateToAdjust.isSelected(), source));
            }
        }

        @Override
        public AbstractDataElementEventConsolidator<?> getConsolidator()
        {
            return myConsolidator;
        }
    }

    /**
     * Adjusts selection of a VisualizationState.
     */
    public static class AdjustSelected extends SingleFieldAdjustmentVisitor
    {
        /** The my consolidator. */
        private final DataElementSelectionChangeConsolidator myConsolidator;

        /** The selection state. */
        private final boolean mySelected;

        /**
         * Instantiates a new adjust selection.
         *
         * @param selected the selection state
         */
        public AdjustSelected(boolean selected)
        {
            myConsolidator = new DataElementSelectionChangeConsolidator();
            mySelected = selected;
        }

        @Override
        public void adjustState(VisualizationState stateToAdjust, long dataElementId, String dtKey, Object source)
        {
            if (stateToAdjust != null && stateToAdjust.setSelected(mySelected))
            {
                myConsolidator
                        .addEvent(new DataElementSelectionChangeEvent(dataElementId, dtKey, stateToAdjust.isSelected(), source));
            }
        }

        @Override
        public AbstractDataElementEventConsolidator<?> getConsolidator()
        {
            return myConsolidator;
        }
    }

    /**
     * Adjusts visibility of a VisualizationState.
     */
    public static class AdjustVisible extends SingleFieldAdjustmentVisitor
    {
        /** The my consolidator. */
        private final DataElementVisibilityChangeConsolidator myConsolidator;

        /** The visible. */
        private final boolean myVisible;

        /**
         * Instantiates a new adjust visible.
         *
         * @param visible the visible
         */
        public AdjustVisible(boolean visible)
        {
            myConsolidator = new DataElementVisibilityChangeConsolidator();
            myVisible = visible;
        }

        @Override
        public void adjustState(VisualizationState stateToAdjust, long dataElementId, String dtKey, Object source)
        {
            if (stateToAdjust != null && stateToAdjust.setVisible(myVisible))
            {
                myConsolidator
                        .addEvent(new DataElementVisibilityChangeEvent(dataElementId, dtKey, stateToAdjust.isVisible(), source));
            }
        }

        @Override
        public AbstractDataElementEventConsolidator<?> getConsolidator()
        {
            return myConsolidator;
        }
    }

    /**
     * The Class SingleFieldAdjustmentVisitor.
     */
    public abstract static class SingleFieldAdjustmentVisitor implements VisualizationStateAdjustmentVisitor
    {
        /**
         * Gets the consolidator.
         *
         * @return the consolidator
         */
        public abstract AbstractDataElementEventConsolidator<?> getConsolidator();

        @Override
        public List<AbstractConsolidatedDataElementChangeEvent> getEvents()
        {
            return Collections
                    .singletonList((AbstractConsolidatedDataElementChangeEvent)getConsolidator().createConsolidatedEvent());
        }

        @Override
        public boolean wereAdjustmentsMade()
        {
            return getConsolidator().hadEvents();
        }
    }
}
